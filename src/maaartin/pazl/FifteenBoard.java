package maaartin.pazl;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedLongs;


/**
 * Represents the standard 4x4 board.
 *
 * <p>Terminology:<ul>
 * <li>Index is a number between 0 and 15 denoting the position on the board.
 * <li>Piece is a number between 0 and 15 with 0 denoting the empty space.
 */
public class FifteenBoard extends Board<FifteenBoard> {
	/**
	 * Create a new board by interpreting the input as list of pieces. Accepts two formats:<ul>
	 *
	 * <li>List of sixteen space-separated decimal numbers like
	 *     {@code "11 15 12 0 14 10 2 13 7 6 9 8 3 5 4 1"}
	 * <li>List of four underscore-separated groups of four hexadecimal digits like
	 *     {@code "0123_4567_89AB_CDEF"}
	 *
	 */
	static FifteenBoard from(String input) {
		checkArgument(INPUT_STRING_PATTERN.matcher(input).matches());
		return FifteenBoard.from(parseStringToLong(input));
	}

	/**
	 * Create a new board by interpreting every digit of the argument as the piece on the corresponding index,
	 * where 0 denotes the empty place.
	 * See also {@link #toString()}.
	 */
	static FifteenBoard from(long indexToPiece) {
		checkArgument(isValidBoard(indexToPiece));
		return new FifteenBoard(indexToPiece, dual(indexToPiece));
	}

	private FifteenBoard(long indexToPiece, long pieceToIndex) {
		assert pieceToIndex == dual(indexToPiece);
		this.indexToPiece = indexToPiece;
		this.pieceToIndex = pieceToIndex;
	}

	private static long parseStringToLong(String input) {
		long result = 0;
		if (input.contains(" ")) {
			final List<String> split = Splitter.on(" ").splitToList(input);
			checkArgument(split.size() == 16);
			for (final String s : split) {
				final int n = Integer.parseInt(s);
				checkArgument(0<=n && n<16);
				result = (result << 4) + n;
			}
		} else {
			return UnsignedLongs.parseUnsignedLong(input.replaceAll("_", ""));
		}
		return result;
	}

	@VisibleForTesting static int indexToRow(int index) {
		assert 0 <= index && index < INDEX_LIMIT;
		return index & 3;
	}

	@VisibleForTesting static int indexToCol(int index) {
		assert 0 <= index && index < INDEX_LIMIT;
		return index >> 2;
	}

	@VisibleForTesting static int toIndex(int col, int row) {
		assert 0 <= col && col < SIZE;
		assert 0 <= row && row < SIZE;
		return 4*col + row;
	}

	@VisibleForTesting static long dual(long data) {
		long result = 0;
		for (int index=0; index<SIZE*SIZE; ++index) result += (long) index << (4 * get(data, index));
		assert isValidBoard(result);
		return result;
	}

	/** Return true if {@code data} in hexadecimal contain all hexadecimal digits. */
	private static boolean isValidBoard(long data) {
		int bitset = 0;
		for (int index=0; index<SIZE*SIZE; ++index) bitset |= 1 << get(data, index);
		return bitset == 0xFFFF;
	}

	/**
	 * Return a string representation of {@code this}, consisting of 4 groups of 4 hexadecimal digits.
	 * The groups are separated by an underscore and each corresponds with a puzzle row.
	 * Every digit corresponds with a piece, with 0 denoting the empty position.
	 */
	@SuppressWarnings("boxing") @Override public String toString() {
		return String.format("%04X_%04X_%04X_%04X",
				(indexToPiece>>48) & 0xFFFF,
				(indexToPiece>>32) & 0xFFFF,
				(indexToPiece>>16) & 0xFFFF,
				(indexToPiece>>00) & 0xFFFF);
	}

	@Override public boolean equals(Object obj) {
		if (!(obj instanceof FifteenBoard)) return false;
		// The other field can be ignored as it's the dual.
		return indexToPiece == ((FifteenBoard) obj).indexToPiece;
	}

