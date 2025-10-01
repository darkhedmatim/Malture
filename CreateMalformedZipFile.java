import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CreateMalformedZipFile {
	public static void main(String[] args) {

		//a file name that contains an invalid UTF-8 character and use it for malture files
		String malformedEntryName = "invalid\u8000name.txt"; 

		for (int arg = 0; arg < args.length; arg++) {
			File file = new File(args[arg]);
			File maltureFile = file.rename("*-malture.*");
			
			//Define used  standard coddings
			Charset Ascii = Charset.forName("ISO-8859-1");
			Charset UTF_8 = Charset.forName("UTF8");

			try (
			//set Acscii as default standard codding
				ZipInputStream zis = new ZipInputStream(file, Ascii);
				ZipOutputStream zos = new ZipOutputStream(maltureFile, Ascii)) {
			//create new file without any contents include malture evasion with Ascii charset
				ZipEntry malformedEntry = new ZipEntry(malformedEntryName);
			//add malture file to new APK
				zos.putNextEntry(malformedEntry);
				zos.closeEntry();

			//add remained files of old APK to new APK
				ZipEntry inputEntry;
				while ((inputEntry = zis.getNextEntry()) != null) {
					ZipEntry outputEntry = new ZipEntry(inputEntry.getName());
					zos.putNextEntry(outputEntry);

					byte[] buffer = new byte[1024];
					int bytesRead;
					while ((bytesRead = zis.read(buffer)) != -1) {
						zos.write(buffer, 0, bytesRead);
					}

					zos.closeEntry();
					zis.closeEntry();
				}
			} catch (IOException e) {
				System.err.println("An error occurred while creating the malformed ZIP file: " + e.getMessage());
			}
		}
	}
}
