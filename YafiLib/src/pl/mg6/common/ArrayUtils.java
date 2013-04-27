package pl.mg6.common;

public class ArrayUtils {
	
	private ArrayUtils() {
	}
	
	public static int removeFromArray(final byte remove, final byte[] array, final int offset, final int count) {
		int removedCount = 0;
		int putOffset = offset;
		while (putOffset < offset + count && array[putOffset] != remove) {
			putOffset++;
		}
		int getOffset = putOffset;
		while (getOffset < offset + count) {
			while (getOffset < offset + count && array[getOffset] == remove) {
				getOffset++;
				removedCount++;
			}
			if (getOffset < offset + count) {
				array[putOffset] = array[getOffset];
				putOffset++;
				getOffset++;
			}
		}
		return removedCount;
	}
}
