package tools.fieldfinder;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.BasicType;

import com.google.gson.reflect.TypeToken;

public class FieldFinderHelpers {

	public static final Type MAPPINGS_JSON_FORMAT_TYPE_TOKEN = new TypeToken<Map<String, Map<String, List<String>>>>() { }.getType();

	public static boolean shouldIgnoreField(JavaClass clazz, Field field) {
		if (clazz.isEnum() && field.isStatic()) {
			return true;
		}
		if (field.isSynthetic() || field.getType() instanceof BasicType) {
			return true;
		}
		return false;
	}

}
