import java.io.File;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;

import java.lang.reflect.Field;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import sun.nio.cs.ThreadLocalCoders;

/**
 * @author PhdLab
 *
 */
public class Unzipper implements ClassFileTransformer {

	public static void premain(String agentArgs, Instrumentation inst) {
		inst.addTransformer(new Unzipper());
	}

	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		if (className.equals("java.util.zip.ZipFile")) {
			try {
				Class<?> clazz = Class.forName(className.replace('/', '.'));
				Field field = clazz.getDeclaredField("zc");
				field.setAccessible(true);
				// int fieldValue = field.getInt(null);
				// System.out.println("Field value: " + fieldValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	static ZipEntry entry;
	static ZipFile zipFile;
	static String entryName;
	static long entrySize, entryIndex;
	static Charset cs;
	static String csName;
	static CharsetDecoder csDecoder;

	static Field zipFile_zc;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long apkSize;
		int stringIndex;

		csName = "UTF-8";
		cs = Charset.forName(csName);
		csDecoder = ThreadLocalCoders.decoderFor(cs);
		csDecoder.onMalformedInput(CodingErrorAction.REPLACE);
		csDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

		/*
		 * csName = "Cp1252"; csName = System.getProperty("sun.jnu.encoding");
		 * cs = Charset.forName(csName); csDecoder =
		 * ThreadLocalCoders.decoderFor(cs);
		 * 
		 * csName = "ISO-8859-1"; cs = Charset.forName(csName); csDecoder =
		 * ThreadLocalCoders.decoderFor(cs);
		 * 
		 * csName = "UTF-8"; cs = Charset.forName(csName); csDecoder =
		 * ThreadLocalCoders.decoderFor(cs);
		 * 
		 * cs = StandardCharsets.ISO_8859_1; csDecoder =
		 * ThreadLocalCoders.decoderFor(cs);
		 * 
		 * cs = StandardCharsets.US_ASCII; csDecoder =
		 * ThreadLocalCoders.decoderFor(cs);
		 * 
		 * cs = StandardCharsets.UTF_16; csDecoder =
		 * ThreadLocalCoders.decoderFor(cs);
		 * 
		 * cs = StandardCharsets.UTF_16BE; csDecoder =
		 * ThreadLocalCoders.decoderFor(cs);
		 * 
		 * cs = StandardCharsets.UTF_16LE; csDecoder =
		 * ThreadLocalCoders.decoderFor(cs);
		 * 
		 * cs = StandardCharsets.UTF_8; csDecoder =
		 * ThreadLocalCoders.decoderFor(cs);
		 */

		cs = StandardCharsets.UTF_8;
		csDecoder = ThreadLocalCoders.decoderFor(cs);

		String apk = "unknown apk file";
		Hashtable<String, Long> keyValue = new Hashtable<String, Long>();
		for (int arg = 0; arg < args.length; arg++) {
			apk = args[arg];

			System.out.println(apk);
			
			keyValue.clear();

			try {
				// zipFile = new ZipFile(apk);
				zipFile = new ZipFile(new File(apk), ZipFile.OPEN_READ, StandardCharsets.UTF_8);

				apkSize = 0;
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				entryIndex = 0;
				while (entries.hasMoreElements()) {
					entryIndex++;
					entry = entries.nextElement();

					// zipFile_zc = ZipFile.class.getField("zc");

					// zipFile.zc.dec.onMalformedInput(CodingErrorAction.REPLACE);
					// 111zipFile.zc.dec.onUnmappableCharacter(CodingErrorAction.REPLACE);
					// csDecoder.onMalformedInput(CodingErrorAction.REPLACE);
					// csDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

					entryName = entry.getName();
					entrySize = entry.getSize();

					System.out.print(entryIndex);
					System.out.print('\t');
					System.out.print(entryName);
					System.out.print('\t');
					System.out.print(entrySize);
					System.out.println();

					stringIndex = entryName.indexOf('\\');
					if (stringIndex == -1)
						stringIndex = entryName.indexOf('/');
					if (stringIndex != -1)
						entryName = entryName.substring(0, stringIndex) + "\\";

					if (keyValue.containsKey(entryName))
						keyValue.replace(entryName, keyValue.get(entryName) + entrySize);
					else
						keyValue.put(entryName, entrySize);
					apkSize += entrySize;

				}

				System.out.print(apkSize);
				System.out.print('\t');
				System.out.print(apk);
				System.out.print('\\');
				System.out.println();

				Iterator<Entry<String, Long>> iteratorKeyValue = keyValue.entrySet().iterator();
				Entry<String, Long> entryKeyValue;
				while (iteratorKeyValue.hasNext()) {
					entryKeyValue = iteratorKeyValue.next();
					System.out.print(entryKeyValue.getValue());
					System.out.print('\t');
					System.out.print(apk);
					System.out.print('\\');
					entryName = entryKeyValue.getKey();
					// System.out.print(name);
					for (stringIndex = 0; stringIndex < entryName.length(); stringIndex++) {
						Boolean printAsHex = false;
						Boolean printAsChar = false;
						char ch = entryName.charAt(stringIndex);

						if (ch >= '0' && ch <= '9' || ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch == '\\'
								|| ch == ' ' || ch == '-' || ch == '_' || ch == '.')
							printAsChar = true;

						if (ch >= 0 && ch <= 31 || ch == '"' || ch == '\'' || ch == '*' || ch == '?' || ch == '%')
							printAsHex = true;

						if (!printAsChar) {
							System.out.print('%');
							String hexString = "000" + Integer.toHexString(ch).toUpperCase();

							hexString = hexString.substring(hexString.length() - 4, hexString.length());
							System.out.print(hexString);
						} else
							System.out.print(ch);
					}
					System.out.println();
				}
				zipFile.close();
			} catch (Exception e) {
				System.err.print(-1);
				System.err.print('\t');
				System.err.print(apk);
				System.err.print('\\');
				System.err.println();
				System.err.println("An error occurred while Unzippin the ZIP file: " + e.getMessage());
		}
		}
	}

}
