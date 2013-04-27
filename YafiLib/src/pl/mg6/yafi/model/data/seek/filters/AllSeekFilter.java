package pl.mg6.yafi.model.data.seek.filters;

import pl.mg6.yafi.model.data.SeekInfo;

public class AllSeekFilter implements SeekFilter {
	
	@Override
	public boolean matches(SeekInfo info) {
		return true;
	}
}
