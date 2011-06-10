package unibo.as.cupido.backendInterfaces.common;

import java.io.Serializable;

/**
 * 
 * Contains the information that an observer needs when he joins a table
 * 
 * @author cane
 * 
 */
public class ObservedGameStatus implements Serializable {
	public PlayerStatus[] ogs;

	public ObservedGameStatus() {

	}

	public ObservedGameStatus(PlayerStatus[] players) {
		ogs = players;
	}
}
