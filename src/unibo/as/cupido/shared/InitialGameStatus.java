package unibo.as.cupido.shared;

/*
 * InitialGameStatus is the status of the game before the cards are dealt (may have less than 4 player)
 */
public class InitialGameStatus {
	
	/*
	 * Opponents are sorted clockwise (game is clockwise)
	 * opponents.lenght is always 3
	 * opponents[i]==null means there is no i-th player
	 * opponents[1] is the player at your left, and so on...
	 */
	public String[] opponents;
	
	/*
	 * return (global) points of all the player
	 * playerPoints[0] are you, playerPoints[1] is the player at your left, and so on
	 */
	public int[] playerPoints;


}