package pl.mg6.yafi;

import java.util.List;

import pl.mg6.common.Settings;
import pl.mg6.common.android.OnSizeChangedListener;
import pl.mg6.common.android.ScrollViewEx;
import pl.mg6.common.android.ViewUtils;
import pl.mg6.common.android.tracker.Tracking;
import pl.mg6.yafi.lib.R;
import pl.mg6.yafi.model.FreechessService;
import android.os.Bundle;
import android.os.Message;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConsoleActivity extends BaseFreechessActivity {
	
	private TextView outputField;
	private EditText inputField;
	private ScrollViewEx outputScroll;

	private LinearLayout tabs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.console_view);
		outputField = (TextView) findViewById(R.id.console_output);
		inputField = (EditText) findViewById(R.id.console_input);
		inputField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				return sendCommand();
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
		tabs = (LinearLayout) findViewById(R.id.console_tabs);
		List<String> frequentlyUsedCommands = Settings.getFrequentlyUsedCommands(this);
		if (frequentlyUsedCommands != null) {
			for (String command : frequentlyUsedCommands) {
				addTab(command);
			}
		}
	}
	
	private boolean sendCommand() {
		String input = inputField.getText().toString().trim();
		int length = input.length();
		if (length > 0) {
			service.sendInput(input + "\n");
			inputField.setText("");
			Settings.addCommand(this, input);
			String cmd = input.split(" ")[0].toLowerCase();
			trackEvent(Tracking.CATEGORY_VETERAN, Tracking.ACTION_COMMAND, cmd, length);
			return true;
		}
		return false;
	}
	
	public void onSendClick(View view) {
		sendCommand();
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		String title = item.getTitle().toString();
		if (title.startsWith("Delete ")) {
			String command = title.substring("Delete ".length());
			Settings.removeFromFrequentlyUsedCommands(this, command);
			for (int i = 0; i < tabs.getChildCount(); i++) {
				Button tab = (Button) tabs.getChildAt(i);
				if (command.contentEquals(tab.getText())) {
					tabs.removeViewAt(i);
					break;
				}
			}
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	private void addTab(final String command) {
		Button b = new Button(this);
		b.setText(command);
		b.setMinWidth(getResources().getDimensionPixelSize(R.dimen.console_tab_item_min_width));
		b.setMaxWidth(getResources().getDimensionPixelSize(R.dimen.console_tab_item_max_width));
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				service.sendInput(command + "\n");
				Settings.addCommand(ConsoleActivity.this, command);
				String cmd = command.split(" ")[0].toLowerCase();
				trackEvent(Tracking.CATEGORY_VETERAN, Tracking.ACTION_COMMAND_CLICK, cmd, command.length());
			}
		});
		b.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
				contextMenu.add("Delete " + command);
			}
		});
		tabs.addView(b);
	}
	
	@Override
	protected void onStartHandlingMessages(boolean firstTime) {
		super.onStartHandlingMessages(firstTime);
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
