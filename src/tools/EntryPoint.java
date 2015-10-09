package tools;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;

import tools.mappinggen.ClassByStringConstantPoolFinder;
import tools.mappinggen.ConstantPoolStringToClassMappingsGen;
import tools.mappinggen.EnumNameRestorer;
import tools.mappinggen.ClassRenameMappingsGen;

public class EntryPoint {

	private static final HashMap<String, Class<? extends Tool>> tools = new HashMap<String, Class<? extends Tool>>();
	static {
		tools.put("mappings", ClassRenameMappingsGen.class);
		tools.put("classfinder", ClassByStringConstantPoolFinder.class);
		tools.put("enumfixer", EnumNameRestorer.class);
		tools.put("classfindermappings", ConstantPoolStringToClassMappingsGen.class);
	}

	public static void main(String args[]) {
		try {
			tools.get(args[0].toLowerCase()).getConstructor(String[].class).newInstance((Object) Arrays.copyOfRange(args, 1, args.length)).run();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			System.err.println("Failed to run tool");
			e.printStackTrace();
		}
	}

}
