package pl.mg6.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlEntityEncoder {
	
	private HtmlEntityEncoder() {
	}
	
	private static final String FORMAT = "&#x%x;";
	
	private static final Pattern WRONG_CHAR = Pattern.compile("[^ -~]|&#x[0-9a-f]{1,4};", Pattern.CASE_INSENSITIVE);
	
	private static final Pattern ENTITY = Pattern.compile("&#x([0-9a-f]{1,4});", Pattern.CASE_INSENSITIVE);
	
	public static String encode(String input) {
		StringBuilder buffer = new StringBuilder(2 * input.length());
		Matcher m = WRONG_CHAR.matcher(input);
		int last = 0;
		while (m.find()) {
			buffer.append(input.substring(last, m.start()));
			String entity = String.format(FORMAT, (int) input.charAt(m.start()));
			buffer.append(entity);
			last = m.start() + 1;
		}
		if (last == 0) {
			return input;
		}
		buffer.append(input.substring(last));
		return buffer.toString();
	}
	
	public static String decode(String input) {
		StringBuilder buffer = new StringBuilder(input.length());
		Matcher m = ENTITY.matcher(input);
		int last = 0;
		while (m.find()) {
			buffer.append(input.substring(last, m.start()));
			char c = (char) Integer.parseInt(m.group(1), 16);
			buffer.append(c);
			last = m.end();
		}
		if (last == 0) {
			return input;
		}
		buffer.append(input.substring(last));
		return buffer.toString();
	}
}
