package pl.mg6.yafi;

import java.util.ArrayList;
import java.util.List;

import pl.mg6.common.HtmlEntityEncoder;
import pl.mg6.common.Settings;
import pl.mg6.common.TimeUtils;
import pl.mg6.common.android.AndroidUtils;
import pl.mg6.common.android.ViewUtils;
import pl.mg6.yafi.lib.R;
import pl.mg6.yafi.model.FreechessService;
import pl.mg6.yafi.model.data.Communication;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends BaseFreechessActivity {
	
	private static final int REQUEST_ID_MATCH = 50000;
	private static final int REQUEST_ID_INFO = 50001;
	private static final int REQUEST_ID_INCHANNEL = 50002;
	
	public static final String EXTRA_NAME_USERNAME = "pl.mg6...ChatActivity.username";
	
	private ListView outputList;
	private EditText inputField;
	private LinearLayout tabs;
	private HorizontalScrollView tabsScrollPortrait;
	private ScrollView tabsScrollLandscape;
	
	private ChatAdapter outputAdapter;
	
	private List<String> allChatIds;
	private String currentChatId;
	private boolean forceCreate;
	
	private String menuName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_view);
		outputAdapter = new ChatAdapter(this);
		outputList = (ListView) findViewById(R.id.chat_output);
		outputList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		outputList.setStackFromBottom(true);
		outputList.setAdapter(outputAdapter);
		outputList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				Communication c = outputAdapter.getItem(info.position);
				menuName = c.getName();
				menu.setHeaderTitle(menuName);
				if (!menuName.equals(currentChatId)) {
					getMenuInflater().inflate(R.menu.chat_item, menu);
				} else {
					getMenuInflater().inflate(R.menu.chat_item_without_chat, menu);
				}
			}
		});
		inputField = (EditText) findViewById(R.id.chat_input);
		inputField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				return sendMessage();
			}
		});
		tabs = (LinearLayout) findViewById(R.id.chat_tabs);
		tabsScrollPortrait = (HorizontalScrollView) findViewById(R.id.chat_tabs_scroll_portrait);
		tabsScrollLandscape = (ScrollView) findViewById(R.id.chat_tabs_scroll_landscape);
		
		String name = getIntent().getStringExtra(EXTRA_NAME_USERNAME);
		if (name != null) {
			currentChatId = name;
			forceCreate = true;
		} else {
			currentChatId = Settings.loadCurrentChat(this);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.mi_match) {
			doMatch(menuName);
		} else if (id == R.id.mi_chat) {
			doChat(menuName);
		} else if (id == R.id.mi_info) {
			doInfo(menuName);
		} else if (id == R.id.mi_inchannel) {
			doInchannel(menuName);
		} else {
			return super.onContextItemSelected(item);
		}
		return true;
	}
	
	private void doMatch(String user) {
		Intent intent = new Intent(this, MatchActivity.class);
		intent.putExtra(MatchActivity.EXTRA_NAME_USERNAME, user);
		startActivityForResult(intent, REQUEST_ID_MATCH);
	}
	
	private void doChat(String user) {
		if (!allChatIds.contains(user)) {
			allChatIds.add(user);
			addTab(user);
		}
		currentChatId = user;
		final Button tab = (Button) tabs.findViewWithTag(user);
		tab.setText(currentChatId);
		updateViews();
		tab.post(new Runnable() {
			@Override
			public void run() {
				positionInCenter(tab);
			}
		});
	}
	
	private void doInfo(String user) {
		Intent intent = new Intent(this, InformationsActivity.class);
		intent.putExtra(InformationsActivity.EXTRA_NAME_USERNAME, user);
		startActivityForResult(intent, REQUEST_ID_INFO);
	}
	
	private void doInchannel(String number) {
		Intent intent = new Intent(this, ListUsersActivity.class);
		intent.putExtra(ListUsersActivity.EXTRA_NAME_INCHANNEL, number);
		startActivityForResult(intent, REQUEST_ID_INCHANNEL);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Settings.saveCurrentChat(this, currentChatId);
	}
	
	public void onSendClick(View view) {
		sendMessage();
	}
	
	public void onChatItemClick(View view) {
		outputList.showContextMenuForChild(view);
	}
	
	private boolean sendMessage() {
		if (currentChatId != null) {
			String message = inputField.getText().toString().trim();
			if (message.length() > 0) {
				message = HtmlEntityEncoder.encode(message);
				if (Communication.ID_ANNOUNCEMENT.equals(currentChatId)) {
					Toast.makeText(this, R.string.you_may_not_announce, Toast.LENGTH_SHORT).show();
					return true;
				} else if (Communication.ID_SHOUT.equals(currentChatId)) {
					service.sendInput("shout " + message + "\n");
				} else if (Communication.ID_CHESS_SHOUT.equals(currentChatId)) {
					service.sendInput("cshout " + message + "\n");
				} else {
					service.sendInput("xtell " + currentChatId + " " + message + "\n");
				}
				inputField.setText("");
				Communication c = Communication.create(currentChatId, service.getRealUsername(), message);
				outputAdapter.addItem(c);
				service.addMessage(c);
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void onStartHandlingMessages(boolean firstTime) {
		super.onStartHandlingMessages(firstTime);
		allChatIds = service.getAllChatIds();
		if (forceCreate && !allChatIds.contains(currentChatId)) {
			allChatIds.add(0, currentChatId);
		}
		if (allChatIds.size() > 0) {
			if (currentChatId == null || !allChatIds.contains(currentChatId)) {
				currentChatId = allChatIds.get(0);
			}
			createViews();
			updateViews();
			final View selected = tabs.getChildAt(allChatIds.indexOf(currentChatId));
			selected.post(new Runnable() {
				@Override
				public void run() {
					positionInCenter(selected);
				}
			});
		} else {
			currentChatId = null;
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case FreechessService.MSG_ID_COMMUNICATION:
				Communication c = (Communication) msg.obj;
				onCommunication(c);
				return true;
			case FreechessService.MSG_ID_NOT_LOGGED_IN:
				onNotLoggedIn((String) msg.obj);
				return true;
		}
		return super.handleMessage(msg);
	}
	
	private void onCommunication(Communication c) {
		String id = c.getId();
		if (!allChatIds.contains(id)) {
			allChatIds.add(id);
			addTab(id);
		}
		if (currentChatId == null) {
			currentChatId = id;
			final View selected = tabs.getChildAt(allChatIds.indexOf(currentChatId));
			selected.post(new Runnable() {
				@Override
				public void run() {
					positionInCenter(selected);
				}
			});
			outputAdapter.setItems(new ArrayList<Communication>());
			outputAdapter.addItem(c);
		} else if (currentChatId.equals(id)) {
			outputAdapter.addItem(c);
		} else {
			((Button) tabs.findViewWithTag(id)).setText(id + " *");
		}
	}
	
	private void onNotLoggedIn(String user) {
		for (String id : allChatIds) {
			if (user.equalsIgnoreCase(id)) {
				Toast.makeText(this, id + " is not logged in.", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}
	
	private void createViews() {
		tabs.removeAllViews();
		for (String id : allChatIds) {
			addTab(id);
		}
	}
	
	private void addTab(String id) {
		Button b = new Button(this);
		b.setText(id);
		b.setMinWidth(getResources().getDimensionPixelSize(R.dimen.chat_tab_item_min_width));
		b.setTag(id);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentChatId.equals(v.getTag())) {
					v.showContextMenu();
				} else {
					currentChatId = (String) v.getTag();
					((Button) v).setText(currentChatId);
					updateViews();
					positionInCenter(v);
				}
			}
		});
		b.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				String id = (String) v.getTag();
				if (Communication.ID_ANNOUNCEMENT.equals(id)) {
					
				} else if (Communication.ID_SHOUT.equals(id)) {
					
				} else if (Communication.ID_CHESS_SHOUT.equals(id)) {
					
				} else {
					try {
						Integer.parseInt(id);
						menuName = id;
						menu.setHeaderTitle(menuName);
						getMenuInflater().inflate(R.menu.chat_tab_channel, menu);
					} catch (NumberFormatException ex) {
						menuName = id;
						menu.setHeaderTitle(menuName);
						getMenuInflater().inflate(R.menu.chat_item_without_chat, menu);
					}
				}
			}
		});
		tabs.addView(b);
	}
	
	private void updateViews() {
		List<Communication> chat = service.getChat(currentChatId);
		outputAdapter.setItems(chat);
	}
	
	private void positionInCenter(View v) {
		for (int i = 0; i < tabs.getChildCount(); i++) {
			tabs.getChildAt(i).setSelected(false);
		}
		v.setSelected(true);
		if (tabsScrollPortrait != null) {
			ViewUtils.centerViewInScroll(v, tabsScrollPortrait);
		} else {
			ViewUtils.centerViewInScroll(v, tabsScrollLandscape);
		}
	}
	
	private static class ChatAdapter extends BaseAdapter {
		
		private LayoutInflater layoutInflater;
		
		private List<Communication> items;
		
		public ChatAdapter(Context context) {
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			items = new ArrayList<Communication>();
		}
		
		@Override
		public int getCount() {
			return items.size();
		}
		
		@Override
		public Communication getItem(int position) {
			return items.get(position);
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Communication c = getItem(position);
			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.chat_item, parent, false);
			}
			updateView(convertView, c);
			return convertView;
		}
		
		private void updateView(View view, Communication c) {
			TextView name = (TextView) view.findViewById(R.id.chat_item_name);
			TextView time = (TextView) view.findViewById(R.id.chat_item_time);
			TextView message = (TextView) view.findViewById(R.id.chat_item_message);
			name.setText(c.getName());
			time.setText(TimeUtils.formatDate(c.getTime()));
			message.setText(c.getMessage());
			AndroidUtils.linkify(message);
		}
		
		public void setItems(List<Communication> items) {
			this.items = items;
			notifyDataSetChanged();
		}
		
		public void addItem(Communication c) {
			this.items.add(c);
			notifyDataSetChanged();
		}
	}
}
