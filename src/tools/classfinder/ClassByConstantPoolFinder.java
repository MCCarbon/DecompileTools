package tools.classfinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.gson.Gson;

import tools.EntryPoint;
import tools.Tool;
import tools.classfinder.ClassByConstantPoolFinder.DeepestClass.ClassMapping;
import tools.utils.EnumerationIterator;
import tools.utils.MappingUtils;
import tools.utils.Utils;

public class ClassByConstantPoolFinder implements Tool {

	@Override
	public void run() {
		String args[] = EntryPoint.getArgs();
		String inputJarFileName = args[0];
		String inputMappingsFileName = args[1];
		String outputSrgMappingsFileName = args[2];
		try (
			PrintWriter outputSrgMappingWriter = new PrintWriter(outputSrgMappingsFileName);
			JarFile inputJarFile = new JarFile(inputJarFileName);
			Reader inputMappingsReader = new InputStreamReader(new FileInputStream(new File(inputMappingsFileName)), StandardCharsets.UTF_8.toString())
		) {
			Map<String, Set<Object>> classToConstants = new Gson().fromJson(inputMappingsReader, ClassFinderHelpers.MAPPINGS_JSON_FORMAT_TYPE_TOKEN);
			Map<String, PossibleClass> bestMatchMappings = new HashMap<String, PossibleClass>();
			//find best match for obfuscated class
			for (JarEntry jarEntry : new EnumerationIterator<>(inputJarFile.entries())) {
				if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class")) {
					continue;
				}
				String fullClassName = Utils.stripClassEnding(jarEntry.getName());
				Set<Object> classConstants = ClassFinderHelpers.getConstantPool(inputJarFile.getInputStream(jarEntry), fullClassName);
				for (Entry<String, Set<Object>> fentry : classToConstants.entrySet()) {
					int classWeight = 0;
					for (Object fconstant : fentry.getValue()) {
						if (classConstants.contains(fconstant)) {
							if (fconstant instanceof String) {
								classWeight += 2;
							} else {
								classWeight += 1;
							}
						}
					}
					if (bestMatchMappings.getOrDefault(fullClassName, PossibleClass.ZERO).weight < classWeight) {
						bestMatchMappings.put(fullClassName, new PossibleClass(fentry.getKey(), classWeight));
					}
				}
			}
			//deduplicate mappings
			Map<String, PossibleClass> uniqueMappings = new HashMap<String, PossibleClass>();
			for (Entry<String, PossibleClass> entry : bestMatchMappings.entrySet()) {
				String targetName = entry.getValue().name;
				if (uniqueMappings.getOrDefault(targetName, PossibleClass.ZERO).weight < entry.getValue().weight) {
					uniqueMappings.put(targetName, new PossibleClass(entry.getKey(), entry.getValue().weight));
				}
			}
			//inner class deepest hierarchy check
			Map<String, DeepestClass> deepestMappings = new HashMap<>();
			for (Entry<String, PossibleClass> entry : uniqueMappings.entrySet()) {
				String[] sourceName = entry.getValue().name.split("[$]");
				String[] targetName = entry.getKey().split("[$]");
				if (sourceName.length == targetName.length) {
					String targetClassBase = targetName[0];
					int currentDepth = targetName.length;
					DeepestClass existing = deepestMappings.getOrDefault(targetClassBase, DeepestClass.ZERO);
					if (existing.depth < currentDepth) {
						deepestMappings.put(targetClassBase, new DeepestClass(sourceName, targetName, targetName.length));
					} else if (existing.depth == currentDepth) {
						existing.add(sourceName, targetName);
					}
				}
			}
			//print srg mappings
			for (DeepestClass clazz : deepestMappings.values()) {
				for (ClassMapping mapping : clazz.mappings) {
					for (int i = 0; i < mapping.sourceName.length; i++) {
						outputSrgMappingWriter.println(MappingUtils.createSRG(
							String.join("$", Arrays.copyOfRange(mapping.sourceName, 0, i + 1)),
							String.join("$", Arrays.copyOfRange(mapping.targetName, 0, i + 1))
						));
					}	
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	protected static class PossibleClass {
		public static final PossibleClass ZERO = new PossibleClass("", 0);
		private final String name;
		private final int weight;
		public PossibleClass(String name, int weight) {
			this.name = name;
			this.weight = weight;
		}
	}

	protected static class DeepestClass {
		public static class ClassMapping {
			private final String[] sourceName;
			private final String[] targetName;
			public ClassMapping(String[] sourceName, String[] targetName) {
				this.sourceName = sourceName;
				this.targetName = targetName;
			}
		}
		public static final DeepestClass ZERO = new DeepestClass(new String[] {}, new String[] {}, 0);
		private final List<ClassMapping> mappings = new ArrayList<>();
		private final int depth;
		public DeepestClass(String[] sourceName, String[] targetName, int deep) {
			this.mappings.add(new ClassMapping(sourceName, targetName));
			this.depth = deep;
		}
		public void add(String[] sourceName, String[] targetName) {
			this.mappings.add(new ClassMapping(sourceName, targetName));
		}
	}

}
