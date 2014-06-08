package pl.mg6.yafi;

import android.app.Activity;
import android.os.Bundle;

public class SelfFinishingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }
}
