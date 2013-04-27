package pl.mg6.yafi;

import java.util.Random;

import pl.mg6.common.Settings;
import pl.mg6.common.android.tracker.Tracking;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class MenuActivity extends BaseFreechessActivity {
	
	private static final int FIRST_ID = 10000;
	
	private static final int DIALOG_ID_CONFIRM_DISCONNECT = FIRST_ID + 0;

	private static final int REQUEST_ID_BOARD = FIRST_ID + 100;
	private static final int REQUEST_ID_CHAT = FIRST_ID + 101;
	private static final int REQUEST_ID_SEEK = FIRST_ID + 102;
	private static final int REQUEST_ID_SOUGHT = FIRST_ID + 103;
	private static final int REQUEST_ID_MATCH = FIRST_ID + 104;
	private static final int REQUEST_ID_CHALLENGES = FIRST_ID + 105;
	private static final int REQUEST_ID_CONSOLE = FIRST_ID + 106;
	private static final int REQUEST_ID_USER_PREFS = FIRST_ID + 107;

	private AlertDialog confirmDisconnectDialog;
	private CheckBox confirmDisconnectCheckbox;
	
	private ViewGroup updateRatePanel;
	private boolean triedShowRate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_view);
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
			text.setText("New version of Yafi available!");
			button.setText("Download");
			button.setTag(false);
		} else if (!(triedShowRate || !Settings.canShowRate(this))) {
			triedShowRate = true;
			if (new Random().nextInt(10) == 0) {
				updateRatePanel.setVisibility(View.VISIBLE);
				TextView text = (TextView) updateRatePanel.findViewById(R.id.main_update_rate_text);
				Button button = (Button) updateRatePanel.findViewById(R.id.main_update_rate_button);
				text.setText("If you enjoy using Yafi,\nplease take a moment to rate it.");
				button.setText("Rate Yafi");
				button.setTag(true);
			}
		}
	}
	
	public void onUpdateRateClick(View view) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=" + getPackageName()));
		startActivity(intent);
		if ((Boolean) view.getTag()) {
			updateRatePanel.setVisibility(View.GONE);
			Settings.setRateClicked(this);
			trackEvent(Tracking.CATEGORY_EXTERNAL, Tracking.ACTION_RATE, null, 0);
		} else {
			trackEvent(Tracking.CATEGORY_EXTERNAL, Tracking.ACTION_UPDATE, null, 0);
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
				builder.setTitle("Do you want to disconnect?");
				View body = getLayoutInflater().inflate(R.layout.confirm_disconnect_dialog, null);
				builder.setView(body);
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (confirmDisconnectCheckbox.isChecked()) {
							Settings.setConfirmDisconnect(MenuActivity.this, false);
						}
						service.quit();
						finish();
					}
				});
				builder.setNegativeButton("Cancel", null);
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
}
