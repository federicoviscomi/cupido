package unibo.as.cupido.shared.cometNotification;

import java.io.Serializable;

/**
 * Every players and every viewers in the table get this notification when game
 * ends. The game could end normally or prematurely. The last happens when
 * player creator leaves the table before normal end of the game, in this case
 * and only in this case all fileds are <code>null</code>.
 * 
 */
public class GameEnded implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * matchPoint[0] are your points, if you have played matchPoint[0] are the
	 * owner's points if you were viewing the others are in clockwise order
	 */
	public int[] matchPoints;

	/*
	 * matchPoint[0] are your points, if you have played matchPoint[0] are the
	 * owner's points if you were viewing the others are in clockwise order
	 */
	public int[] playersTotalPoints;

	public GameEnded() {
	}

	public GameEnded(int[] matchPoints, int[] playersTotalPoints) {
		this.matchPoints = matchPoints;
		this.playersTotalPoints = playersTotalPoints;
	}
}
