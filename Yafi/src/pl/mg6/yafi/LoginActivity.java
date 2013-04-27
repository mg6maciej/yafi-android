package pl.mg6.yafi;

import pl.mg6.common.Settings;
import pl.mg6.common.android.AndroidUtils;
import pl.mg6.common.android.tracker.Tracking;
import pl.mg6.yafi.model.FreechessService;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends BaseFreechessActivity {
	
	private static final String TAG = LoginActivity.class.getSimpleName();
	
	private static final int FIRST_ID = 20000;
	
	private static final int DIALOG_ID_CONNECTING = FIRST_ID + 0;
	private static final int DIALOG_ID_INVALID_USERNAME = FIRST_ID + 1;
	private static final int DIALOG_ID_INVALID_PASSWORD = FIRST_ID + 2;
	private static final int DIALOG_ID_UNABLE_TO_LOG_ON = FIRST_ID + 3;
	private static final int DIALOG_ID_UNABLE_TO_CONNECT = FIRST_ID + 4;
	
	private static final int REQUEST_ID_MAIN = FIRST_ID + 100;
	private static final int REQUEST_ID_USER_PREFS = FIRST_ID + 101;
	
	private EditText usernameField;
	private EditText passwordField;
	private Button loginButton;
	
	private ProgressDialog connectingDialog;
	
	private AlertDialog unableToLogOnDialog;
	private String unableToLogOnInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_view);
		usernameField = (EditText) findViewById(R.id.login_username);
		usernameField.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				int resId;
				if (usernameField.length() > 0) {
					resId = R.string.login;
				} else {
					resId = R.string.login_as_guest;
				}
				loginButton.setText(resId);
			}
		});
		passwordField = (EditText) findViewById(R.id.login_password);
		passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				initLogin();
				return false;
			}
		});
		loginButton = (Button) findViewById(R.id.login_submit);
		
		checkLoggedOn = false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		usernameField.setText(Settings.getUsername(this));
		passwordField.setText(Settings.getPassword(this));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		String username = usernameField.getText().toString();
		String password = null;
		if (Settings.isRememberPassword(this)) {
			password = passwordField.getText().toString();
		}
		Settings.setUsernameAndPassword(this, username, password);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		usernameField.setText(null);
		passwordField.setText(null);
	}
	
	public void onLoginClick(View view) {
		initLogin();
	}
	
	private void initLogin() {
		String username = usernameField.getText().toString();
		String password = passwordField.getText().toString();
		Intent intent = new Intent(this, FreechessService.class);
		intent.putExtra(FreechessService.EXTRA_NAME_USERNAME, username);
		intent.putExtra(FreechessService.EXTRA_NAME_PASSWORD, password);
		startService(intent);
	}
	
	private void cancelLogin() {
		service.quit();
	}
	
	@Override
	protected void onStartHandlingMessages() {
		super.onStartHandlingMessages();
		switch (service.getState()) {
			case Connecting:
				break;
			case AfterUsernameRequest:
			case AfterNoPasswordRequired:
				onSendingUsername();
				break;
			case AfterPasswordRequest:
				onSendingPassword();
				break;
			case LoggedOn:
				onLoggedOn();
				break;
			default:
				dismissConnectingDialog();
				break;
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case FreechessService.MSG_ID_CONNECTING:
				onConnecting();
				return true;
			case FreechessService.MSG_ID_INVALID_USERNAME:
				onInvalidUsername();
				return true;
			case FreechessService.MSG_ID_SENDING_USERNAME:
				onSendingUsername();
				return true;
			case FreechessService.MSG_ID_SENDING_PASSWORD:
				onSendingPassword();
				return true;
			case FreechessService.MSG_ID_INVALID_PASSWORD:
				onInvalidPassword();
				return true;
			case FreechessService.MSG_ID_UNABLE_TO_LOG_ON:
				onUnableToLogOn((String) msg.obj);
				return true;
			case FreechessService.MSG_ID_LOGGED_ON:
				onLoggedOn();
				return true;
		}
		return super.handleMessage(msg);
	}
	
	private void onConnecting() {
		showDialog(DIALOG_ID_CONNECTING);
	}
	
	private void onInvalidUsername() {
		showDialog(DIALOG_ID_INVALID_USERNAME);
	}
	
	private void onSendingUsername() {
		connectingDialog.setMessage("Sending username.");
	}
	
	private void onSendingPassword() {
		connectingDialog.setMessage("Sending password.");
	}
	
	private void onInvalidPassword() {
		dismissConnectingDialog();
		showDialog(DIALOG_ID_INVALID_PASSWORD);
	}
	
	private void onUnableToLogOn(String info) {
		dismissConnectingDialog();
		unableToLogOnInfo = info;
		showDialog(DIALOG_ID_UNABLE_TO_LOG_ON);
	}
	
	private void onLoggedOn() {
		dismissConnectingDialog();
		Intent intent = new Intent(this, MenuActivity.class);
		startActivityForResult(intent, REQUEST_ID_MAIN);
		
		trackEvent(Tracking.CATEGORY_LOGIN, Tracking.ACTION_APP_VERSION, AndroidUtils.getVersionName(this), AndroidUtils.getVersionCode(this));
		trackEvent(Tracking.CATEGORY_LOGIN, Tracking.ACTION_DEVICE, Build.MODEL, Build.VERSION.SDK_INT);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		int rotation = getWindowManager().getDefaultDisplay().getOrientation();
		int density = (int) (DisplayMetrics.DENSITY_DEFAULT * dm.density);
		Log.i(TAG, "size: " + width + " " + height);
		trackEvent(Tracking.CATEGORY_LOGIN, Tracking.ACTION_SCREEN, width + "x" + height + "@" + density + "dpi", rotation);
	}
	
	protected void onDisconnected() {
		dismissConnectingDialog();
		showDialog(DIALOG_ID_UNABLE_TO_CONNECT);
	}
	
	private void dismissConnectingDialog() {
		if (connectingDialog != null && connectingDialog.isShowing()) {
			connectingDialog.dismiss();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_ID_CONNECTING: {
				connectingDialog = new ProgressDialog(this);
				connectingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						cancelLogin();
					}
				});
				return connectingDialog;
			}
			case DIALOG_ID_INVALID_USERNAME: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Invalid username");
				builder.setNeutralButton("OK", null);
				return builder.create();
			}
			case DIALOG_ID_INVALID_PASSWORD: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Invalid password");
				builder.setNeutralButton("OK", null);
				return builder.create();
			}
			case DIALOG_ID_UNABLE_TO_LOG_ON: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Unable to log on");
				builder.setMessage(unableToLogOnInfo);
				builder.setNeutralButton("OK", null);
				unableToLogOnDialog = builder.create();
				return unableToLogOnDialog;
			}
			case DIALOG_ID_UNABLE_TO_CONNECT: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Unable to connect");
				builder.setMessage("Please check your internet connection settings.");
				builder.setNeutralButton("OK", null);
				return builder.create();
			}
		}
		return super.onCreateDialog(id);
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
			case DIALOG_ID_CONNECTING: {
				connectingDialog.setMessage("Connecting...");
				break;
			}
			case DIALOG_ID_UNABLE_TO_LOG_ON: {
				unableToLogOnDialog.setMessage(unableToLogOnInfo);
				break;
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.mi_preferences:
				Intent intent = new Intent(this, UserPreferencesActivity.class);
				startActivityForResult(intent, REQUEST_ID_USER_PREFS);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
