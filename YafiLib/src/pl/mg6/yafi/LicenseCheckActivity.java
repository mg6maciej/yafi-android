package pl.mg6.yafi;

import java.util.List;

import pl.mg6.common.android.BaseActivity;
import pl.mg6.common.android.tracker.Tracking;
import pl.mg6.yafi.lib.R;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Obfuscator;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

public class LicenseCheckActivity extends BaseActivity {
	
	private static final String TAG = LicenseCheckActivity.class.getSimpleName();
	
	private LicenseChecker checker;
	
	private View progressView;
	private View errorView;
	private TextView descriptionField;
	private Button goToMarketButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.license_check_view);
		
		progressView = findViewById(R.id.license_check_progress);
		errorView = findViewById(R.id.license_check_error);
		descriptionField = (TextView) findViewById(R.id.license_check_description);
		goToMarketButton = (Button) findViewById(R.id.license_check_go_to_market);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		checkLicense();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		checker.onDestroy();
	}
	
	public void onRetryClick(View view) {
		errorView.setVisibility(View.GONE);
		progressView.setVisibility(View.VISIBLE);
		checkLicense();
	}
	
	public void onGoToMarketClick(View view) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=" + getPackageName()));
		List<ResolveInfo> infos = getPackageManager().queryIntentActivities(intent, 0);
		if (infos.size() == 0) {
			intent.setData(Uri.parse("http://yafi.pl/android/apk/Yafi.apk"));
			Toast.makeText(this, "Market application not found. Downloading Yafi from http://yafi.pl", Toast.LENGTH_LONG).show();
		}
		startActivity(intent);
	}
	
	private void checkLicense() {
		byte[] salt = new byte[] { 37, 97, 72, -80, 89, -45, -17, 37, -114, -49, -35, -76, 41, -114, 72, -29, -5, -94, -116, 22 };
		String applicationId = getPackageName();
		String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		Obfuscator obfuscator = new AESObfuscator(salt, applicationId, deviceId);
		Policy policy = new ServerManagedPolicy(this, obfuscator);
		String encodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApf+8fY+YuGMEGayTj1SDDxMd9Yb8ehUigWuxpiQd5n2gSzQol7hx7YMuu1sl0D4rlkAWNWuwNsYeLitg2f73HBrYCvVPE+gZGinAyLdOWvnrSJnpw1rhK1wPMgRMsRwMjayQXR+4a74iSaQERCpGqLzd8AGijkXh+CGL+uqHFW91stp7vede32Ox1OwVW8/vFWg+H/LFbS5ySgzh/VaUAlQJvCZLaipvEbEF8cq9fYrVZXJWywVSr1PUcW+9d1bZVFKX/YzUpr93tnHrY7RAHJesc9IvCgUhQuI45VA4qT0aPWgTu6eMKK2ycCxUK/PD5BS4zTTwXTkOMUXt7tjufQIDAQAB";
		checker = new LicenseChecker(this, policy, encodedPublicKey);
		checker.checkAccess(new LicenseCheckerCallback() {
			
			@Override
			public void dontAllow(final int reason) {
				Log.i(TAG, "dontAllow " + reason);
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showError(reason);
					}
				});
				trackEvent(Tracking.CATEGORY_PAID_APP, Tracking.ACTION_LICENSE, Tracking.LABEL_DONT_ALLOW, reason);
			}
			
			@Override
			public void applicationError(final int errorCode) {
				Log.i(TAG, "applicationError " + errorCode);
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showError(errorCode);
					}
				});
				trackEvent(Tracking.CATEGORY_PAID_APP, Tracking.ACTION_LICENSE, Tracking.LABEL_APPLICATION_ERROR, errorCode);
			}
			
			@Override
			public void allow(int reason) {
				Log.i(TAG, "allow " + reason);
				if (isFinishing()) {
					return;
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Intent intent = new Intent(LicenseCheckActivity.this, LoginActivity.class);
						startActivity(intent);
						finish();
					}
				});
				trackEvent(Tracking.CATEGORY_PAID_APP, Tracking.ACTION_LICENSE, Tracking.LABEL_ALLOW, reason);
			}
		});
	}
	
	private void showError(int code) {
		int description;
		boolean includeCode = false;
		boolean showGoToMarket = true;
		switch (code) {
			case LicenseCheckerCallback.ERROR_CHECK_IN_PROGRESS:
				description = R.string.error_check_in_progress;
				break;
			case LicenseCheckerCallback.ERROR_INVALID_PACKAGE_NAME:
				description = R.string.error_invalid_package_name;
				break;
			case LicenseCheckerCallback.ERROR_INVALID_PUBLIC_KEY:
				description = R.string.error_invalid_public_key;
				break;
			case LicenseCheckerCallback.ERROR_MISSING_PERMISSION:
				description = R.string.error_missing_permission;
				break;
			case LicenseCheckerCallback.ERROR_NON_MATCHING_UID:
				description = R.string.error_non_matching_uid;
				break;
			case LicenseCheckerCallback.ERROR_NOT_MARKET_MANAGED:
				description = R.string.error_not_market_managed;
				break;
			case Policy.NOT_LICENSED:
				description = R.string.error_not_licensed;
				break;
			case Policy.RETRY:
				description = R.string.error_retry;
				showGoToMarket = false;
				break;
			default:
				description = R.string.error_unknown_code;
				includeCode = true;
				break;
		}
		progressView.setVisibility(View.GONE);
		errorView.setVisibility(View.VISIBLE);
		if (!includeCode) {
			descriptionField.setText(description);
		} else {
			String format = getString(description);
			String text = String.format(format, code);
			descriptionField.setText(text);
		}
		goToMarketButton.setVisibility(showGoToMarket ? View.VISIBLE : View.INVISIBLE);
	}
}
