package pl.mg6.yafi;

import java.util.ArrayList;
import java.util.List;

import pl.mg6.common.android.tracker.Tracking;
import pl.mg6.yafi.model.FreechessService;
import pl.mg6.yafi.model.FreechessUtils;
import pl.mg6.yafi.model.data.SeekInfo;
import pl.mg6.yafi.model.data.SeekInfoList;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class SoughtActivity extends BaseFreechessActivity {
	
	private static final int REQUEST_ID_MATCH = 60000;
	private static final int REQUEST_ID_CHAT = 60001;
	private static final int REQUEST_ID_INFO = 60002;
	
	private boolean recevingSeeks;
	
	private SoughtAdapter soughtAdapter;
	private GridView soughtList;
	private TextView emptyView;
	
	private String menuName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sought_view);
		soughtAdapter = new SoughtAdapter(this);
		setupSoughtList();
		emptyView = (TextView) findViewById(R.id.sought_empty_view);
		emptyView.setText(R.string.getting_sought_items);
		soughtList.setEmptyView(emptyView);
	}
	
	private void setupSoughtList() {
		soughtList = (GridView) findViewById(R.id.sought_list);
		soughtList.setAdapter(soughtAdapter);
		soughtList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SeekInfo seekInfo = soughtAdapter.getItem(position);
				if (seekInfo != null) {
					doPlay(seekInfo);
				}
			}
		});
		soughtList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				SeekInfo seek = soughtAdapter.getItem(info.position);
				if (seek != null) {
					menuName = seek.getName();
					menu.setHeaderTitle(menuName);
					getMenuInflater().inflate(R.menu.chat_item, menu);
				}
			}
		});
	}
	
	private void doPlay(SeekInfo seekInfo) {
		service.sendInput("play " + seekInfo.getId() + "\n");
		String type = seekInfo.getType();
		boolean rated = seekInfo.isRated();
		int time = seekInfo.getTime();
		int increment = seekInfo.getIncrement();
		String label = type + " " + (rated ? "r" : "u") + " " + time + " " + increment;
		int value = time + 2 * increment / 3;
		trackEvent(Tracking.CATEGORY_GETGAME, Tracking.ACTION_SOUGHT, label, value);
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
		service.sendInput("iset seekinfo 1\n");
		emptyView.setText(R.string.getting_sought_items);
	}
	
	@Override
	protected void onStopHandlingMessages() {
		super.onStopHandlingMessages();
		service.sendInput("iset seekinfo 0\n");
		recevingSeeks = false;
		soughtAdapter.removeAll();
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case FreechessService.MSG_ID_SEEKINFO_SET:
				onSeekInfoSet((SeekInfoList) msg.obj);
				return true;
			case FreechessService.MSG_ID_SEEKINFO_SET_ERROR:
				onSeekInfoSetError();
				return true;
			case FreechessService.MSG_ID_RECEIVED_SEEK:
				onReceivedSeek((SeekInfo) msg.obj);
				return true;
			case FreechessService.MSG_ID_REMOVED_SEEKS:
				onRemovedSeeks((SeekInfoList) msg.obj);
				return true;
		}
		return super.handleMessage(msg);
	}
	
	private void onSeekInfoSet(SeekInfoList seeks) {
		recevingSeeks = true;
		soughtAdapter.addAll(seeks);
		emptyView.setText(R.string.no_sought_items);
	}
	
	private void onSeekInfoSetError() {
		emptyView.setText(R.string.you_are_playing_or_examining);
	}
	
	private void onReceivedSeek(SeekInfo seek) {
		if (recevingSeeks) {
			soughtAdapter.add(seek);
		} else {
			Log.i("tag", "onReceivedSeek when not recevingSeeks");
		}
	}
	
	private void onRemovedSeeks(SeekInfoList seeks) {
		if (recevingSeeks) {
			soughtAdapter.removeAll(seeks);
		} else {
			Log.i("tag", "onRemovedSeeks when not recevingSeeks");
		}
	}
	
	private static class SoughtAdapter extends BaseAdapter {
		
		private static final int VIEW_TYPE_EMPTY = 0;
		private static final int VIEW_TYPE_SEEK = 1;
		private static final int VIEW_TYPE_COUNT = 2;
		
		private LayoutInflater layoutInflater;
		
		private List<SeekInfo> items = new ArrayList<SeekInfo>();

		public SoughtAdapter(Context context) {
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}
		
		@Override
		public boolean isEnabled(int position) {
			return (getItem(position) != null);
		}
		
		@Override
		public int getItemViewType(int position) {
			return (getItem(position) != null) ? VIEW_TYPE_SEEK : VIEW_TYPE_EMPTY;
		}
		
		@Override
		public int getViewTypeCount() {
			return VIEW_TYPE_COUNT;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SeekInfo seekInfo = getItem(position);
			if (seekInfo == null) {
				if (convertView == null) {
					convertView = layoutInflater.inflate(R.layout.sought_item_empty, parent, false);
				}
			} else {
				if (convertView == null) {
					convertView = layoutInflater.inflate(R.layout.sought_item, parent, false);
				}
				updateView(convertView, seekInfo);
			}
			return convertView;
		}
		
		private void updateView(View view, SeekInfo item) {
			TextView name = (TextView) view.findViewById(R.id.sought_item_name);
			TextView rating = (TextView) view.findViewById(R.id.sought_item_rating);
			TextView type = (TextView) view.findViewById(R.id.sought_item_type);
			TextView other = (TextView) view.findViewById(R.id.sought_item_other);
//			if ("atomic".equals(item.getType())) {
//				view.setBackgroundResource(R.drawable.type_atomic);
//			} else {
//				view.setBackgroundDrawable(null);
//			}
			name.setText(item.getName());
			rating.setVisibility(FreechessUtils.isGuest(item.getTitles()) ? View.GONE : View.VISIBLE);
			rating.setText(item.getRating() == 0 ? "[unrated]" : "" + item.getRating());
			type.setText(item.getType());
			other.setText(item.getTime() + " " + item.getIncrement() + (item.isRated() ? " rated" : " unr") + (item.isManual() ? " m" : ""));
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public SeekInfo getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			SeekInfo item = getItem(position);
			if (item != null) {
				return item.getId();
			} else {
				return 0;
			}
		}
		
		public void add(SeekInfo seek) {
			int index = items.indexOf(null);
			if (index == -1) {
				items.add(seek);
			} else {
				items.set(index, seek);
			}
			notifyDataSetChanged();
		}
		
		public void addAll(SeekInfoList seeks) {
			int index;
			for (SeekInfo seek : seeks) {
				index = items.indexOf(null);
				if (index == -1) {
					items.add(seek);
				} else {
					items.set(index, seek);
				}
			}
			notifyDataSetChanged();
		}
		
		public void removeAll(SeekInfoList seeks) {
			int index;
			boolean removed = false;
			for (SeekInfo seek : seeks) {
				index = items.indexOf(seek);
				if (index != -1) {
					items.set(index, null);
					removed = true;
				}
			}
			if (removed) {
				boolean clear = true;
				for (SeekInfo seek : items) {
					if (seek != null) {
						clear = false;
						break;
					}
				}
				if (clear) {
					items.clear();
				}
				notifyDataSetChanged();
			}
		}
		
		private void removeAll() {
			if (!items.isEmpty()) {
				items.clear();
				notifyDataSetChanged();
			}
		}
	}
}
