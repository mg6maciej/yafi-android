package pl.mg6.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;

public final class Settings {
	
	private Settings() {
	}
	
	public static final boolean LOG_LIFECYCLE = false;
	public static final boolean LOG_SERVER_COMMUNICATION = false;
	public static final boolean LOG_ADS = false;
	
	public static final int SOURCE_ANDROID_MARKET = 1;
	public static final int SOURCE_YAFI_PL = 2;
	public static final int SOURCE_ID = SOURCE_ANDROID_MARKET;
	
	public static final boolean REMOVE_ADS_AFTER_CLICK = false;
	
	public static final String PREF_CONFIRM_DISCONNECT = "user_pref.confirm_disconnect";
	private static final boolean CONFIRM_DISCONNECT_DEFAULT_VALUE = true;
	private static final String PREF_USERNAME = "user_pref.username";
	private static final String PREF_PASSWORD = "user_pref.password";
	public static final String PREF_REMEMBER_PASSWORD = "user_pref.remember_password";
	public static final String PREF_BOARD_INPUT_METHOD = "user_pref.board_input_method";
	public static final String PREF_BOARD_PIECES = "user_pref.board_pieces";
	public static final String PREF_BOARD_COLORS = "user_pref.board_colors";
	public static final String PREF_BOARD_PREMOVE = "user_pref.board_premove";
	private static final String PREF_SEEK_TIME = "user_pref.seek_time";
	private static final String PREF_SEEK_INCREMENT = "user_pref.seek_increment";
	private static final String PREF_SEEK_TYPE = "user_pref.seek_type";
	private static final String PREF_SEEK_RATED = "user_pref.seek_rated";
	private static final String PREF_MATCH_TIME = "user_pref.match_time";
	private static final String PREF_MATCH_INCREMENT = "user_pref.match_increment";
	private static final String PREF_MATCH_TYPE = "user_pref.match_type";
	private static final String PREF_MATCH_RATED = "user_pref.match_rated";
	private static final String PREF_SOUGHT_GAME_TYPE = "user_pref.sought_game_type";
	private static final String PREF_SOUGHT_OPPONENT = "user_pref.sought_opponent";
	private static final String PREF_SOUGHT_TIME = "user_pref.sought_time";
	public static final String PREF_HELP_IMPROVE = "user_pref.help_improve";
	private static final boolean HELP_IMPROVE_DEFAULT_VALUE = true;
	private static final String PREF_CONSOLE_COMMANDS = "user_pref.console_commands";
	private static final int MIN_COMMAND_USE_COUNT = 5;
	private static final int MAX_COMMAND_COUNT = 16;
	public static final String PREF_VIBRATE = "user_pref.vibrate";
	public static final String PREF_SOUND = "user_pref.sound";
	private static final boolean SOUND_DEFAULT_VALUE = true;
	public static final int BOARD_INPUT_METHOD_DRAG_AND_DROP = 1;
	public static final int BOARD_INPUT_METHOD_CLICK_CLICK = 2;
	public static final int BOARD_INPUT_METHOD_BOTH = BOARD_INPUT_METHOD_DRAG_AND_DROP | BOARD_INPUT_METHOD_CLICK_CLICK;
	private static final String BOARD_INPUT_METHOD_DEFAULT_VALUE = String.valueOf(BOARD_INPUT_METHOD_BOTH);
	public static final String PREF_SHOW_GAME_END_DIALOG = "user_pref.show_game_end_dialog";
	public static final boolean SHOW_GAME_END_DIALOG_DEFAULT_VALUE = true;
	
	private static final String PREF_CURRENT_GAME = "app_pref.current_game";
	private static final String PREF_CURRENT_CHAT = "app_pref.current_chat";
	private static final String PREF_RATE_CLICKED = "app_pref.rate_clicked";
	private static final String PREF_SHOW_RATE_DELAY = "app_pref.show_rate_delay";
	private static final int SHOW_RATE_INITIAL_DELAY = 6;
	private static final String PREF_AD_CLICKED = "app_pref.ad_clicked";
	private static final String PREF_REGISTERED_USER = "app_pref.registered_user";
	
