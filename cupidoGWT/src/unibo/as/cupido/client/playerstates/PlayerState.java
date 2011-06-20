package unibo.as.cupido.client.playerstates;

import unibo.as.cupido.common.structures.Card;

public interface PlayerState {

	public void disableControls();
	
	/**
	 * Returns false if this event can't be handled right now, but it can
	 * be handled in a later state. It will be notified again at each state
	 * transition, until it is handled.
	 */
	public boolean handleCardPassed(Card[] cards);

	/**
	 * Returns false if this event can't be handled right now, but it can
	 * be handled in a later state. It will be notified again at each state
	 * transition, until it is handled.
	 */
	public boolean handleCardPlayed(Card card, int playerPosition);

	/**
	 * Returns false if this event can't be handled right now, but it can
	 * be handled in a later state. It will be notified again at each state
	 * transition, until it is handled.
	 */
	public boolean handleGameEnded(int[] matchPoints, int[] playersTotalPoints);

	/**
	 * Returns false if this event can't be handled right now, but it can
	 * be handled in a later state. It will be notified again at each state
	 * transition, until it is handled.
	 */
	public boolean handleGameStarted(Card[] myCards);

	/**
	 * Returns false if this event can't be handled right now, but it can
	 * be handled in a later state. It will be notified again at each state
	 * transition, until it is handled.
	 */
	public boolean handlePlayerLeft(String player);
}
