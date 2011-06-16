package unibo.as.cupido.backendInterfaces.common;

import java.io.Serializable;

/**
 * 
 * Contains the information that an observer needs when he joins a table
 * 
 * @author cane
 * 
 */
public class ObservedGameStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	public PlayerStatus[] playerStatus;

	/**
	 * The index of the first player that dealt a card in the current trick.
	 * When this is 0, the bottom player led the trick, when it's 1 the trick
	 * was led by the left player, and so on for other players, in clockwise
	 * order.
	 * 
	 * If there is currently no trick, this is -1. This can happen in three
	 * cases: when some players are still missing in the table, when players are
	 * passing cards, when the players have passed cards but no-one has dealt
	 * the two of clubs yet.
	 */
	public int firstDealerInTrick;

	public ObservedGameStatus() {
		playerStatus = new PlayerStatus[4];
	}

	public ObservedGameStatus(PlayerStatus[] ogs, int firstDealerInTrick) {
		this.playerStatus = ogs;
		this.firstDealerInTrick = firstDealerInTrick;
	}
}
