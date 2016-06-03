package nl.utwente.csc.fmt.locklesshashtable.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tools{

	public static void removeDuplicates(File input, File output) throws IOException {
		Set<List<Integer>> vectorSet = new HashSet<List<Integer>>();
		int[][] vectors = Test.readVectors(new FileInputStream(input));
		for (int[] vector : vectors) {
			List<Integer> vectorList = new ArrayList<Integer>();
			for (int i : vector) {
				vectorList.add(i);
			}
			vectorSet.add(vectorList);
		}

		PrintStream pOut = new PrintStream(output);

		for (List<Integer> vector : vectorSet) {
			if (vector.size() > 0) {
				pOut.print(vector.get(0));

				for (int i = 1; i < vector.size(); i++) {
					pOut.print(',');
					pOut.print(vector.get(i));
				}

				pOut.println();
			}
		}
		pOut.close();
	}

	public static void main(String[] args) throws IOException {
		int[] a1 = new int[]{5,7,3,2,1};
		int[] a2 = new int[]{5,7,3,2,1};
		System.out.printf("a1 == a2: %b%n", a1 == a2);
		System.out.printf("a1 equals a2: %b%n", Arrays.equals(a1, a2));
	}
}
