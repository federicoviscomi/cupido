package unibo.as.cupido.client.gamestates;

public interface StateManager {

	public void transitionToCardPassingAsPlayer();                                                                                              
	public void transitionToCardPassingAsViewer();                                                                                              
	public void transitionToCardPassingEndAsPlayer();                                                                                           
	public void transitionToCardPassingWaitingAsPlayer();                                                                                       
	public void transitionToEndOfTrickAsPlayer();                                                                                               
	public void transitionToEndOfTrickAsViewer();
	public void transitionToFirstDealer();
	public void transitionToWaitingDealAsPlayer();
	public void transitionToWaitingFirstDealAsPlayer();
	public void transitionToWaitingFirstDealAsViewer();
	public void transitionToYourTurn();
}
