package tools.utils;

import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class RootLevelJarIterate implements Iterable<JarEntry> {

	protected final JarFile jarfile;
	public RootLevelJarIterate(JarFile file) {
		this.jarfile = file;
	}

	@Override
	public Iterator<JarEntry> iterator() {
		return new JarIterator();
	}

	private class JarIterator implements Iterator<JarEntry> {

		private final EnumerationIterator<JarEntry> entryIt = new EnumerationIterator<>(jarfile.entries());
		private JarEntry next = findNext();

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public JarEntry next() {
			JarEntry result = next;
			next = findNext();
			return result;
		}

		private JarEntry findNext() {
			if (!entryIt.hasNext()) {
				return null;
			}
			do {
				JarEntry entry = entryIt.next();
				if (!entry.isDirectory() && entry.getName().indexOf('/') == -1 && entry.getName().endsWith(".class")) {
					return entry;
				}
			} while (entryIt.hasNext());
			return null;
		}

	}

}
