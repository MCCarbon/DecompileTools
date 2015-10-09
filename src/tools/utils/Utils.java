package tools.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

import com.sun.org.apache.bcel.internal.classfile.ClassFormatException;
import com.sun.org.apache.bcel.internal.classfile.ClassParser;
import com.sun.org.apache.bcel.internal.classfile.Constant;
import com.sun.org.apache.bcel.internal.classfile.ConstantPool;
import com.sun.org.apache.bcel.internal.classfile.ConstantString;

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

	public static HashSet<String> getConstantPoolStrings(InputStream is, String name) throws ClassFormatException, IOException {
		HashSet<String> constants = new HashSet<String>();
		ClassParser parser = new ClassParser(is, name);
		ConstantPool pool = parser.parse().getConstantPool();
		for (Constant constant : pool.getConstantPool()) {
			//Strings used in code are stored using String constant, they are just references to Utf8 constant
			//but we can't use Utf8 constants because they are used to store field/method/class names and signatures too
			if (constant instanceof ConstantString) {
				String string = (String) ((ConstantString) constant).getConstantValue(pool);
				//skip empty string
				if (string.isEmpty()) {
					continue;
				}
				//skip string containng endline
				if (string.indexOf('\n') != -1) {
					continue;
				}
				//skip string that contain ";", it is used as a separator, this check should be here actually, but well...
				if (string.indexOf(';') != -1) {
					continue;
				}
				constants.add(string);
			}
		}
		return constants;
	}

}
