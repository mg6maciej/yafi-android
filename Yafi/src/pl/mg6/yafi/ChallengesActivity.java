package pl.mg6.yafi;

import pl.mg6.common.android.tracker.Tracking;
import pl.mg6.yafi.model.FreechessService;
import pl.mg6.yafi.model.data.ChallengePendingOffer;
import pl.mg6.yafi.model.data.PendingInfo;
import pl.mg6.yafi.model.data.PendingOffer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ChallengesActivity extends BaseFreechessActivity {
	
	private static final int REQUEST_ID_MATCH = 100000;
	private static final int REQUEST_ID_CHAT = 100001;
	private static final int REQUEST_ID_INFO = 100002;
	
	//private static final String TAG = ChallengesActivity.class.getSimpleName();
	
	private ChallengesAdapter adapterSent;
	private ListView listSent;
	private TextView sentEmptyView;
	
	private ChallengesAdapter adapterReceived;
	private ListView listReceived;
	private TextView recevicedEmptyView;
	
	private String menuName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.challenges_view);
		
		AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				parent.showContextMenuForChild(view);
			}
		};
		
		adapterSent = new ChallengesAdapter(this, R.layout.challenge_sent_item);
		listSent = (ListView) findViewById(R.id.challenges_list_sent);
		listSent.setAdapter(adapterSent);
		listSent.setOnItemClickListener(listener);
		listSent.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				ChallengePendingOffer cpo = adapterSent.getItem(info.position);
				menuName = cpo.getName();
				menu.setHeaderTitle(menuName);
				getMenuInflater().inflate(R.menu.chat_item, menu);
			}
		});
		sentEmptyView = (TextView) findViewById(R.id.challenges_list_sent_empty);
		listSent.setEmptyView(sentEmptyView);
		
		adapterReceived = new ChallengesAdapter(this, R.layout.challenge_received_item);
		listReceived = (ListView) findViewById(R.id.challenges_list_received);
		listReceived.setAdapter(adapterReceived);
		listReceived.setOnItemClickListener(listener);
		listReceived.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				ChallengePendingOffer cpo = adapterReceived.getItem(info.position);
				menuName = cpo.getName();
				menu.setHeaderTitle(menuName);
				getMenuInflater().inflate(R.menu.chat_item, menu);
			}
		});
		recevicedEmptyView = (TextView) findViewById(R.id.challenges_list_received_empty);
		listReceived.setEmptyView(recevicedEmptyView);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.mi_match:
				doMatch(menuName);
				return true;
			case R.id.mi_chat:
				doChat(menuName);
				return true;
			case R.id.mi_info:
				doInfo(menuName);
				return true;
		}
		return super.onContextItemSelected(item);
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
	protected void onStartHandlingMessages() {
		super.onStartHandlingMessages();
		service.sendInput("pending\n");
	}
	
	public void onRefreshClick(View view) {
		service.sendInput("pending\n");
		adapterSent.clear();
		adapterReceived.clear();
		sentEmptyView.setText(R.string.challenges_getting);
		recevicedEmptyView.setText(R.string.challenges_getting);
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case FreechessService.MSG_ID_PENDING_INFO:
				onPendingInfo((PendingInfo) msg.obj);
				return true;
			case FreechessService.MSG_ID_REMOVE_MATCH_TO:
				onRemoveMatchTo((String) msg.obj);
				return true;
			case FreechessService.MSG_ID_REMOVE_MATCH_FROM:
				onRemoveMatchFrom((String) msg.obj);
				return true;
		}
		return super.handleMessage(msg);
	}
	
	private void onPendingInfo(PendingInfo info) {
		adapterSent.setNotifyOnChange(false);
		adapterSent.clear();
		for (int i = 0; i < info.getSentOfferCount(); i++) {
			PendingOffer po = info.getSentOffer(i);
			if (po instanceof ChallengePendingOffer) {
				adapterSent.add((ChallengePendingOffer) po);
			}
		}
		adapterSent.notifyDataSetChanged();
		sentEmptyView.setText(R.string.challenges_sent_empty);
		
		adapterReceived.setNotifyOnChange(false);
		adapterReceived.clear();
		for (int i = 0; i < info.getReceivedOffersCount(); i++) {
			PendingOffer po = info.getReceivedOffer(i);
			if (po instanceof ChallengePendingOffer) {
				adapterReceived.add((ChallengePendingOffer) po);
			}
		}
		adapterReceived.notifyDataSetChanged();
		recevicedEmptyView.setText(R.string.challenges_received_empty);
	}
	
	private void onRemoveMatchTo(String user) {
		adapterSent.removeWithName(user);
	}
	
	private void onRemoveMatchFrom(String user) {
		adapterReceived.removeWithName(user);
	}
	
	public void onCommandClick(View view) {
		String cmd = (String) view.getTag();
		ChallengePendingOffer cpo = (ChallengePendingOffer) ((View) view.getParent()).getTag();
		service.sendInput(cmd + " " + cpo.getId() + "\n");
		if ("accept".equals(cmd)) {
			String type = cpo.getType();
			boolean rated = cpo.isRated();
			int time = cpo.getTime();
			int increment = cpo.getIncrement();
			String label = type + " " + (rated ? "r" : "u") + " " + time + " " + increment;
			int value = time + 2 * increment / 3;
			trackEvent(Tracking.CATEGORY_GET_GAME, Tracking.ACTION_CHALLENGE, label, value);
		}
	}
	
	private static class ChallengesAdapter extends ArrayAdapter<ChallengePendingOffer> {
		
		private LayoutInflater layoutInflater;
		
		private int layoutId;
		
		public ChallengesAdapter(Context context, int layoutId) {
			super(context, 0);
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.layoutId = layoutId;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = layoutInflater.inflate(layoutId, parent, false);
			}
			TextView who = (TextView) convertView.findViewById(R.id.challenge_item_who);
			TextView time = (TextView) convertView.findViewById(R.id.challenge_item_time);
			TextView increment = (TextView) convertView.findViewById(R.id.challenge_item_increment);
			TextView type = (TextView) convertView.findViewById(R.id.challenge_item_type);
			TextView rated = (TextView) convertView.findViewById(R.id.challenge_item_rated);
			
			ChallengePendingOffer cpo = getItem(position);
			convertView.setTag(cpo);

			who.setText(cpo.getName());
			time.setText("" + cpo.getTime());
			increment.setText("" + cpo.getIncrement());
			type.setText(cpo.getType());
			rated.setText(cpo.isRated() ? "rated" : "unrated");
			
			return convertView;
		}
		
		public void removeWithName(String user) {
			for (int i = 0; i < getCount(); i++) {
				ChallengePendingOffer cpo = getItem(i);
				if (user.equals(cpo.getName())) {
					remove(cpo);
					break;
				}
			}
		}
	}
}
