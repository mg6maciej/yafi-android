package pl.mg6.yafi;

import java.util.UUID;

import pl.mg6.common.Settings;
import pl.mg6.common.TimeUtils;
import pl.mg6.common.android.BaseActivity;
import pl.mg6.common.android.ads.BaseAdListener;
import pl.mg6.common.android.tracker.Tracking;
import pl.mg6.yafi.lib.R;
import pl.mg6.yafi.model.FreechessService;
import pl.mg6.yafi.model.FreechessService.FreechessServiceInterface;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.ads.Ad;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

public class BaseFreechessActivity extends BaseActivity implements ServiceConnection, Callback {
	
	private static final String TAG = BaseFreechessActivity.class.getSimpleName();
	
	protected static final int RESULT_DISCONNECTED = RESULT_FIRST_USER + 1000;
	protected static final int RESULT_GAME_CREATE = RESULT_FIRST_USER + 1001;
	
	private static final int DIALOG_ID_DISCONNECTED = 1000;
	
	private AlertDialog disconnectedDialog;
	
	protected Handler handler;
	
	protected FreechessServiceInterface service;
	protected boolean bindFreechessService = true;
	private boolean stopped = true;
	protected boolean checkLoggedOn = true;
	
	private boolean firstTime = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler(this);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}
	
	@Override
	public void onContentChanged() {
		super.onContentChanged();
		View view = findViewById(R.id.ad_view);
		if (view != null) {
			if (Settings.isPaidApp(this)) {
				ViewGroup parent = (ViewGroup) view.getParent();
				parent.removeView(view);
			} else {
				final AdView adView = (AdView) view;
				adView.setAdListener(new BaseAdListener() {
					private long startTime = 0L;
					@Override
					public void onPresentScreen(Ad ad) {
						super.onPresentScreen(ad);
						startTime = TimeUtils.getTimestamp();
					}
					
					@Override
					public void onLeaveApplication(Ad ad) {
						super.onLeaveApplication(ad);
						startTime = TimeUtils.getTimestamp();
					}
					@Override
					public void onDismissScreen(Ad ad) {
						super.onDismissScreen(ad);
						long endTime = TimeUtils.getTimestamp();
						int time = (int) (endTime - startTime);
						trackEvent(Tracking.CATEGORY_ADMOB, Tracking.ACTION_CLICKED, BaseFreechessActivity.this.getClass().getSimpleName(), time);
					}
					@Override
					public void onFailedToReceiveAd(Ad ad, ErrorCode code) {
						super.onFailedToReceiveAd(ad, code);
						trackEvent(Tracking.CATEGORY_ADMOB, Tracking.ACTION_FAILED_TO_LOAD, code.name(), 0);
					}
				});
				AdRequest request = new AdRequest();
				request.addTestDevice(AdRequest.TEST_EMULATOR);
				request.addTestDevice("ADC437B053BD10CA22B9DCF950994CD8");
				adView.loadAd(request);
			}
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		stopped = false;
		if (bindFreechessService) {
			Intent intent = new Intent(this, FreechessService.class);
			bindService(intent, this, Context.BIND_AUTO_CREATE);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		stopped = true;
		if (bindFreechessService) {
			unbindService(this);
		}
		if (service != null) {
			synchronized (service) {
				onStopHandlingMessages();
				service.removeCallbackHandler(handler);
				service = null;
			}
		}
		dismissDisconnectedDialog();
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder binder) {
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onServiceConnected " + name + " " + binder);
		}
		if (!stopped) {
			service = (FreechessServiceInterface) binder;
			synchronized (binder) {
				if (checkLoggedOn && !service.isLoggedOn()) {
					onDisconnected();
				} else {
					service.addCallbackHandler(handler);
					onStartHandlingMessages(firstTime);
					firstTime = false;
				}
			}
		}
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		if (Settings.LOG_LIFECYCLE) {
			Log.d(TAG, this + " onServiceDisconnected " + name);
		}
	}
	
	protected void onStartHandlingMessages(boolean firstTime) {
	}
	
	protected void onStopHandlingMessages() {
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case FreechessService.MSG_ID_DISCONNECTED:
				onDisconnected();
				return true;
			case FreechessService.MSG_ID_GAME_CREATE:
				Settings.saveCurrentGame(this, (UUID) msg.obj);
				onGameCreate();
				return true;
		}
		return false;
	}
	
	protected void onDisconnected() {
		showDialog(DIALOG_ID_DISCONNECTED);
	}
	
	protected void onGameCreate() {
		setResult(RESULT_GAME_CREATE);
		finish();
	}
	
	private void dismissDisconnectedDialog() {
		if (disconnectedDialog != null && disconnectedDialog.isShowing()) {
			disconnectedDialog.dismiss();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_ID_DISCONNECTED: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.disconnected);
				builder.setNeutralButton(R.string.ok, null);
				disconnectedDialog = builder.create();
				disconnectedDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						setResult(RESULT_DISCONNECTED);
						finish();
					}
				});
				return disconnectedDialog;
			}
		}
		return super.onCreateDialog(id);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_DISCONNECTED && checkLoggedOn) {
			setResult(RESULT_DISCONNECTED);
			finish();
		} else if (resultCode == RESULT_GAME_CREATE) {
			onGameCreate();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
