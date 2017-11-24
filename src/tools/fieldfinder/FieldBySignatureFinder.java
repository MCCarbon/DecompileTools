package tools.fieldfinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import com.google.gson.Gson;

import tools.EntryPoint;
import tools.Tool;
import tools.utils.EnumerationIterator;
import tools.utils.MappingUtils;
import tools.utils.Utils;

public class FieldBySignatureFinder implements Tool {

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
			Map<String, Map<String, List<String>>> fieldMappings = new Gson().fromJson(inputMappingsReader, FieldFinderHelpers.MAPPINGS_JSON_FORMAT_TYPE_TOKEN);
			for (JarEntry jarEntry : new EnumerationIterator<>(inputJarFile.entries())) {
				if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class")) {
					continue;
				}
				String fullClassName = Utils.stripClassEnding(jarEntry.getName());
				Map<String, List<String>> localFieldMappingsByType = fieldMappings.get(fullClassName);
				if (localFieldMappingsByType != null) {
					JavaClass clazz = new ClassParser(inputJarFile.getInputStream(jarEntry), fullClassName).parse();
					for (Field field : clazz.getFields()) {
						if (FieldFinderHelpers.shouldIgnoreField(clazz, field)) {
							continue;
						}
						List<String> names = localFieldMappingsByType.get(field.getSignature());
						if (names != null && !names.isEmpty()) {
							outputSrgMappingWriter.println(MappingUtils.createSRG(fullClassName, field.getName(), names.remove(0)));
						}
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
