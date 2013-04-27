package pl.mg6.yafi.model.data.seek.filters;

import pl.mg6.yafi.model.data.SeekInfo;

public class ComputerSeekFilter implements SeekFilter {
	
	@Override
	public boolean matches(SeekInfo info) {
		return (info.getTitles() & 2) == 2;
	}
	
}
