package nl.utwente.csc.fmt.locklesshashtable.test;

import nl.utwente.csc.fmt.locklesshashtable.generalhashtable.NonBlockingHashtable;

import java.util.Map.Entry;

public class OldTest {
	public static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();
	public static final int NUMBER_OF_THREADS = 7;
	
	public static void testGenericHashtable(){

		NonBlockingHashtable<Integer, Character> ht = new NonBlockingHashtable();
		
		class TestThread extends Thread {
			private NonBlockingHashtable<Integer, Character> ht;
			private int start, end;

			TestThread(NonBlockingHashtable<Integer, Character> ht, int start, int end) {
				this.ht = ht;
				this.start = start;
				this.end = end;
				
			}

			@Override
			public void run() {
				for(int i = start; i < end; i++){
					ht.put(i, ALPHABET[i]);
					System.out.printf("Thread %d puts (%d, %s)%n", getId(), i, ALPHABET[i]);
				}
				
				for(Entry<Integer, Character> entry: ht.entrySet()){
					System.out.printf("Thread %d gets (%d, %s)%n", getId(), entry.getKey(), entry.getValue());
				}

			}

		}
		
		int size = 26 / NUMBER_OF_THREADS;
		for(int i = 0; i < NUMBER_OF_THREADS; i++){
			new TestThread(ht,i * size , (i + 1) * size).start();
		}
	}
	
	/*
	public static void testSPEHashtable(){
		reducedhashtable.NonBlockingHashtable<Integer, Character> ht = new reducedhashtable.NonBlockingHashtable<Integer, Character>();
		
		class TestThread extends Thread {
			private reducedhashtable.NonBlockingHashtable<Integer, Character> ht;

			TestThread(reducedhashtable.NonBlockingHashtable<Integer, Character> ht) {
				this.ht = ht;
				
			}

			@Override
			public void run() {
				int start = (int) (Math.random() * 26);
				for(int i = 0; i < 10; i ++) {
					int index = (i + start) % 26;
					boolean result = ht.findOrPut(index, ALPHABET[index]) ;
					System.out.printf("Thread %d (%d, %s), result = %b%n", getId(), index, ALPHABET[index], result);
				}
			}
		}
	
		int size = 26 / NUMBER_OF_THREADS;
		for(int i = 0; i < NUMBER_OF_THREADS; i++){
			new TestThread(ht).start();
		}
		
	}
	*/
	public static void main(String[] args) {
		// testSPEHashtable();
	}

}
