package unibo.as.cupido.shared.cometNotification;

import java.io.Serializable;

public class NewPlayerJoined implements Serializable {

	private static final long serialVersionUID = 1L;

	String name;

	/**
	 * isBot is true is the player is a bot; otherwise is false
	 */
	boolean isBot;

	/*
	 * player total points
	 */
	int points;

	/*
	 * If you are viewing, position=1 is the player at the owner's left, and so
	 * on position range is [1-3]
	 */
	int position;
}