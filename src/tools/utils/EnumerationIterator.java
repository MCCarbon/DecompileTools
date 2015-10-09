package tools.utils;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterator<T> implements Iterator<T>, Iterable<T> {

	private final Enumeration<T> enumeration;
	public EnumerationIterator(Enumeration<T> enumeration) {
		this.enumeration = enumeration;
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return enumeration.hasMoreElements();
	}

	@Override
	public T next() {
		return enumeration.nextElement();
	}

}
