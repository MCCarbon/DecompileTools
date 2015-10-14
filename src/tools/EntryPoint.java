package tools;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;

import tools.classfinder.ClassByStringConstantPoolFinder;
import tools.classfinder.ConstantPoolStringToClassMappingsGen;
import tools.renamers.AllClassesRenamer;
import tools.renamers.EnumConstNameRestorer;

public class EntryPoint {

	private static final HashMap<String, Class<? extends Tool>> tools = new HashMap<String, Class<? extends Tool>>();
	static {
		tools.put("ClassFinderFind", ClassByStringConstantPoolFinder.class);
		tools.put("ClassFinderGenerate", ConstantPoolStringToClassMappingsGen.class);
		tools.put("ClassesRename", AllClassesRenamer.class);
		tools.put("EnumConstantsRestore", EnumConstNameRestorer.class);
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
