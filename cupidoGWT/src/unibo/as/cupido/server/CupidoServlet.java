package unibo.as.cupido.server;


import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.TableInterface;

import javax.servlet.http.HttpSession;

import net.sourceforge.htmlunit.corejs.javascript.tools.shell.Global;
import net.zschech.gwt.comet.server.CometServlet;
import net.zschech.gwt.comet.server.CometSession;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.exception.AllLTMBusyException;
import unibo.as.cupido.backendInterfaces.exception.FatalException;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;
import unibo.as.cupido.client.CupidoInterface;
import unibo.as.cupido.shared.cometNotification.NewLocalChatMessage;
import unibo.as.cupido.shared.cometNotification.PlayerLeft;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CupidoServlet extends RemoteServiceServlet implements
		CupidoInterface {

	private static final long serialVersionUID = 1L;
	/**
	 * hostname and port of registry to locate GlobalTableManagerInterface
	 * default is 127.0.0.1:1099
	 */
	private static final String registryHost= "127.0.0.1";
	private static final int registryPort = 1099;

	/**
	 * Define here methods to be notified by the table
	 */
	ServletNotificationsInterface sni = new ServletNotificationsInterface() {

		/**
		 * Called by the table when a player left the game
		 */
		@Override
		public void notifyPlayerLeft(String name) {
			// Get the HTTP session for the browser
			//FIXME come faccio a controllare che ritorni la httpSession giusta?
			HttpSession httpSession = getThreadLocalRequest().getSession(false);
			if (httpSession == null) {
				//Notifica playerLeft al tavolo
				return;
			}

			// Get the Comet session for the browser
			CometSession cometSession = CometServlet.getCometSession(httpSession, false);
			if (cometSession==null){
				//Notifica playerLeft al tavolo
				httpSession.invalidate();
				return;
			}
			PlayerLeft playerLeft=new PlayerLeft(name);
			cometSession.enqueue(playerLeft);
		}

		@Override
		public void notifyPlayerJoined(String name, boolean isBot, int point,
				int position) {
			// TODO Auto-generated method stub

		}

		@Override
		public void notifyPlayedCard(Card card, int playerPosition) {
			// TODO Auto-generated method stub

		}

		@Override
		public void notifyPassedCards(Card[] cards) {
			// TODO Auto-generated method stub

		}

		@Override
		public void notifyLocalChatMessage(ChatMessage message) {
			System.out
					.println("Servlet: received a notification from the backend. Sending it to the client...");
			// cometSession.enqueue(message);
		}

		@Override
		public void notifyGameStarted(Card[] cards) {
			// TODO Auto-generated method stub

		}

		@Override
		public void notifyGameEnded(int[] matchPoints, int[] playersTotalPoint) {
			// TODO Auto-generated method stub

		}
	};

	public interface SessionClosedListener {
		public void onSessionClosed();
	}

	public CupidoServlet() {

	}

	@Override
	public ChatMessage[] viewLastMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendGlobalChatMessage(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendLocalChatMessage(String message) {

		// FIXME: This implementation does *not* really work, it is only meant
		// for debugging purposes.
		// It only displays the user's own messages.

		CometSession cometSession = (CometSession) getServletContext()
				.getAttribute("cometSession");
		String username = (String) getServletContext().getAttribute("username");

		System.out.println("Servlet: Sending back the message to the client.");
		NewLocalChatMessage x = new NewLocalChatMessage();
		x.message = message;

		x.user = username;
		cometSession.enqueue(x);
	}

	@Override
	public boolean login(String username, String password) {
		// TODO retieve information from the DB
		return false;
	}

	@Override
	public boolean registerUser(String username, String password) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUserRegistered(String username) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void logout() {
		// TODO Auto-generated method stub

	}

	@Override
	public TableData[] getTableList() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Create a new table
	 * FIXME: scegli lo username corretto, ed ottieni il punteggio dal DB
	 * 
	 * @throws RemoteException
	 *             , AllLTMBusyException
	 */
	@Override
	public InitialTableStatus createTable() throws FatalException,
	AllLTMBusyException {
		InitialTableStatus its = new InitialTableStatus();
		try {
			Registry registry = LocateRegistry.getRegistry(registryHost,registryPort);
			GlobalTableManagerInterface gtm = (GlobalTableManagerInterface) registry
			.lookup("globaltableserver");
			TableInterface ti = gtm.createTable("username", sni);

			// Get or create the HTTP session for the browser
			HttpSession httpSession = getThreadLocalRequest().getSession();
			if (httpSession == null) {
				return null;
			}
			httpSession.setAttribute("tableInterface", ti);
			httpSession.setAttribute("servletNotificationInterface", sni);

			//FIXME: retrieve point from DB
			its.playerPoints[0]=99;

		} catch (RemoteException e) {
			System.out.println("Servlet: on createTable(): catched RemoteException-> "
					+ e.getMessage());
			//e.printStackTrace();
			throw new FatalException("GTM not reachable");
		} catch (NotBoundException e) {
			System.out.println("Servlet: on createTable(): catched NotBoundException-> "
					+ e.getMessage());
			//e.printStackTrace();
			throw new FatalException();
		} catch (AllLTMBusyException e) {
			throw new AllLTMBusyException();
		}
		
		return its;
	}

	@Override
	public InitialTableStatus joinTable(String server, int tableId)
			throws FullTableException, NoSuchTableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObservedGameStatus viewTable(String server, int tableId)
			throws NoSuchTableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void leaveTable() {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 */
	@Override
	public void playCard(Card card) {
		// TODO Auto-generated method stub

	}

	@Override
	public void passCards(Card[] cards) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addBot(int position) throws PositionFullException {
		// TODO Auto-generated method stub

	}

	@Override
	public void openCometConnection() {

		System.out.println("Servlet: Opening a Comet connession...");

		// Get or create the HTTP session for the browser
		HttpSession httpSession = getThreadLocalRequest().getSession();
		if (httpSession == null){
			return;
		}
		System.out.println("Servlet: HttpSession opened " + httpSession.getId());
		httpSession.setAttribute("sessionClosedListener",
				new SessionClosedListener() {
					@Override
					public void onSessionClosed() {
						System.out
								.println("Servlet: onSessionClosed() was called.");
						//Notify player left at the table
					}
				});

		// Get or create the Comet session for the browser
		//CometSession cometSession = CometServlet.getCometSession(httpSession);

		System.out.println("Servlet: Comet connession opened.");
	}

}
