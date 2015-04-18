package maaartin.pazl;

import java.util.Map;
import java.util.PriorityQueue;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

@RequiredArgsConstructor(access=AccessLevel.PRIVATE) class Solver<B extends Board<B>> {
	public static <B extends Board<B>> Solver<B> create(B startBoard, B endBoard, Reporter reporter) {
		return new Solver<B>(startBoard, endBoard, endBoard.alternative(), reporter);
	}

	void solve() {
		double minFutureCost = Double.MAX_VALUE;
		queue.add(new Node<B>(startBoard, 0, startBoard.distanceTo(endBoard)));
		for (long step=0; ; ++step) {
			final Node<B> node = queue.poll();
			if (node==null) return;
			final boolean isSolution = node.board().equals(endBoard) || node.board().equals(alternativeEndBoard);
			if (isSolution) {
				reporter.report(EventType.DONE, step, node);
			} else if (node.futureCost() < minFutureCost) {
				minFutureCost = node.futureCost();
				reporter.report(EventType.CLOSER, step, node);
			} else {
				reporter.report(EventType.NOTHING, step, node);
			}
			if (isSolution) return;
			for (final B b : node.board().children()) {
				final Node<B> oldNode = map.get(b);
				if (oldNode==null) {
					final int childPastCost = node.pastCost() + 1;
					final int childFutureCost = b.distanceTo(endBoard);
					final Node<B> childNode = new Node<B>(b, childPastCost, childFutureCost);
					queue.add(childNode);
					map.put(b, childNode);
				}
			}
		}
	}

	private final B startBoard;
	private final B endBoard;
	private final B alternativeEndBoard;

	private final Reporter reporter;

	private final PriorityQueue<Node<B>> queue = Queues.newPriorityQueue();
	private final Map<B, Node<B>> map = Maps.newHashMap();
}
