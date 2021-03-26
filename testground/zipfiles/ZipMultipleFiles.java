import java.util.zip.*;
import java.io.*;

// Zipping a single file

public class ZipMultipleFiles {
	public static void main(String[] args) throws IOException {
	
		String[] files = new String[] { "test-data/file1.txt", "test-data/file2.txt", "test-data/file3.txt" };
		String[] names = new String[] { "file1.txt", "file2.txt", "file3.txt" };

		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("compressed.zip"));
		for(int i=0; i<files.length; i++) {
			FileInputStream fis = new FileInputStream(new File(files[i]));

			ZipEntry zipEntry = new ZipEntry(names[i]);
			zos.putNextEntry(zipEntry);

			byte[] buffer = new byte[8 * 1024]; // 8 Kb
			int bytesRead = 0;
			while((bytesRead = fis.read(buffer)) >= 0)
				zos.write(buffer, 0, bytesRead);
			
			zos.closeEntry();
			fis.close();
		}
		zos.close();
	
	}
}
