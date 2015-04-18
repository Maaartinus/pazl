package maaartin.pazl;

import static maaartin.pazl.FifteenBoard.*;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import junit.framework.TestCase;

import de.grajcar.dout.Dout;

@SuppressWarnings("boxing")
public class FifteenBoardTest extends TestCase {
	private static class RandomBoardGenerator {
		RandomBoardGenerator() {
			for (int i=shuffledPieces.length; i-->0; ) shuffledPieces[i] = i;
		}

		long newRandomBoardAsLong() {
			for (int i = shuffledPieces.length; i-->0; ) {
				final int index = random.nextInt(i + 1);
				final int tmp = shuffledPieces[index];
				shuffledPieces[index] = shuffledPieces[i];
				shuffledPieces[i] = tmp;
			}
			long result = 0;
			for (final int p : shuffledPieces) result = (result<<4) + p;
			return result;
		}

		FifteenBoard newRandomBoard() {
			return FifteenBoard.from(newRandomBoardAsLong());
		}

		private final Random random = new Random(42);
		private final int[] shuffledPieces = new int[16];
	}

	public void testToString() {
		for (int i=0; i<10; ++i) {
			final long indexToPiece = generator.newRandomBoardAsLong();
			final FifteenBoard board = FifteenBoard.from(indexToPiece);
			assertEquals(String.format("%016X", indexToPiece), board.toString().replaceAll("_", ""));
		}
	}

	public void testDual() {
		for (int i=0; i<10; ++i) {
			final long indexToPiece = generator.newRandomBoardAsLong();
			final long pieceToIndex = FifteenBoard.dual(indexToPiece);
			final long indexToPiece2 = FifteenBoard.dual(pieceToIndex);
			assertEquals(indexToPiece, indexToPiece2);
		}
	}

	public void testDistanceTo() {
		final List<FifteenBoard> boards = Lists.newArrayList();
		for (int i=0; i<10; ++i) boards.add(generator.newRandomBoard());
		for (final FifteenBoard b1 : boards) {
			assertEquals(0, b1.distanceTo(b1)); // coincidence
			for (final FifteenBoard b2 : boards) {
				final int expected = slowDistance(b1, b2);
				assertEquals(expected, b1.distanceTo(b2));
				assertEquals(expected, b2.distanceTo(b1)); // symmetry
			}
		}
	}

	private int slowDistance(FifteenBoard b1, FifteenBoard b2) {
		int result = 0;
		for (int i=1; i<16; ++i) {
			final int index1 = b1.pieceToIndex(i);
			final int index2 = b2.pieceToIndex(i);
			final int colDiff = Math.abs(indexToCol(index1) - indexToCol(index2));
			final int rowDiff = Math.abs(indexToRow(index1) - indexToRow(index2));
			result += colDiff + rowDiff;
		}
		return result;
	}

	public void testAlternative() {
		for (int i=0; i<10; ++i) {
			final FifteenBoard b = generator.newRandomBoard();
			final FifteenBoard b2 = b.alternative();
			assertFalse(b.equals(b2));
			assertTrue(b.distanceTo(b2) >= 2);
			assertEquals(b, b2.alternative()); //idempotency
		}
	}

	public void testChildren() {
		final FifteenBoard board = STANDARD_BOARD;
		assertEquals(0, board.distanceTo(board));
		Dout.a(board);

		final Collection<FifteenBoard> children = Sets.newHashSet(board.children());
		assertEquals(2, children.size());
		for (final FifteenBoard b : children) {
			assertEquals(1, b.distanceTo(board));
			for (final FifteenBoard b2 : children) {
				assertEquals(b==b2 ? 0 : 2, b2.distanceTo(b));
			}
		}

		final Collection<FifteenBoard> grandChildren = Sets.newHashSet();
		for (final FifteenBoard b : children) grandChildren.addAll(b.children());
		assertEquals(5, grandChildren.size());

		for (final FifteenBoard b : grandChildren) {
			assertEquals(b.equals(board) ? 0 : 2, b.distanceTo(board));
			for (final FifteenBoard b2 : grandChildren) {
				assertTrue(b2.distanceTo(b) <= 4);
			}
		}
	}

	private final RandomBoardGenerator generator = new RandomBoardGenerator();

	private static final FifteenBoard STANDARD_BOARD = FifteenBoard.from(0x0123_4567_89AB_CDEFL);
}
