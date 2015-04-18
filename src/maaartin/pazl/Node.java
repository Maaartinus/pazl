package maaartin.pazl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor public class Node<B extends Board<B>> implements Comparable<Node<B>> {
	@Override public int compareTo(Node<B> o) {
		return Double.compare(total(), o.total());
	}

	@Override public String toString() {
		return "(" + board + " " + pastCost + "+" + futureCost + ")";
	}

	private double total() {
		return pastCost + futureCost;
		//		return 0.65 * pastCost + futureCost;
		//		return futureCost;
	}

	private final B board;
	private final int pastCost;
	private final int futureCost;
}
