package pl.mg6.yafi.model.data;

import java.util.Date;

import pl.mg6.common.HtmlEntityEncoder;

public class Communication {
	
	public enum Type {
		
		PrivateTell, // .
		Say,
		PartnerTell, // ;
		ChannelTell, // ,
		Announcement,
		Shout, // !
		ShoutIt, // :
		ChessShout, // ^
		Kibitz, // *
		Whisper, // #
	}
	
	public static final String ID_ANNOUNCEMENT = "/announcement/";
	public static final String ID_SHOUT = "/shout/";
	public static final String ID_CHESS_SHOUT = "/chess-shout/";
	
	private Communication(String id, Type type, String name, String message) {
		this.id = id;
		this.type = type;
		this.name = name;
		this.message = HtmlEntityEncoder.decode(message);
		this.time = new Date();
	}

	private final String id;
	
	private final Type type;
	
	private final String name;
	
	private final String message;
	
	private final Date time;
	
	public String getId() {
		return id;
	}
	
	public Type getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Date getTime() {
		return time;
	}
	
	public boolean isPrivate() {
		return type == Type.PrivateTell || type == Type.Say || type == Type.PartnerTell;
	}
	
	public static Communication createPrivateTell(String name, String message) {
		Communication c = new Communication(name, Type.PrivateTell, name, message);
		return c;
	}
	
	public static Communication createSay(String name, String message) {
		Communication c = new Communication(name, Type.Say, name, message);
		return c;
	}
	
	public static Communication createPartnerTell(String name, String message) {
		Communication c = new Communication(name, Type.PartnerTell, name, message);
		return c;
	}
	
	public static Communication createChannelTell(String channel, String name, String message) {
		Communication c = new Communication(channel, Type.ChannelTell, name, message);
		return c;
	}
	
	public static Communication createAnnouncement(String name, String message) {
		Communication c = new Communication(ID_ANNOUNCEMENT, Type.Announcement, name, message);
		return c;
	}
	
	public static Communication createShout(String name, String message) {
		Communication c = new Communication(ID_SHOUT, Type.Shout, name, message);
		return c;
	}
	
	public static Communication createShoutIt(String name, String message) {
		Communication c = new Communication(ID_SHOUT, Type.ShoutIt, name, message);
		return c;
	}
	
	public static Communication createChessShout(String name, String message) {
		Communication c = new Communication(ID_CHESS_SHOUT, Type.ChessShout, name, message);
		return c;
	}
	
	public static Communication createKibitz(String gameId, String name, String message) {
		Communication c = new Communication(gameId, Type.Kibitz, name, message);
		return c;
	}
	
	public static Communication createWhisper(String gameId, String name, String message) {
		Communication c = new Communication(gameId, Type.Whisper, name, message);
		return c;
	}
	
	public static Communication create(String id, String name, String message) {
		Communication c = new Communication(id, null, name, message);
		return c;
	}
}
