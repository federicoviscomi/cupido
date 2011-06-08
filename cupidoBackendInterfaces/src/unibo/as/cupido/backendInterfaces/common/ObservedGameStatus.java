package unibo.as.cupido.backendInterfaces.common;

/**
 * 
 * Contains the information that an observer needs when he joins a table
 * 
 * @author cane
 * 
 */
public class ObservedGameStatus {
	public PlayerStatus[] ogs;

	public ObservedGameStatus(PlayerStatus[] players) {
		ogs = players;
	}
}
