package unibo.as.cupido.client.playerstates;

import unibo.as.cupido.client.CardsGameWidget.CardRole;
import unibo.as.cupido.common.structures.Card;

public interface PlayerState {
	
	/**
	 * Activates the state.
	 * This is needed, because a state can't start an animation in the constructor
	 * because it is not registered as the event listener yet, so it would miss
	 * some animation-related events.
	 */
	public void activate();

	public void freeze();
	
	/**
	 * This is called before starting an animation.
	 */
	public void handleAnimationStart();

	/**
	 * This is called when an animation finishes.
	 */
	public void handleAnimationEnd();

	/**
	 * This is called when the user clicks on a card, except during
	 * animations and when the CardsGameWidget's controls are disabled.
	 * 
	 * @player: the player to whom the card belongs
	 * @card: the card that was clicked, or `null' if a covered card was
	 *        clicked.
	 * @isRaised: this is true only if state==HAND and this card is
	 *            currently raised.
	 */
	public void handleCardClicked(int player, Card card, CardRole.State state,
			boolean isRaised);
	
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
