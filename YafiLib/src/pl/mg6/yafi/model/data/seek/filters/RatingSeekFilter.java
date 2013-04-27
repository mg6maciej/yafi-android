package pl.mg6.yafi.model.data.seek.filters;

import pl.mg6.yafi.model.data.SeekInfo;

public class RatingSeekFilter implements SeekFilter {
	
	private int minRating;
	private int maxRating;
	
	public RatingSeekFilter(int minRating, int maxRating) {
		this.minRating = minRating;
		this.maxRating = maxRating;
	}
	
	@Override
	public boolean matches(SeekInfo info) {
		int rating = info.getRating();
		return minRating <= rating && rating <= maxRating;
	}
}
