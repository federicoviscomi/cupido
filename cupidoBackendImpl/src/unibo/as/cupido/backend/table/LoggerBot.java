package unibo.as.cupido.backend.table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;

import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;

public class LoggerBot implements NonRemoteBotInterface {

	private PrintWriter out;

	public LoggerBot(final String botName) {
		try {
			File outputFile = new File("cupidoBackendImpl/botlog/nonremote/"
					+ botName);
			outputFile.delete();
			outputFile.createNewFile();
			out = new PrintWriter(new FileWriter(outputFile));
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					System.err
							.println("shuting down non remote replacementBot "
									+ botName);
					out.close();
				}
			});
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
		//
	}

	@Override
	public void notifyGameStarted(Card[] cards) throws RemoteException {
		//
	}

	@Override
	public void notifyLocalChatMessage(ChatMessage message)
			throws RemoteException {
		//
	}

	@Override
	public void notifyPassedCards(Card[] cards) throws RemoteException {
		//
	}

	@Override
	public void notifyPlayedCard(Card card, int playerPosition)
			throws RemoteException {
		//
	}

	@Override
	public void notifyPlayerJoined(String playerName, boolean isBot, int score,
			int position) throws RemoteException {
		//
	}

	@Override
	public void notifyPlayerReplaced(String botName, int position)
			throws RemoteException {
		//
	}

	@Override
	public void notifyPlayerLeft(String playerName) throws RemoteException {
		//
	}

	@Override
	public void passCards(Card[] cards) throws RemoteException {
		//
	}

	@Override
	public void playCard(Card card) throws RemoteException {
		//
	}
}
