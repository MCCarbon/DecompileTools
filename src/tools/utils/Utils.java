package tools.utils;

public class Utils {

	private static final int classLength = ".class".length();
	public static String stripClassEnding(String string) {
		return string.substring(0, string.length() - classLength);
	}

}
