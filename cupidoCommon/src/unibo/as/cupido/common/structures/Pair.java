package unibo.as.cupido.common.structures;

import java.io.Serializable;

public class Pair<T1, T2> implements Serializable {

	private static final long serialVersionUID = 1L;

	public T1 first;

	public T2 second;

	public Pair() {
	}

	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public String toString() {
		return "[" + first + ", " + second + "]";
	}
}
