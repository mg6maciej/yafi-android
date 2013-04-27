package pl.mg6.yafi.model.data;

import java.util.regex.Matcher;

public final class SeekInfo {
	
	private int id;
	private String name;
	private int titles;
	private int rating;
	private String type;
	private int time;
	private int increment;
	private boolean rated;
	private boolean manual;
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public int getTitles() {
		return titles;
	}
	
	public int getRating() {
		return rating;
	}

	public String getType() {
		return type;
	}

	public int getTime() {
		return time;
	}

	public int getIncrement() {
		return increment;
	}

	public boolean isRated() {
		return rated;
	}
	
	public boolean isManual() {
		return manual;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (getClass() != o.getClass()) {
			return false;
		}
		SeekInfo other = (SeekInfo) o;
		return id == other.id;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	public static SeekInfo fromMatch(Matcher m) {
		// <s> (\\d+) w=" + HANDLE + " ti=(\\w+) rt=(\\d+)([P E]) t=(\\d+) i=(\\d+) r=([ru]) tp=(\\S+) c=([?WB]) rr=(\\d+)-(\\d+) a=([ft]) f=([ft])
		String id = m.group(1);
		SeekInfo info = SeekInfo.withId(id);
		info.name = m.group(2).intern();
		info.titles = Integer.parseInt(m.group(3), 16);
		info.rating = Integer.parseInt(m.group(4));
		info.time = Integer.parseInt(m.group(6));
		info.increment = Integer.parseInt(m.group(7));
		info.rated = "r".equals(m.group(8));
		info.type = m.group(9);
		if ("lightning".equals(info.type) || "blitz".equals(info.type) || "standard".equals(info.type)) {
			info.type = "chess";
		}
		info.manual = "f".equals(m.group(13));
		return info;
	}
	
	public static SeekInfo withId(String strId) {
		int id = Integer.parseInt(strId);
		return SeekInfo.withId(id);
	}
	
	public static SeekInfo withId(int id) {
		SeekInfo info = new SeekInfo();
		info.id = id;
		return info;
	}
}
