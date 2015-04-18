package maaartin.pazl;

import static com.google.common.base.Verify.verify;

import java.util.Random;

import de.grajcar.dout.Dout;

public class FunnySumBenchmark {
	FunnySumBenchmark() {
		final Random random = new Random(0);
		for (int i=0; i<data.length; ++i) data[i] = random.nextLong();
	}

	public static void main(String[] args) {
		Dout.a("STARTED");
		new FunnySumBenchmark().go();
		Dout.a("DONE");
	}

	private void go() {
		verify(timeShiftySum() == timeFunnySum());
		verify(timeMaaartySum() == timeFunnySum());

		final int n = 1000;
		for (int i=0; i<n; ++i) blackhole += timeFunnySum();
		for (int i=0; i<n; ++i) blackhole += timeShiftySum();
		for (int i=0; i<n; ++i) blackhole += timeFreakySum();
		for (int i=0; i<n; ++i) blackhole += timeMaaartySum();

		start = System.nanoTime();
		for (int i=0; i<n; ++i) blackhole += timeFunnySum();
		after("funny");

		start = System.nanoTime();
		for (int i=0; i<n; ++i) blackhole += timeFreakySum();
		after("freaky");

		start = System.nanoTime();
		for (int i=0; i<n; ++i) blackhole += timeShiftySum();
		after("shifty");

		start = System.nanoTime();
		for (int i=0; i<n; ++i) blackhole += timeMaaartySum();
		after("maaarty");
	}

	void before() {
		start = System.nanoTime();
	}

	@SuppressWarnings("boxing") private void after(String name) {
		System.out.format("%-20s %6.3f\n", name, 1e-9 * (System.nanoTime() - start));
	}

	int timeFunnySum() {
		int result = 0;
		for (int i=0; i<data.length; ++i) {
			for (int j=0; j<data.length; ++j) {
				result += funnySum(data[i], data[j]);
			}
		}
		return result;
	}

	int timeFreakySum() {
		int result = 0;
		for (int i=0; i<data.length; ++i) {
			for (int j=0; j<data.length; ++j) {
				result += freakySum(data[i], data[j]);
			}
		}
		return result;
	}

	int timeShiftySum() {
		int result = 0;
		for (int i=0; i<data.length; ++i) {
			for (int j=0; j<data.length; ++j) {
				result += shiftySum(data[i], data[j]);
			}
		}
		return result;
	}

	int timeMaaartySum() {
		int result = 0;
		for (int i=0; i<data.length; ++i) {
			for (int j=0; j<data.length; ++j) {
				result += maaartySum(data[i], data[j]);
			}
		}
		return result;
	}

	int funnySum(long x, long y) {
		int result = 0;
		for (int i=0; i<32; ++i, x>>=2, y>>=2) {
			final int xNum = (int) (x & 3);
			final int yNum = (int) (y & 3);
			result += Math.abs(xNum - yNum);
		}
		return result;
	}

	int freakySum(long x, long y) {
		long result = x^y;
		long is3 = result & result << 1; //high bit per pair will contain whether the pair is 3
		is3 &= 0xaaaa_aaaa_aaaa_aaaaL; //extract the high bit per pair
		long is0 = x&y;
		is0 = ~(is0 | is0 << 1);//high bit per pair will contain whether the pair is 0
		is0 &= 0xaaaa_aaaa_aaaa_aaaaL; //extract the high bit per pair
		result ^= is3 & is0; // only invert the bits set in both is3 and is0

		result = (result & 0x3333_3333_3333_3333L) + ((result >>  2) & 0x3333_3333_3333_3333L);
		result = (result & 0x0f0f_0f0f_0f0f_0f0fL) + ((result >>  4) & 0x0f0f_0f0f_0f0f_0f0fL);
		result = (result & 0x00ff_00ff_00ff_00ffL) + ((result >>  8) & 0x00ff_00ff_00ff_00ffL);
		result = (result & 0x0000_ffff_0000_ffffL) + ((result >> 16) & 0x0000_ffff_0000_ffffL);
		result = (result & 0x0000_0000_ffff_ffffL) + ((result >> 32) & 0x0000_0000_ffff_ffffL);

		return (int) result;
	}

	private static final long COMP      = 0x1111111111111111L;
	private static final long TWOBITS   = 0x3333333333333333L;
	private static final long THREEBITS = 0x7777777777777777L;
	private static final long SIGNBIT   = 0x4444444444444444L;
	private static final long FOURSET   = 0x0f0f0f0f0f0f0f0fL;

	long twoBitDiff(final long x, final long y) {
		// this method only works on alternating 2-bit blocks.
		// it masks out the ignored bits with the TWOBITS mask.

		// two's compliment on 2-bit sets of y - to make 3bit values.
		// third bit is the sign bit.
		long tc = y & TWOBITS;
		tc ^= THREEBITS;
		tc += COMP;
		tc &= THREEBITS;

		long diff =  (x & TWOBITS) + tc;

		// there may be an overflow in to the 4th bit. Remove it.
		diff &= THREEBITS;

		// negate any negative results - (abs value)
		// two's complement again
		// the sign -bit will be 0 for positive results,
		// thus the following will not change anything for positive values.
		final long sign = (diff & SIGNBIT) >>> 2;
		diff ^= sign;
		diff ^= (sign << 1);
		diff += sign;
		return diff & TWOBITS;
	}

	int shiftySum(long x, long y) {
		final long adiff = twoBitDiff(x, y);
		final long bdiff = twoBitDiff(x >>> 2, y >>> 2);
		final long foursum = adiff + bdiff;

		// max sum of diff is x=0xffffffffffffffff and y = 0x0000000000000000
		// which is 3 * 32 -> 96. 96 is represented in 8 bits...
		// so, we make clever with our first sum. Ensure no values in
		// the 8-bit result space other than what belongs by `& FOURSET`

		final long eightsum    = (foursum  + (foursum >>> 4)) & FOURSET;

		// after this, we don't care about masking off mid-order bits
		final long sixteensum  = eightsum + (eightsum >>> 32);
		final long threetwosum = sixteensum + (sixteensum >>> 16);
		final long lastsum     = threetwosum + (threetwosum >>> 8);

		return (int)(lastsum & 0xffL);
	}

	private static final long LOW = 0x5555555555555555L;
	private static final long HIGH = ~LOW;

	int maaartySum(long x, long y) {
		final long xor = x^y;
		// high bit per pair will contain whether the pair is 3, low bit is garbled
		final long is3 = xor & (xor << 1);

		// high bit per pair will contain whether the pair is non-zero, low bit is garbled
		final long x2 = x | (x << 1);
		final long y2 = y | (y << 1);

		// high bit per pair will contain whether both pairs are non-zero, low bit is garbled
		final long is0 = x2 & y2;

		// high bit per pair will contain whether the pairs need correction, low bit is 0
		final long isBoth = (is3 & is0) & HIGH;
		final long val = xor ^ isBoth; // only invert the bits set in both is3 and is0

		// count the high bits twice
		return Long.bitCount(val) + Long.bitCount(val & HIGH);
	}


	public volatile long blackhole;
	private long start;

	private final long[] data = new long[1000];
}
