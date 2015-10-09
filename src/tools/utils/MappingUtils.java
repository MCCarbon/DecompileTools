package tools.utils;

public class MappingUtils {

	public static String createSRG(String oldClassName, String newClassName) {
		return oldClassName + " " + newClassName;
	}

	public static String createSRG(String className, String oldName, String newName) {
		return className + " " + oldName + " " + newName;
	}

}
