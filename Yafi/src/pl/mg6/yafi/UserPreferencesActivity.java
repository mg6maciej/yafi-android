package pl.mg6.yafi;

import pl.mg6.common.Settings;
import pl.mg6.common.StringUtils;
import pl.mg6.common.android.tracker.TrackedPreferenceActivity;
import pl.mg6.common.android.tracker.Tracking;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;

import com.google.android.apps.analytics.easytracking.EasyTracker;

public class UserPreferencesActivity extends TrackedPreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.user_prefs);
		if (Build.VERSION.SDK_INT >= 11) {
			Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			if (!vibrator.hasVibrator()) {
				findPreference(Settings.PREF_VIBRATE).setEnabled(false);
			}
		}
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
	}
	
	private OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			boolean helpImprove = sharedPreferences.getBoolean(Settings.PREF_HELP_IMPROVE, true);
			if (Settings.PREF_HELP_IMPROVE.equals(key)) {
				if (helpImprove) {
					EasyTracker.getTracker().trackActivityStart(UserPreferencesActivity.this);
				}
				EasyTracker.getTracker().trackEvent(Tracking.CATEGORY_SETTINGS, Tracking.ACTION_HELP_IMPROVE, null, helpImprove ? 100 : 0);
				if (!helpImprove) {
					EasyTracker.getTracker().trackActivityStop(UserPreferencesActivity.this);
				}
			} else if (helpImprove) {
				if (Settings.PREF_CONFIRM_DISCONNECT.equals(key)) {
					boolean value = sharedPreferences.getBoolean(key, true);
					trackEvent(Tracking.CATEGORY_SETTINGS, Tracking.ACTION_CONFIRM_DISCONNECTION, null, value);
				} else if (Settings.PREF_REMEMBER_PASSWORD.equals(key)) {
					boolean value = sharedPreferences.getBoolean(key, false);
					trackEvent(Tracking.CATEGORY_SETTINGS, Tracking.ACTION_REMEMBER_PASSWORD, null, value);
				} else if (Settings.PREF_BOARD_INPUT_METHOD.equals(key)) {
					String value = sharedPreferences.getString(key, null);
					if ("1".equals(value)) {
						value = Tracking.LABEL_DRAG_AND_DROP;
					} else if ("2".equals(value)) {
						value = Tracking.LABEL_CLICK_CLICK;
					} else if ("3".equals(value)) {
						value = Tracking.LABEL_BOTH;
					}
					trackEvent(Tracking.CATEGORY_SETTINGS, Tracking.ACTION_INPUT_METHOD, value, 0);
				} else if (Settings.PREF_BOARD_PIECES.equals(key)) {
					String value = sharedPreferences.getString(key, null);
					value = StringUtils.underlinedToPascalCase(value);
					trackEvent(Tracking.CATEGORY_SETTINGS, Tracking.ACTION_PIECES, value, 0);
				} else if (Settings.PREF_BOARD_COLORS.equals(key)) {
					String value = sharedPreferences.getString(key, null);
					value = StringUtils.underlinedToPascalCase(value);
					trackEvent(Tracking.CATEGORY_SETTINGS, Tracking.ACTION_COLORS, value, 0);
				} else if (Settings.PREF_BOARD_PREMOVE.equals(key)) {
					boolean value = sharedPreferences.getBoolean(key, true);
					trackEvent(Tracking.CATEGORY_SETTINGS, Tracking.ACTION_PREMOVE, null, value);
				} else if (Settings.PREF_SOUND.equals(key)) {
					boolean value = sharedPreferences.getBoolean(key, true);
					trackEvent(Tracking.CATEGORY_SETTINGS, Tracking.ACTION_SOUND, null, value);
				} else if (Settings.PREF_VIBRATE.equals(key)) {
					boolean value = sharedPreferences.getBoolean(key, true);
					trackEvent(Tracking.CATEGORY_SETTINGS, Tracking.ACTION_VIBRATE, null, value);
				}
			}
		}
	};
}
