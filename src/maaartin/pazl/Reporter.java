package maaartin.pazl;

public class Reporter {
	public void report(EventType type, long step, Node<?> node) {
		if (type==EventType.NOTHING && step<nextStep) return;
		doOutput(type, step, node);
		nextStep = PERIOD * (step / PERIOD + 1);
	}

	@SuppressWarnings("boxing")
	private void doOutput(EventType type, long step, Node<?> node) {
		final int cost = node.pastCost() + node.futureCost();
		System.out.format("%-10s %12d %-27s %2d\n", type, step, node, cost);
	}

	private static final long PERIOD = 1000_000;

	private long nextStep;
}
