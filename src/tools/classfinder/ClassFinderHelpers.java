package tools.classfinder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantObject;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;

import com.google.gson.reflect.TypeToken;

public class ClassFinderHelpers {

	public static final Type MAPPINGS_JSON_FORMAT_TYPE_TOKEN = new TypeToken<Map<String, Set<Object>>>() { }.getType();

	public static Set<Object> getConstantPool(InputStream is, String name) throws ClassFormatException, IOException {
		HashSet<Object> constants = new HashSet<Object>();
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
				constants.add(string);
			}
			if (constant instanceof ConstantInteger || constant instanceof ConstantLong || constant instanceof ConstantFloat || constant instanceof ConstantDouble) {
				constants.add(((ConstantObject) constant).getConstantValue(pool));
			}
		}
		return constants;
	}

}
