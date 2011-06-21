package unibo.as.cupido.common.structures;

import java.io.Serializable;

public class RankingEntry implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String username;
	public int rank;
	public int points;
	public RankingEntry(){
	}
	public RankingEntry(String username, int rank, int points){
		this.username=username;
		this.rank=rank;
		this.points=points;
	}
}
