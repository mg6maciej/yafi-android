package pl.mg6.yafi.model.data;

import java.util.List;

public class WelcomeData {

	private List<NewsItem> newsItems;
	
	private int unreadMessages;
	
	private int allMessages;
	
	private String[] friends;
	
	public WelcomeData(List<NewsItem> newsItems, int unreadMessages, int allMessages, String[] friends) {
		this.newsItems = newsItems;
		this.unreadMessages = unreadMessages;
		this.allMessages = allMessages;
		this.friends = friends;
	}
	
	public List<NewsItem> getNewsItems() {
		return newsItems;
	}
	
	public int getUnreadMessages() {
		return unreadMessages;
	}
	
	public void resetUnreadMessages() {
		unreadMessages = 0;
	}
	
	public int getAllMessages() {
		return allMessages;
	}
	
	public void decrementAllMessages() {
		allMessages--;
	}
	
	public String[] getFriends() {
		return friends;
	}
}
