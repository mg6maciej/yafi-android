package pl.mg6.yafi;

import pl.mg6.common.Settings;
import pl.mg6.common.android.tracker.Tracking;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MatchActivity extends BaseFreechessActivity {
	
	public static final String EXTRA_NAME_USERNAME = "pl.mg6...MatchActivity.username";
	
	private EditText userField;
	private EditText timeField;
	private EditText incrementField;
	private Spinner typeField;
	private CheckBox ratedField;

	private String[] typeIds;
	
	private static final String MATCH_FORMAT = "match %s %d %d %s %s\n";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.match_view);
		
		userField = (EditText) findViewById(R.id.match_user);
		timeField = (EditText) findViewById(R.id.match_time);
		incrementField = (EditText) findViewById(R.id.match_increment);
		typeField = (Spinner) findViewById(R.id.match_type);
		ratedField = (CheckBox) findViewById(R.id.match_rated);
		
		typeIds = getResources().getStringArray(R.array.types_ids);
		
		userField.setText(getIntent().getStringExtra(EXTRA_NAME_USERNAME));
		timeField.setText(Settings.getMatchTime(this));
		incrementField.setText(Settings.getMatchIncrement(this));
		String type = Settings.getMatchType(this);
		for (int i = 0; i < typeIds.length; i++) {
			if (typeIds[i].equals(type)) {
				typeField.setSelection(i);
				break;
			}
		}
		ratedField.setChecked(Settings.isMatchRated(this));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		String time = timeField.getText().toString();
		String increment = incrementField.getText().toString();
		String type = typeIds[typeField.getSelectedItemPosition()];
		boolean rated = ratedField.isChecked();
		Settings.setMatchData(this, time, increment, type, rated);
	}
	
	public void onMatchClick(View view) {
		doMatch();
	}
	
	private void doMatch() {
		String user = userField.getText().toString();
		if (user.length() > 1) {
			int time;
			int increment;
			if (timeField.length() == 0 && incrementField.length() == 0) {
				time = 2;
				increment = 12;
			} else if (incrementField.length() == 0) {
				time = Integer.parseInt(timeField.getText().toString());
				increment = 0;
			} else if (timeField.length() == 0) {
				time = 0;
				increment = Integer.parseInt(incrementField.getText().toString());
			} else {
				time = Integer.parseInt(timeField.getText().toString());
				increment = Integer.parseInt(incrementField.getText().toString());
			}
			String type = typeIds[typeField.getSelectedItemPosition()];
			boolean rated = ratedField.isChecked();
			
			service.sendInput(String.format(MATCH_FORMAT, user, time, increment, type, (rated ? "r" : "u")));
			String label = type + " " + (rated ? "r" : "u") + " " + time + " " + increment;
			int value = time + 2 * increment / 3;
			trackEvent(Tracking.CATEGORY_GET_GAME, Tracking.ACTION_MATCH, label, value);
		} else {
			Toast.makeText(this, R.string.enter_username, Toast.LENGTH_SHORT).show();
		}
	}
}
