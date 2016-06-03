package nl.utwente.csc.fmt.locklesshashtable.singlecell;

import java.util.concurrent.atomic.AtomicInteger;

public class SingleCell {

	public static final int E = 0, W = 1, D = 2;
	public static final int PUT = 0, SEEN = 1, COLN = 2;
	private AtomicInteger sync;
	private int data;

	SingleCell() {
		sync = new AtomicInteger(E);
	}

	int findOrPut(int v) {
		if (sync.compareAndSet(E, W)) {
			data = v;
			sync.set(D);
			return PUT;
		} else {
			while (sync.get() == W) { // every call of sync.get() updates the cache
			}
			if (sync.get() == D) {
				if (data == v)
					return SEEN;
				else
					return COLN;
			}
		}
		return -1;
	}

	public static void main(String[] args) {
		SingleCell scell = new SingleCell();
		class Test extends Thread {
			SingleCell scell;

			Test(SingleCell scell) {
				this.scell = scell;
			}

			@Override
			public void run() {
				System.out.printf(
						"Thread id: %d%nResult findOrPut (42) : %d%n", getId(),
						scell.findOrPut(42));

			}

		}

		for (int i = 0; i < 15; i++) {
			(new Test(scell)).start();
		}

	}

}
