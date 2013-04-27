package pl.mg6.yafi.model.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import pl.mg6.common.Settings;
import pl.mg6.yafi.model.FreechessUtils;
import android.util.Log;

public class JournalInfo implements Iterable<JournalInfo.Entry> {
	
	private static final String TAG = JournalInfo.class.getSimpleName();
	
	public static class Entry {
		
		private String id;
		
		private String whiteName;
		
		private int whiteRating;
		
		private String blackName;
		
		private int blackRating;
		
		private char type;
		
		private boolean rated;
		
		private int time;
		
		private int increment;
		
		private String eco;
		
		private String resultDescription;
		
		private String result;
		
		public String getId() {
			return id;
		}
		
		public String getWhiteName() {
			return whiteName;
		}
		
		public int getWhiteRating() {
			return whiteRating;
		}
		
		public String getBlackName() {
			return blackName;
		}
		
		public int getBlackRating() {
			return blackRating;
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
		
		public String getResult() {
			return result;
		}
		
		private static Entry fromMatch(Matcher m) {
			// ([%]\\d{2}): (\\S+) +(\\d+) +(\\S+) +(\\d+) +\\[ ([bslwBzSLxun])([ru]) *(\\d+) +(\\d+)\\] +(\\S+) +(\\S+) +(1-0|0-1|1/2-1/2|\\*)
			Entry entry = new Entry();
			entry.id = m.group(1);
			entry.whiteName = m.group(2);
			entry.whiteRating = Integer.parseInt(m.group(3));
			entry.blackName = m.group(4);
			entry.blackRating = Integer.parseInt(m.group(5));
			entry.type = m.group(6).charAt(0);
			entry.rated = "r".equals(m.group(7));
			entry.time = Integer.parseInt(m.group(8));
			entry.increment = Integer.parseInt(m.group(9));
			entry.eco = m.group(10);
			entry.resultDescription = m.group(11);
			entry.result = m.group(12);
			return entry;
		}
	}
	
	private String user;
	
	private List<Entry> entries;
	
	public JournalInfo() {
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
	
	public static JournalInfo fromMatch(Matcher m) {
		JournalInfo info = new JournalInfo();
		info.user = m.group(1);
		m = FreechessUtils.JOURNAL_ENTRY.matcher(m.group(2));
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
