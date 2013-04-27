package pl.mg6.yafi.model.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import android.util.Log;

import pl.mg6.common.Settings;
import pl.mg6.yafi.model.FreechessUtils;

public class HistoryInfo implements Iterable<HistoryInfo.Entry> {
	
	private static final String TAG = HistoryInfo.class.getSimpleName();
	
	public static class Entry extends HistoricalGameEntry {
		
		private int result;
		private int rating;
		private Color color;
		private int opponentRating;
		private String opponentName;
		private char type;
		private boolean rated;
		private int time;
		private int increment;
		private String eco;
		private String resultDescription;
		private String date;
		
		public int getResult() {
			return result;
		}
		
		public int getRating() {
			return rating;
		}
		
		public Color getColor() {
			return color;
		}
		
		public int getOpponentRating() {
			return opponentRating;
		}
		
		public String getOpponentName() {
			return opponentName;
		}
		
		public char getType() {
			return type;
		}
		
		public boolean isRated() {
			return rated;
		}
		
		public int getTime() {
			return time;
		}
		
		public int getIncrement() {
			return increment;
		}
		
		public String getEco() {
			return eco;
		}
		
		public String getResultDescription() {
			return resultDescription;
		}
		
		public String getDate() {
			return date;
		}
		
		private static Entry fromMatch(Matcher m) {
			Entry entry = new Entry();
			entry.id = m.group(1);
			entry.result = "+".equals(m.group(2)) ? 1 : "-".equals(m.group(2)) ? -1 : 0;
			entry.rating = Integer.parseInt(m.group(3));
			entry.color = "W".equals(m.group(4)) ? Color.WHITE : Color.BLACK;
			entry.opponentRating = Integer.parseInt(m.group(5));
			entry.opponentName = m.group(6);
			entry.type = m.group(7).charAt(0);
			entry.rated = "r".equals(m.group(8));
			entry.time = Integer.parseInt(m.group(9));
			entry.increment = Integer.parseInt(m.group(10));
			entry.eco = m.group(11);
			entry.resultDescription = m.group(12);
			entry.date = m.group(13);
			return entry;
		}
	}
	
	private String user;
	
	private List<Entry> entries;
	
	public HistoryInfo() {
		entries = new ArrayList<Entry>();
	}
	
	public String getUser() {
		return user;
	}
	
	public int getEntryCount() {
		return entries.size();
	}
	
	public Entry getEntry(int index) {
		return entries.get(index);
	}
	
	@Override
	public Iterator<Entry> iterator() {
		return entries.iterator();
	}
	
	public static HistoryInfo fromMatch(Matcher m) {
		HistoryInfo info = new HistoryInfo();
		info.user = m.group(1);
		m = FreechessUtils.HISTORY_ENTRY.matcher(m.group(2));
		int lastMatchEnd = 0;
		while (m.find()) {
			if (Settings.LOG_SERVER_COMMUNICATION) {
				if (m.start() != lastMatchEnd) {
					Log.e(TAG, info.user + "\n" + m.group());
				}
				lastMatchEnd = m.end();
			}
			Entry e = Entry.fromMatch(m);
			info.entries.add(e);
		}
		return info;
	}
}
