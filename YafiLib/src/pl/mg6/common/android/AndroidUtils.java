package pl.mg6.common.android;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.util.Linkify;
import android.widget.TextView;

public class AndroidUtils {
	
	private AndroidUtils() {
	}
	
	public static String getVersionName(Context context) {
		String versionName;
		try {
			versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			versionName = "0.0.1";
		}
		return versionName;
	}
	
	public static int getVersionCode(Context context) {
		int versionCode;
		try {
			versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			versionCode = 0;
		}
		return versionCode;
	}
	
	public static void linkify(TextView... views) {
		for (TextView tv : views) {
			Linkify.addLinks(tv, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
		}
	}
}
