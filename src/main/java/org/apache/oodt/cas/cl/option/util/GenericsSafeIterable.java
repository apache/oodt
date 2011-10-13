package org.apache.oodt.cas.cl.option.util;

import java.util.Iterator;

public class GenericsSafeIterable<T> implements Iterable<T> {

	private static final long serialVersionUID = -4763442582059214684L;

	private Iterable<?> iterable;

	public GenericsSafeIterable(Iterable<?> iterable) {
		this.iterable = iterable;
	}

	public Iterator<T> iterator() {
		return new Iterator<T>() {
			
			private Iterator<?> iterator = iterable.iterator();
			private T next = null;

			public boolean hasNext() {
				if (next == null) {
					next = safeGetNext(iterator);
				}
				return next != null;
			}

			public T next() {
				if (next == null) {
					next = safeGetNext(iterator);
				}
				T curNext = next;
				next = safeGetNext(iterator);
				return curNext;
			}

			public void remove() {
				// do nothing
			}
			
		};
	}

	private T safeGetNext(Iterator<?> iterator) {
		while (iterator.hasNext()) {
			try {
				return (T) iterator.next();
			} catch (ClassCastException e) {}
		}
		return null;
	}
}
