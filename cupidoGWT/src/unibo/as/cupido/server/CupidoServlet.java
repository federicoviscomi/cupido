package unibo.as.cupido.server;


import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Collection;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.ServletNotificationsInterface;
import unibo.as.cupido.backendInterfaces.TableInterface;

import javax.servlet.http.HttpSession;

import net.zschech.gwt.comet.server.CometServlet;
import net.zschech.gwt.comet.server.CometSession;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.TableInfoForClient;
import unibo.as.cupido.backendInterfaces.exception.AllLTMBusyException;
import unibo.as.cupido.backendInterfaces.exception.DuplicateUserNameException;
import unibo.as.cupido.backendInterfaces.exception.FatalException;
import unibo.as.cupido.backendInterfaces.exception.FullTableException;
import unibo.as.cupido.backendInterfaces.exception.IllegalMoveException;
import unibo.as.cupido.backendInterfaces.exception.MaxNumTableReachedException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchLTMException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchServerException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.exception.NoSuchUserException;
import unibo.as.cupido.backendInterfaces.exception.NotCreatorException;
import unibo.as.cupido.backendInterfaces.exception.PlayerNotFoundException;
import unibo.as.cupido.backendInterfaces.exception.PositionFullException;
import unibo.as.cupido.backendInterfaces.exception.UserNotAuthenticatedException;
import unibo.as.cupido.client.CupidoInterface;
import unibo.as.cupido.shared.cometNotification.NewLocalChatMessage;
import unibo.as.cupido.shared.cometNotification.PlayerLeft;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * must check before call other component's:
 * - if user are authenticated
 * - if user are at a table or not
 * @author Lorenzo Belli
 *
 */
