package pl.mg6.common.android;

import java.util.ArrayList;
import java.util.List;

import pl.mg6.common.Settings;
import pl.mg6.common.android.tracker.TrackedActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class BaseActivity extends TrackedActivity {
	
	private static final String TAG = BaseActivity.class.getSimpleName();
	
	private static List<String> aliveActivities = new ArrayList<String>();
	
	public BaseActivity() {
		if (Settings.LOG_LIFECYCLE) {
			aliveActivities.add(getClass().getSimpleName() + hashCode());
			Log.d(TAG, this + " constructor; count=" + aliveActivities.size());
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (Settings.LOG_LIFECYCLE) {
			aliveActivities.remove(getClass().getSimpleName() + hashCode());
			Log.d(TAG, this + " finalize; count=" + aliveActivities.size());
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onCreate " + savedInstanceState);
		}
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onRestart");
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onStart");
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onResume");
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onPause");
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onStop");
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onDestroy");
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onSaveInstanceState " + outState);
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onRestoreInstanceState " + savedInstanceState);
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		Object o = super.onRetainNonConfigurationInstance();
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onRetainNonConfigurationInstance");
		}
		return o;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onActivityResult " + requestCode + " " + resultCode + " " + data);
		}
	}
}
