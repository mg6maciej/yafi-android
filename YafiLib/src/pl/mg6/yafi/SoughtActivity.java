package pl.mg6.yafi;

import java.util.ArrayList;
import java.util.List;

import pl.mg6.common.Settings;
import pl.mg6.common.TimeUtils;
import pl.mg6.common.android.tracker.Tracking;
import pl.mg6.yafi.lib.R;
import pl.mg6.yafi.model.FreechessService;
import pl.mg6.yafi.model.FreechessUtils;
import pl.mg6.yafi.model.data.SeekInfo;
import pl.mg6.yafi.model.data.SeekInfoList;
import pl.mg6.yafi.model.data.seek.filters.AllSeekFilter;
import pl.mg6.yafi.model.data.seek.filters.ComputerSeekFilter;
import pl.mg6.yafi.model.data.seek.filters.GameTypeSeekFilter;
import pl.mg6.yafi.model.data.seek.filters.GuestSeekFilter;
import pl.mg6.yafi.model.data.seek.filters.RatingSeekFilter;
import pl.mg6.yafi.model.data.seek.filters.RegisteredUserSeekFilter;
import pl.mg6.yafi.model.data.seek.filters.SeekFilter;
import pl.mg6.yafi.model.data.seek.filters.TimeSeekFilter;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SoughtActivity extends BaseFreechessActivity {
	
	private static final int REQUEST_ID_MATCH = 60000;
	private static final int REQUEST_ID_CHAT = 60001;
	private static final int REQUEST_ID_INFO = 60002;
	
	private boolean recevingSeeks;
	
	private SoughtAdapter soughtAdapter;
	private GridView soughtList;
	private TextView emptyView;
	
	private Spinner gameTypeFilter;
	private String[] gameTypeFilterPatterns;
	
	private Spinner opponentFilter;
	private SeekFilter[] opponentFilterPatterns;
	
	private Spinner timeFilter;
	private SeekFilter[] timeFilterPatterns;
	
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
		
		gameTypeFilterPatterns = getResources().getStringArray(R.array.sought_game_types_pattern);
		gameTypeFilter = (Spinner) findViewById(R.id.sought_game_type);
		gameTypeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				soughtAdapter.setGameTypeFilter(new GameTypeSeekFilter(gameTypeFilterPatterns[position]));
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		gameTypeFilter.setSelection(Settings.getSoughtGameType(this));
		
		opponentFilterPatterns = new SeekFilter[] {
			new AllSeekFilter(),
			new RegisteredUserSeekFilter(),
			new GuestSeekFilter(),
			new ComputerSeekFilter(),
			new RatingSeekFilter(1, 1199),
			new RatingSeekFilter(1000, 1499),
			new RatingSeekFilter(1300, 1799),
			new RatingSeekFilter(1600, 2099),
			new RatingSeekFilter(1900, 9999),
		};
		opponentFilter = (Spinner) findViewById(R.id.sought_opponent);
		opponentFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				soughtAdapter.setOpponentFilter(opponentFilterPatterns[position]);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		opponentFilter.setSelection(Settings.getSoughtOpponent(this));
		
		timeFilterPatterns = new SeekFilter[] {
			new AllSeekFilter(),
			new TimeSeekFilter(3, 14),
			new TimeSeekFilter(3, 3),
			new TimeSeekFilter(5, 5),
			new TimeSeekFilter(10, 10),
			new TimeSeekFilter(15, 9999),
			new TimeSeekFilter(15, 15),
			new TimeSeekFilter(0, 2),
		};
		timeFilter = (Spinner) findViewById(R.id.sought_time);
		timeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				soughtAdapter.setTimeFilter(timeFilterPatterns[position]);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		timeFilter.setSelection(Settings.getSoughtTime(this));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		int gameType = gameTypeFilter.getSelectedItemPosition();
		int opponent = opponentFilter.getSelectedItemPosition();
		int time = timeFilter.getSelectedItemPosition();
		Settings.setSoughtData(this, gameType, opponent, time);
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
				SeekInfo seekInfo = soughtAdapter.getItem(info.position);
				if (seekInfo != null) {
					menuName = seekInfo.getName();
					menu.setHeaderTitle(menuName);
					getMenuInflater().inflate(R.menu.chat_item, menu);
				}
			}
		});
	}
	
	private void doPlay(SeekInfo seekInfo) {
		service.sendInput("play " + seekInfo.getId() + "\n");
		if (seekInfo.isManual()) {
			Toast.makeText(this, "Matching " + seekInfo.getName() + ".", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Playing " + seekInfo.getName() + ".", Toast.LENGTH_SHORT).show();
		}
		String type = seekInfo.getType();
		boolean rated = seekInfo.isRated();
		int time = seekInfo.getTime();
		int increment = seekInfo.getIncrement();
		String label = type + " " + (rated ? "r" : "u") + " " + time + " " + increment;
		int value = time + 2 * increment / 3;
		trackEvent(Tracking.CATEGORY_GET_GAME, Tracking.ACTION_SOUGHT, label, value);
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
			case FreechessService.MSG_ID_SEEK_NOT_AVAILABLE:
				Toast.makeText(this, "That seek is not available.", Toast.LENGTH_SHORT).show();
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
		
		private static final int VIEW_TYPE_SEEK = 0;
		private static final int VIEW_TYPE_EMPTY = 1;
		private static final int VIEW_TYPE_COUNT = 2;
		
		private LayoutInflater layoutInflater;
		
		private List<SeekInfo> allItems = new ArrayList<SeekInfo>();
		private List<ItemStrategy> items = new ArrayList<ItemStrategy>();
		
		private SeekFilter gameTypeFilter;
		private SeekFilter oppFilter;
		private SeekFilter etimeFilter;

		public SoughtAdapter(Context context) {
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}
		
		@Override
		public boolean isEnabled(int position) {
			return items.get(position).isEnabled();
		}
		
		@Override
		public int getItemViewType(int position) {
			return items.get(position).getItemViewType();
		}
		
		@Override
		public int getViewTypeCount() {
			return VIEW_TYPE_COUNT;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return items.get(position).getView(convertView, parent);
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public SeekInfo getItem(int position) {
			return items.get(position).getItem();
		}

		@Override
		public long getItemId(int position) {
			return items.get(position).getItemId();
		}
		
		private boolean isExpired(int position) {
			return items.get(position).isExpired();
		}
		
		private boolean matches(SeekInfo seek) {
			return (gameTypeFilter == null || gameTypeFilter.matches(seek))
					&& (oppFilter == null || oppFilter.matches(seek)
					&& (etimeFilter == null || etimeFilter.matches(seek)));
		}
		
		public void add(SeekInfo seek) {
			if (matches(seek)) {
				int index = -1;
				for (int i = 0; i < getCount(); i++) {
					if (isExpired(i)) {
						index = i;
						break;
					}
				}
				SeekItemStrategy sis = new SeekItemStrategy(layoutInflater, seek);
				if (index == -1) {
					items.add(sis);
				} else {
					items.set(index, sis);
				}
				notifyDataSetChanged();
			}
			allItems.add(seek);
		}
		
		public void addAll(SeekInfoList seeks) {
			int index;
			for (SeekInfo seek : seeks) {
				if (matches(seek)) {
					index = -1;
					for (int i = 0; i < getCount(); i++) {
						if (isExpired(i)) {
							index = i;
							break;
						}
					}
					SeekItemStrategy sis = new SeekItemStrategy(layoutInflater, seek);
					if (index == -1) {
						items.add(sis);
					} else {
						items.set(index, sis);
					}
				}
				allItems.add(seek);
			}
			notifyDataSetChanged();
		}
		
		public void removeAll(SeekInfoList seeks) {
			int index;
			boolean removed = false;
			for (SeekInfo seek : seeks) {
				index = -1;
				for (int i = 0; i < getCount(); i++) {
					if (seek.equals(getItem(i))) {
						index = i;
					}
				}
				if (index != -1) {
					items.set(index, new EmptyItemStrategy(layoutInflater));
					removed = true;
				}
				allItems.remove(seek);
			}
			if (removed) {
				boolean clear = true;
				for (ItemStrategy item : items) {
					if (item.isEnabled()) {
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
			allItems.clear();
		}
		
		public void setGameTypeFilter(SeekFilter filter) {
			gameTypeFilter = filter;
			refilter();
		}
		
		public void setOpponentFilter(SeekFilter filter) {
			oppFilter = filter;
			refilter();
		}
		
		public void setTimeFilter(SeekFilter filter) {
			etimeFilter = filter;
			refilter();
		}
		
		private void refilter() {
			List<SeekInfo> all = new ArrayList<SeekInfo>(allItems);
			removeAll();
			SeekInfoList list = new SeekInfoList();
			for (SeekInfo seekInfo : all) {
				list.add(seekInfo);
			}
			addAll(list);
		}
		
		private interface ItemStrategy {
			
			boolean isEnabled();
			
			int getItemViewType();
			
			View getView(View convertView, ViewGroup parent);
			
			SeekInfo getItem();
			
			long getItemId();
			
			boolean isExpired();
		}
		
		private static class SeekItemStrategy implements ItemStrategy {
			
			private LayoutInflater layoutInflater;
			
			private SeekInfo seekInfo;
			
			public SeekItemStrategy(LayoutInflater layoutInflater, SeekInfo seekInfo) {
				this.layoutInflater = layoutInflater;
				this.seekInfo = seekInfo;
			}

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public int getItemViewType() {
				return VIEW_TYPE_SEEK;
			}

			@Override
			public View getView(View convertView, ViewGroup parent) {
				ViewHolder holder;
				if (convertView == null) {
					holder = new ViewHolder();
					convertView = layoutInflater.inflate(R.layout.sought_item, parent, false);
					convertView.setTag(holder);
					holder.name = (TextView) convertView.findViewById(R.id.sought_item_name);
					holder.rating = (TextView) convertView.findViewById(R.id.sought_item_rating);
					holder.type = (TextView) convertView.findViewById(R.id.sought_item_type);
					holder.other = (TextView) convertView.findViewById(R.id.sought_item_other);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}
				holder.name.setText(seekInfo.getName());
				holder.rating.setVisibility(FreechessUtils.isGuest(seekInfo.getTitles()) ? View.GONE : View.VISIBLE);
				holder.rating.setText(seekInfo.getRating() == 0 ? "[unrated]" : "" + seekInfo.getRating());
				holder.type.setText(seekInfo.getType());
				holder.other.setText(seekInfo.getTime() + " " + seekInfo.getIncrement() + (seekInfo.isRated() ? " rated" : " unr") + (seekInfo.isManual() ? " m" : ""));
				return convertView;
			}

			@Override
			public SeekInfo getItem() {
				return seekInfo;
			}

			@Override
			public long getItemId() {
				return seekInfo.getId();
			}
			
			@Override
			public boolean isExpired() {
				return false;
			}
			
			private static class ViewHolder {
				
				public TextView name;
				public TextView rating;
				public TextView type;
				public TextView other;
			}
		}
		
		private static class EmptyItemStrategy implements ItemStrategy {
			
			private LayoutInflater layoutInflater;
			
			private Long expiryTime;
			
			public EmptyItemStrategy(LayoutInflater layoutInflater) {
				this.layoutInflater = layoutInflater;
				this.expiryTime = TimeUtils.getTimestamp() + 3000L;
			}

			@Override
			public boolean isEnabled() {
				return false;
			}

			@Override
			public int getItemViewType() {
				return VIEW_TYPE_EMPTY;
			}

			@Override
			public View getView(View convertView, ViewGroup parent) {
				if (convertView == null) {
					convertView = layoutInflater.inflate(R.layout.sought_item_empty, parent, false);
				}
				return convertView;
			}

			@Override
			public SeekInfo getItem() {
				return null;
			}

			@Override
			public long getItemId() {
				return 0;
			}
			
			@Override
			public boolean isExpired() {
				return expiryTime < TimeUtils.getTimestamp();
			}
		}
	}
}
