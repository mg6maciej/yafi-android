package pl.mg6.common;

public class StringUtils {
	
	private StringUtils() {
	}
	
	public static String join(String j, String[] array) {
		StringBuilder builder = new StringBuilder();
		builder.append(array[0]);
		for (int i = 1; i < array.length; i++) {
			builder.append(j);
			builder.append(array[i]);
		}
		return builder.toString();
	}
}
