package tools.mappinggen;

import java.io.PrintWriter;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import tools.Tool;
import tools.utils.MappingUtils;
import tools.utils.RootLevelJarIterate;
import tools.utils.Utils;

public class ClassRenameMappingsGen implements Tool {

	private String[] args;
	public ClassRenameMappingsGen(String args[]) {
		this.args = args;
	}

	@Override
	public void run() {
		String jarfilename = args[0];
		String mappingsfilename = args[1];
		try (PrintWriter writer = new PrintWriter(mappingsfilename); JarFile file = new JarFile(jarfilename)) {
			for (JarEntry entry : new RootLevelJarIterate(file)) {
				String original = Utils.stripClassEnding(entry.getName());
				String remapped = String.join("$", rename(original.split("[$]")));
				writer.println(MappingUtils.createSRG(original, remapped));
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
