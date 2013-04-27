package pl.mg6.yafi.model.data;

import java.util.regex.Matcher;

public class VariablesInfo {
	
	private String user;
	
	private String clientName;
	
	public String getUser() {
		return user;
	}
	
	public String getClientName() {
		return clientName;
	}
	
	public static VariablesInfo fromMatch(Matcher m) {
		VariablesInfo info = new VariablesInfo();
		info.user = m.group(1);
		info.clientName = m.group(2);
		return info;
	}
}
