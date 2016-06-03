package nl.utwente.csc.fmt.locklesshashtable.test;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import nl.utwente.csc.fmt.locklesshashtable.spehashtable.Hashtable;

import com.lexicalscope.jewel.cli.CliFactory;

public class Test {
	public static final String SEPARATOR = ";";
	private Arguments arguments;

	public Test(Arguments arguments) {
		this.arguments = arguments;
	}

	public static int[][] readVectors(InputStream in) {
		ArrayList<int[]> vectors = new ArrayList<int[]>();
		Scanner scanner = new Scanner(in);
		while (scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split(",");
			int[] vector = new int[line.length];
			for (int i = 0; i < line.length; i++) {
				vector[i] = Integer.parseInt(line[i]);
			}
			vectors.add(vector);
		}
		scanner.close();

		int[][] result = new int[vectors.size()][];
		vectors.toArray(result);
		return result;
	}

	public void run() {
		if (arguments.getNumberOfThreads() == 1)
			testSingleThread();
		else
			testHashtable();
	}

	private void testSingleThread() {
		printFirstLine();
		int[][] vectors = arguments.getVectors();
		Hashtable hashtable = arguments.getHashtable();
		PrintStream out = arguments.getPrintStream();

		for (int i = 0; i < vectors.length; i++) {
			boolean found = hashtable.lookup(vectors[i]);
			if (arguments.getDebug())
				printDebugLine(out, vectors[i], found);
		}
	}

	private void printFirstLine() {
		if (arguments.getNumberOfThreads() == 1)
			arguments.getPrintStream()
					.printf("vector %s is found%n", SEPARATOR);
		else
			arguments.getPrintStream().printf(
					"thread name %1$s vector %1$s is found%n", SEPARATOR);
	}

	static void printDebugLine(PrintStream out, String threadName,
			int[] vector, boolean found) {
		synchronized (out) {
			out.print(threadName);
			out.print(SEPARATOR);
			if (vector.length > 0) {
				out.print(vector[0]);
				for (int i = 1; i < vector.length; i++) {
					out.print(',');
					out.print(vector[i]);
				}
			}
			out.print(SEPARATOR);
			out.println(found);
		}
	}

	static void printDebugLine(PrintStream out, int[] vector, boolean found) {
		synchronized (out) {
			if (vector.length > 0) {
				out.print(vector[0]);
				for (int i = 1; i < vector.length; i++) {
					out.print(',');
					out.print(vector[i]);
				}
			}
			out.print(SEPARATOR);
			out.println(found);
		}
	}

	private void testHashtable() {
		int numberOfThreads = arguments.getNumberOfThreads();
		TestThread[] threads = new TestThread[numberOfThreads];
		int start = 0;
		int lookups = arguments.getVectors().length / numberOfThreads
				/ (arguments.getContinueWithRandom() ? 2 : 1);
		ThreadListener listener = new ThreadListener();

		if (arguments.getDebug())
			printFirstLine();

		for (int i = 0; i < numberOfThreads; i++) {
			threads[i] = new TestThread(arguments.getHashtable(),
					arguments.getVectors(), start, lookups,
					arguments.getPrintStream(), arguments.getDebug(),
					arguments.getContinueWithRandom(), listener);
			start += lookups;
		}

		long startTime = System.nanoTime();

		for (int i = 0; i < numberOfThreads; i++) {
			threads[i].start();
		}

		for (int i = 0; i < numberOfThreads; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		long totalTime = System.nanoTime() - startTime;

		if (arguments.getDebug()) {
			System.out
					.println("Benchmarking complete, time not relevant since print == true");
		} else {
			System.out.printf("Benchmarking complete, time: %d nanoseconds%n",
					totalTime);
		}
	}

	/*
	 * // Does not work properly public void testHashTable(int parallelism,
	 * boolean print) { ForkJoinPool fjp = new ForkJoinPool(parallelism); long
	 * startTime = System.currentTimeMillis(); fjp.invoke(new TestAction(new
	 * Hashtable(vectors.length * 10), vectors, 0, vectors.length, print)); int
	 * totalTime = (int) (System.currentTimeMillis() - startTime); if (print)
	 * System.out
	 * .println("Benchmarking complete, time not relevant since print == true");
	 * else System.out.printf("Benchmarking complete, time: %d milliseconds%n",
	 * totalTime); }
	 */

	public static void main(String[] args) {
		Arguments arguments = CliFactory.parseArgumentsUsingInstance(
				new Arguments(), args);
		Test test = null;
		test = new Test(arguments);

		if (test != null)
			test.run();
	}

}
