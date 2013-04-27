package pl.mg6.yafi.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pl.mg6.common.TimeUtils;

public class TimesealProtocolImpl implements ConnectionProtocol {
	
	private TelnetProtocolImpl telnet;

	private static final byte[] stamp = "\n[G]\n".getBytes();
	private static final byte[] stampReply = "\n\u00029\n".getBytes();
	private static final byte[] about ="Timestamp (FICS) v1.0 - programmed by Henrik Gram.".getBytes();
	
	private byte[] readBuffer, writeBuffer;
	private int readBufferCount;
	private int writeBufferCount;
	private byte[] encodeBuffer;
	private long startTime;

	public TimesealProtocolImpl(InputStream istream, OutputStream ostream) throws IOException {
		telnet = new TelnetProtocolImpl(istream, ostream);
		
		readBuffer = new byte[4096];
		
		writeBuffer = new byte[4096];
		encodeBuffer = new byte[240];
		startTime = TimeUtils.getTimestamp();
	}
	
	public int read() throws IOException {
		throw new IOException();
	}
	
	@Override
	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}
	
	@Override
	public int read(byte[] buffer, int offset, int count) throws IOException {
		if (readBufferCount - (stamp.length - 1) >= count) {
			System.arraycopy(readBuffer, 0, buffer, offset, count);
			readBufferCount -= count;
			System.arraycopy(readBuffer, count, readBuffer, 0, readBufferCount);
		} else {
			int size = telnet.read(readBuffer, readBufferCount, readBuffer.length - readBufferCount);
			if (size == -1) {
				if (readBufferCount > 0) {
					count = Math.min(count, readBufferCount);
					System.arraycopy(readBuffer, 0, buffer, offset, count);
					readBufferCount -= count;
					System.arraycopy(readBuffer, count, readBuffer, 0, readBufferCount);
				} else {
					count = -1;
				}
			} else {
				readBufferCount += size;
				outer:
				for (int i = 0; i <= readBufferCount - stamp.length; i++) {
					for (int j = 0; j < stamp.length; j++) {
						if (readBuffer[i + j] != stamp[j]) {
							continue outer;
						}
					}
					// stamp found
					write(stampReply);
					System.arraycopy(readBuffer, i + stamp.length, readBuffer, i, readBufferCount - (i + stamp.length));
					readBufferCount -= stamp.length;
					i--;
				}
				if (readBufferCount - (stamp.length - 1) >= count) {
					System.arraycopy(readBuffer, 0, buffer, offset, count);
					readBufferCount -= count;
					System.arraycopy(readBuffer, count, readBuffer, 0, readBufferCount);
				} else {
					int leaveLastBytes = Math.min(readBufferCount, stamp.length - 1);
					outer:
					while (leaveLastBytes > 0) {
						for (int i = 0; i < leaveLastBytes; i++) {
							if (readBuffer[readBufferCount - leaveLastBytes + i] != stamp[i]) {
								leaveLastBytes--;
								continue outer;
							}
						}
						break;
					}
					count = Math.min(count, readBufferCount - leaveLastBytes);
					System.arraycopy(readBuffer, 0, buffer, offset, count);
					readBufferCount -= count;
					System.arraycopy(readBuffer, count, readBuffer, 0, readBufferCount);
				}
			}
		}
		return count;
	}
	
	@Override
	synchronized public void write(int b) throws IOException {
		if (b == '\n') {
			encodeAndWrite(writeBuffer, writeBufferCount);
			writeBufferCount = 0;
		} else {
			if (writeBuffer.length == writeBufferCount) {
				byte[] tmp = new byte[writeBuffer.length << 1];
				System.arraycopy(writeBuffer, 0, tmp, 0, writeBufferCount);
				writeBuffer = tmp;
			}
			writeBuffer[writeBufferCount] = (byte) b;
			writeBufferCount++;
		}
	}
	
	@Override
	synchronized public void write(byte[] buffer) throws IOException {
		write(buffer, 0, buffer.length);
	}
	
	@Override
	synchronized public void write(byte[] buffer, int offset, int count) throws IOException {
		for (int i = 0; i < count; i++) {
			write(buffer[offset + i]);
		}
	}

	private void encodeAndWrite(byte[] data, int count) throws IOException {
		long currentTime = TimeUtils.getTimestamp();
		long diff = currentTime - startTime;
		byte[] timestamp = Long.toString(diff).getBytes();
		int length = count + 1 + timestamp.length;
		length += 12 - length % 12;
		if (length > encodeBuffer.length) {
			encodeBuffer = new byte[length];
		}
		int i = 0;
		System.arraycopy(data, 0, encodeBuffer, i, count);
		i += count;
		encodeBuffer[i++] = 24;
		System.arraycopy(timestamp, 0, encodeBuffer, i, timestamp.length);
		i += timestamp.length;
		encodeBuffer[i++] = 25;
		while (i < length) {
			encodeBuffer[i++] = 49;
		}
		for (i = 0; i < length; i += 12) {
			for (int j = 0; j < 6; j += 2) {
				byte tmp = encodeBuffer[i + j];
				encodeBuffer[i + j] = encodeBuffer[i + 11 - j];
				encodeBuffer[i + 11 - j] = tmp;
			}
		}
		for (i = 0; i < length; i++) {
			encodeBuffer[i] |= 128;
			encodeBuffer[i] ^= about[(i + 6) % about.length];
			encodeBuffer[i] -= 32;
		}
		telnet.write(encodeBuffer, 0, length);
		telnet.write(134);
		telnet.write(10);
		telnet.flush();
	}
}
