package pl.mg6.yafi.model;

import java.io.IOException;

public interface ConnectionProtocol {
	
	int read() throws IOException;
	
	int read(byte[] buffer) throws IOException;
	
	int read(byte[] buffer, int offset, int count) throws IOException;
	
	void write(int b) throws IOException;
	
	void write(byte[] buffer) throws IOException;
	
	void write(byte[] buffer, int offset, int count) throws IOException;
}
