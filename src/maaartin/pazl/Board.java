package maaartin.pazl;

import java.util.Collection;

/** Represents a Slidig Puzzle Board. All instances must be immutable. */
public abstract class Board<B extends Board<B>> {
	/**
	 * Return the manhattan distance between {@code this} and {@code other},
	 * which gets computed as the sum over all pieces (ignoring the empty field).
	 *
	 * <p>Note that this is a valid lower bound on the necessary number of steps.
	 */
	public abstract int distanceTo(B other);

	/** Return the 2-4 children obtained by moving a neighboring piece to the empty field. */
	public abstract Collection<B> children();

	/**
	 *  Return a board differing by a single swap. This gets used for nearly-solving unsolvable problems.
	 *
	 *  <p>The operation must be self-inverse.
	 */
	public abstract B alternative();
}
