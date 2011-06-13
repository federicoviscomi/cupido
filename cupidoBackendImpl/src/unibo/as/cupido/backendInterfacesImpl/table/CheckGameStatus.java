package unibo.as.cupido.backendInterfacesImpl.table;

import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.TableInterface.GameStatus;

/**
 * Just check and stores game status as specified by {@link TableInterface}
 * 
 * @author cane
 * 
 */
public class CheckGameStatus {

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

}
