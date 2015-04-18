package maaartin.pazl;

import de.grajcar.dout.Dout;

/**
 * Solver for the 4x4 sliding puzzle.
 * The goal is to find an optimal solution, so hacky optimizations are needed.
 * The board is represented by a long containing 4x4 nibbles.
 */
public class FifteenPazlDemo {
	public static void main(String[] args) {
		Dout.a("STARTED");
		final Solver<FifteenBoard> solver = Solver.create(START_BOARD, END_BOARD, new Reporter());
		solver.solve();
		Dout.a("DONE");
	}

	private static final FifteenBoard START_BOARD = FifteenBoard.from(0x287B_504F_D9E3_1A6CL);
	private static final FifteenBoard END_BOARD = FifteenBoard.from(0x1234_5678_9ABC_DEF0L);
}
