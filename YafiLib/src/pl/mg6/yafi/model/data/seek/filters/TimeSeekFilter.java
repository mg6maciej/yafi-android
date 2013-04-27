package pl.mg6.yafi.model.data.seek.filters;

import pl.mg6.yafi.model.data.SeekInfo;

public class TimeSeekFilter implements SeekFilter {
	
	private int minTime;
	private int maxTime;
	
	public TimeSeekFilter(int minTime, int maxTime) {
		this.minTime = minTime;
		this.maxTime = maxTime;
	}
	
	@Override
	public boolean matches(SeekInfo info) {
		int time = info.getTime();
		int increment = info.getIncrement();
		if (time == 0 && increment == 0) {
			return false;
		}
		int etime = time + 2 * increment / 3;
		return minTime <= etime && etime <= maxTime;
	}
}