public class CupidoServlet extends RemoteServiceServlet implements
		CupidoInterface {

	private static final long serialVersionUID = 1L;
	/*
	 * hostname and port of registry to locate the registry default is
	 * 127.0.0.1:1099
	 * GTM is the name GlobalTableManager use to register.
	 * Values of GTM in defined in GlobalTableManagerInterface
	 */
	private static final String registryHost = "127.0.0.1";
	private static final int registryPort = 1099;
	private static final String GTM = "globaltableserver";
	/*
	 * Names used in httpSession attributes
	 */
	private static final String SNI = "servletNotificationInterface";
	private static final String TI = "tableInterfaces";
	private static final String USERNAME = "username";
	private static final String ISAUTHENTICATED = "isAuthenticated";

	/**
	 * Implements here action to perform when notified by the table
	 * 
	 * @param hSession
	 * @param ctSession
	 * @return
	 */
	private ServletNotificationsInterface getServletNotificationsInterface(final HttpSession hSession, final CometSession cSession){
		return new ServletNotificationsInterface() {

			private HttpSession httpSession=hSession;
			private CometSession cometSession=cSession;

			/**
			 * Called by the table when a player left the game
			 */
			@Override
			public void notifyPlayerLeft(String name) {
				// Get the HTTP session for the browser
				//FIXME come faccio a controllare che ritorni la httpSession giusta?
				if (httpSession == null || cometSession==null) {
					//Notifica playerLeft al tavolo
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
	}

	/**
	 * HttpListener use this interface to perform action
	 */
	public interface SessionClosedListener {
		/**
		 * HttpListener must call this method when httpSession is closing
		 */
		public void onSessionClosed();
	}

	public CupidoServlet() {

	}

	@Override
	/*
	 * refers to GlobalChat. Client poll this method
	 */
	public ChatMessage[] viewLastMessages() throws UserNotAuthenticatedException,
	FatalException {
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null){
			return null;
		}
		if (! (Boolean)httpSession.getAttribute(ISAUTHENTICATED)){
			throw new UserNotAuthenticatedException();
		}
		// TODO retrieve messages
		return null;
	}

	@Override
	public void sendGlobalChatMessage(String message) throws IllegalArgumentException, UserNotAuthenticatedException,
	FatalException{
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null){
			return;
		}
		if (! (Boolean)httpSession.getAttribute(ISAUTHENTICATED)){
			throw new UserNotAuthenticatedException();
		}
		// TODO Auto-generated method stub

	}

	@Override
	public void sendLocalChatMessage(String message)
			throws IllegalArgumentException, UserNotAuthenticatedException,
			FatalException {

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
	public boolean login(String username, String password) throws FatalException {
		// TODO retRieve information from the DB
		if (true){
			HttpSession httpSession = getThreadLocalRequest().getSession(false);
			if (httpSession != null){
				httpSession.setAttribute(ISAUTHENTICATED, true);
				httpSession.setAttribute(USERNAME, username);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean registerUser(String username, String password) throws FatalException{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isUserRegistered(String username) throws FatalException{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * @see unibo.as.cupido.client.CupidoInterface#logout()
	 */
	@Override
	public void logout() {
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession != null) {
			httpSession.setAttribute(ISAUTHENTICATED, false);
			httpSession.removeAttribute(USERNAME);
		}
	}

	@Override
	public Collection<TableInfoForClient> getTableList()
			throws UserNotAuthenticatedException, FatalException {

		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			return null;
		}
		if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
			throw new UserNotAuthenticatedException();
		}

		Registry registry;
		try {
			registry = LocateRegistry.getRegistry(registryHost, registryPort);
			GlobalTableManagerInterface gtm = (GlobalTableManagerInterface) registry
					.lookup(GTM);
			return gtm.getTableList();
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on getTableList() catched RemoteException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (NotBoundException e) {
			System.out
					.println("Servlet: on getTableList() catched NotBoundException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		}
	}

	/**
	 * Create a new table
	 * FIXME: ottieni il punteggio dal DB
	 * @throws MaxNumTableReachedException 
	 * 
	 * @throws RemoteException in case of internal errors
	 * @throws AllLTMBusyException if catch AllLTMBusyException
	 */
	@Override
	public InitialTableStatus createTable() throws MaxNumTableReachedException,
	UserNotAuthenticatedException, FatalException {
		InitialTableStatus its = new InitialTableStatus();
		try {
			Registry registry = LocateRegistry.getRegistry(registryHost,registryPort);
			GlobalTableManagerInterface gtm = (GlobalTableManagerInterface) registry
			.lookup(GTM);

			// Get or create the HTTP session for the browser
			HttpSession httpSession = getThreadLocalRequest().getSession(false);
			if (httpSession == null) {
				return null;
			}
			if (! (Boolean)httpSession.getAttribute(ISAUTHENTICATED)){
				throw new UserNotAuthenticatedException();
			}
			// Get or create the Comet session for the browser
			CometSession cometSession = CometServlet.getCometSession(httpSession);
			if (cometSession == null) {
				return null;
			}

			ServletNotificationsInterface sni = getServletNotificationsInterface(httpSession, cometSession);
			httpSession.setAttribute(SNI, sni);
			httpSession.getAttribute(USERNAME);
			TableInterface ti = gtm.createTable(USERNAME, sni);
			httpSession.setAttribute(TI, ti);
			UnicastRemoteObject.exportObject(sni);

			//FIXME: retrieve point from DB
			its.playerScores[0]=99;

		} catch (RemoteException e) {
			System.out.println("Servlet: on createTable() catched RemoteException-> "
					+ e.getMessage());
			//e.printStackTrace();
			throw new FatalException("GTM not reachable");
		} catch (NotBoundException e) {
			System.out.println("Servlet: on createTable() catched NotBoundException-> "
					+ e.getMessage());
			//e.printStackTrace();
			throw new FatalException();
		} catch (AllLTMBusyException e) {
			throw new MaxNumTableReachedException();
		}

		return its;
	}

	/**
	 * 
	 * @param ltmId
	 * @param tableId
	 * @return InitialTableStatus state of the match not yet started
	 * @throws FullTableException
	 *             if game already have 4 players
	 * @throws NoSuchTableException
	 *             if table is no more available, but user can join other table
	 * @throws DuplicateUserNameException
	 *             if player is already playing or viewing the selected table
	 * @throws NoSuchServerException
	 *             if server does not exist
	 * @throws UserNotAuthenticatedException
	 * @throws FatalException
	 *             in case of internal serious error while joining the table,
	 *             probably future action will not be performed
	 */
	@Override
	public InitialTableStatus joinTable(String ltmId, int tableId)
			throws FullTableException, NoSuchTableException, NoSuchServerException,
			DuplicateUserNameException, UserNotAuthenticatedException,
			FatalException {
		try {
			HttpSession httpSession = getThreadLocalRequest().getSession(false);
			if (httpSession == null) {
				return null;
			}
			if(! (Boolean)httpSession.getAttribute(ISAUTHENTICATED)){
				throw new UserNotAuthenticatedException();
			}

			CometSession cometSession = CometServlet
			.getCometSession(httpSession);
			if (cometSession == null) {
				httpSession.invalidate();
				return null;
			}

			Registry registry = LocateRegistry.getRegistry(registryHost,
					registryPort);
			GlobalTableManagerInterface gtm = (GlobalTableManagerInterface) registry
			.lookup(GTM);
			LocalTableManagerInterface LTMinterf = gtm.getLTMInterface(ltmId);
			TableInterface ti = LTMinterf.getTable(tableId);
			httpSession.setAttribute(TI, ti);
			return ti.joinTable((String) httpSession.getAttribute(USERNAME),
					(ServletNotificationsInterface) httpSession
					.getAttribute(SNI));

		} catch (NoSuchTableException e) {
			throw e;
		} catch (FullTableException e) {
			throw e;
		} catch (DuplicateUserNameException e){
			throw e;
		} catch (RemoteException e) {
			System.out
			.println("Servlet: on joinTable() catched RemoteException-> "
					+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (NotBoundException e) {
			System.out
			.println("Servlet: on joinTable() catched NotBoundException-> "
					+ e.getMessage());
			// e.printStackTrace();
			throw new NoSuchServerException();
		} catch (NoSuchLTMException e) {
			System.out
			.println("Servlet: on joinTable() catched NoSuchLTMException-> "
					+ e.getMessage());
			// e.printStackTrace();
			throw new IllegalArgumentException();
		} catch (IllegalArgumentException e) {
			System.out
			.println("Servlet: on joinTable() catched IllegalArgumentException-> "
					+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (IllegalStateException e) {
			System.out
			.println("Servlet: on joinTable() catched IllegalStateException-> "
					+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (SQLException e) {
			System.out
			.println("Servlet: on joinTable() catched SQLException-> "
					+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (NoSuchUserException e) {
			System.out
			.println("Servlet: on joinTable() catched NoSuchUserException-> "
					+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		}
	}

	@Override
	public ObservedGameStatus viewTable(String server, int tableId)
			throws NoSuchTableException, UserNotAuthenticatedException,
			FatalException, NoSuchServerException {

		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			return null;
		}
		if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
			throw new UserNotAuthenticatedException();
		}

		LocalTableManagerInterface ltmi = null;
		try {
			Registry registry = LocateRegistry.getRegistry(registryHost,
					registryPort);
			GlobalTableManagerInterface gtm = (GlobalTableManagerInterface) registry
					.lookup(GTM);
			ltmi = gtm.getLTMInterface(server);
		} catch (NotBoundException e) {
			System.out
					.println("Servlet: on viewTable() catched NotBoundException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (AccessException e) {
			System.out
					.println("Servlet: on viewTable() catched AccessException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on viewTable() catched RemoteException1-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (NoSuchLTMException e) {
			throw new NoSuchServerException();
		}

		try {
			TableInterface ti = ltmi.getTable(tableId);
			httpSession.setAttribute(TI, ti);
			CometSession cometSession = CometServlet
					.getCometSession(httpSession);
			ServletNotificationsInterface sni = getServletNotificationsInterface(
					httpSession, cometSession);
			httpSession.setAttribute(SNI, sni);
			return ti.viewTable((String) httpSession.getAttribute(USERNAME),
					sni);
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on viewTable() catched RemoteException2-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (NoSuchTableException e) {
			throw e;
		}
	}

	@Override
	public void leaveTable() throws UserNotAuthenticatedException,
			NoSuchTableException, FatalException {
		HttpSession httpSession = getThreadLocalRequest().getSession();
		if (httpSession == null) {
			return;
		}
		if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
			throw new UserNotAuthenticatedException();
		}
		TableInterface ti = (TableInterface) httpSession.getAttribute(TI);
		if (ti == null) {
			throw new NoSuchTableException();
		}

		try {
			ti.leaveTable(USERNAME);
		} catch (RemoteException e) {
			System.out
					.println("Servlet: onLeaveTable catched RemoteException ->"
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (PlayerNotFoundException e) {
			throw new NoSuchTableException();
		}
		httpSession.removeAttribute(TI);
	}

	@Override
	public void playCard(Card card) throws IllegalMoveException,
			FatalException, IllegalArgumentException, NoSuchTableException,
			UserNotAuthenticatedException {
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			return;
		}
		if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
			throw new UserNotAuthenticatedException();
		}
		TableInterface ti = (TableInterface) httpSession.getAttribute(TI);
		if (ti == null) {
			System.out.println("Servlet: on playCard() ti == null");
			// player is not at table
			throw new NoSuchTableException();
		}

		try {
			ti.playCard((String) httpSession.getAttribute(USERNAME), card);
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on playCard() catched RemoteException ->"
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (IllegalMoveException e) {
			throw e;
		}
	}

	@Override
	public void passCards(Card[] cards) throws IllegalStateException,
			IllegalArgumentException, NoSuchTableException,
			UserNotAuthenticatedException, FatalException {
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			return;
		}
		if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
			throw new UserNotAuthenticatedException();
		}
		TableInterface ti = (TableInterface) httpSession.getAttribute(TI);
		if (ti == null) {
			System.out.println("Servlet: on passCards() ti == null");
			throw new NoSuchTableException();
		}
		try {
			ti.passCards((String) httpSession.getAttribute(USERNAME), cards);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (IllegalStateException e) {
			throw e;
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on playCard() catched RemoteException ->"
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		}
	}

	@Override
	public void addBot(int position) throws PositionFullException,
			FullTableException, NotCreatorException, IllegalArgumentException,
			UserNotAuthenticatedException, FatalException, NoSuchTableException {
		
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			return;
		}
		if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
			throw new UserNotAuthenticatedException();
		}
		TableInterface ti = (TableInterface) httpSession.getAttribute(TI);
		if (ti == null) {
			throw new NoSuchTableException();
		}
		try {
			ti.addBot((String) httpSession.getAttribute(USERNAME), position);

		} catch (PositionFullException e) {
			throw e;
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (FullTableException e) {
			throw e;
		} catch (NotCreatorException e) {
			throw e;
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on addBot() catched RemoteException-> "
							+ e.getMessage());
			// e.printStackTrace();
		} catch (IllegalStateException e) {
			System.out
					.println("Servlet: on addBot() catched RemoteException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new NoSuchTableException();
		}
	}

	/**
	 * Open connection and perform actions to bind httpSession with remote service interfaces
	 */
	@Override
	public void openCometConnection() {

		System.out.println("Servlet: Opening a Comet connession...");

		// Get the HTTP session for the browser
		HttpSession httpSession = getThreadLocalRequest().getSession();
		if (httpSession == null) {
			return;
		}
		System.out
		.println("Servlet: HttpSession opened " + httpSession.getId());
		httpSession.setAttribute("sessionClosedListener",
				new SessionClosedListener() {
			@Override
			public void onSessionClosed() {
				System.out
				.println("Servlet: onSessionClosed() was called.");
				// TODO: Notify player left at the table
			}
		});
		
		httpSession.setAttribute(ISAUTHENTICATED, Boolean.FALSE);
		// Create the Comet session for the browser
		CometServlet.getCometSession(httpSession);
		System.out.println("Servlet: Comet connession opened.");
	}

	/**
	 * Invalidate httpSession
	 */
	@Override
	public void destroySession() {
		getThreadLocalRequest().getSession(false).invalidate();
	}
}