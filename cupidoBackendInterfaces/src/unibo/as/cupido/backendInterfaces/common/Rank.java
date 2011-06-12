package unibo.as.cupido.backendInterfaces.common;

import java.util.ArrayList;
import java.util.Iterator;

import unibo.as.cupido.backendInterfaces.common.Rank.RankEntry;

public class Rank implements Iterable<RankEntry> {
	public static class RankEntry {
		String userName;
		int score;
		int rank;
	}

	private ArrayList<RankEntry> rank;

	@Override
	public Iterator<RankEntry> iterator() {
		return rank.iterator();
	}
}
