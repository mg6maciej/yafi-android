package pl.mg6.common.android.tracker;

import pl.mg6.common.Settings;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.google.android.apps.analytics.easytracking.EasyTracker;

public class TrackedPreferenceActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EasyTracker.getTracker().setContext(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (Settings.isHelpImprove(this)) {
			EasyTracker.getTracker().trackActivityStart(this);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (Settings.isHelpImprove(this)) {
			EasyTracker.getTracker().trackActivityStop(this);
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		Object o = super.onRetainNonConfigurationInstance();
		if (Settings.isHelpImprove(this)) {
			EasyTracker.getTracker().trackActivityRetainNonConfigurationInstance();
		}
		return o;
	}
	
	protected void trackEvent(String category, String action, String label, int value) {
		if (Settings.isHelpImprove(this)) {
			EasyTracker.getTracker().trackEvent(category, action, label, value);
		}
	}
	
	protected void trackEvent(String category, String action, String label, boolean value) {
		trackEvent(category, action, label, value ? 100 : 0);
	}
}
