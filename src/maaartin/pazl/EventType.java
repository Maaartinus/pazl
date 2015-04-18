package maaartin.pazl;


public enum EventType {
	NOTHING,
	CLOSER,
	LOWER_BOUND,
	UPPER_BOUND,
	DONE,
	;

	@Override public String toString() {
		switch (this) {
			case NOTHING: return "-";
			case CLOSER: return "C";
			default: return name();
		}
	}
}
