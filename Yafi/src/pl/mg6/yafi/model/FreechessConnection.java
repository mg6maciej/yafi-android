package pl.mg6.yafi.model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import pl.mg6.common.Settings;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public final class FreechessConnection implements Runnable, Handler.Callback {
	
	private static final String TAG = FreechessConnection.class.getSimpleName();
	
	private Listener listener;
	
	private static final String SERVER_NAME = "freechess.org";
	private static final int SERVER_PORT = 5000;
	private static final String ALT_SERVER_NAME = "69.36.243.188";
	private static final int ALT_SERVER_PORT = 23;
	
	private final String username;
	private final String password;
	private final String interfaceName;
	private String realUsername;
	
	private ConnectionState state;
	
	private Thread readerThread;
	private HandlerThread writerThread;
	private Handler writerHandler;
	
	private static final int MSG_ID_COMMAND = 666;
	
	private ConnectionProtocol protocol;
	
	private StringBuilder readerBuffer;
	
	public FreechessConnection(String username, String password, String interfaceName) {
		if (username.length() == 0 || "g".equalsIgnoreCase(username)) {
			username = "guest";
		}
		if (password.length() == 0) {
			password = "y";
		}
		this.username = username;
		this.password = password;
		this.interfaceName = interfaceName;
		state = ConnectionState.NotConnected;
	}
	
	public void connect() {
		if (!FreechessUtils.validateUsername(username)) {
			notifyInvalidUsername();
		} else {
			readerThread = new Thread(this, getClass().getSimpleName() + "Reader");
			readerThread.start();
		}
	}
	
	public ConnectionState getState() {
		return state;
	}

	public boolean isLoggedOn() {
		return state == ConnectionState.LoggedOn;
	}
	
	public void forceEnd() {
		state = ConnectionState.Disconnected;
	}
	
	public String getRealUsername() {
		return realUsername;
	}
	
	@Override
	public void run() {
		Thread current = Thread.currentThread();
		if (current == readerThread) {
			runReaderThread();
		}
	}
	
	private void runReaderThread() {
		Socket socket = null;
		InputStream istream = null;
		OutputStream ostream = null;
		try {
			state = ConnectionState.Connecting;
			notifyConnecting();
			
			try {
				socket = new Socket(SERVER_NAME, SERVER_PORT);
			} catch (IOException ex) {
				socket = new Socket(ALT_SERVER_NAME, ALT_SERVER_PORT);
			}
			istream = new BufferedInputStream(socket.getInputStream());
			ostream = socket.getOutputStream();
			protocol = new TimesealProtocolImpl(istream, ostream);
			
			writerThread = new HandlerThread(getClass().getSimpleName() + "Writer");
			writerThread.start();
			writerHandler = new Handler(writerThread.getLooper(), this);
			
			send(String.format("TIMESTAMP|%s|%s|\n", interfaceName, Build.MODEL));
			// compressmove audiochat    seekremove   defprompt
			// lock         startpos     block        gameinfo
			// [xdr]        pendinfo     graph        seekinfo
			// extascii     nohighlight  vthighlight  showserver
			// pin          ms           pinginfo     boardinfo
			// extuserinfo  seekca       showownseek  premove
			// smartmove    movecase     suicide      crazyhouse
			// losers       wildcastle   fr           nowrap
			// allresults   [obsping]    singleboard
			send("%b00011000000001000100100000000001000\n");
			
			loop();
			
		} catch (Throwable ex) {
			// UnknownHostException: freechess.org
			// SocketException: Connection reset by peer
			// SocketTimeoutException: Connection timed out
			Log.e(TAG, "connection error", ex);
		} finally {
			
			state = ConnectionState.Disconnected;
			notifyDisconnected();
			
			if (writerHandler != null) {
				writerHandler.getLooper().quit();
				writerHandler = null;
				writerThread = null;
			}
			
			protocol = null;
			
			if (ostream != null) {
				try {
					ostream.flush();
					ostream.close();
				} catch (IOException ex) {
					// ignore
				}
				ostream = null;
			}
			if (istream != null) {
				try {
					istream.close();
				} catch (IOException ex) {
					// ignore
				}
				istream = null;
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ex) {
					// ignore
				}
				socket = null;
			}
		}
	}
	
	private void loop() throws IOException {
		readerBuffer = new StringBuilder(4096);
		byte[] readBuffer = new byte[4096];
		int count = protocol.read(readBuffer);
		while (state != ConnectionState.Disconnected && count != -1) {
			parse(readBuffer, count);
			count = protocol.read(readBuffer);
		}
		int len = "fics% ".length();
		while (readerBuffer.length() >= len && readerBuffer.substring(0, len).equals("fics% ")) {
			readerBuffer.delete(0, len);
		}
		if (readerBuffer.length() > 0) {
			notifyReceivedOutput(readerBuffer.toString());
		}
	}
	
	private void parse(byte[] buffer, int count) {
		String str = new String(buffer, 0, count);
		if (Settings.LOG_SERVER_COMMUNICATION) {
			//Log.i(TAG, "data: [" + str.replace("\n\n\n\n", "\n \n \n \n").replace("\n\n\n", "\n \n \n").replace("\n\n", "\n \n") + "]");
		}
		readerBuffer.append(str);
		int index;
		switch (state) {
			case Connecting: {
				index = readerBuffer.indexOf("\nlogin: ");
				if (index != -1) {
					send(username + "\n");
					readerBuffer.delete(0, index + "\nlogin: ".length());
					state = ConnectionState.AfterUsernameRequest;
					notifySendingUsername();
					break;
				}
				break;
			}
			case AfterUsernameRequest: {
				index = readerBuffer.indexOf("\npassword: ");
				if (index != -1) {
					send(password + "\n");
					readerBuffer.delete(0, index + "\npassword: ".length());
					state = ConnectionState.AfterPasswordRequest;
					notifySendingPassword();
					break;
				}
				index = readerBuffer.indexOf("\nPress return to enter the server as \"");
				if (index != -1) {
					send("\n");
					readerBuffer.delete(0, index + "\nPress return to enter the server as \"".length());
					state = ConnectionState.AfterNoPasswordRequired;
					break;
				}
				index = readerBuffer.indexOf("Due to abuse problems, guest connections have been prevented.");
				if (index != -1) {
					state = ConnectionState.UnableToLogOn;
					notifyUnableToLogOn("Guest connections have been prevented temporarily. Try logging on in a few minutes or register at http://freechess.org");
					break;
				}
				break;
			}
			case AfterPasswordRequest: {
				index = readerBuffer.indexOf("\n**** Invalid password! ****\n");
				if (index != -1) {
					state = ConnectionState.Disconnected;
					notifyInvalidPassword();
					break;
				}
				// no break
			}
			case AfterNoPasswordRequired: {
				index = readerBuffer.indexOf("\n**** Starting FICS session as ");
				if (index != -1) {
					readerBuffer.delete(0, index + "\n**** Starting FICS session as ".length());
				}
				index = readerBuffer.indexOf(" ****\n");
				if (index != -1) {
					realUsername = readerBuffer.substring(0, index);
					readerBuffer.delete(0, index + " ****\n".length());
					index = realUsername.indexOf('(');
					if (index != -1) {
						realUsername = realUsername.substring(0, index);
					}
					send("set interface " + interfaceName + "\n");
					send("set autoflag 1\n");
					send("set gin 0\n");
					send("set pin 0\n");
					send("set ptime 0\n");
					send("set seek 0\n");
					send("set style 12\n");
					send("set unobserve 3\n");
					send("showlist channel\n");
					state = ConnectionState.LoggedOn;
					notifyLoggedOn();
					break;
				}
				index = readerBuffer.indexOf(" is already logged in ***");
				if (index != -1) {
					state = ConnectionState.UnableToLogOn;
					notifyUnableToLogOn(username + " is already logged in.");
					break;
				}
				break;
			}
			case LoggedOn: {
				int len = "fics% ".length();
				while (readerBuffer.length() >= len && readerBuffer.substring(0, len).equals("fics% ")) {
					readerBuffer.delete(0, len);
				}
				index = readerBuffer.indexOf("\nfics% ");
				while (index != -1) {
					String output = readerBuffer.substring(0, index + "\n".length());
					readerBuffer.delete(0, index + "\nfics% ".length());
					notifyReceivedOutput(output);
					while (readerBuffer.length() >= len && readerBuffer.substring(0, len).equals("fics% ")) {
						readerBuffer.delete(0, len);
					}
					index = readerBuffer.indexOf("\nfics% ");
				}
			}
		}
	}
	
	public static enum ConnectionState {
		NotConnected,
		Connecting,
		AfterUsernameRequest,
		AfterPasswordRequest,
		AfterNoPasswordRequired,
		UnableToLogOn,
		LoggedOn,
		Disconnected,
	}
	
	public void send(String cmd) {
		if (writerHandler != null) {
			writerHandler.sendMessage(writerHandler.obtainMessage(MSG_ID_COMMAND, cmd));
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		String cmd = (String) msg.obj;
		write(cmd);
		return true;
	}
	
	private void write(String cmd) {
		try {
			protocol.write(cmd.getBytes());
			if (Settings.LOG_SERVER_COMMUNICATION) {
				Log.d(TAG, "sent: [" + (cmd.endsWith("\n") ? cmd.substring(0, cmd.length() - 1) : cmd) + "]");
			}
		} catch (Throwable ex) {
			// SocketException: Broken pipe
			Log.e(TAG, "writing error", ex);
		}
	}
	
	private void notifyConnecting() {
		if (listener != null) {
			listener.onConnecting();
		}
	}
	
	private void notifyInvalidUsername() {
		if (listener != null) {
			listener.onInvalidUsername();
		}
	}
	
	private void notifySendingUsername() {
		if (listener != null) {
			listener.onSendingUsername();
		}
	}
	
	private void notifySendingPassword() {
		if (listener != null) {
			listener.onSendingPassword();
		}
	}
	
	private void notifyInvalidPassword() {
		if (listener != null) {
			listener.onInvalidPassword();
		}
	}
	
	private void notifyUnableToLogOn(String info) {
		if (listener != null) {
			listener.onUnableToLogOn(info);
		}
	}
	
	private void notifyLoggedOn() {
		if (listener != null) {
			listener.onLoggedOn();
		}
	}
	
	private void notifyDisconnected() {
		if (listener != null) {
			listener.onDisconnected();
		}
	}
	
	private void notifyReceivedOutput(String output) {
		if (listener != null) {
			listener.onReceivedOutput(output);
		}
	}
	
	public void setListener(Listener l) {
		listener = l;
	}
	
	public interface Listener {
		
		void onConnecting();
		
		void onInvalidUsername();
		
		void onSendingUsername();
		
		void onSendingPassword();
		
		void onInvalidPassword();
		
		void onUnableToLogOn(String info);
		
		void onLoggedOn();
		
		void onDisconnected();
		
		void onReceivedOutput(String output);
	}
}
