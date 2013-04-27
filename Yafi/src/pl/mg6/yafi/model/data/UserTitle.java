package pl.mg6.yafi.model.data;

import java.util.HashMap;
import java.util.Map;

public class UserTitle {
	
	private UserTitle() {
	}
	
	private static final Map<String, String> abbrTextMap = new HashMap<String, String>();
	
	static {
		abbrTextMap.put("*", "Administrator");
		abbrTextMap.put("SR", "Service Representative");
		abbrTextMap.put("TM", "Tournament Manager");
		abbrTextMap.put("TD", "Non-playing Computer Program");
		abbrTextMap.put("CA", "Chess Advisor");
		abbrTextMap.put("C", "Computer Program");
		abbrTextMap.put("U", "Unregistered User");
		abbrTextMap.put("B", "Blindfolded User");
		abbrTextMap.put("T", "Team Account");
		abbrTextMap.put("D", "Demonstration Account");
		abbrTextMap.put("GM", "Grandmaster");
		abbrTextMap.put("IM", "International Master");
		abbrTextMap.put("FM", "FIDE Master");
		abbrTextMap.put("WGM", "Woman Grandmaster");
		abbrTextMap.put("WIM", "Woman International Master");
		abbrTextMap.put("WFM", "Woman FIDE Master");
	}
	
	public static final int UNREGISTERED = 1;
	public static final int COMPUTER = 2;
	
	public static String abbrToText(String title) {
		return abbrTextMap.get(title);
	}
}
