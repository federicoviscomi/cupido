package unibo.as.cupido.backendInterfaces.common;

/**
 * InitialTableStatus is the status of the game before the cards are dealt (may
 * have less than 4 player)
 */
public class InitialTableStatus {

	public InitialTableStatus(String[] opponents, int[] playerPoints, boolean[] whoIsBot) {
		this.opponents = opponents;
		this.playerPoints = playerPoints;
		this.whoIsBot = whoIsBot;
	}

	/**
	 * Opponents are sorted clockwise (game is clockwise) opponents.lenght is
	 * always 3 opponents[i]==null means there is no i-th player opponents[0] is
	 * the player at your left, and so on...
	 */
	public String[] opponents;

	/**
	 * (global) points of all the player playerPoints[0] are you,
	 * playerPoints[1] is the player at your left, and so on
	 */
	public int[] playerPoints;

	/**
	 * if opponents[i]==null then whoIsBot[i] has no meaning. if opponents[i]!=
	 * null then whoIsBot[i] is true if the player i is a bot, otherwise is
	 * false
	 * 
	 * 
	 */
	public boolean[] whoIsBot;
}