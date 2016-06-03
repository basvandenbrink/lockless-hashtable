package nl.utwente.csc.fmt.locklesshashtable.test;

import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

import nl.utwente.csc.fmt.locklesshashtable.spehashtable.Hashtable;

@SuppressWarnings("serial")
public class TestAction extends RecursiveAction {
	public static final int THRESHOLD = 100;

	private Hashtable hashtable;
	private int startIndex, endIndex;
	private int[][] vectors;
	private boolean print;

	public TestAction(Hashtable hashtable, int[][] vectors, int startIndex,
			int endIndex, boolean print) {
		this.hashtable = hashtable;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.vectors = vectors;
		this.print = print;
	}

	@Override
	protected void compute() {
		int diff = endIndex - startIndex;
		if (diff < THRESHOLD) {
			for (int i = startIndex; i < endIndex; i++) {
				boolean found = hashtable.lookup(vectors[i]);
				if (print)
					System.out.printf("vector: %s, found: %s%n",
							Arrays.toString(vectors[i]), found ? "yes" : "no");
			}
		} else {
			TestAction ta1 = new TestAction(hashtable, vectors, startIndex,
					startIndex + diff / 2, print);
			TestAction ta2 = new TestAction(hashtable, vectors, startIndex
					+ diff / 2, endIndex, print);
			ta1.fork();
			ta2.fork();
			ta1.join();
			ta2.join();
		}

	}

}
