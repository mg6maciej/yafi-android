package pl.mg6.yafi.model.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import pl.mg6.yafi.model.FreechessUtils;

public class PendingInfo {
	
	private List<PendingOffer> sentOffers = new ArrayList<PendingOffer>();
	
	private List<PendingOffer> receivedOffers = new ArrayList<PendingOffer>();
	
	public int getSentOfferCount() {
		return sentOffers.size();
	}
	
	public PendingOffer getSentOffer(int index) {
		return sentOffers.get(index);
	}
	
	public int getReceivedOffersCount() {
		return receivedOffers.size();
	}
	
	public PendingOffer getReceivedOffer(int index) {
		return receivedOffers.get(index);
	}
	
	public static PendingInfo fromMatch(Matcher m) {
		PendingInfo info = new PendingInfo();
		String to = m.group(1);
		String from = m.group(2);
		if (to != null) {
			m = FreechessUtils.PENDING_OFFER_TO.matcher(to);
			while (m.find()) {
				PendingOffer po = PendingOffer.fromMatch(m);
				info.sentOffers.add(po);
			}
		}
		if (from != null) {
			m = FreechessUtils.PENDING_OFFER_FROM.matcher(from);
			while (m.find()) {
				PendingOffer po = PendingOffer.fromMatch(m);
				info.receivedOffers.add(po);
			}
		}
		return info;
	}
}
