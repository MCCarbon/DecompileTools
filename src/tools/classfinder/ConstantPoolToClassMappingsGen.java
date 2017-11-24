package tools.classfinder;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.gson.GsonBuilder;

import tools.EntryPoint;
import tools.Tool;
import tools.utils.EnumerationIterator;
import tools.utils.Utils;

public class ConstantPoolToClassMappingsGen implements Tool {

	@Override
	public void run() {
		String args[] = EntryPoint.getArgs();
		String inputJarFileName = args[0];
		String classPathStart = args[1];
		String outputMappingsFileName = args[2];
		try (
			PrintWriter outputMappingsWriter = new PrintWriter(outputMappingsFileName, StandardCharsets.UTF_8.toString());
			JarFile inputJarFile = new JarFile(inputJarFileName)
		) {
			Map<String, Set<Object>> classToConstants = new HashMap<>();
			for (JarEntry jarEntry : new EnumerationIterator<>(inputJarFile.entries())) {
				if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class") || !jarEntry.getName().startsWith(classPathStart)) {
					continue;
				}
				String fullClassName = Utils.stripClassEnding(jarEntry.getName());
				Set<Object> classConstants = ClassFinderHelpers.getConstantPool(inputJarFile.getInputStream(jarEntry), fullClassName);
				if (!classToConstants.containsValue(classConstants)) {
					classToConstants.put(Utils.lastName(fullClassName), classConstants);
				}
			}
			new GsonBuilder()
			.setPrettyPrinting()
			.serializeSpecialFloatingPointValues()
			.create()
			.toJson(
				classToConstants,
				ClassFinderHelpers.MAPPINGS_JSON_FORMAT_TYPE_TOKEN,
				outputMappingsWriter
			);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
