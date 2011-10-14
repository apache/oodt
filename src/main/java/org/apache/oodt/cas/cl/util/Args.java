package org.apache.oodt.cas.cl.util;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang.Validate;

public class Args implements Iterable<String> {
	private int curIndex;
	private String[] args;

	public Args(String[] args) {
		Validate.notNull(args);

		curIndex = 0;
		this.args = args;
	}

	public String[] getArgs() {
		return args;
	}

	public String[] getArgsLeft() {
		return Arrays.copyOfRange(args, curIndex, args.length);
	}

	public int getCurrentIndex() {
		return curIndex;
	}

	public void incrementIndex() {
		curIndex++;
	}

	public void descrementIndex() {
		curIndex--;
	}

	public String incrementAndGet() {
		incrementIndex();
		return getCurrentArg();
	}

	public String getAndIncrement() {
		String next = getCurrentArg();
		incrementIndex();
		return next;
	}

	public int numArgs() {
		return args.length;
	}

	public String getArg(int index) {
		return args[index];
	}

	public boolean hasNext() {
		return curIndex < args.length;
	}

	public String getCurrentArg() {
		if (hasNext()) {
			return args[curIndex];
		} else {
			return null;
		}
	}

	public Iterator<String> iterator() {
		return new Iterator<String>() {

			public boolean hasNext() {
				return Args.this.hasNext();
			}

			public String next() {
				if (!hasNext()) {
					throw new IndexOutOfBoundsException(curIndex + "");
				}
				return getAndIncrement();
			}

			public void remove() {
				// do nothing
			}

		};
	}
}
