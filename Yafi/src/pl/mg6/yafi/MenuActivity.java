package pl.mg6.yafi;

import java.util.List;
import java.util.Random;

import pl.mg6.common.Settings;
import pl.mg6.common.android.tracker.Tracking;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class MenuActivity extends BaseFreechessActivity {
	
	private static final int FIRST_ID = 10000;
	
	private static final int DIALOG_ID_CONFIRM_DISCONNECT = FIRST_ID + 0;

	private static final int REQUEST_ID_BOARD = FIRST_ID + 100;
	private static final int REQUEST_ID_CHAT = FIRST_ID + 101;
	private static final int REQUEST_ID_SEEK = FIRST_ID + 102;
	private static final int REQUEST_ID_SOUGHT = FIRST_ID + 103;
	private static final int REQUEST_ID_MATCH = FIRST_ID + 104;
	private static final int REQUEST_ID_CHALLENGES = FIRST_ID + 105;
	private static final int REQUEST_ID_SEARCH_FOR_GAME = FIRST_ID + 106;
	private static final int REQUEST_ID_CONSOLE = FIRST_ID + 107;
	private static final int REQUEST_ID_USER_PREFS = FIRST_ID + 108;

	private AlertDialog confirmDisconnectDialog;
	private CheckBox confirmDisconnectCheckbox;
	
	private ViewGroup updateRatePanel;
	private boolean triedShowRate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_view);
		updateRatePanel = (ViewGroup) findViewById(R.id.main_update_rate_panel);
	}
	
	public void onBoardClick(View view) {
		Intent intent = new Intent(this, BoardActivity.class);
		startActivityForResult(intent, REQUEST_ID_BOARD);
	}
	
	public void onChatClick(View view) {
		Intent intent = new Intent(this, ChatActivity.class);
		startActivityForResult(intent, REQUEST_ID_CHAT);
	}
	
	public void onSeekClick(View view) {
		Intent intent = new Intent(this, SeekActivity.class);
		startActivityForResult(intent, REQUEST_ID_SEEK);
	}
	
	public void onSoughtClick(View view) {
		Intent intent = new Intent(this, SoughtActivity.class);
		startActivityForResult(intent, REQUEST_ID_SOUGHT);
	}
	
	public void onMatchClick(View view) {
		Intent intent = new Intent(this, MatchActivity.class);
		startActivityForResult(intent, REQUEST_ID_MATCH);
	}
	
	public void onChallengesClick(View view) {
		Intent intent = new Intent(this, ChallengesActivity.class);
		startActivityForResult(intent, REQUEST_ID_CHALLENGES);
	}
	
	public void onSearchForGameClick(View view) {
		Intent intent = new Intent(this, SearchForGameActivity.class);
		startActivityForResult(intent, REQUEST_ID_SEARCH_FOR_GAME);
	}
	
	public void onConsoleClick(View view) {
		Intent intent = new Intent(this, ConsoleActivity.class);
		startActivityForResult(intent, REQUEST_ID_CONSOLE);
	}
	
	public void onUserPrefsClick(View view) {
		Intent intent = new Intent(this, UserPreferencesActivity.class);
		startActivityForResult(intent, REQUEST_ID_USER_PREFS);
	}
	
	@Override
	public void onBackPressed() {
		if (Settings.isConfirmDisconnect(this)) {
			showDialog(DIALOG_ID_CONFIRM_DISCONNECT);
		} else {
			service.quit();
			finish();
		}
	}
	
	private void dismissConfirmDisconnectDialog() {
		if (confirmDisconnectDialog != null && confirmDisconnectDialog.isShowing()) {
			confirmDisconnectDialog.dismiss();
		}
	}
	
	@Override
	protected void onStartHandlingMessages() {
		super.onStartHandlingMessages();
		if (service.isCurrentVersionOld()) {
			updateRatePanel.setVisibility(View.VISIBLE);
			TextView text = (TextView) updateRatePanel.findViewById(R.id.main_update_rate_text);
			Button button = (Button) updateRatePanel.findViewById(R.id.main_update_rate_button);
			text.setText(R.string.new_version_available);
			button.setText(R.string.update);
			button.setTag(false);
		} else if (!(triedShowRate || !Settings.canShowRate(this))) {
			triedShowRate = true;
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://details?id=" + getPackageName()));
			List<ResolveInfo> infos = getPackageManager().queryIntentActivities(intent, 0);
			if (infos.size() > 0 && new Random().nextInt(6) == 0) {
				updateRatePanel.setVisibility(View.VISIBLE);
				TextView text = (TextView) updateRatePanel.findViewById(R.id.main_update_rate_text);
				Button button = (Button) updateRatePanel.findViewById(R.id.main_update_rate_button);
				text.setText(R.string.if_enjoy_please_rate);
				button.setText(R.string.rate_yafi);
				button.setTag(true);
			}
		}
	}
	
	public void onUpdateRateClick(View view) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=" + getPackageName()));
		List<ResolveInfo> infos = getPackageManager().queryIntentActivities(intent, 0);
		if (infos.size() > 0) {
			startActivity(intent);
			if ((Boolean) view.getTag()) {
				updateRatePanel.setVisibility(View.GONE);
				Settings.setRateClicked(this);
				trackEvent(Tracking.CATEGORY_EXTERNAL, Tracking.ACTION_RATE, null, 0);
			} else {
				String label = infos.get(0).activityInfo.applicationInfo.packageName;
				for (int i = 1; i < infos.size(); i++) {
					label += "; " + infos.get(i).activityInfo.applicationInfo.packageName;
				}
				trackEvent(Tracking.CATEGORY_EXTERNAL, Tracking.ACTION_UPDATE, label, 0);
			}
		} else {
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("http://yafi.pl/android/apk/Yafi.apk"));
			startActivity(intent);
			Toast.makeText(this, "Market application not found. Downloading Yafi from http://yafi.pl", Toast.LENGTH_LONG).show();
			trackEvent(Tracking.CATEGORY_EXTERNAL, Tracking.ACTION_UPDATE, "http://yafi.pl", 0);
		}
	}
	
	@Override
	protected void onDisconnected() {
		super.onDisconnected();
		dismissConfirmDisconnectDialog();
	}
	
	@Override
	protected void onGameCreate() {
		Intent intent = new Intent(this, BoardActivity.class);
		startActivityForResult(intent, REQUEST_ID_BOARD);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_ID_CONFIRM_DISCONNECT: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.want_to_disconnect_question);
				View body = getLayoutInflater().inflate(R.layout.confirm_disconnect_dialog, null);
				builder.setView(body);
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (confirmDisconnectCheckbox.isChecked()) {
							Settings.setConfirmDisconnect(MenuActivity.this, false);
							trackEvent(Tracking.CATEGORY_SETTINGS, Tracking.ACTION_CONFIRM_DISCONNECTION, Tracking.LABEL_DIALOG, false);
						}
						service.quit();
						finish();
					}
				});
				builder.setNegativeButton(R.string.cancel, null);
				confirmDisconnectDialog = builder.create();
				confirmDisconnectCheckbox = (CheckBox) body.findViewById(R.id.confirm_disconnect_checkbox);
				return confirmDisconnectDialog;
			}
		}
		return super.onCreateDialog(id);
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
			case DIALOG_ID_CONFIRM_DISCONNECT: {
				confirmDisconnectCheckbox.setChecked(false);
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.mi_observe_blitz:
				service.sendInput("observe /b\n");
				trackEvent(Tracking.CATEGORY_SHOW_GAME, Tracking.ACTION_OBSERVE, Tracking.LABEL_HIGH_RATED_BLITZ, 0);
				return true;
			case R.id.mi_observe_standard:
				service.sendInput("observe /s\n");
				trackEvent(Tracking.CATEGORY_SHOW_GAME, Tracking.ACTION_OBSERVE, Tracking.LABEL_HIGH_RATED_STANDARD, 0);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
