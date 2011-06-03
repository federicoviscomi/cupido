package unibo.as.cupido.shared.cometNotification;

import java.io.Serializable;

import unibo.as.cupido.backendInterfaces.common.Card;

/*
 * this notification isn's sent to viewers
 */
public class GameStarted implements Serializable {

	private static final long serialVersionUID = 1L;

	Card[] myCards;

}
