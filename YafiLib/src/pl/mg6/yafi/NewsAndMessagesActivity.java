package pl.mg6.yafi;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import pl.mg6.common.android.AndroidUtils;
import pl.mg6.yafi.lib.R;
import pl.mg6.yafi.model.FreechessService;
import pl.mg6.yafi.model.data.NewsItem;
import pl.mg6.yafi.model.data.ReceivedMessage;
import pl.mg6.yafi.model.data.WelcomeData;
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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class NewsAndMessagesActivity extends BaseFreechessActivity {
	
	private static final int REQUEST_ID_SEND_MESSAGE = 100;
	private static final int REQUEST_ID_INFO = 101;
	
	private ListView content;
	private NewsMessagesAdapter adapter;
	private int smallestNews;
	
	private String menuName;
	private int itemId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_messages_view);
		adapter = new NewsMessagesAdapter(this);
		content = (ListView) findViewById(R.id.news_messages_list);
		content.setAdapter(adapter);
//		content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				onContentItemClick(view, position);
//			}
//		});
		content.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				Object item = info.targetView.getTag();
				if (item instanceof ReceivedMessage) {
					ReceivedMessage message = (ReceivedMessage) item;
					menuName = message.getFrom();
					itemId = message.getId();
					menu.setHeaderTitle(menuName);
					getMenuInflater().inflate(R.menu.message_item, menu);
				}
			}
		});
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.mi_reply) {
			doReply(menuName);
		} else if (id == R.id.mi_delete) {
			doDelete(itemId);
		} else if (id == R.id.mi_info) {
			doInfo(menuName);
		} else {
			return super.onContextItemSelected(item);
		}
		return true;
	}
	
	private void doReply(String user) {
		Intent intent = new Intent(this, SendMessageActivity.class);
		intent.putExtra(SendMessageActivity.EXTRA_NAME_USERNAME, user);
		startActivityForResult(intent, REQUEST_ID_SEND_MESSAGE);
	}
	
	private void doDelete(int id) {
		service.sendInput("clearmessages " + id + "\n");
		adapter.removeMessage(id);
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
			WelcomeData data = service.getWelcomData();
			if (data != null && data.getUnreadMessages() > 0) {
				service.sendInput("messages u\n");
			}
			if (!service.isRegistered()) {
				service.sendInput("news\n");
			}
			if (data != null) {
				adapter.setData(data);
			}
			adapter.setCanHaveMessages(service.isRegistered());
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case FreechessService.MSG_ID_WELCOME_DATA:
				WelcomeData data = (WelcomeData) msg.obj;
				if (data.getUnreadMessages() > 0) {
					service.sendInput("messages u\n");
				}
				adapter.setData(data);
				return true;
			case FreechessService.MSG_ID_MESSAGES:
				List<ReceivedMessage> messages = (List<ReceivedMessage>) msg.obj;
				onMessages(messages);
				return true;
			case FreechessService.MSG_ID_NEWS:
				List<NewsItem> news = (List<NewsItem>) msg.obj;
				onNews(news);
				return true;
		}
		return super.handleMessage(msg);
	}
	
	private void onMessages(List<ReceivedMessage> messages) {
		adapter.setMessages(messages);
	}
	
	private void onNews(List<NewsItem> news) {
		adapter.addNews(news);
	}
	
	public void onShowAllMessagesClick(View view) {
		service.sendInput("messages\n");
		view.setEnabled(false);
	}
	
	public void onShowOlderNewsClick(View view) {
		if (adapter.news != null) {
			if (smallestNews == 0) {
				smallestNews = adapter.news.last().getId();
			}
			int last = smallestNews - 1;
			int first = last - 99;
			if (first < 1) {
				first = 1;
			}
			smallestNews = first;
			service.sendInput(String.format("news %d-%d\n", first, last));
		} else {
			service.sendInput("news\n");
		}
		view.setEnabled(false);
		View showAll = ((View) view.getParent()).findViewById(R.id.messages_show_all);
		if (showAll != null) {
			showAll.setEnabled(false);
		}
	}
	
	public void onShowAllNewsClick(View view) {
		int last = adapter.news.last().getId() - 1;
		service.sendInput(String.format("news 1-%d\n", last));
		view.setEnabled(false);
		((View) view.getParent()).findViewById(R.id.messages_show_older).setEnabled(false);
	}
	
	public void onMessageClick(View view) {
		content.showContextMenuForChild(view);
	}
	
	public void onNewsClick(View view) {
		NewsItem item = (NewsItem) view.getTag();
		Intent intent = new Intent(this, NewsDetailsActivity.class);
		intent.putExtra(NewsDetailsActivity.EXTRA_NAME_NEWS, item);
		startActivityForResult(intent, 0);
	}
	
	private static class NewsMessagesAdapter extends BaseAdapter {
		
		private LayoutInflater layoutInflater;
		private boolean canHaveMessages = true;
		private WelcomeData data;
		private List<ReceivedMessage> messages;
		private SortedSet<NewsItem> news;
		
		public NewsMessagesAdapter(Context context) {
			layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			int count = 2;
			if (messages != null) {
				count += messages.size();
				if (messages.get(messages.size() - 1).getId() > 1) {
					count += 1;
				}
			} else {
				count += 1;
			}
			if (news != null) {
				count += news.size();
				if (news.last().getId() > 1) {
					count += 1;
				}
			} else {
				count += 1;
			}
			return count;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		public void setCanHaveMessages(boolean canHaveMessages) {
			this.canHaveMessages = canHaveMessages;
			notifyDataSetChanged();
		}
		
		public void setData(WelcomeData data) {
			this.data = data;
			if (data.getNewsItems() != null) {
				this.news = new TreeSet<NewsItem>();
				this.news.addAll(data.getNewsItems());
			}
			notifyDataSetChanged();
		}
		
		public void setMessages(List<ReceivedMessage> messages) {
			this.messages = messages;
			this.data.resetUnreadMessages();
			notifyDataSetChanged();
		}
		
		public void removeMessage(int id) {
			for (ReceivedMessage message : messages) {
				if (id == message.getId()) {
					messages.remove(message);
					break;
				} else {
					message.decrementId();
				}
			}
			if (messages.size() == 0) {
				messages = null;
			}
			data.decrementAllMessages();
			notifyDataSetChanged();
		}
		
		public void addNews(List<NewsItem> news) {
			if (this.news == null) {
				this.news = new TreeSet<NewsItem>();
			}
			this.news.addAll(news);
			notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (position == 0) {
				return layoutInflater.inflate(R.layout.messages_title_item, parent, false);
			}
			position--;
			if (messages != null) {
				if (position < messages.size()) {
					ReceivedMessage message = messages.get(position);
					View messageView = layoutInflater.inflate(R.layout.message_item, parent, false);
					TextView from = (TextView) messageView.findViewById(R.id.message_item_from);
					TextView date = (TextView) messageView.findViewById(R.id.message_item_date);
					TextView content = (TextView) messageView.findViewById(R.id.message_item_content);
					messageView.setTag(message);
					from.setText(message.getFrom());
					date.setText(message.getDate());
					content.setText(message.getContent());
					AndroidUtils.linkify(content);
					return messageView;
				}
				position -= messages.size();
				if (messages.get(messages.size() - 1).getId() > 1) {
					if (position == 0) {
						return layoutInflater.inflate(R.layout.messages_show_all_item, parent, false);
					}
					position--;
				}
			} else {
				if (position == 0) {
					if (!canHaveMessages) {
						return layoutInflater.inflate(R.layout.messages_only_registered_can_have_messages_item, parent, false);
					} else if (data == null || data.getUnreadMessages() > 0) {
						return layoutInflater.inflate(R.layout.common_item_loading, parent, false);
					} else if (data.getAllMessages() > 0) {
						return layoutInflater.inflate(R.layout.messages_no_new_item, parent, false);
					} else {
						return layoutInflater.inflate(R.layout.messages_no_item, parent, false);
					}
				}
				position--;
			}
			if (position == 0) {
				return layoutInflater.inflate(R.layout.news_title_item, parent, false);
			}
			position--;
			if (news != null) {
				if (position < news.size()) {
					Iterator<NewsItem> iterator = news.iterator();
					while (position > 0) {
						iterator.next();
						position--;
					}
					NewsItem item = iterator.next();
					View newsView = layoutInflater.inflate(R.layout.news_item, parent, false);
					TextView date = (TextView) newsView.findViewById(R.id.news_item_date);
					TextView title = (TextView) newsView.findViewById(R.id.news_item_title);
					newsView.setTag(item);
					date.setText(item.getDate());
					title.setText(item.getTitle());
					AndroidUtils.linkify(title);
					return newsView;
				}
				position -= news.size();
				if (position == 0) {
					return layoutInflater.inflate(R.layout.news_show_all_item, parent, false);
				}
			} else {
				if (position == 0) {
					if (data == null) {
						return layoutInflater.inflate(R.layout.common_item_loading, parent, false);
					} else {
						return layoutInflater.inflate(R.layout.news_no_new_item, parent, false);
					}
				}
			}
			throw new IllegalStateException();
		}
		
		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}
		
		@Override
		public boolean isEnabled(int position) {
			if (position == 0) {
				return false;
			}
			position--;
			if (messages != null) {
				if (position < messages.size()) {
					return true;
				}
				position -= messages.size();
				if (messages.get(messages.size() - 1).getId() > 1) {
					if (position == 0) {
						return false;
					}
					position--;
				}
			} else {
				if (position == 0) {
					return false;
				}
				position--;
			}
			if (position == 0) {
				return false;
			}
			position--;
			if (news != null) {
				if (position < news.size()) {
					return true;
				}
				position -= news.size();
				if (position == 0) {
					return false;
				}
			} else {
				if (position == 0) {
					return false;
				}
			}
			throw new IllegalStateException();
		}
	}
}
