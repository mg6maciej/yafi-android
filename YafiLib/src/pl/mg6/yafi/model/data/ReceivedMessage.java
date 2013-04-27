package pl.mg6.yafi.model.data;

import java.util.regex.Matcher;

import pl.mg6.common.HtmlEntityEncoder;

public class ReceivedMessage {
	
	private int id;
	
	private String from;
	
	private String date;
	
	private String content;
	
	public int getId() {
		return id;
	}
	
	public void decrementId() {
		this.id--;
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getContent() {
		return content;
	}
	
	public static ReceivedMessage fromMatcher(Matcher m) {
		ReceivedMessage message = new ReceivedMessage();
		message.id = Integer.parseInt(m.group(1));
		message.from = m.group(2);
		message.date = m.group(3);
		message.content = HtmlEntityEncoder.decode(m.group(4));
		return message;
	}
}
