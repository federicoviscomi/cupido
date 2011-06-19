package unibo.as.cupido.backendInterfacesImpl.table;

import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.TableInterface.GameStatus;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.Card.Suit;

/**
 * Just check and stores game status as specified by {@link TableInterface}
 * 
 * @author cane
 * 
 */
public class CheckGameStatus {

	public static void main(String[] args) {
		Card a = new Card(2, Suit.CLUBS);
		Card b = new Card(2, Suit.CLUBS);
	}

	protected GameStatus gameStatus;

	public CheckGameStatus() {
		gameStatus = GameStatus.INIT;
	}

	public void checkAddBot() throws IllegalStateException {
		if (gameStatus.equals(GameStatus.ENDED))
			throw new IllegalStateException();

	}

	public void checkJoinTable() throws IllegalStateException {
		if (!gameStatus.equals(GameStatus.INIT))
			throw new IllegalStateException();
	}

	public void checkPassCards() throws IllegalStateException {
		if (!gameStatus.equals(GameStatus.PASSING_CARDS))
			throw new IllegalStateException();
	}

}
