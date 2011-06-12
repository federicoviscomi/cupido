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
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.TableDescriptor;
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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CupidoServlet extends RemoteServiceServlet implements
		CupidoInterface {

	private static final long serialVersionUID = 1L;

	ServletNotificationsInterface sni = new ServletNotificationsInterface() {

		@Override
		public void notifyPlayerLeft(String name) {
			// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
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
	 * Create a new table FIXME: scegli il nome nel registry, lo username
	 * corretto
	 * 
	 * @throws RemoteException
	 *             , AllLTMBusyException
	 */
	@Override
	public InitialTableStatus createTable() throws FatalException,
			AllLTMBusyException {
		try {
			// Locate a registry on localhost:1099
			Registry registry = LocateRegistry.getRegistry(null);
			GlobalTableManagerInterface gtm = (GlobalTableManagerInterface) registry
					.lookup("gtm");
			ServletNotificationsInterface snf = new ServletNotificationsInterfaceImpl();
			TableInterface a = gtm.createTable("username", snf);

		} catch (RemoteException e) {
			System.out.println("Servlet: catched RemoteException-> "
					+ e.getMessage());
			e.printStackTrace();
			throw new FatalException("GTM not reachable");
		} catch (NotBoundException e) {
			System.out.println("Servlet: catched NotBoundException-> "
					+ e.getMessage());
			e.printStackTrace();
			throw new FatalException();
		} catch (AllLTMBusyException e) {
			throw new AllLTMBusyException();
		}
		return null;
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

		httpSession.setAttribute("sessionClosedListener",
				new SessionClosedListener() {
					@Override
					public void onSessionClosed() {
						System.out
								.println("Servlet: onSessionClosed() was called.");
					}
				});

		// Get or create the Comet session for the browser
		CometSession cometSession = CometServlet.getCometSession(httpSession);

		getServletContext().setAttribute("cometSession", cometSession);

		// FIXME: Use the correct username.
		getServletContext().setAttribute("username", "pippo");

		System.out.println("Servlet: Comet connession opened.");
	}

}
