package pl.mg6.common.android.tracker;

public class Tracking {
	
	private Tracking() {
	}
	
	public static final String CATEGORY_LOGIN = "Login";
	public static final String ACTION_APP_VERSION = "AppVersion";
	public static final String ACTION_DEVICE = "Device";
	public static final String ACTION_SCREEN = "Screen";
	public static final String ACTION_SOURCE = "Source";
	public static final String[] LABEL_ARRAY_SOURCES = { null, "market.android.com", "yafi.pl" };
	
	public static final String CATEGORY_SETTINGS = "Settings";
	public static final String ACTION_CONFIRM_DISCONNECTION = "ConfirmDisconnection";
	public static final String LABEL_DIALOG = "Dialog";
	public static final String ACTION_REMEMBER_PASSWORD = "RememberPassword";
	public static final String ACTION_INPUT_METHOD = "InputMethod";
	public static final String LABEL_DRAG_AND_DROP = "DragAndDrop";
	public static final String LABEL_CLICK_CLICK = "ClickClick";
	public static final String LABEL_BOTH = "Both";
	public static final String ACTION_PIECES = "Pieces";
	public static final String ACTION_COLORS = "Colors";
	public static final String ACTION_PREMOVE = "Premove";
	public static final String ACTION_SOUND = "Sound";
	public static final String ACTION_VIBRATE = "Vibrate";
	public static final String ACTION_HELP_IMPROVE = "HelpImprove";
	
	public static final String CATEGORY_GET_GAME = "GetGame";
	public static final String ACTION_SEEK = "Seek";
	public static final String ACTION_SOUGHT = "Sought";
	public static final String ACTION_MATCH = "Match";
	public static final String ACTION_CHALLENGE = "Challenge";
	
	public static final String CATEGORY_SHOW_GAME = "ShowGame";
	public static final String ACTION_OBSERVE = "Observe";
	public static final String LABEL_HIGH_RATED_BLITZ = "HighRatedBlitz";
	public static final String LABEL_HIGH_RATED_STANDARD = "HighRatedStandard";
	public static final String ACTION_EXAMINE = "Examine";
	public static final String LABEL_INFO_HISTORY = "InfoHistory";
	public static final String LABEL_INFO_JOURNAL = "InfoJournal";
	public static final String LABEL_INFO_ADJOURNED = "InfoAdjourned";
	
	public static final String CATEGORY_VETERAN = "Veteran";
	public static final String ACTION_COMMAND = "Command";
	public static final String ACTION_COMMAND_CLICK = "CommandClick";
	
	public static final String CATEGORY_EXTERNAL = "External";
	public static final String ACTION_UPDATE = "Update";
	public static final String ACTION_RATE = "Rate";
	public static final String ACTION_MATCH_ME = "MatchMe";
	
	public static final String CATEGORY_ADMOB = "Admob";
	public static final String ACTION_HOSTS = "Hosts";
	public static final String ACTION_CLICKED = "Clicked";
	public static final String ACTION_FAILED_TO_LOAD = "FailedToLoad";
}
