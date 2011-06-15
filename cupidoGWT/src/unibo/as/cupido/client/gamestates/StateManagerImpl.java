package unibo.as.cupido.client.gamestates;

import unibo.as.cupido.client.screens.ScreenSwitcher;

public class StateManagerImpl implements StateManager {
	
	private Object currentState = null;
	private ScreenSwitcher screenSwitcher;
	
	public StateManagerImpl(ScreenSwitcher screenSwitcher) {
		this.screenSwitcher = screenSwitcher;
		
	}

	@Override
	public void transitionToCardPassingAsPlayer() {
		currentState = new CardPassingAsPlayer();
	}

	@Override
	public void transitionToCardPassingAsViewer() {
		currentState = new CardPassingAsViewer();
	}

	@Override
	public void transitionToCardPassingEndAsPlayer() {
		currentState = new CardPassingEndAsPlayer();
	}

	@Override
	public void transitionToCardPassingWaitingAsPlayer() {
		currentState = new CardPassingWaitingAsPlayer();
	}

	@Override
	public void transitionToEndOfTrickAsPlayer() {
		currentState = new EndOfTrickAsPlayer();
	}

	@Override
	public void transitionToEndOfTrickAsViewer() {
		currentState = new EndOfTrickAsViewer();
	}

	@Override
	public void transitionToFirstDealer() {
		currentState = new FirstDealer();
	}

	@Override
	public void transitionToWaitingDealAsPlayer() {
		currentState = new WaitingDealAsPlayer();
	}

	@Override
	public void transitionToWaitingFirstDealAsPlayer() {
		currentState = new WaitingFirstDealAsPlayer();
	}

	@Override
	public void transitionToWaitingFirstDealAsViewer() {
		currentState = new WaitingFirstDealAsViewer();
	}

	@Override
	public void transitionToYourTurn() {
		currentState = new YourTurn();
	}
}
