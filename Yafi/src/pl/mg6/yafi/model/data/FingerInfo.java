package pl.mg6.yafi.model.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class FingerInfo {
	
	private String user;
	
	private String[] titles;
	private static final String[] NO_TITLES = new String[0];
	
	private RatingInfo blitz;
	private RatingInfo standard;
	private RatingInfo lightning;
	private RatingInfo wild;
	private RatingInfo buhouse;
	private RatingInfo crazyhouse;
	private RatingInfo suicide;
	private RatingInfo losers;
	private RatingInfo atomic;
	
	private List<String> lines;
	
	public FingerInfo() {
		lines = new ArrayList<String>();
	}
	
	public String getUser() {
		return user;
	}
	
	public String[] getTitles() {
		return titles;
	}
	
	public RatingInfo getBlitz() {
		return blitz;
	}
	
	public RatingInfo getStandard() {
		return standard;
	}
	
	public RatingInfo getLightning() {
		return lightning;
	}
	
	public RatingInfo getWild() {
		return wild;
	}
	
	public RatingInfo getBuhouse() {
		return buhouse;
	}
	
	public RatingInfo getCrazyhouse() {
		return crazyhouse;
	}
	
	public RatingInfo getSuicide() {
		return suicide;
	}
	
	public RatingInfo getLosers() {
		return losers;
	}
	
	public RatingInfo getAtomic() {
		return atomic;
	}
	
	public int getLineCount() {
		return lines.size();
	}
	
	public String getLine(int index) {
		return lines.get(index);
	}
	
	public static FingerInfo fromMatch(Matcher m) {
		FingerInfo info = new FingerInfo();
		info.user = m.group(1);
		String titles = m.group(2);
		info.titles = titles != null ? titles.split("\\)\\(") : NO_TITLES;
		info.blitz = RatingInfo.fromFingerMatch(m, 19);
		info.standard = RatingInfo.fromFingerMatch(m, 26);
		info.lightning = RatingInfo.fromFingerMatch(m, 33);
		info.wild = RatingInfo.fromFingerMatch(m, 40);
		info.buhouse = RatingInfo.fromFingerMatch(m, 47);
		info.crazyhouse = RatingInfo.fromFingerMatch(m, 54);
		info.suicide = RatingInfo.fromFingerMatch(m, 61);
		info.losers = RatingInfo.fromFingerMatch(m, 68);
		info.atomic = RatingInfo.fromFingerMatch(m, 75);
		for (int i = 0; i < 10; i++) {
			String line = m.group(82 + i);
			if (line == null) {
				break;
			}
			info.lines.add(line);
		}
		return info;
	}
}
