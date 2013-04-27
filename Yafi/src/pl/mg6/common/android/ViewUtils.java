package pl.mg6.common.android;

import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;

public class ViewUtils {
	
	private ViewUtils() {
	}
	
	public static void setTextAndScroll(final ScrollView scrollView, final TextView textView, final String text) {
		int textViewHeight = textView.getHeight();
		int scrollViewHeight = scrollView.getHeight();
		int scrollViewPosition = scrollView.getScrollY();
		textView.setText(text);
		if (scrollViewPosition + scrollViewHeight >= textViewHeight - 10 || scrollView.getTag() != null && (scrollView.getTag() instanceof Boolean) && (Boolean) scrollView.getTag()) {
			scrollView.setTag(true);
			scrollView.post(new Runnable() {
				@Override
				public void run() {
					int textViewHeight = textView.getHeight();
					int scrollViewHeight = scrollView.getHeight();
					scrollView.scrollTo(0, textViewHeight - scrollViewHeight);
					scrollView.setTag(false);
				}
			});
		}
	}
	
	public static void centerViewInScroll(View view, HorizontalScrollView scrollView) {
		int left = view.getLeft();
		int width = view.getWidth();
		int center = left + width / 2;
		int scrollWidth = scrollView.getWidth();
		int scrollCenter = scrollWidth / 2;
		scrollView.smoothScrollTo(center - scrollCenter, 0);
	}
	
	public static void centerViewInScroll(View view, ScrollView scrollView) {
		int top = view.getTop();
		int height = view.getHeight();
		int center = top + height / 2;
		int scrollHeight = scrollView.getHeight();
		int scrollCenter = scrollHeight / 2;
		scrollView.smoothScrollTo(0, center - scrollCenter);
	}
}
