package unibo.as.cupido.backendInterfacesImpl;

import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.TableManagerInterface.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.FullTableException;
import unibo.as.cupido.backendInterfaces.common.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PositionFullException;

/**
 * 
 * 
 * @author cane
 *
 */
public class Table implements Runnable, TableInterface {

	@Override
	public void addBot(String userName, int position)
			throws PositionFullException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InitialTableStatus joinTable(String userName,
			ServletNotifcationsInterface snf) throws FullTableException,
			NoSuchTableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void leaveTable(String userName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void passCards(String userName, Card[] cards)
			throws IllegalMoveException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playCard(String userName, Card card)
			throws IllegalMoveException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendMessage(String userName, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObservedGameStatus viewTable(String userName,
			ServletNotifcationsInterface snf) throws NoSuchTableException {
		// TODO Auto-generated method stub
		return null;
	}

}
