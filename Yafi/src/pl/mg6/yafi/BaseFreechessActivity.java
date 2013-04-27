package pl.mg6.yafi;

import java.util.UUID;

import pl.mg6.common.Settings;
import pl.mg6.common.android.BaseActivity;
import pl.mg6.yafi.model.FreechessService;
import pl.mg6.yafi.model.FreechessService.FreechessServiceInterface;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler(this);
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
					onStartHandlingMessages();
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
	
	protected void onStartHandlingMessages() {
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
				builder.setTitle("Disconnected");
				builder.setNeutralButton("OK", null);
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
