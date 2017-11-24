package tools.utils;

public class Utils {

	private static final int classLength = ".class".length();
	public static String stripClassEnding(String string) {
		return string.substring(0, string.length() - classLength);
	}

	public static String lastName(String string) {
		int lastIndex = string.lastIndexOf('/');
		if (lastIndex == -1) {
			return string;
		}
		return string.substring(lastIndex + 1, string.length());
	}

}
