package tools.renamers;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import tools.EntryPoint;
import tools.Tool;
import tools.utils.EnumerationIterator;
import tools.utils.MappingUtils;
import tools.utils.Utils;

public class AllClassesRenamer implements Tool {

	@Override
	public void run() {
		String args[] = EntryPoint.getArgs();
		String inputJarFileName = args[0];
		String outputSrgMappingsFileName = args[1];
		try (
			PrintWriter outputSrgMappingWriter = new PrintWriter(outputSrgMappingsFileName);
			JarFile inputJarFile = new JarFile(inputJarFileName)
		) {
			for (JarEntry jarEntry : new EnumerationIterator<>(inputJarFile.entries())) {
				if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class")) {
					continue;
				}
				String original = Utils.stripClassEnding(jarEntry.getName());
				String[] pathAndName = original.split("[/]");
				String path = pathAndName.length > 1 ? String.join("/", Arrays.copyOf(pathAndName, pathAndName.length - 1)) : null;
				String remappedname = String.join("$", rename(pathAndName[pathAndName.length - 1].split("[$]")));
				String remapped = path == null ? remappedname : path + "/" + remappedname;
				outputSrgMappingWriter.println(MappingUtils.createSRG(original, remapped));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static String[] rename(String[] elements) {
		for (int i = 0; i < elements.length; i++) {
			if (i == 0) {
				elements[i] = "class_" + elements[i];
			} else {
				elements[i] = "class_" + elements[i] + "_in_" + elements[i - 1];
			}
		}
		return elements;
	}

}
