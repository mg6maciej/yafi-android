package pl.mg6.yafi.model.data.seek.filters;

import java.util.regex.Pattern;

import pl.mg6.yafi.model.data.SeekInfo;

public class GameTypeSeekFilter implements SeekFilter {
	
	private Pattern gameTypePattern;
	
	public GameTypeSeekFilter(String pattern) {
		gameTypePattern = Pattern.compile(pattern);
	}
	
	@Override
	public boolean matches(SeekInfo info) {
		return gameTypePattern.matcher(info.getType()).matches();
	}
}
