package unibo.as.cupido.client.viewerstates;

import unibo.as.cupido.common.structures.Card;

public interface ViewerState {

	/**
	 * Activates the state.
	 * This is needed, because a state can't start an animation in the constructor
	 * because it is not registered as the event listener yet, so it would miss
	 * some animation-related events.
	 */
	public void activate();
	
	public void disableControls();
	
	/**
	 * This is called before starting an animation.
	 */
	public void handleAnimationStart();

	/**
	 * This is called when an animation finishes.
	 */
	public void handleAnimationEnd();
	
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
	public boolean handlePlayerLeft(String player);
}
