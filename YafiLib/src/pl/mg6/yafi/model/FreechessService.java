package pl.mg6.yafi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pl.mg6.common.Settings;
import pl.mg6.common.android.AndroidUtils;
import pl.mg6.yafi.lib.R;
import pl.mg6.yafi.model.FreechessConnection.ConnectionState;
import pl.mg6.yafi.model.data.AdjournedInfo;
import pl.mg6.yafi.model.data.Communication;
import pl.mg6.yafi.model.data.FingerInfo;
import pl.mg6.yafi.model.data.Game;
import pl.mg6.yafi.model.data.HistoryInfo;
import pl.mg6.yafi.model.data.InchannelInfo;
import pl.mg6.yafi.model.data.JournalInfo;
import pl.mg6.yafi.model.data.NewsItem;
import pl.mg6.yafi.model.data.PendingInfo;
import pl.mg6.yafi.model.data.ReceivedMessage;
import pl.mg6.yafi.model.data.SeekInfo;
import pl.mg6.yafi.model.data.SeekInfoList;
import pl.mg6.yafi.model.data.VariablesInfo;
import pl.mg6.yafi.model.data.WelcomeData;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

public class FreechessService extends Service implements FreechessConnection.Listener, FreechessModel.Listener {
	
	private static final String TAG = FreechessService.class.getSimpleName();
	
	public static final String EXTRA_NAME_USERNAME = "pl.mg6...FreechessService.username";
	public static final String EXTRA_NAME_PASSWORD = "pl.mg6...FreechessService.password";
	
	public static final int MSG_ID_CONNECTING = 1000;
	public static final int MSG_ID_SENDING_USERNAME = 1001;
	public static final int MSG_ID_INVALID_USERNAME = 1002;
	public static final int MSG_ID_SENDING_PASSWORD = 1003;
	public static final int MSG_ID_INVALID_PASSWORD = 1004;
	public static final int MSG_ID_UNABLE_TO_LOG_ON = 1005;
	public static final int MSG_ID_LOGGED_ON = 1006;
	public static final int MSG_ID_DISCONNECTED = 1007;
	
	public static final int MSG_ID_RECEIVED_OUTPUT = 2000;
	
	public static final int MSG_ID_GAME_CREATE = 3000;
	public static final int MSG_ID_GAME_UPDATE = 3001;
	public static final int MSG_ID_ILLEGAL_MOVE = 3002;
	public static final int MSG_ID_PENDING_INFO = 3003;
	public static final int MSG_ID_DRAW_OFFER = 3004;
	public static final int MSG_ID_ABORT_REQUEST = 3005;
	public static final int MSG_ID_SEEKINFO_SET = 3006;
	public static final int MSG_ID_SEEKINFO_SET_ERROR = 3007;
	public static final int MSG_ID_RECEIVED_SEEK = 3008;
	public static final int MSG_ID_REMOVED_SEEKS = 3009;
	public static final int MSG_ID_COMMUNICATION = 3010;
	
	public static final int MSG_ID_FINGER = 4000;
	public static final int MSG_ID_VARIABLES = 4001;
	public static final int MSG_ID_HISTORY = 4002;
	public static final int MSG_ID_JOURNAL = 4003;
	public static final int MSG_ID_ADJOURNED = 4004;
	public static final int MSG_ID_NO_HISTORY = 4005;
	public static final int MSG_ID_NO_JOURNAL = 4006;
	public static final int MSG_ID_PRIVATE_JOURNAL = 4007;
	public static final int MSG_ID_UNREG_JOURNAL = 4008;
	public static final int MSG_ID_NO_ADJOURNED = 4009;
	
	public static final int MSG_ID_INCHANNEL_NUMBER = 5000;
	public static final int MSG_ID_REMOVE_MATCH_FROM = 5001;
	public static final int MSG_ID_REMOVE_MATCH_TO = 5002;
	
	public static final int MSG_ID_CANT_PLAY_VARIANTS_UNTIMED = 6000;
	public static final int MSG_ID_TIME_CONTROLS_TOO_LARGE = 6001;
	public static final int MSG_ID_ALREADY_HAVE_SAME_SEEK = 6002;
	public static final int MSG_ID_CANNOT_CHALLENGE_WHILE_EXAMINING = 6003;
	public static final int MSG_ID_CANNOT_CHALLENGE_WHILE_PLAYING = 6004;
	public static final int MSG_ID_CAN_HAVE_3_SEEKS = 6005;
	public static final int MSG_ID_SEEK_NOT_AVAILABLE = 6006;
	public static final int MSG_ID_NOT_LOGGED_IN = 6007;
	