	@Override public int hashCode() {
		// This may be a premature optimization, but something like Long.hashCode might lead to too many collisions.
		final long result = (123456789 * indexToPiece);
		return Longs.hashCode(result);
	}

	@Override public int distanceTo(FifteenBoard other) {
		// Every pair of bits in x and y is one coordinate.
		// The coordinates of the empty space don't matter and therefore get excluded via & ~15.
		// For all others, we compute the sum of absolute values of their differences.
		// See http://codereview.stackexchange.com/a/86907/14363.
		final long x = pieceToIndex  & ~15;
		final long y = other.pieceToIndex & ~15;
		final long xor = x^y;
		// High bit per pair will contain whether the pair is 3, low bit is garbled.
		final long is3 = xor & (xor << 1);

		// High bit per pair will contain whether the pair is non-zero, low bit is garbled.
		final long x2 = x | (x << 1);
		final long y2 = y | (y << 1);

		// High bit per pair will contain whether both pairs are non-zero, low bit is garbled.
		final long is0 = x2 & y2;

		// High bit per pair will contain whether the pairs need correction, low bit is 0.
		final long isBoth = (is3 & is0) & HIGH;
		final long val = xor ^ isBoth; // only invert the bits set in both is3 and is0

		// Count the high bits twice and the low bits ones.
		return Long.bitCount(val) + Long.bitCount(val & HIGH);
	}

	@Override public Collection<FifteenBoard> children() {
		return addChildrenTo(Lists.<FifteenBoard>newArrayListWithCapacity(4));
	}

	private Collection<FifteenBoard> addChildrenTo(Collection<FifteenBoard> result) {
		final int emptyIndex = pieceToIndex(0);
		final int col = indexToCol(emptyIndex);
		final int row = indexToRow(emptyIndex);
		if (col > 0) result.add(swap(emptyIndex, emptyIndex-4));
		if (col < SIZE-1) result.add(swap(emptyIndex, emptyIndex+4));
		if (row > 0) result.add(swap(emptyIndex, emptyIndex-1));
		if (row < SIZE-1) result.add(swap(emptyIndex, emptyIndex+1));
		return result;
	}

	@Override public FifteenBoard alternative() {
		// Swap the first two non-empty positions.
		final int index1 = indexToPiece(0) == 0 ? 2 : 0;
		final int index2 = indexToPiece(1) == 0 ? 2 : 1;
		return swap(index1, index2);
	}

	/**
	 * Swap the two pieces at the indexes given by the arguments.
	 *
	 * <p>This is a valid move iff the indexes correspond to neighboring positions and one of the positions is empty.
	 */
	private FifteenBoard swap(int index1, int index2) {
		final long piece1 = indexToPiece(index1);
		final long piece2 = indexToPiece(index2);
		final long pieceXor = piece1 ^ piece2;
		final long childIndexToPiece = indexToPiece ^ (pieceXor << 4*index1) ^ (pieceXor << 4*index2);
		final long indexXor = index2 ^ index1;
		final long childPieceToIndex = pieceToIndex ^ (indexXor << 4*piece1) ^ (indexXor << 4*piece2);
		return new FifteenBoard(childIndexToPiece, childPieceToIndex);
	}

	@VisibleForTesting int indexToPiece(int index) {
		return get(indexToPiece, index);
	}

	@VisibleForTesting int pieceToIndex(int piece) {
		return get(pieceToIndex, piece);
	}

	private static int get(long data, int index) {
		return (int) ((data >>> (4*index)) & 0xF);
	}

	private static final long HIGH = 0xAAAAAAAAAAAAAAAAL;
	private static final Pattern INPUT_STRING_PATTERN = Pattern.compile("(\\d+ ){15}\\d+|(\\w{4}_){3}\\w{4}");

	private static final int SIZE = 4;
	private static final int INDEX_LIMIT = SIZE*SIZE;

	@VisibleForTesting final long indexToPiece;
	@VisibleForTesting final long pieceToIndex;
}
