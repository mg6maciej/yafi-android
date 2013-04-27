package pl.mg6.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	
	private StringUtils() {
	}
	
	private static final Pattern FIRST_OR_AFTER_UNDERLINE = Pattern.compile("(?:^|_+)[a-z]");
	
	public static String join(String j, String[] array) {
		StringBuilder builder = new StringBuilder();
		builder.append(array[0]);
		for (int i = 1; i < array.length; i++) {
			builder.append(j);
			builder.append(array[i]);
		}
		return builder.toString();
	}
	
	public static String underlinedToPascalCase(String str) {
		StringBuilder buffer = new StringBuilder(str.length());
		Matcher m = FIRST_OR_AFTER_UNDERLINE.matcher(str);
		int last = 0;
		while (m.find()) {
			buffer.append(str.substring(last, m.start()));
			char c = Character.toUpperCase(str.charAt(m.end() - 1));
			buffer.append(c);
			last = m.end();
		}
		buffer.append(str.substring(last));
		return buffer.toString();
	}
}
