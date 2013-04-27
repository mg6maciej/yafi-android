package pl.mg6.yafi;

import pl.mg6.common.Settings;
import pl.mg6.common.android.tracker.Tracking;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class SeekActivity extends BaseFreechessActivity {
	
	private EditText timeField;
	private EditText incrementField;
	private Spinner typeField;
	private CheckBox ratedField;

	private String[] typeIds;
	
	private static final String SEEK_FORMAT = "seek %d %d %s %s\n";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.seek_view);
		
		timeField = (EditText) findViewById(R.id.seek_time);
		incrementField = (EditText) findViewById(R.id.seek_increment);
		typeField = (Spinner) findViewById(R.id.seek_type);
		ratedField = (CheckBox) findViewById(R.id.seek_rated);
		
		typeIds = getResources().getStringArray(R.array.types_ids);
		
		timeField.setText(Settings.getSeekTime(this));
		incrementField.setText(Settings.getSeekIncrement(this));
		String type = Settings.getSeekType(this);
		for (int i = 0; i < typeIds.length; i++) {
			if (typeIds[i].equals(type)) {
				typeField.setSelection(i);
				break;
			}
		}
		ratedField.setChecked(Settings.isSeekRated(this));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		String time = timeField.getText().toString();
		String increment = incrementField.getText().toString();
		String type = typeIds[typeField.getSelectedItemPosition()];
		boolean rated = ratedField.isChecked();
		Settings.setSeekData(this, time, increment, type, rated);
	}
	
	public void onSeekClick(View view) {
		doSeek();
	}
	
	private void doSeek() {
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
		
		service.sendInput(String.format(SEEK_FORMAT, time, increment, type, (rated ? "r" : "u")));
		String label = type + " " + (rated ? "r" : "u") + " " + time + " " + increment;
		int value = time + 2 * increment / 3;
		trackEvent(Tracking.CATEGORY_GET_GAME, Tracking.ACTION_SEEK, label, value);
	}
}
