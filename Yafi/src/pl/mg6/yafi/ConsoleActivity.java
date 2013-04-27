package pl.mg6.yafi;

import pl.mg6.common.android.OnSizeChangedListener;
import pl.mg6.common.android.ScrollViewEx;
import pl.mg6.common.android.ViewUtils;
import pl.mg6.common.android.tracker.Tracking;
import pl.mg6.yafi.model.FreechessService;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ConsoleActivity extends BaseFreechessActivity {
	
	private TextView outputField;
	private EditText inputField;
	private ScrollViewEx outputScroll;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.console_view);
		outputField = (TextView) findViewById(R.id.console_output);
		inputField = (EditText) findViewById(R.id.console_input);
		inputField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				String input = inputField.getText().toString().trim();
				int length = input.length();
				if (length > 0) {
					String cmd = input.split(" ")[0].toLowerCase();
					input += "\n";
					service.sendInput(input);
					inputField.setText("");
					trackEvent(Tracking.CATEGORY_VETERAN, Tracking.ACTION_COMMAND, cmd, length);
					return true;
				}
				return false;
			}
		});
		outputScroll = (ScrollViewEx) findViewById(R.id.console_output_scroll);
		outputScroll.setOnSizeChangedListener(new OnSizeChangedListener() {
			@Override
			public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {
				int textViewHeight = outputField.getHeight();
				int scrollViewHeight = oldh;
				int scrollViewPosition = outputScroll.getScrollY();
				if (scrollViewPosition + scrollViewHeight >= textViewHeight - 10) {
					outputScroll.post(new Runnable() {
						@Override
						public void run() {
							int textViewHeight = outputField.getHeight();
							int scrollViewHeight = outputScroll.getHeight();
							outputScroll.smoothScrollTo(0, textViewHeight - scrollViewHeight);
						}
					});
				}
			}
		});
	}
	
	@Override
	protected void onStartHandlingMessages() {
		super.onStartHandlingMessages();
		String output = service.getOutput();
		outputField.setText("");
		ViewUtils.setTextAndScroll(outputScroll, outputField, output);
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case FreechessService.MSG_ID_RECEIVED_OUTPUT: {
				String output = (String) msg.obj;
				setOutput(output);
				return true;
			}
		}
		return super.handleMessage(msg);
	}
	
	private void setOutput(String output) {
		ViewUtils.setTextAndScroll(outputScroll, outputField, output);
	}
}
