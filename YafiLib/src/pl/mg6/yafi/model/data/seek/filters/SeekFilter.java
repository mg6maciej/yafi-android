package pl.mg6.yafi.model.data.seek.filters;

import pl.mg6.yafi.model.data.SeekInfo;

public interface SeekFilter {
	
	boolean matches(SeekInfo info);
}
