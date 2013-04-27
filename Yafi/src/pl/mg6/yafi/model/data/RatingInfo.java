package pl.mg6.yafi.model.data;

import java.util.regex.Matcher;

public class RatingInfo {
	
	private String rating;
	
	private String rd;
	
	private int wins;
	
	private int losses;
	
	private int draws;
	
	private int total;
	
	private String best;
	
	public String getRating() {
		return rating;
	}
	
	public String getRd() {
		return rd;
	}
	
	public int getWins() {
		return wins;
	}
	
	public int getLosses() {
		return losses;
	}
	
	public int getDraws() {
		return draws;
	}
	
	public int getTotal() {
		return total;
	}
	
	public String getBest() {
		return best;
	}
	
	public static RatingInfo fromFingerMatch(Matcher m, int index) {
		if (m.group(index) == null) {
			return null;
		}
		RatingInfo info = new RatingInfo();
		info.rating = m.group(index + 0);
		info.rd = m.group(index + 1);
		info.wins = Integer.parseInt(m.group(index + 2));
		info.losses = Integer.parseInt(m.group(index + 3));
		info.draws = Integer.parseInt(m.group(index + 4));
		info.total = Integer.parseInt(m.group(index + 5));
		info.best = m.group(index + 6);
		return info;
	}
}
