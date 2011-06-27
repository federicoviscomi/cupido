package unibo.as.cupido.backend.table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Arrays;

import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;

/**
 * Logs creator operations
 */
public class LoggerBot implements NonRemoteBotInterface {

	private PrintWriter out;
	private final String botName;

	public LoggerBot(final String name) {
		this.botName = name;
		try {
			File outputFile = new File("cupidoBackendImpl/botlog/nonremote/"
					+ name);
			outputFile.delete();
			outputFile.createNewFile();
			out = new PrintWriter(new FileWriter(outputFile));
		} catch (IOException e) {
			System.err.println("logger bot catched IOException "
					+ e.getLocalizedMessage()
					+ ". Using standard output for log.");
			out = new PrintWriter(System.out);
		}
	}

	@Override
	public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint)
			throws RemoteException {
		out.println("" + botName + " notifyGameEnded("
				+ Arrays.toString(matchPoints) + ", "
				+ Arrays.toString(playersTotalPoint) + ")");
	}

	@Override
	public void notifyGameStarted(Card[] cards) throws RemoteException {
		out.println("" + botName + " notifyGameStarted("
				+ Arrays.toString(cards) + ")");
	}

	@Override
	public void notifyLocalChatMessage(ChatMessage message)
			throws RemoteException {
		out.println("" + botName + " notifyChatMessage(" + message + ")");
	}

	@Override
	public void notifyPassedCards(Card[] cards) throws RemoteException {
		out.println("" + botName + " notifyPassedCards("
				+ Arrays.toString(cards) + ")");
	}

	@Override
	public void notifyPlayedCard(Card card, int playerPosition)
			throws RemoteException {
		out.println("" + botName + " notifyPlayedCard(" + card + ", "
				+ playerPosition + ")");
	}

	@Override
	public void notifyPlayerJoined(String playerName, boolean isBot, int score,
			int position) throws RemoteException {
		out.println("" + botName + " notifyPlayerJoined(" + playerName + ", "
				+ isBot + ", " + score + ")");
	}

	@Override
	public void notifyPlayerLeft(String playerName) throws RemoteException {
		out.println("" + botName + " notifyPlayerLefy(" + playerName + ")");
	}

	@Override
	public void notifyPlayerReplaced(String botName, int position)
			throws RemoteException {
		out.println("" + botName + " notifyPlayerReplaced(" + botName + ", "
				+ position + ")");
	}

	@Override
	public void passCards(Card[] cards) throws RemoteException {
		out.println("" + botName + " passCards(" + Arrays.toString(cards) + ")");
	}

	@Override
	public void playCard(Card card) throws RemoteException {
		out.println("" + botName + " playCard(" + card + ")");
	}

	@Override
	public void activate(TableInterface tableInterface) throws RemoteException {
		out.println("" + botName + " activate(" + tableInterface + ")");
	}
}
