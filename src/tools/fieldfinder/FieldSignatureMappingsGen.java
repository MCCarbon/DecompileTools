package tools.fieldfinder;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import com.google.gson.GsonBuilder;

import tools.EntryPoint;
import tools.Tool;
import tools.utils.EnumerationIterator;
import tools.utils.Utils;

public class FieldSignatureMappingsGen implements Tool {

	@Override
	public void run() {
		String args[] = EntryPoint.getArgs();
		String inputJarFileName = args[0];
		String classPathStart = args[1];
		int minFieldNameLength = Integer.parseInt(args[2]);
		String outputMappingsFileName = args[3];
		try (
			PrintWriter outputMappingsWriter = new PrintWriter(outputMappingsFileName, StandardCharsets.UTF_8.toString());
			JarFile inputJarFile = new JarFile(inputJarFileName)
		) {
			Map<String, Map<String, List<String>>> fieldMappings = new HashMap<>();
			for (JarEntry jarEntry : new EnumerationIterator<>(inputJarFile.entries())) {
				if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class") || !jarEntry.getName().startsWith(classPathStart)) {
					continue;
				}
				String fullClassName = Utils.stripClassEnding(jarEntry.getName());
				JavaClass clazz = new ClassParser(inputJarFile.getInputStream(jarEntry), fullClassName).parse();
				Map<String, List<String>> localFieldMappingsByType = new HashMap<>();
				for (Field field : clazz.getFields()) {
					if (FieldFinderHelpers.shouldIgnoreField(clazz, field)) {
						continue;
					}
					if (field.getName().length() < minFieldNameLength) {
						continue;
					}
					localFieldMappingsByType.compute(field.getSignature(), (ftype, names) -> {
						if (names == null) {
							names = new ArrayList<>();
						}
						return names;
					})
					.add(field.getName());
				}
				if (!localFieldMappingsByType.isEmpty()) {
					fieldMappings.put(fullClassName, localFieldMappingsByType);
				}
			}
			new GsonBuilder()
			.setPrettyPrinting()
			.create()
			.toJson(
				fieldMappings,
				FieldFinderHelpers.MAPPINGS_JSON_FORMAT_TYPE_TOKEN,
				outputMappingsWriter
			);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
