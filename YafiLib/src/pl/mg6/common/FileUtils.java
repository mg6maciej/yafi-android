package pl.mg6.common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtils {
	
	private FileUtils() {
	}
	
	public static String tryReadFile(String path) {
		FileInputStream stream = null;
		try {
			 stream = new FileInputStream(path);
			 BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			 StringBuilder sb = new StringBuilder();
			 char[] buffer = new char[4096];
			 int count = reader.read(buffer);
			 while (count != -1) {
				 sb.append(buffer, 0, count);
				 count = reader.read(buffer);
			 }
			 return sb.toString();
		} catch (IOException ex) {
			return null;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException ex) {
					// ignore
				}
			}
		}
	}
}
