package pl.mg6.common.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class ButtonEx extends Button {

	public ButtonEx(Context context) {
		super(context);
	}

	public ButtonEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ButtonEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public void setPressed(boolean pressed) {
		if (pressed && ((View) getParent()).isPressed()) {
			return;
		}
		super.setPressed(pressed);
	}
}
