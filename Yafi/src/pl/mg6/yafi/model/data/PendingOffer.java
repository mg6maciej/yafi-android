package pl.mg6.yafi.model.data;

import java.util.regex.Matcher;

public abstract class PendingOffer {
	
	private int id;
	
	private String name;
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public static PendingOffer fromMatch(Matcher m) {
		PendingOffer po;
		if (m.group(3) != null) {
			po = new DrawPendingOffer();
		} else if (m.group(4) != null) {
			if ("abort".equals(m.group(4))) {
				po = new AbortPendingOffer();
			} else {
				po = new AdjournPendingOffer();
			}
		} else if (m.group(5) != null) {
			TakebackPendingOffer tpo = new TakebackPendingOffer();
			tpo.moves = Integer.parseInt(m.group(5));
			po = tpo;
		} else if (m.group(6) != null) {
			ChallengePendingOffer cpo = new ChallengePendingOffer();
			cpo.rated = "rated".equals(m.group(6));
			cpo.type = m.group(7);
			if ("lightning".equals(cpo.type) || "blitz".equals(cpo.type) || "standard".equals(cpo.type)) {
				cpo.type = "chess";
			}
			cpo.time = Integer.parseInt(m.group(8));
			cpo.increment = Integer.parseInt(m.group(9));
			po = cpo;
		} else {
			throw new IllegalArgumentException();
		}
		po.id = Integer.parseInt(m.group(1));
		po.name = m.group(2);
		return po;
	}
}
