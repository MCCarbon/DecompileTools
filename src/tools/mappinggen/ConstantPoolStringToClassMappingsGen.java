package tools.mappinggen;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import tools.Tool;
import tools.utils.EnumerationIterator;
import tools.utils.Utils;

public class ConstantPoolStringToClassMappingsGen implements Tool {

	private String[] args;
	public ConstantPoolStringToClassMappingsGen(String args[]) {
		this.args = args;
	}

	@Override
	public void run() {
		String jarfilename = args[0];
		String scanpath = args[1];
		String mappingsfilename = args[2];
		try (PrintWriter writer = new PrintWriter(mappingsfilename, StandardCharsets.UTF_8.toString()); JarFile file = new JarFile(jarfilename)) {
			HashMap<String, HashSet<String>> classToConstants = new HashMap<>();
			for (JarEntry entry : new EnumerationIterator<>(file.entries())) {
				if (entry.isDirectory() || !entry.getName().endsWith(".class") || !entry.getName().startsWith(scanpath)) {
					continue;
				}
				String original = Utils.stripClassEnding(entry.getName());
				classToConstants.put(Utils.lastName(original), Utils.getConstantPoolStrings(file.getInputStream(entry), original));
			}
			for (Entry<String, HashSet<String>> entry : classToConstants.entrySet()) {
				//is cp is empty then skip class
				if (entry.getValue().isEmpty()) {
					continue;
				}
				//now check if the mapping is unique
				boolean unique = true;
				for (Entry<String, HashSet<String>> otherEntry : classToConstants.entrySet()) {
					if (otherEntry.getKey().equals(entry.getKey())) {
						continue;
					}
					if (otherEntry.getValue().containsAll(entry.getValue())) {
						unique = false;
						break;
					}
				}
				//print unique mappings to file
				if (unique) {
					StringBuilder builder = new StringBuilder();
					for (String cpString : entry.getValue()) {
						builder.append(cpString).append(";");
					}
					builder.append(entry.getKey());
					writer.println(builder.toString());
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
