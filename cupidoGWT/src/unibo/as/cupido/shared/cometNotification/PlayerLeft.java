package unibo.as.cupido.shared.cometNotification;

import java.io.Serializable;

/*
 * when a player leaves and the game is not started, there is a free seat!
 * otherwise a playing bot enters the game.
 */
public class PlayerLeft implements Serializable {

	private static final long serialVersionUID = 1L;

	public String player;
	
	public PlayerLeft(){
		super();
	}
	public PlayerLeft(String player){
		this.player=player;
	}
}
