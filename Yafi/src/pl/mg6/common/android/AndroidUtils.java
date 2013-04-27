package pl.mg6.common.android;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

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
	
//	public static int dipToPix(Context context, float dip) {
//		return (int) (dip * context.getResources().getDisplayMetrics().density);
//	}
}
