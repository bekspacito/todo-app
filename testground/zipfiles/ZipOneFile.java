import java.util.zip.*;
import java.io.*;

// Zipping a single file

public class ZipOneFile {
	public static void main(String[] args) throws IOException {
	
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("compressed.zip"));
		FileInputStream fis = new FileInputStream(new File("test-data/file1.txt"));

		ZipEntry zipEntry = new ZipEntry("file1.txt");
		zos.putNextEntry(zipEntry);

		byte[] buffer = new byte[8 * 1024]; // 8 Kb
		int bytesRead = 0;
		while((bytesRead = fis.read(buffer)) >= 0)
			zos.write(buffer, 0, bytesRead);

		fis.close();
		zos.close();
	
	}
}