	private static SharedPreferences getSharedPrefs(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	private static SharedPreferences.Editor getSharedPrefsEditor(Context context) {
		return getSharedPrefs(context).edit();
	}
	
	public static boolean isConfirmDisconnect(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		boolean confirm = prefs.getBoolean(PREF_CONFIRM_DISCONNECT, CONFIRM_DISCONNECT_DEFAULT_VALUE);
		return confirm;
	}
	
	public static void setConfirmDisconnect(Context context, boolean confirm) {
		SharedPreferences.Editor editor = getSharedPrefsEditor(context);
		editor.putBoolean(PREF_CONFIRM_DISCONNECT, confirm);
		editor.commit();
	}
	
	public static boolean isShowGameEndDialog(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		boolean show = prefs.getBoolean(PREF_SHOW_GAME_END_DIALOG, SHOW_GAME_END_DIALOG_DEFAULT_VALUE);
		return show;
	}
	
	public static void setShowGameEndDialog(Context context, boolean show) {
		SharedPreferences.Editor editor = getSharedPrefsEditor(context);
		editor.putBoolean(PREF_SHOW_GAME_END_DIALOG, show);
		editor.commit();
	}
	
	public static String getUsername(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		String username = prefs.getString(PREF_USERNAME, null);
		return username;
	}
	
	public static String getPassword(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		String password = prefs.getString(PREF_PASSWORD, null);
		if (password != null) {
			if (password.startsWith("v1|")) {
				password = password.substring("v1|".length());
			} else {
				password = null;
			}
		}
		return password;
	}
	
	public static void setUsernameAndPassword(Context context, String username, String password) {
		if (password != null) {
			password = "v1|" + password;
		}
		SharedPreferences.Editor editor = getSharedPrefsEditor(context);
		editor.putString(PREF_USERNAME, username);
		editor.putString(PREF_PASSWORD, password);
		editor.commit();
	}
	
	public static boolean isRememberPassword(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		boolean remember = prefs.getBoolean(PREF_REMEMBER_PASSWORD, false);
		return remember;
	}
	
	public static int getBoardInputMethod(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		String pref = prefs.getString(PREF_BOARD_INPUT_METHOD, BOARD_INPUT_METHOD_DEFAULT_VALUE);
		int inputMethod = Integer.parseInt(pref);
		return inputMethod;
	}
	
	public static String getBoardPieces(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		String pieces = prefs.getString(PREF_BOARD_PIECES, "default");
		return pieces;
	}
	
	public static String getBoardColors(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		String colors = prefs.getString(PREF_BOARD_COLORS, "default");
		return colors;
	}
	
	public static boolean isBoardPremove(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		boolean premove = prefs.getBoolean(PREF_BOARD_PREMOVE, true);
		return premove;
	}
	
	public static String getSeekTime(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		String time = prefs.getString(PREF_SEEK_TIME, null);
		return time;
	}
	
	public static String getSeekIncrement(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		String increment = prefs.getString(PREF_SEEK_INCREMENT, null);
		return increment;
	}
	
	public static String getSeekType(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		String type = prefs.getString(PREF_SEEK_TYPE, "chess");
		return type;
	}
	
	public static boolean isSeekRated(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		boolean rated = prefs.getBoolean(PREF_SEEK_RATED, false);
		return rated;
	}
	
	public static void setSeekData(Context context, String time, String increment, String type, boolean rated) {
		SharedPreferences.Editor editor = getSharedPrefsEditor(context);
		editor.putString(PREF_SEEK_TIME, time);
		editor.putString(PREF_SEEK_INCREMENT, increment);
		editor.putString(PREF_SEEK_TYPE, type);
		editor.putBoolean(PREF_SEEK_RATED, rated);
		editor.commit();
	}
	
	public static String getMatchTime(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		String time = prefs.getString(PREF_MATCH_TIME, null);
		return time;
	}
	
	public static String getMatchIncrement(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		String increment = prefs.getString(PREF_MATCH_INCREMENT, null);
		return increment;
	}
	
	public static String getMatchType(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		String type = prefs.getString(PREF_MATCH_TYPE, "chess");
		return type;
	}
	
	public static boolean isMatchRated(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		boolean rated = prefs.getBoolean(PREF_MATCH_RATED, false);
		return rated;
	}
	
	public static void setMatchData(Context context, String time, String increment, String type, boolean rated) {
		SharedPreferences.Editor editor = getSharedPrefsEditor(context);
		editor.putString(PREF_MATCH_TIME, time);
		editor.putString(PREF_MATCH_INCREMENT, increment);
		editor.putString(PREF_MATCH_TYPE, type);
		editor.putBoolean(PREF_MATCH_RATED, rated);
		editor.commit();
	}
	
	public static int getSoughtGameType(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		int gameType = prefs.getInt(PREF_SOUGHT_GAME_TYPE, 1);
		return gameType;
	}
	
	public static int getSoughtOpponent(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		int opponent = prefs.getInt(PREF_SOUGHT_OPPONENT, 0);
		return opponent;
	}
	
	public static int getSoughtTime(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		int time = prefs.getInt(PREF_SOUGHT_TIME, 0);
		return time;
	}
	
	public static void setSoughtData(Context context, int gameType, int opponent, int time) {
		SharedPreferences.Editor editor = getSharedPrefsEditor(context);
		editor.putInt(PREF_SOUGHT_GAME_TYPE, gameType);
		editor.putInt(PREF_SOUGHT_OPPONENT, opponent);
		editor.putInt(PREF_SOUGHT_TIME, time);
		editor.commit();
	}
	
	public static boolean isHelpImprove(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		boolean helpImprove = prefs.getBoolean(PREF_HELP_IMPROVE, HELP_IMPROVE_DEFAULT_VALUE);
		return helpImprove;
	}
	
	public static void addCommand(Context context, String command) {
		SharedPreferences prefs = getSharedPrefs(context);
		String pref = prefs.getString(PREF_CONSOLE_COMMANDS, null);
		Set<String> commands = new HashSet<String>();
		if (pref != null) {
			for (String tmp : pref.split("\n")) {
				commands.add(tmp);
			}
		}
		int count = 0;
		for (String old : commands) {
			int index = old.indexOf(' ');
			if (old.substring(index + 1).equals(command)) {
				count = Integer.parseInt(old.substring(0, index));
				commands.remove(old);
				break;
			}
		}
		count++;
		commands.add(String.format("%06d %s", count, command));
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREF_CONSOLE_COMMANDS, StringUtils.join("\n", commands.toArray(new String[0])));
		editor.commit();
	}
	