	public static final int MSG_ID_WELCOME_DATA = 7000;
	public static final int MSG_ID_MESSAGES = 7001;
	public static final int MSG_ID_NEWS = 7002;
	public static final int MSG_ID_NEWS_DETAILS = 7003;
	
	private LocalBinder binder = new LocalBinder();
	
	private FreechessConnection connection;
	private FreechessModel model;
	
	private MediaPlayer movePlayer;
	private MediaPlayer tellPlayer;
	
	private PowerManager.WakeLock wakeLock;
	
	@Override
	public void onCreate() {
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onCreate");
		}
		movePlayer = MediaPlayer.create(this, R.raw.move);
		tellPlayer = MediaPlayer.create(this, R.raw.tell);
		
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "freechess.org connection");
		wakeLock.setReferenceCounted(false);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onStartCommand " + intent + " " + flags + " " + startId);
		}
		String username = intent.getStringExtra(EXTRA_NAME_USERNAME);
		String password = intent.getStringExtra(EXTRA_NAME_PASSWORD);
		createConnection(username, password);
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onBind " + intent);
		}
		return binder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onUnbind " + intent);
		}
		return true;
	}
	
	@Override
	public void onRebind(Intent intent) {
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onRebind " + intent);
		}
	}
	
	@Override
	public void onDestroy() {
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onDestroy");
		}
		cleanup();
	}
	
	private void cleanup() {
		if (connection != null) {
			connection.send("quit\n");
			connection.forceEnd();
			connection.setListener(null);
			connection = null;
		}
		if (model != null) {
			model.setListener(null);
			model = null;
		}
		
		wakeLock.release();
	}
	
	private void createConnection(String username, String password) {
		
		cleanup();
		
		wakeLock.acquire();
		
		model = new FreechessModel();
		int currentVersion = AndroidUtils.getVersionCode(this);
		model.setCurrentVersion(currentVersion);
		model.setListener(this);
		String interfaceName = "Yafi " + AndroidUtils.getVersionName(this);
		connection = new FreechessConnection(username, password, interfaceName);
		connection.setListener(this);
		connection.connect();
	}
	
	@Override
	public void onConnecting() {
		binder.sendMessage(MSG_ID_CONNECTING);
	}
	
	@Override
	public void onInvalidUsername() {
		binder.sendMessage(MSG_ID_INVALID_USERNAME);
		cleanup();
		stopSelf();
	}
	
	@Override
	public void onSendingUsername() {
		binder.sendMessage(MSG_ID_SENDING_USERNAME);
	}
	
	@Override
	public void onSendingPassword() {
		binder.sendMessage(MSG_ID_SENDING_PASSWORD);
	}
	
	@Override
	public void onInvalidPassword() {
		binder.sendMessage(MSG_ID_INVALID_PASSWORD);
		cleanup();
		stopSelf();
	}
	
	@Override
	public void onUnableToLogOn(String info) {
		binder.sendMessage(MSG_ID_UNABLE_TO_LOG_ON, info);
		cleanup();
		stopSelf();
	}
	
	@Override
	public void onLoggedOn() {
		if (connection.isRegistered()) {
			Settings.setRegisteredUser(this);
		}
		binder.sendMessage(MSG_ID_LOGGED_ON);
	}
	
	@Override
	public void onDisconnected() {
		binder.sendMessage(MSG_ID_DISCONNECTED);
		cleanup();
		stopSelf();
	}
	
	@Override
	public void onReceivedOutput(String output) {
		if (model.parse(output)) {
			binder.sendMessage(MSG_ID_RECEIVED_OUTPUT, model.getOutput());
		}
	}
	
	@Override
	public void onGameCreate(UUID gameId) {
		binder.sendMessage(MSG_ID_GAME_CREATE, gameId);
	}
	
	@Override
	public void onGameUpdate(UUID gameId) {
		binder.sendMessage(MSG_ID_GAME_UPDATE, gameId);
		if (Settings.isVibrate(this)) {
			Game game = model.getGame(gameId);
			if (game != null && game.getPosition(game.getPositionCount() - 1).getRelation() == 1) {
				Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(100);
			}
		}
		if (Settings.isSound(this)) {
			Game game = model.getGame(gameId);
			if (game != null && game.getPosition(game.getPositionCount() - 1).getRelation() == 1) {
				movePlayer.start();
			}
		}
	}
	
	@Override
	public void onIllegalMove() {
		binder.sendMessage(MSG_ID_ILLEGAL_MOVE);
	}
	
	@Override
	public void onPendingInfo(PendingInfo info) {
		binder.sendMessage(MSG_ID_PENDING_INFO, info);
	}
	
	@Override
	public void onDrawOffer() {
		binder.sendMessage(MSG_ID_DRAW_OFFER);
	}
	
	@Override
	public void onAbortRequest() {
		binder.sendMessage(MSG_ID_ABORT_REQUEST);
	}
	
	@Override
	public void onSeekInfoSet(SeekInfoList seeks) {
		binder.sendMessage(MSG_ID_SEEKINFO_SET, seeks);
	}
	
	@Override
	public void onSeekInfoSetError() {
		binder.sendMessage(MSG_ID_SEEKINFO_SET_ERROR);
	}
	
	@Override
	public void onReceivedSeek(SeekInfo seek) {
		binder.sendMessage(MSG_ID_RECEIVED_SEEK, seek);
	}
	
	@Override
	public void onRemovedSeeks(SeekInfoList list) {
		binder.sendMessage(MSG_ID_REMOVED_SEEKS, list);
	}
	
	@Override
	public void onCommunication(Communication c) {
		binder.sendMessage(MSG_ID_COMMUNICATION, c);
		if (c.isPrivate()) {
			if (Settings.isVibrate(this)) {
				Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(new long[] {0,200,100,200}, -1);
			}
			if (Settings.isSound(this)) {
				tellPlayer.start();
			}
		}
	}
	
	@Override
	public void onFinger(FingerInfo info) {
		binder.sendMessage(MSG_ID_FINGER, info);
	}
	
	@Override
	public void onVariables(VariablesInfo info) {
		binder.sendMessage(MSG_ID_VARIABLES, info);
	}
	
	@Override
	public void onHistory(HistoryInfo info) {
		binder.sendMessage(MSG_ID_HISTORY, info);
	}
	
	@Override
	public void onJournal(JournalInfo info) {
		binder.sendMessage(MSG_ID_JOURNAL, info);
	}
	
	@Override
	public void onAdjourned(AdjournedInfo info) {
		binder.sendMessage(MSG_ID_ADJOURNED, info);
	}
	
	@Override
	public void onNoHistory(String user) {
		binder.sendMessage(MSG_ID_NO_HISTORY, user);
	}
	
	@Override
	public void onNoJournal(String user) {
		binder.sendMessage(MSG_ID_NO_JOURNAL, user);
	}
	
	@Override
	public void onPrivateJournal() {
		binder.sendMessage(MSG_ID_PRIVATE_JOURNAL);
	}
	
	@Override
	public void onUnregJournal() {
		binder.sendMessage(MSG_ID_UNREG_JOURNAL);
	}
	
	@Override
	public void onNoAdjourned(String user) {
		binder.sendMessage(MSG_ID_NO_ADJOURNED, user);
	}
	
	@Override
	public void onInchannelInfo(InchannelInfo info) {
		binder.sendMessage(MSG_ID_INCHANNEL_NUMBER, info);
	}
	
	@Override
	public void onHandlePrefix(List<String> users) {
//		for (String user : users) {
//			connection.send("finger " + user + "\n");
//			connection.send("vars " + user + "\n");
//			connection.send("history " + user + "\n");
//			connection.send("journal " + user + "\n");
//			connection.send("stored " + user + "\n");
//		}
	}
	
	@Override
	public void onWhoIbslwbslx(List<String> users) {
//		for (String user : users) {
//			connection.send("finger " + user + "\n");
//			connection.send("vars " + user + "\n");
//			connection.send("history " + user + "\n");
//			connection.send("journal " + user + "\n");
//			connection.send("stored " + user + "\n");
//		}
	}
	
	@Override
	public void onRemoveMatchOfferFrom(String user) {
		binder.sendMessage(MSG_ID_REMOVE_MATCH_FROM, user);
	}
	
	@Override
	public void onRemoveMatchOfferTo(String user) {
		binder.sendMessage(MSG_ID_REMOVE_MATCH_TO, user);
	}
	
	@Override
	public void onCantPlayVariantsUntimed() {
		binder.sendMessage(MSG_ID_CANT_PLAY_VARIANTS_UNTIMED);
	}
	
	@Override
	public void onTimeControlsTooLarge() {
		binder.sendMessage(MSG_ID_TIME_CONTROLS_TOO_LARGE);
	}
	
	@Override
	public void onAlreadyHaveSameSeek() {
		binder.sendMessage(MSG_ID_ALREADY_HAVE_SAME_SEEK);
	}
	
	@Override
	public void onCannotChallengeWhileExamining() {
		binder.sendMessage(MSG_ID_CANNOT_CHALLENGE_WHILE_EXAMINING);
	}
	
	@Override
	public void onCannotChallengeWhilePlaying() {
		binder.sendMessage(MSG_ID_CANNOT_CHALLENGE_WHILE_PLAYING);
	}
	
	@Override
	public void onCanHave3Seeks() {
		binder.sendMessage(MSG_ID_CAN_HAVE_3_SEEKS);
	}
	
	@Override
	public void onSeekNotAvailable() {
		binder.sendMessage(MSG_ID_SEEK_NOT_AVAILABLE);
	}
	
	@Override
	public void onNotLoggedIn(String user) {
		binder.sendMessage(MSG_ID_NOT_LOGGED_IN, user);
	}
	
	@Override
	public void onMotdExtended(WelcomeData data) {
		binder.sendMessage(MSG_ID_WELCOME_DATA, data);
	}
	
	@Override
	public void onNews(List<NewsItem> news) {
		binder.sendMessage(MSG_ID_NEWS, news);
	}
	
	@Override
	public void onNewsDetails(NewsItem newsItem) {
		binder.sendMessage(MSG_ID_NEWS_DETAILS, newsItem);
	}
	
	@Override
	public void onMessages(List<ReceivedMessage> messages) {
		binder.sendMessage(MSG_ID_MESSAGES, messages);
	}
	
	public interface FreechessServiceInterface {
		
		void addCallbackHandler(Handler h);
		
		void removeCallbackHandler(Handler h);
		
		void sendInput(String input);
		
		String getOutput();
		
		Game getGame(UUID gameId);
		
		List<UUID> getAllGamesIds();
		
		void removeGame(UUID gameId);
		
		List<Communication> getChat(String id);
		
		void addMessage(Communication c);
		
		List<String> getAllChatIds();
		
		WelcomeData getWelcomData();
		
		FreechessConnection.ConnectionState getState();

		boolean isLoggedOn();
		
		String getRealUsername();
		
		boolean isRegistered();
		
		void quit();
		
		boolean isCurrentVersionOld();
	}
	
	private class LocalBinder extends Binder implements FreechessServiceInterface {
		
		private List<Handler> listeners = new ArrayList<Handler>();
		
		@Override
		synchronized public void addCallbackHandler(Handler h) {
			listeners.add(h);
		}
		
		@Override
		synchronized public void removeCallbackHandler(Handler h) {
			h.removeCallbacksAndMessages(null);
			listeners.remove(h);
		}
		
		@Override
		public ConnectionState getState() {
			if (connection != null) {
				return connection.getState();
			}
			return ConnectionState.Disconnected;
		}
		
		@Override
		public boolean isLoggedOn() {
			boolean loggedOn = false;
			if (connection != null) {
				loggedOn = connection.isLoggedOn();
			}
			return loggedOn;
		}
		
		@Override
		public String getRealUsername() {
			return connection.getRealUsername();
		}
		
		@Override
		public boolean isRegistered() {
			return connection.isRegistered();
		}
		
		@Override
		public void sendInput(String input) {
			if (connection != null) {
				connection.send(input);
			}
		}
		
		@Override
		public String getOutput() {
			return model.getOutput();
		}
		
		@Override
		public Game getGame(UUID gameId) {
			if (model == null) {
				return null;
			}
			return model.getGame(gameId);
		}
		
		@Override
		public List<UUID> getAllGamesIds() {
			return model.getAllGamesIds();
		}
		
		@Override
		public void removeGame(UUID gameId) {
			model.removeGame(gameId);
		}
		
		@Override
		public List<Communication> getChat(String id) {
			return model.getCommunicationById(id);
		}
		
		@Override
		public void addMessage(Communication c) {
			model.addMessage(c);
		}
		
		@Override
		public List<String> getAllChatIds() {
			return model.getAllCommunicationIds();
		}
		
		@Override
		public WelcomeData getWelcomData() {
			return model.getWelcomeData();
		}
		
		synchronized private void sendMessage(int msgId) {
			for (Handler h : listeners) {
				h.sendEmptyMessage(msgId);
			}
		}
		
		synchronized private void sendMessage(int msgId, Object data) {
			for (Handler h : listeners) {
				h.sendMessage(h.obtainMessage(msgId, data));
			}
		}
		
		@Override
		public void quit() {
			cleanup();
			stopSelf();
		}
		
		@Override
		public boolean isCurrentVersionOld() {
			return model.isCurrentVersionOld();
		}
		
//		synchronized private void sendMessage(int msgId, int data) {
//			sendMessage(msgId, data, 0);
//		}
//		
//		synchronized private void sendMessage(int msgId, int data1, int data2) {
//			for (Handler h : listeners) {
//				h.sendMessage(h.obtainMessage(msgId, data1, data2));
//			}
//		}
	}
}
