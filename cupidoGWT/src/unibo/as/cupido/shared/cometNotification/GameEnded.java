package unibo.as.cupido.shared.cometNotification;

import java.io.Serializable;

/*
 * game may finish if the the owner leaves or the match is complete
 */
public class GameEnded implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * matchPoint[0] are you points, if you have played matchPoint[0] are the
	 * owner's points if you were viewing the others are in clockwise order
	 */
	public int[] matchPoints;

	/*
	 * matchPoint[0] are your points, if you have played matchPoint[0] are the
	 * owner's points if you were viewing the others are in clockwise order
	 */
	public int[] playersTotalPoints;
}
