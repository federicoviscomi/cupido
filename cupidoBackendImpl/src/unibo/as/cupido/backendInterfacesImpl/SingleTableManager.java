package unibo.as.cupido.backendInterfacesImpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Collections;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.Table;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.TableDescriptor;
import unibo.as.cupido.backendInterfaces.ServletNotifcationsInterface;
import unibo.as.cupido.backendInterfaces.TableInterface;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.FullTableException;
import unibo.as.cupido.backendInterfaces.common.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PlayerNotFoundException;
import unibo.as.cupido.backendInterfaces.common.PlayerStatus;
import unibo.as.cupido.backendInterfaces.common.PositionFullException;

/**
 * 
 * @author cane
 * 
 */
public class SingleTableManager implements TableInterface {

	public static void main(String[] args) throws Exception {
		try {
			// TODO implement the following class
			ServletNotifcationsInterface sni = new DummyLoggerServletNotifyer();
			SingleTableManager stm = new SingleTableManager(sni, new Table("Owner", 0, new TableDescriptor(
					"servercane", 34453)), null);
			stm.printGameStatus();
			stm.addBot("bot", 1);
			stm.printGameStatus();
			stm.joinTable("Cane", sni);
			stm.printGameStatus();
			stm.joinTable("Gatto", sni);
			stm.printGameStatus();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private CardsManager cardsManager;
	/* posso spostarlo in CardsManagers? */
	private GameStatus gameStatus;
	private PlayersManager playersManager;
	private ToBeNotifyed toNotify;
	private BotManager botManager;

	public SingleTableManager(ServletNotifcationsInterface snf, Table table, GlobalTableManagerInterface gtm)
			throws RemoteException {
		toNotify = new ToBeNotifyed();
		cardsManager = new CardsManager();
		playersManager = new PlayersManager(table.owner, false);
		botManager = new BotManager(cardsManager);
		toNotify.notifyPlayerJoined(table.owner, 0, snf);
		gameStatus = GameStatus.INIT;
	}

	@Override
	public void addBot(String botName, int position) throws PositionFullException, FullTableException {
		if (gameStatus.equals(GameStatus.ENDED)) {
			throw new IllegalStateException();
		}
		playersManager.addBot(botName, position);
		toNotify.notifyBotJoined(botName, position, 0);
		botManager.addBot(botName, position);
		if (playersManager.getPlayersCount() == 4) {
			if (gameStatus.equals(GameStatus.INIT)) {
				gameStatus = GameStatus.PASSING_CARDS;
				botPassCard();
			} else if (gameStatus.equals(GameStatus.PASSING_CARDS)) {
				gameStatus = GameStatus.STARTED;
				botPlayCard();
			} else if (gameStatus.equals(GameStatus.STARTED)) {
				botPlayCard();
			}
		}
	}

	/**
	 * Hearts Strategy - Passing
	 * 
	 * With considerable latitude in play, Hearts allows players to create and
	 * improvise strategies that can make the game exciting and fun. Obviously,
	 * it is of prime importance to avoid taking points, but it is often a
	 * better strategy to plan on taking just a few at every hand. This keeps
	 * opponents from running and delivering 26 points to your scoresheet.
	 * 
	 * What to pass from your hand during the passing phase is the first place
	 * that strategy comes into play. First, it is nearly always a mistake to
	 * pass low spades (jack or under). A low spade is usually the safest lead
	 * for any player except the one who holds the queen, thus they are handy
	 * cards to have. Also, you might be passed the queen of spades and it is
	 * always good to have this card backed up by as many other cards as
	 * possible to prevent other players from playing low spades until you have
	 * to play the queen of spades on yourself.
	 * 
	 * If you are dealt the queen of spades the decision to pass it can be
	 * excruciating. It is almost always wise to hold it if you have at least
	 * three cards behind it. It should probably be passed with two or less
	 * spades behind it. The great thing about holding it is that you know where
	 * it is and when it can be played and you can sometimes orchestrate play so
	 * that it is played on your leading opponent.
	 * 
	 * If you absolutely must pass the queen of spades, don't forget where you
	 * have passed it. You may be passed the ace or king of spades and must know
	 * when it is safe to play (behind the player with the queen once he has
	 * played a different spade).
	 * 
	 * It should be an object in passing to void yourself in either diamonds or
	 * clubs. To be void in one of these suits helps to slough hearts onto your
	 * opponents and also relieve yourself of high cards in other suits. But
	 * this is merely a broad objective. Since the player passing to you will
	 * have the same objective in mind, you are quite likely to get cards
	 * similar or worse than to those you are passing. However, if you are
	 * lucky, you will acquire an unbalanced hand with many of one suit and few
	 * or none of another.
	 * 
	 * Should you pass hearts? No, and then again yes. Generally, it is best to
	 * keep your hearts for sloughing on your opponents. It is better to be void
	 * in another suit. Yet ... there is one good reason to pass a middling
	 * heart to your opponent. If he is considering running, this will nearly
	 * always provide a "poison pill". For during play, if you hold a high heart
	 * and your opponent must play the middling heart, you hold his stopper. If
	 * you take four points as a result, it is a small price to pay to prevent a
	 * run.
	 * 
	 * Now, if you are dealt a hand full of high cards, with no hearts or the
	 * ace, king, queen of hearts you can throw all of the above advice out the
	 * window. You may be in a position to "run". In this case, you want to hord
	 * your high cards and pass your low stuff. If you have any low hearts, pass
	 * them (unless you have so many high hearts as to make them safe) so they
	 * will not act as a "poison pill". Remember, to run, you do not need all
	 * the tricks, merely all the points.
	 * 
	 * A good pass is half the battle in hearts. You can also read passes from
	 * other players as unintended signals. A player passing ace, king or queen
	 * of spades is likely short of this suit. A player passing low hearts
	 * (especially two or more) is contemplating a run. A player passing a low
	 * spade is probably a novice. A pass of low cards in general means possible
	 * run.
	 * 
	 * Passing is somewhat of an art and somewhat of a science, but, ultimately,
	 * hearts is just a game and it is well not to take it too seriously.
	 * 
	 * Next Page
	 */

	/**
	 * Hearts Strategy - Passing
	 * 
	 * Strategy:
	 * <ul>
	 * <li>avoid taking points</li>
	 * <li>taking just a few points at every hand. This keeps opponents from
	 * running and delivering 26 points to your scoresheet.</li>
	 * </ul>
	 * 
	 * <code>
	 * if (player holds queen of spades){
	 * 	if (player holds at least three cards behind queen of spades) {
	 * 		keep the queen of spades
	 * 	} else {
	 * 		pass the queen of spades and remember where you have passed it
	 * 		?pass eventual king and ace of spades?
	 * 		
	 * 		try to void yourself in either diamonds or clubs or pass the higest cards of diamonds and clubs  		
	 * 	}
	 * 	 	
	 * } else {
	 *  	?pass eventual king and ace of spades?
	 * 		it is nearly always a mistake to pass low spades (jack or under)
	 * 		try to void yourself in either diamonds or clubs or pass the higest cards of diamonds and clubs
	 * }
	 * 
	 * 
	 * </code>
	 * 
	 * 
	 * Generally, it is best to keep your hearts for sloughing on your
	 * opponents. It is better to be void in another suit. Yet there is one
	 * good reason to pass a middling heart to your opponent. If he is
	 * considering running, this will nearly always provide a "poison pill". For
	 * during play, if you hold a high heart and your opponent must play the
	 * middling heart, you hold his stopper. If you take four points as a
	 * result, it is a small price to pay to prevent a run.
	 * 
	 * Now, if you are dealt a hand full of high cards, with no hearts or the
	 * ace, king, queen of hearts you can throw all of the above advice out the
	 * window. You may be in a position to "run". In this case, you want to hold
	 * your high cards and pass your low stuff. If you have any low hearts, pass
	 * them (unless you have so many high hearts as to make them safe) so they
	 * will not act as a "poison pill". Remember, to run, you do not need all
	 * the tricks, merely all the points.
	 * 
	 * A good pass is half the battle in hearts. You can also read passes from
	 * other players as unintended signals. A player passing ace, king or queen
	 * of spades is likely short of this suit. A player passing low hearts
	 * (especially two or more) is contemplating a run. A player passing a low
	 * spade is probably a novice. A pass of low cards in general means possible
	 * run.
	 * 
	 * Passing is somewhat of an art and somewhat of a science, but, ultimately,
	 * hearts is just a game and it is well not to take it too seriously.
	 * 
	 * Next Page
	 */
	private void botPassCard() {
		for (int i = 1; i < 4; i++) {
			if (playersManager.isBot(i)) {
				botManager.passCard(i);
			}
		}
		if (cardsManager.allPlayerPassedCards()) {
			gameStatus = GameStatus.STARTED;
			cardsManager.passCards();
			toNotify.notifyGameStarted(null, null);
		}
	}

	/**
	 * Hearts Play Strategies
	 * 
	 * Knowing what to play on the first trick is easy. If you have the two of
	 * clubs, you must lead it. As you cannot slough any points on the first
	 * trick, use this opportunity to get rid of a high club, if you are void in
	 * clubs, slough another high card, perhaps a king or ace of spades - two
	 * cards that could lead to trouble later if they are not sufficiently
	 * backed up.
	 * 
	 * Many players go right into leading spades. This is done in order to force
	 * the queen out, and it is considered the safest lead possible. The worst
	 * that can happen is the trick could draw a heart or two. This is something
	 * that the holder of a shallow queen of spades will dread. Her best
	 * strategy is then to seize the lead, if possible, and divert her opponents
	 * by leading back diamonds or clubs. An alternative strategy is to come
	 * back with high diamonds or clubs in order to draw a heart. Once the heart
	 * is broken she can dish out a low heart which can cause a heart war that
	 * might create a distraction from her precarious spade position, and for a
	 * shallow queen of spades, any distraction is a good distraction.
	 * 
	 * This heart war may occur because of a common tactic when playing the
	 * heart suit. When a player must take a trick, he should do so with as high
	 * a card as possible. This leaves him with the opportunity to dish out a
	 * low card in the same suit, getting rid of the lead and forcing another
	 * player to take several points. This can create a vicious cycle,
	 * especially where hearts are involved.
	 * 
	 * Of course, playing low and avoiding the queen are paramount, but this
	 * does not mean that a player should avoid points altogether. Taking a few
	 * points in every hand is generally a good idea because it prevents any
	 * other player from running. The best way to stop other players from
	 * running is to save a high heart and not slough it until at least two
	 * people have points. Holding back a stopper to prevent a run is only good
	 * sense, especially if you are sure your opponents have a middling heart
	 * losers (which you may well have passed at least one of them).
	 * 
	 * In games with sharp players, a successful run is infrequent. This is
	 * because a table of sharp players will generally pass a middling heart and
	 * orient their own hand in such a way as to prevent the run by their
	 * opponents while minimizing their own exposure to points, especially the
	 * queen of spades. But there will be times when the cards align to almost
	 * make a run seem inevitable. The best configuration for a run will be when
	 * no heart losers are held or only the very highest hearts in sequence.
	 * Ace, king and queen of hearts is nice, but trying to run with the king,
	 * queen, jack is a dangerous maneuver.
	 * 
	 * If such a run were to be attempted, the running player should try to draw
	 * the ace before making his intention to run perfectly obvious. Otherwise,
	 * the ace will be a sure stopper. If a player has a mere middling heart
	 * loser, another strategy is to lead high in clubs or diamonds until a
	 * heart has been drawn and then lead back the middling heart. If the
	 * opponents are not sure of his intentions they might let the trick slide
	 * and put the prospective runner in a good position to run.
	 * 
	 * Hearts is as much a game of personalities and bluffing as is poker. It
	 * requires skill and intuition to determine how opponents will react to
	 * certain styles of play. For example, a player who tries and fails to run
	 * frequently will be watched so closely in subsequent play that his
	 * prospects of any deceptive plays become impossible. Also, inexperienced
	 * players tend to shy away from running, so they usually need not be played
	 * closely. This means that when passing to such a player, you do not have
	 * to worry so much about making certain they have a middling heart or that
	 * by passing them three low hearts you are sending a clear signal of your
	 * intention to run.
	 * 
	 * There are so many possibilities of different hands that it is impossible
	 * to make provision or comment on every circumstance. The key to remember
	 * is that Hearts is only a game. Play it well, but play it to have fun.
	 */
	private void botPlayCard() {
		// TODO Auto-generated method stub

	}

	@Override
	public InitialTableStatus joinTable(String playerName, ServletNotifcationsInterface snf) throws FullTableException,
			NoSuchTableException {
		if (!gameStatus.equals(GameStatus.INIT)) {
			throw new IllegalStateException();
		}
		if (playerName == null || snf == null) {
			throw new IllegalArgumentException();
		}
		InitialTableStatus its = playersManager.addPlayer(playerName);
		toNotify.notifyPlayerJoined(playerName, playersManager.getPlayerPosition(playerName), snf);
		if (playersManager.getPlayersCount() == 4) {
			gameStatus = GameStatus.PASSING_CARDS;
			botPassCard();
		}
		return its;
	}

	@Override
	public void leaveTable(String playerName) throws PlayerNotFoundException {
		if (gameStatus.equals(GameStatus.ENDED)) {
			throw new IllegalStateException();
		}
		int position = playersManager.getPlayerPosition(playerName);
		if (gameStatus.equals(GameStatus.INIT)) {
			playersManager.removePlayer(playerName);
			toNotify.remove(playerName);
		} else {
			try {
				this.addBot("_bot." + playerName, position);
			} catch (PositionFullException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FullTableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void passCards(String userName, Card[] passedCards) throws IllegalArgumentException {
		if (gameStatus.ordinal() != GameStatus.PASSING_CARDS.ordinal())
			throw new IllegalStateException();
		if (userName == null || passedCards == null || passedCards.length != 3)
			throw new IllegalArgumentException();
		int position = playersManager.getPlayerPosition(userName);
		if (position == -1)
			throw new IllegalArgumentException("invalid user name");
		cardsManager.setCardPassing(position, passedCards);
		toNotify.notifyCardPassed(passedCards, playersManager.getPlayerName((position + 1) % 4));
		if (cardsManager.allPlayerPassedCards()) {
			gameStatus = GameStatus.STARTED;
			cardsManager.passCards();
			toNotify.notifyGameStarted(userName, passedCards);
		}
	}

	@Override
	public void playCard(String userName, Card card) throws IllegalMoveException {
		if (gameStatus.ordinal() != GameStatus.STARTED.ordinal()
				&& gameStatus.ordinal() != GameStatus.STARTED.ordinal()) {
			throw new IllegalStateException();
		}
		int position = playersManager.getPlayerPosition(userName);
		cardsManager.playCard(position, card);
		toNotify.notifyCardPlayed(userName, card, position);
		if (cardsManager.allPlayerPlayedCards()) {
			if (gameStatus.ordinal() == GameStatus.STARTED.ordinal()) {
				int winner = cardsManager.getWinner();
				int points = cardsManager.getPoints();
				playersManager.addPoint(winner, points);
				gameStatus = GameStatus.STARTED;
			}
			if (cardsManager.gameEnded()) {
				// FIXME
				toNotify.notifyGameEnded(playersManager.getAllPoints(), playersManager.getAllPoints());
			}
		}
	}

	private void printGameStatus() {
		System.out.format("\n\nGame status: %s", gameStatus.toString());
		cardsManager.print(playersManager);
	}

	@Override
	public void sendMessage(ChatMessage message) {
		toNotify.notifyMessageSent(message);
	}

	@Override
	public ObservedGameStatus viewTable(String userName, ServletNotifcationsInterface snf) throws NoSuchTableException,
			IllegalArgumentException, IllegalStateException {
		if (gameStatus.equals(GameStatus.ENDED)) {
			throw new IllegalStateException();
		}
		if (userName == null || snf == null)
			throw new IllegalArgumentException();
		toNotify.viewerJoined(userName, snf);
		ObservedGameStatus ogs = new ObservedGameStatus();
		PlayerStatus[] ps = new PlayerStatus[4];
		for (int i = 0; i < 4; i++)
			ps[i] = new PlayerStatus(playersManager.players[i].name, playersManager.players[i].score,
					cardsManager.cardPlayed[i], cardsManager.cards[i].size(), playersManager.players[i].isBot);
		return ogs;
	}
}
