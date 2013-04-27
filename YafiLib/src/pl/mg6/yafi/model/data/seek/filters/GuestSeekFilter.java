package pl.mg6.yafi.model.data.seek.filters;

import pl.mg6.yafi.model.data.SeekInfo;

public class GuestSeekFilter implements SeekFilter {
	
	@Override
	public boolean matches(SeekInfo info) {
		return (info.getTitles() & 1) == 1;
	}
}
