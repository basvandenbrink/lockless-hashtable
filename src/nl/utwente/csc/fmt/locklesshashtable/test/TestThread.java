package nl.utwente.csc.fmt.locklesshashtable.test;

import java.io.PrintStream;
import java.util.Random;

import nl.utwente.csc.fmt.locklesshashtable.spehashtable.Hashtable;

public class TestThread extends Thread {
	private final Hashtable hashtable;
	private final int[][] vectors;
	private int start, lookups;
	private PrintStream print;
	private ThreadListener listener;
	private boolean quit = false;
	private boolean continueWithRandom;
	private boolean debug;

	private boolean[] results = null;

	public TestThread(Hashtable hashtable, int[][] vectors, int start,
			int lookups, PrintStream print, boolean debug,
			boolean continueWithRandom, ThreadListener listener) {
		this.hashtable = hashtable;
		this.vectors = vectors;
		this.start = start;
		this.lookups = lookups;
		this.print = print;
		this.listener = listener;
		this.continueWithRandom = continueWithRandom;
		this.debug = debug;
		if (continueWithRandom)
			listener.add(this);
		if (debug)
			results = new boolean[lookups];

	}

	@Override
	public void run() {
		{
			int i = start;
			int end = start + lookups;
			while (i < end) {
				boolean found = hashtable.lookup(vectors[i]);
				if (debug)
					results[i - start] = found;
				i++;
			}
		}
		listener.notifyDone();
		if (continueWithRandom)
			testRandom();
		if (debug) {
			for (int i = 0; i < lookups; i++) {
				Test.printDebugLine(print, getName(), vectors[start + i], results[i]);
			}
		}

	}

	private void testRandom() {
		Random random = new Random();
		int halfLength = vectors.length / 2;
		int lookups = 0;
		while (!quit) {
			int i = halfLength + random.nextInt(halfLength);
			hashtable.lookup(vectors[i]);
			lookups ++;
		}
		
		System.out.printf("%s, lookups: %d%n", getName(), lookups);
	}

	public void quit() {
		quit = true;
	}

}
