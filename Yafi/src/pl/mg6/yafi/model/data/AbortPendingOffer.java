package pl.mg6.yafi.model.data;

public final class AbortPendingOffer extends PendingOffer {
	
	@Override
	public String toString() {
		return getName() + ": abort";
	}
}
