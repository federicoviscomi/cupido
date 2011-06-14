package unibo.as.cupido.client.states;

public interface StateManager {

	public void transitionToBeforeGameAsOwner();                                                                                                
	public void transitionToBeforeGameNotOwner();                                                                                               
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
