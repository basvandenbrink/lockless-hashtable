package nl.utwente.csc.fmt.locklesshashtable.spehashtable;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicIntegerArray;

// import java.lang.reflect.Field;
import sun.misc.Unsafe;

public class Hashtable {

	private static final Unsafe unsafe = UtilUnsafe.getUnsafe();
	private static final int oBase = unsafe.arrayBaseOffset(int[].class);
	private static final int oScale = unsafe.arrayIndexScale(int[].class);

	private static long rawIndex(final int[][] ary, final int idx) {
		assert idx >= 0 && idx < ary.length;
		return oBase + idx * oScale;
	}

	/*
	 * // --- Setup to use Unsafe private static final long hvsOffset; static {
	 * // <clinit> Field f = null; try { f =
	 * Hashtable.class.getDeclaredField("hvs"); } catch
	 * (java.lang.NoSuchFieldException e) { throw new RuntimeException(e); }
	 * hvsOffset = unsafe.objectFieldOffset(f); }
	 */

	// --- hash ----------------------------------------------------------------
	// Helper function to spread lousy hashCodes
	private static final int hash(final int[] vector) {
		int h = Arrays.hashCode(vector); // The real hashCode call
		// Spread bits to regularize both segment and index locations,
		// using variant of single-word Wang/Jenkins hash.
		h += (h << 15) ^ 0xffffcd7d;
		h ^= (h >>> 10);
		h += (h << 3);
		h ^= (h >>> 6);
		h += (h << 2) + (h << 14);
		return h ^ (h >>> 16);
	}

	// hvs[0] contains hashes, other locations vectors
	private int[][] hvs; // Does not have to be declared as volatile when using
							// on x86 or SPARC hardware, since CAS then takes
							// care of reordering
	
	private AtomicIntegerArray hashes;

	// Maximum number of values in the table, first location contains hashes.
	private static final int len(int[][] values) {
		return values.length;
	}

	// --- Minimum table size ----------------
	// Pick size 8 K/V pairs, which turns into (8+1)*4+12 = 48 bytes on a
	// standard 32-bit HotSpot, and (8+1)*8+12 = 84 bytes on 64-bit Azul.
	private static final int MIN_SIZE_LOG = 3; //
	private static final int MIN_SIZE = (1 << MIN_SIZE_LOG); // Must be power of
																// 2

	// Note that these are static, so that the caller is forced to read the _kvs
	// field only once, and share that read across all key/val calls - lest the
	// _kvs field move out from under us and back-to-back key & val calls refer
	// to different _kvs arrays.
	private static final int[] val(int[][] hvs, int idx) {
		return hvs[idx];
	}
	
	private static final boolean CAS_val(int[][] hvs, int idx, int[] old,
			int[] vector) {
		return unsafe.compareAndSwapObject(hvs, rawIndex(hvs, idx), old,
				vector);
	}

	/**
	 * Create a new NonBlockingHashtable with default minimum size (currently
	 * set to 8 K/V pairs or roughly 84 bytes on a standard 32-bit JVM).
	 */
	public Hashtable() {
		this(MIN_SIZE);
	}

	/**
	 * Create a new NonBlockingHashtable with initial room for the given number
	 * of elements, thus avoiding internal resizing operations to reach an
	 * appropriate size. Large numbers here when used with a small count of
	 * elements will sacrifice space for a small amount of time gained. The
	 * initial size will be rounded up internally to the next larger power of 2.
	 */
	public Hashtable(final int initial_sz) {
		initialize(initial_sz);
	}

	private final void initialize(int initial_sz) {
		if (initial_sz < 0)
			throw new IllegalArgumentException();
		int i; // Convert to next largest power-of-2
		if (initial_sz > 1024 * 1024)
			initial_sz = 1024 * 1024;
		for (i = MIN_SIZE_LOG; (1 << i) < (initial_sz << 1); i++)
			;
		hvs = new int[1 << i][];
		hashes = new AtomicIntegerArray(1 << i);
	}

	// Version for subclassed readObject calls, to be called after the
	// defaultReadObject
	protected final void initialize() {
		initialize(MIN_SIZE);
	}

	private static boolean valEq(int[] vector1, int[] vector2, AtomicIntegerArray hashes,
			int hash, int fullHash) {
		int storedHash = hashes.get(hash);
		return vector1 == vector2 || // Either vectors match exactly OR
				// hash exists and matches? hash can be zero during the install
				// of a
				// new vector.
				((storedHash == 0 || storedHash == fullHash) &&
				// Do the match the hard way - with the users' key being the
				// loop-
				// invariant "this" pointer. I could have flipped the order of
				// operands (since equals is commutative), but I'm making
				// mega-morphic
				// v-calls in a reprobing loop and nailing down the 'this'
				// argument
				// gives both the JIT and the hardware a chance to prefetch the
				// call target.
				Arrays.equals(vector1, vector2)); // Finally do the hard match
	}

	public boolean lookup(int[] vector) {
		final int fullhash = hash(vector); // throws NullPointerException if key
											// null
		final int len = len(hvs); // Count of key/value pairs, reads kvs.length
		int idx = fullhash & (len - 1);

		int[] v = null;
		while (true) {
			v = val(hvs, idx);

			if (v == null) {
				if (CAS_val(hvs, idx, null, vector)) {
					hashes.set(idx, fullhash);
					return false;
				}
			}

			// CAS failed
			v = val(hvs, idx);
			while(hashes.get(idx) == 0); //spinlock until hash value is set
			if (valEq(vector, v, hashes, idx, fullhash)) {
				// vector found
				return true;
			}

			idx = (idx + 1) & (len - 1);
		}
	}
}
