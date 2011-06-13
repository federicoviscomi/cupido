package unibo.as.cupido.backendInterfaces.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

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
