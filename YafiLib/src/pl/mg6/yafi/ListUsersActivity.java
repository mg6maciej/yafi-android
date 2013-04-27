package pl.mg6.yafi;

import pl.mg6.yafi.lib.R;
import pl.mg6.yafi.model.FreechessService;
import pl.mg6.yafi.model.data.InchannelInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListUsersActivity extends BaseFreechessActivity {
	
	private static final int REQUEST_ID_MATCH = 70000;
	private static final int REQUEST_ID_CHAT = 70001;
	private static final int REQUEST_ID_INFO = 70002;
	
	public static final String EXTRA_NAME_INCHANNEL = "pl.mg6...ListUsersActivity.inchannel";
	
	private ListView listView;
	private TextView emptyView;
	private ArrayAdapter<String> listAdapter;
	
	private boolean received;
	
	private String menuName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listusers_view);
		listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				parent.showContextMenuForChild(view);
			}
		});
		listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				String user = listAdapter.getItem(info.position);
				menuName = user;
				menu.setHeaderTitle(menuName);
				getMenuInflater().inflate(R.menu.chat_item, menu);
			}
		});
		emptyView = (TextView) findViewById(R.id.empty);
		listView.setEmptyView(emptyView);
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
		Intent intent = new Intent(this, ChatActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(ChatActivity.EXTRA_NAME_USERNAME, user);
		startActivityForResult(intent, REQUEST_ID_CHAT);
	}
	
	private void doInfo(String user) {
		Intent intent = new Intent(this, InformationsActivity.class);
		intent.putExtra(InformationsActivity.EXTRA_NAME_USERNAME, user);
		startActivityForResult(intent, REQUEST_ID_INFO);
	}
	
	@Override
	protected void onStartHandlingMessages(boolean firstTime) {
		super.onStartHandlingMessages(firstTime);
		if (firstTime) {
			String data = getIntent().getStringExtra(EXTRA_NAME_INCHANNEL);
			service.sendInput("inchannel " + data + "\n");
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case FreechessService.MSG_ID_INCHANNEL_NUMBER:
				onInchannelNumber((InchannelInfo) msg.obj);
				break;
		}
		return super.handleMessage(msg);
	}
	
	private void onInchannelNumber(InchannelInfo info) {
		if (!received && info.getChannelNumber().equals(getIntent().getStringExtra(EXTRA_NAME_INCHANNEL))) {
			for (int i = 0; i < info.getUsersCount(); i++) {
				listAdapter.add(info.getUser(i));
			}
			received = true;
		}
	}
}
