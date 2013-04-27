package pl.mg6.yafi.model.data;

import java.util.ArrayList;
import java.util.List;

public class InchannelInfo {
	
	private String channelNumber;
	
	private String channelName;
	
	private List<String> users;
	
	public InchannelInfo(String channelNumber, String channelName) {
		this.channelNumber = channelNumber;
		this.channelName = channelName;
		this.users = new ArrayList<String>();
	}
	
	public void add(String user) {
		users.add(user);
	}
	
	public String getChannelNumber() {
		return channelNumber;
	}
	
	public String getChannelName() {
		return channelName;
	}
	
	public int getUsersCount() {
		return users.size();
	}
	
	public String getUser(int index) {
		return users.get(index);
	}
}
