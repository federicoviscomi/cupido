package unibo.as.cupido.backendInterfacesImpl.table;

import unibo.as.cupido.backendInterfaces.common.Card;

public interface Bot {

	Card[] chooseCardsToPass();

	Card chooseCardToPlay();

}
