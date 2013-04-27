package pl.mg6.yafi;

import pl.mg6.common.android.BaseActivity;
import android.content.Intent;
import android.os.Bundle;

public class YafiActivity extends BaseActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}
}
