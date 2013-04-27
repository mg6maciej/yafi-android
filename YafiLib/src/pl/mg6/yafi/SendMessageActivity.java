package pl.mg6.yafi;

import pl.mg6.common.HtmlEntityEncoder;
import pl.mg6.yafi.lib.R;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SendMessageActivity extends BaseFreechessActivity {
	
	public static final String EXTRA_NAME_USERNAME = "pl.mg6...SendMessageActivity.username";
	
	private EditText usernameField;
	private EditText messageField;
	private Button sendButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_message_view);
		usernameField = (EditText) findViewById(R.id.send_message_username);
		messageField = (EditText) findViewById(R.id.send_message_message);
		sendButton = (Button) findViewById(R.id.send_message_send);
		
		TextWatcher listener = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				boolean enabled = usernameField.length() > 0 && messageField.length() > 0;
				sendButton.setEnabled(enabled);
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		};
		usernameField.addTextChangedListener(listener);
		messageField.addTextChangedListener(listener);
		
		String username = getIntent().getStringExtra(EXTRA_NAME_USERNAME);
		usernameField.setText(username);
	}
	
	public void onSendClick(View view) {
		sendMessage();
	}
	
	private void sendMessage() {
		String username = usernameField.getText().toString();
		String message = messageField.getText().toString();
		if (username.length() > 0 && message.length() > 0) {
			service.sendInput("message " + username + " " + HtmlEntityEncoder.encode(message) + "\n");
			messageField.setText(null);
		}
	}
}
