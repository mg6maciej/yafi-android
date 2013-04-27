package pl.mg6.yafi.model.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import pl.mg6.common.Settings;
import pl.mg6.yafi.model.FreechessUtils;
import android.util.Log;

public class AdjournedInfo implements Iterable<AdjournedInfo.Entry> {
	
	private static final String TAG = AdjournedInfo.class.getSimpleName();
	
	public static class Entry extends HistoricalGameEntry {
		
		private Color color;
		
		private String opponentName;
		
		private boolean opponentOnline;
		
		private char type;
		
		private boolean rated;
		
		private int time;
		
		private int increment;
		
		private int whiteStrength;
		
		private int blackStrength;
		
		public Color getColor() {
			return color;
		}
		
		public String getOpponentName() {
			return opponentName;
		}
		
		public boolean isOpponentOnline() {
			return opponentOnline;
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
		
		public int getWhiteStrength() {
			return whiteStrength;
		}
		
		public int getBlackStrength() {
			return blackStrength;
		}
		
		private static Entry fromMatch(Matcher m) {
			// \\d+: ([WB]) " + HANDLE + " +([NY]) \\[([ p])([bslwBzSLxun])([ru]) *(\\d+) +(\\d+)\\] +(\\d+)-(\\d+) +(\\S+) +(\\S+) +(.*)
			Entry entry = new Entry();
			entry.color = "W".equals(m.group(1)) ? Color.WHITE : Color.BLACK;
			entry.opponentName = m.group(2);
			entry.id = entry.opponentName;
			entry.opponentOnline = "Y".equals(m.group(3));
			entry.type = m.group(5).charAt(0);
			entry.rated = "r".equals(m.group(6));
			entry.time = Integer.parseInt(m.group(7));
			entry.increment = Integer.parseInt(m.group(8));
			entry.whiteStrength = Integer.parseInt(m.group(9));
			entry.blackStrength = Integer.parseInt(m.group(10));
			return entry;
		}
	}
	
	private String user;
	
	private List<Entry> entries;
	
	public AdjournedInfo() {
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
	
	public static AdjournedInfo fromMatch(Matcher m) {
		AdjournedInfo info = new AdjournedInfo();
		info.user = m.group(1);
		m = FreechessUtils.ADJOURNED_ENTRY.matcher(m.group(2));
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
