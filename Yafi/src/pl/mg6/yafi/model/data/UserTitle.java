package pl.mg6.yafi.model.data;

import java.util.HashMap;
import java.util.Map;

import pl.mg6.yafi.R;

public class UserTitle {
	
	private UserTitle() {
	}
	
	private static final Map<String, Integer> abbrTextMap = new HashMap<String, Integer>();
	
	static {
		abbrTextMap.put("*", R.string.title_admin);
		abbrTextMap.put("SR", R.string.title_sr);
		abbrTextMap.put("TM", R.string.title_tm);
		abbrTextMap.put("TD", R.string.title_td);
		abbrTextMap.put("CA", R.string.title_advisor);
		abbrTextMap.put("C", R.string.title_comp);
		abbrTextMap.put("U", R.string.title_unreg);
		abbrTextMap.put("B", R.string.title_blind);
		abbrTextMap.put("T", R.string.title_team);
		abbrTextMap.put("D", R.string.title_demo);
		abbrTextMap.put("GM", R.string.title_gm);
		abbrTextMap.put("IM", R.string.title_im);
		abbrTextMap.put("FM", R.string.title_fm);
		abbrTextMap.put("WGM", R.string.title_wgm);
		abbrTextMap.put("WIM", R.string.title_wim);
		abbrTextMap.put("WFM", R.string.title_wfm);
	}
	
	public static final int UNREGISTERED = 1;
	public static final int COMPUTER = 2;
	
	public static int abbrToText(String title) {
		return abbrTextMap.get(title);
	}
}
