package pl.mg6.common.android.ads;

import pl.mg6.common.Settings;

import android.util.Log;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest.ErrorCode;

public class BaseAdListener implements AdListener {
	
	private static final String TAG = BaseAdListener.class.getSimpleName();
	
	@Override
	public void onDismissScreen(Ad ad) {
		if (Settings.LOG_ADS) {
			Log.i(TAG, "onDismissScreen " + ad);
		}
	}
	
	@Override
	public void onFailedToReceiveAd(Ad ad, ErrorCode code) {
		if (Settings.LOG_ADS) {
			Log.i(TAG, "onFailedToReceiveAd " + ad + " " + code);
		}
	}
	
	@Override
	public void onLeaveApplication(Ad ad) {
		if (Settings.LOG_ADS) {
			Log.i(TAG, "onLeaveApplication " + ad);
		}
	}
	
	@Override
	public void onPresentScreen(Ad ad) {
		if (Settings.LOG_ADS) {
			Log.i(TAG, "onPresentScreen " + ad);
		}
	}
	
	@Override
	public void onReceiveAd(Ad ad) {
		if (Settings.LOG_ADS) {
			Log.i(TAG, "onReceiveAd " + ad);
		}
	}
}
