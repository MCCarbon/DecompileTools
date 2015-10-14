package tools.classfinder;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import tools.Tool;
import tools.utils.MappingUtils;
import tools.utils.RootLevelJarIterate;
import tools.utils.Utils;

public class ClassByStringConstantPoolFinder implements Tool {

	private String[] args;
	public ClassByStringConstantPoolFinder(String args[]) {
		this.args = args;
	}

	@Override
	public void run() {
		String jarfilename = args[0];
		String finderfilename = args[1];
		String mappingsfilename = args[2];
		try (PrintWriter writer = new PrintWriter(mappingsfilename); JarFile file = new JarFile(jarfilename); Scanner scanner = new Scanner(new File(finderfilename), StandardCharsets.UTF_8.toString())) {
			List<FinderEntry> finderEntries = getFinderEntries(scanner);
			HashMap<String, String> mappings = new HashMap<String, String>();
			for (JarEntry entry : new RootLevelJarIterate(file)) {
				String original = Utils.stripClassEnding(entry.getName());
				HashSet<String> constants = Utils.getConstantPoolStrings(file.getInputStream(entry), original);
				for (FinderEntry fentry : finderEntries) {
					if (constants.containsAll(fentry.strings)) {
						//if mappings already contains the value then it means that mapping is not unique anymore
						if (mappings.containsKey(fentry.className)) {
							mappings.put(fentry.className, "");
						} else {
							mappings.put(fentry.className, original);
						}
						break;
					}
				}
			}
			HashSet<String> finalmappings = new HashSet<String>();
			for (Entry<String, String> entry : mappings.entrySet()) {
				//skip non entries marked as non unique
				if (entry.getValue().isEmpty()) {
					continue;
				}
				//check if inner hierarchy matches the mapping one
				String[] originalSplit = entry.getValue().split("[$]");
				String[] classNameSplit = entry.getKey().split("[$]");
				if (originalSplit.length == classNameSplit.length) {
					for (int i = 0; i < originalSplit.length; i++) {
						//create mappings for while hierarcy
						finalmappings.add(MappingUtils.createSRG(
							String.join("$", Arrays.copyOfRange(originalSplit, 0, i + 1)),
							String.join("$", Arrays.copyOfRange(classNameSplit, 0, i + 1))
						));
					}
				}
			}
			//now print final mappings
			for (String finalmapping : finalmappings) {
				writer.println(finalmapping);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private List<FinderEntry> getFinderEntries(Scanner scanner) {
		ArrayList<FinderEntry> result = new ArrayList<>();
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			if (line.isEmpty()) {
				continue;
			}
			result.add(new FinderEntry(line));
		}
		return result;
	}

	private static final class FinderEntry {
		private List<String> strings;
		private String className;

		public FinderEntry(String unparsedString) {
			String[] split = unparsedString.split("[;]");
			className = split[split.length - 1];
			strings = Arrays.asList(Arrays.copyOfRange(split, 0, split.length - 1));
		}
	}

}
