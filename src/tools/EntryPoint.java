package tools;

import java.util.Arrays;
import java.util.HashMap;

import tools.classfinder.ClassByConstantPoolFinder;
import tools.classfinder.ConstantPoolToClassMappingsGen;
import tools.fieldfinder.FieldBySignatureFinder;
import tools.fieldfinder.FieldSignatureMappingsGen;
import tools.renamers.AllClassesRenamer;
import tools.renamers.EnumConstNameRestorer;

public class EntryPoint {

	private static String[] args;
	public static String[] getArgs() {
		return args;
	}

	private static final HashMap<String, Class<? extends Tool>> tools = new HashMap<String, Class<? extends Tool>>();
	static {
		tools.put("ClassFinderGenerate", ConstantPoolToClassMappingsGen.class);
		tools.put("ClassFinderFind", ClassByConstantPoolFinder.class);
		tools.put("FieldFinderGenerate", FieldSignatureMappingsGen.class);
		tools.put("FieldFinderFind", FieldBySignatureFinder.class);
		tools.put("ClassesRename", AllClassesRenamer.class);
		tools.put("EnumConstantsRestore", EnumConstNameRestorer.class);
	}

	public static void main(String args[]) {
		EntryPoint.args = Arrays.copyOfRange(args, 1, args.length);
		if (args.length == 0) {
			System.out.println("Tools: " + tools.keySet());
			return;
		}
		try {
			tools.get(args[0]).newInstance().run();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
			System.err.println("Failed to run tool");
			e.printStackTrace();
		}
	}

}
