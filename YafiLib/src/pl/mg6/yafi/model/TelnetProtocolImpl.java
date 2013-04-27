package pl.mg6.yafi.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pl.mg6.common.ArrayUtils;

public class TelnetProtocolImpl implements ConnectionProtocol {
	
	private static final byte CR = '\r';

	private InputStream istream;
	private OutputStream ostream;
	
	public TelnetProtocolImpl(InputStream istream, OutputStream ostream) throws IOException {
		this.istream = istream;
		this.ostream = ostream;
	}

	@Override
	public int read() throws IOException {
		int b = istream.read();
		while (b == CR) {
			b = istream.read();
		}
		return b;
	}
	
	@Override
	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}
	
	@Override
	public int read(byte[] buffer, int offset, int count) throws IOException {
		count = istream.read(buffer, offset, count);
		count -= ArrayUtils.removeFromArray(CR, buffer, offset, count);
		return count;
	}
	
	@Override
	public void write(int b) throws IOException {
		ostream.write(b);
	}

	@Override
	public void write(byte[] buffer) throws IOException {
		write(buffer, 0, buffer.length);
	}
	
	@Override
	public void write(byte[] buffer, int offset, int count) throws IOException {
		ostream.write(buffer, offset, count);
	}
	
	public void flush() throws IOException {
		ostream.flush();
	}
}
