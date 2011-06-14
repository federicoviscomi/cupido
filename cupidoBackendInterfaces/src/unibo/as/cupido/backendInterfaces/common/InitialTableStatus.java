package unibo.as.cupido.backendInterfaces.common;

import java.io.Serializable;

/**
 * InitialTableStatus is the status of the game before the cards are dealt (may
 * have less than 4 player)
 */
public class InitialTableStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * opponents.length is 3. opponents[i] is the name of the opponent i
	 * positions next to you in clockwise order. A <code>null</code> value
	 * indicates that opponent does not exist yet
	 */
	public String[] opponents;

	/**
	 * playerScores.length is 3. playerScores[i] is the score of the opponent i
	 * positions next to you in clockwise order. A <code>null</code> value
	 * indicates that opponent does not exist yet.
	 */
	public int[] playerScores;

	/**
	 * whoIsBot.length is 3. whoIsBot[i] is the score of the opponent i
	 * positions next to you in clockwise order. A <code>null</code> value
	 * indicates that opponent does not exist yet
	 */
	public boolean[] whoIsBot;

	public InitialTableStatus() {
	}

	public InitialTableStatus(String[] opponents, int[] playerScores,
			boolean[] whoIsBot) {
		this.opponents = opponents;
		this.playerScores = playerScores;
		this.whoIsBot = whoIsBot;
	}
}