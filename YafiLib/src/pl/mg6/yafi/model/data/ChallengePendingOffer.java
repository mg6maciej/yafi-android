package pl.mg6.yafi.model.data;

public final class ChallengePendingOffer extends PendingOffer {
	
	boolean rated;
	
	String type;
	
	int time;
	
	int increment;
	
	public boolean isRated() {
		return rated;
	}
	
	public String getType() {
		return type;
	}
	
	public int getTime() {
		return time;
	}
	
	public int getIncrement() {
		return increment;
	}
}
