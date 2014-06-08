package pl.mg6.yafi;

import pl.mg6.common.Settings;
import pl.mg6.common.android.tracker.Tracking;
import pl.mg6.yafi.lib.R;
import pl.mg6.yafi.model.FreechessService;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class SeekActivity extends BaseFreechessActivity {
	
	private EditText timeField;
	private EditText incrementField;
	private Spinner typeField;
	private CheckBox ratedField;
	private CheckBox formulaField;
	private EditText minRatingField;
	private EditText maxRatingField;

	private String[] typeIds;
	
	private static final String SEEK_FORMAT = "seek %d %d %s %s %d-%d\n";
	private static final String SEEK_FORMAT_WITH_FORMULA = "seek %d %d %s %s f %d-%d\n";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.seek_view);
		
		timeField = (EditText) findViewById(R.id.seek_time);
		incrementField = (EditText) findViewById(R.id.seek_increment);
		typeField = (Spinner) findViewById(R.id.seek_type);
		ratedField = (CheckBox) findViewById(R.id.seek_rated);
		formulaField = (CheckBox) findViewById(R.id.seek_formula);
		minRatingField = (EditText) findViewById(R.id.seek_min_rating);
		maxRatingField = (EditText) findViewById(R.id.seek_max_rating);
		
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
		formulaField.setChecked(Settings.isSeekWithFormula(this));
		minRatingField.setText(Settings.getSeekMinRating(this));
		maxRatingField.setText(Settings.getSeekMaxRating(this));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		String time = timeField.getText().toString();
		String increment = incrementField.getText().toString();
		String type = typeIds[typeField.getSelectedItemPosition()];
		boolean rated = ratedField.isChecked();
		boolean formula = formulaField.isChecked();
		String minRating = minRatingField.getText().toString();
		String maxRating = maxRatingField.getText().toString();
		Settings.setSeekData(this, time, increment, type, rated, formula, minRating, maxRating);
	}
	
	public void onSeekClick(View view) {
		doSeek();
	}
	
	private void doSeek() {
		int time;
		int increment;
		try {
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
		} catch (NumberFormatException ex) {
			time = 2;
			increment = 12;
		}
		String type = typeIds[typeField.getSelectedItemPosition()];
		boolean rated = ratedField.isChecked();
		boolean formula = formulaField.isChecked();
		int minRating = 0;
		int maxRating = 9999;
		try {
			if (minRatingField.length() > 0) {
				minRating = Integer.parseInt(minRatingField.getText().toString());
			}
			if (maxRatingField.length() > 0) {
				maxRating = Integer.parseInt(maxRatingField.getText().toString());
			}
		} catch (NumberFormatException ex) {
		}
		
		String format = formula ? SEEK_FORMAT_WITH_FORMULA : SEEK_FORMAT;
		service.sendInput(String.format(format, time, increment, type, (rated ? "r" : "u"), minRating, maxRating));
		if (time == 0 && increment == 0) {
			Toast.makeText(this, "Seeking untimed game.", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Seeking " + time + "min + " + increment + "s per move game.", Toast.LENGTH_SHORT).show();
		}
		String label = type + " " + (rated ? "r" : "u") + " " + time + " " + increment;
		int value = time + 2 * increment / 3;
		trackEvent(Tracking.CATEGORY_GET_GAME, Tracking.ACTION_SEEK, label, value);
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case FreechessService.MSG_ID_CANT_PLAY_VARIANTS_UNTIMED:
				Toast.makeText(this, "You can't play chess variants untimed.", Toast.LENGTH_SHORT).show();
				return true;
			case FreechessService.MSG_ID_TIME_CONTROLS_TOO_LARGE:
				Toast.makeText(this, "The time controls are too large.", Toast.LENGTH_SHORT).show();
				return true;
			case FreechessService.MSG_ID_ALREADY_HAVE_SAME_SEEK:
				Toast.makeText(this, "You already have an active seek with the same parameters.", Toast.LENGTH_SHORT).show();
				return true;
			case FreechessService.MSG_ID_CANNOT_CHALLENGE_WHILE_EXAMINING:
				Toast.makeText(this, "You cannot challenge while you are examining a game.", Toast.LENGTH_SHORT).show();
				return true;
			case FreechessService.MSG_ID_CANNOT_CHALLENGE_WHILE_PLAYING:
				Toast.makeText(this, "You cannot challenge while you are playing a game.", Toast.LENGTH_SHORT).show();
				return true;
			case FreechessService.MSG_ID_CAN_HAVE_3_SEEKS:
				Toast.makeText(this, "You can only have 3 active seeks.", Toast.LENGTH_SHORT).show();
				return true;
		}
		return super.handleMessage(msg);
	}
}
