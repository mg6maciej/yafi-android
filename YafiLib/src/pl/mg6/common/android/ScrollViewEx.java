package pl.mg6.common.android;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ScrollViewEx extends ScrollView {
	
	private OnSizeChangedListener onSizeChangedListener;
	
	public ScrollViewEx(Context context) {
		super(context);
	}
	
	public ScrollViewEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ScrollViewEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		notifySizeChanged(w, h, oldw, oldh);
	}
	
	public void setOnSizeChangedListener(OnSizeChangedListener listener) {
		onSizeChangedListener = listener;
	}
	
	private void notifySizeChanged(int w, int h, int oldw, int oldh) {
		if (onSizeChangedListener != null) {
			onSizeChangedListener.onSizeChanged(this, w, h, oldw, oldh);
		}
	}
}
