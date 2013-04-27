package pl.mg6.yafi.model.data;

public final class TakebackPendingOffer extends PendingOffer {
	
	int moves;
	
	public int getMoves() {
		return moves;
	}
	
	@Override
	public String toString() {
		return String.format("%s: takeback (%d moves)", getName(), moves);
	}

}