	public static List<String> getFrequentlyUsedCommands(Context context) {
		List<String> commands = null;
		SharedPreferences prefs = getSharedPrefs(context);
		String pref = prefs.getString(PREF_CONSOLE_COMMANDS, null);
		if (pref != null) {
			commands = new ArrayList<String>();
			for (String tmp : pref.split("\n")) {
				commands.add(tmp);
			}
			Collections.sort(commands, Collections.reverseOrder());
			for (int i = commands.size() - 1; i >= 0; i--) {
				String command = commands.get(i);
				int index = command.indexOf(' ');
				int count = Integer.parseInt(command.substring(0, index));
				if (count < MIN_COMMAND_USE_COUNT) {
					commands.remove(i);
				} else {
					commands.set(i, command.substring(index + 1));
				}
			}
			if (commands.size() > MAX_COMMAND_COUNT) {
				commands.subList(MAX_COMMAND_COUNT, commands.size()).clear();
			}
		}
		return commands;
	}
	
	public static boolean isVibrate(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		boolean vibrate = prefs.getBoolean(PREF_VIBRATE, Build.VERSION.SDK_INT < 11 || ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).hasVibrator());
		return vibrate;
	}
	
	public static boolean isSound(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		boolean sound = prefs.getBoolean(PREF_SOUND, SOUND_DEFAULT_VALUE);
		return sound;
	}
	
	public static void saveCurrentGame(Context context, UUID gameId) {
		SharedPreferences.Editor editor = getSharedPrefsEditor(context);
		String pref = null;
		if (gameId != null) {
			pref = gameId.toString();
		}
		editor.putString(PREF_CURRENT_GAME, pref);
		editor.commit();
	}
	
	public static UUID loadCurrentGame(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		String pref = prefs.getString(PREF_CURRENT_GAME, null);
		UUID gameId = null;
		if (pref != null) {
			gameId = UUID.fromString(pref);
		}
		return gameId;
	}
	
	public static void saveCurrentChat(Context context, String chatId) {
		SharedPreferences.Editor editor = getSharedPrefsEditor(context);
		editor.putString(PREF_CURRENT_CHAT, chatId);
		editor.commit();
	}
	
	public static String loadCurrentChat(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		String chatId = prefs.getString(PREF_CURRENT_CHAT, null);
		return chatId;
	}
	
	public static boolean canShowRate(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		boolean rateClicked = prefs.getBoolean(PREF_RATE_CLICKED, false);
		if (rateClicked) {
			return false;
		}
		int delay = prefs.getInt(PREF_SHOW_RATE_DELAY, SHOW_RATE_INITIAL_DELAY) - 1;
		if (delay <= 0) {
			return true;
		}
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(PREF_SHOW_RATE_DELAY, delay);
		editor.commit();
		return false;
	}
	
	public static void setRateClicked(Context context) {
		SharedPreferences.Editor editor = getSharedPrefsEditor(context);
		editor.putBoolean(PREF_RATE_CLICKED, true);
		editor.commit();
	}
	
	public static boolean isAdClicked(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		boolean clicked = prefs.getBoolean(PREF_AD_CLICKED, false);
		return clicked;
	}
	
	public static void setAdClicked(Context context, boolean clicked) {
		SharedPreferences.Editor editor = getSharedPrefsEditor(context);
		editor.putBoolean(PREF_AD_CLICKED, clicked);
		editor.commit();
	}
	
	public static boolean isRegisteredUser(Context context) {
		SharedPreferences prefs = getSharedPrefs(context);
		boolean registered = prefs.getBoolean(PREF_REGISTERED_USER, false);
		return registered;
	}
	
	public static void setRegisteredUser(Context context) {
		SharedPreferences.Editor editor = getSharedPrefsEditor(context);
		editor.putBoolean(PREF_REGISTERED_USER, true);
		editor.commit();
	}
	
	public static boolean isPaidApp(Context context) {
		return "pl.mg6.yafi.plus".equals(context.getPackageName());
	}
}
