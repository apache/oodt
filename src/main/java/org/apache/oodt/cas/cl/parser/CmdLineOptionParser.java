package org.apache.oodt.cas.cl.parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;

public abstract class CmdLineOptionParser {

	public Set<CmdLineOptionInstance<?>> parse(String[] args, Set<CmdLineOption<?>> validOptions) throws IOException {
		return parse(new Args(args), validOptions);
	}

	public abstract Set<CmdLineOptionInstance<?>> parse(Args args, Set<CmdLineOption<?>> validOptions) throws IOException;

	protected class Args implements Iterable<String> {
		private int curIndex;
		private String[] args;

		public Args(String[] args) {
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

		public boolean incrementIndex() {
			if (curIndex + 1 < args.length) {
				curIndex++;
				return true;
			} else {
				return false;
			}
		}

		public int numArgs() {
			return args.length;
		}

		public String getArg(int index) {
			return args[index];
		}

		public String getCurrentArg() {
			return args[curIndex];
		}

		public Iterator<String> iterator() {
			return new Iterator<String>() {

				private boolean hasNext = curIndex < args.length;

				public boolean hasNext() {
					return hasNext;
				}

				public String next() {
					if (!hasNext) {
						throw new IndexOutOfBoundsException(curIndex + "");
					}
					String currentArg = getCurrentArg();
					hasNext = incrementIndex();
					return currentArg;
				}

				public void remove() {
					// do nothing
				}

			};
		}
	}

	protected class GroupCmdLineOptionInstance extends
			CmdLineOptionInstance<CmdLineOptionInstance<?>> {
	}

	protected class StringCmdLineOptionInstance extends
			CmdLineOptionInstance<String> {
	}
}
