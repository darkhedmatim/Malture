import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class RepairZipFile {

	public static void main(String[] args) {

		for (int arg = 0; arg < args.length; arg++) {
			File file = new File(args[arg]);
			File repairFile = file.rename("*-repair.*");

			//Define used  standard coddings
			Charset Ascii = Charset.forName("ISO-8859-1");
			Charset UTF_8 = Charset.forName("UTF8");

			try (
			//set Acscii as default standard codding
				ZipInputStream zis = new ZipInputStream(file, Ascii);
			//set UTF_8 as default standard codding
				ZipOutputStream zos = new ZipOutputStream(repairFile, UTF_8)) {


				ZipEntry inputEntry;
				while ((inputEntry = zis.getNextEntry()) != null) {
					String fixedEntryName = new String(inputEntry.getName().getBytes(Ascii), UTF_8);

					ZipEntry outputEntry = new ZipEntry(fixedEntryName);
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
				System.err.println("An error occurred while repairing the ZIP file: " + e.getMessage());
			}
		}
	}
}
