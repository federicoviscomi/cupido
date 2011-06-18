/*
 * WARNING put comment lines in constructor method CupidoServlet() to test Servlet without registry
 * put comment lines in registerUser() for fake registration
 */
package unibo.as.cupido.server;

/*
 * FIXME: do NOT remove unused imports and variables
 */
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Collection;

import unibo.as.cupido.backendInterfaces.GlobalChatInterface;
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
import unibo.as.cupido.shared.cometNotification.CardPassed;
import unibo.as.cupido.shared.cometNotification.CardPlayed;
import unibo.as.cupido.shared.cometNotification.GameEnded;
import unibo.as.cupido.shared.cometNotification.GameStarted;
import unibo.as.cupido.shared.cometNotification.NewLocalChatMessage;
import unibo.as.cupido.shared.cometNotification.NewPlayerJoined;
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
	 * hostname and port of registry, default is 127.0.0.1:1099
	 * GTMLookupName and GCLookupName are name for registry lookup
	 * Values of GTMLookupName in defined in GlobalTableManagerInterface
	 * Values of GCLookupName can be found in GlobalChatInterface
	 */
	private static final String registryHost = "127.0.0.1";
	private static final int registryPort = 1099;
	private static final String GTMLookupName = "globaltableserver";
	private static final String GCLookupName = GlobalChatInterface.globalChatName;
	/*
	 * Names used in httpSession and servlet-context attributes
	 */
	private static final String SNI = "servletNotificationInterface";
	private static final String TI = "tableInterfaces";
	private static final String USERNAME = "username";
	private static final String ISAUTHENTICATED = "isAuthenticated";
	private static final String GCI = "globalChatInterface";
	private static final String GTMI = "globalTableManagerInterface";
	private static final String DBI = "databaseInterface";
	
	/**
	 * WARNING comments this method to test servlet without RMI registry and DB
	 * 
	 * Saves registry lookups in servlet context, and initialize DBmanager
	 * 
	 */
	public CupidoServlet() {
		Registry registry;
		GlobalChatInterface gci = null;
		GlobalTableManagerInterface gtmi = null;
		try {
			registry = LocateRegistry.getRegistry(registryHost, registryPort);
			gci = (GlobalChatInterface) registry.lookup(GCLookupName);
			gtmi = (GlobalTableManagerInterface) registry.lookup(GTMLookupName);
		} catch (RemoteException e) {
			System.out.println("Servlet: on CupidoServlet() catched RemoteException->");
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("Servlet: on CupidoServlet() catched NotBoundException->");
			e.printStackTrace();
		}
		
		getServletContext().setAttribute(GCI, gci);
		getServletContext().setAttribute(GTMI, gtmi);
	
		DatabaseInterface dbi = new DatabaseManager();
		getServletContext().setAttribute(DBI, dbi);
	}
	
	/*
	 * Implements here action to perform when notified by the table
	 * must create CometNotification classes
	 * @param hSession
	 * @param ctSession
	 * @return
	 */
	private ServletNotificationsInterface getServletNotificationsInterface(
			final HttpSession hSession, final CometSession cSession) {
		return new ServletNotificationsInterface() {

			private HttpSession httpSession = hSession;
			private CometSession cometSession = cSession;

			/*
			 * Called by the table when a player left the game
			 */
			@Override
			public void notifyPlayerLeft(String name) {
				if (httpSession == null || cometSession == null) {
					System.out.println("SerletNotInterf: session null");
					return;
				}
				// Notifica playerLeft al client
				cometSession.enqueue(new PlayerLeft(name));
			}

			@Override
			public void notifyPlayerJoined(String name, boolean isBot,
					int point, int position) {
				if (httpSession == null || cometSession == null) {
					System.out.println("SerletNotInterf: session null");
					return;
				}
				NewPlayerJoined j = new NewPlayerJoined();
				j.name = name;
				j.isBot = isBot;
				j.points = point;
				j.position = position;
				cometSession.enqueue(j);
			}

			@Override
			public void notifyPlayedCard(Card card, int playerPosition) {
				if (httpSession == null || cometSession == null) {
					System.out.println("SerletNotInterf: session null");
					return;
				}
				CardPlayed p = new CardPlayed();
				p.card = card;
				p.playerPosition = playerPosition;
				cometSession.enqueue(p);
			}

			@Override
			public void notifyPassedCards(Card[] cards) {
				if (httpSession == null || cometSession == null) {
					System.out.println("SerletNotInterf: session null");
					return;
				}
				CardPassed p = new CardPassed();
				p.cards = cards;
				cometSession.enqueue(p);
			}

			@Override
			public void notifyLocalChatMessage(ChatMessage message) {
				if (httpSession == null || cometSession == null) {
					System.out.println("SerletNotInterf: session null");
					return;
				}
				NewLocalChatMessage m = new NewLocalChatMessage();
				m.message = message.message;
				m.user = message.userName;
				cometSession.enqueue(m);
			}

			@Override
			public void notifyGameStarted(Card[] cards) {
				if (httpSession == null || cometSession == null) {
					System.out.println("SerletNotInterf: session null");
					return;
				}
				GameStarted g = new GameStarted();
				g.myCards = cards;
				cometSession.enqueue(g);
			}

			@Override
			public void notifyGameEnded(int[] matchPoints,
					int[] playersTotalPoint) {
				if (httpSession == null || cometSession == null) {
					System.out.println("SerletNotInterf: session null");
					return;
				}
				GameEnded g = new GameEnded();
				g.matchPoints = matchPoints;
				g.playersTotalPoints = playersTotalPoint;
				cometSession.enqueue(g);
			}
		};
	}

	/*
	 * HttpListener use this interface to perform action
	 */
	public interface SessionClosedListener {
		/**
		 * HttpListener must call this method when httpSession is closing
		 */
		public void onSessionClosed(HttpSession httpSession);
	}

	@Override
	/*
	 * refers to GlobalChat. Client poll this method
	 */
	public ChatMessage[] viewLastMessages()
			throws UserNotAuthenticatedException, FatalException {
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			return null;
		}
		if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
			throw new UserNotAuthenticatedException();
		}
		GlobalChatInterface gci = (GlobalChatInterface) getServletContext()
				.getAttribute(GCI);
		try {
			return gci.getLastMessages();
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on viewLastMessages() catched RemoteException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		}
	}

	/*
	 * TODO: IllegalArgumentException in never thrown because input is never checked
	 * TODO: choose legal messages
	 * @see unibo.as.cupido.client.CupidoInterface#sendGlobalChatMessage(java.lang.String)
	 */
	@Override
	public void sendGlobalChatMessage(String message) throws IllegalArgumentException, UserNotAuthenticatedException,
	FatalException{
			HttpSession httpSession = getThreadLocalRequest().getSession(false);
			if (httpSession == null) {
				return;
			}
			if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
				throw new UserNotAuthenticatedException();
			}
			GlobalChatInterface gci = (GlobalChatInterface) getServletContext()
					.getAttribute(GCI);
			try {
				ChatMessage m= new ChatMessage((String) httpSession.getAttribute(USERNAME), message);
				gci.sendMessage(m);
			} catch (RemoteException e) {
				System.out
						.println("Servlet: on sendGlobalChatMessage() catched RemoteException-> "
								+ e.getMessage());
				// e.printStackTrace();
				throw new FatalException();
			}
	}

	/*
	 * TODO: IllegalArgumentExceptions never thrown TODO: choose legal messages
	 * 
	 * @see
	 * unibo.as.cupido.client.CupidoInterface#sendLocalChatMessage(java.lang
	 * .String)
	 */
	@Override
	public void sendLocalChatMessage(String message)
			throws IllegalArgumentException, UserNotAuthenticatedException,
			FatalException, NoSuchTableException {

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
			ChatMessage m = new ChatMessage(
					(String) httpSession.getAttribute(USERNAME), message);
			ti.sendMessage(m);
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on sendLocalChatMessage() catched RemoteException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (NoSuchUserException e) {
			throw new NoSuchTableException();
		}
	}

	/*
	 * Login errors are non-informative
	 * @see unibo.as.cupido.client.CupidoInterface#login(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean login(String username, String password) throws FatalException {
	
		/* FAKE LOGIN
		if (true){
			HttpSession httpSession = getThreadLocalRequest().getSession(false);
			if (httpSession != null){
				httpSession.setAttribute(ISAUTHENTICATED, true);
				httpSession.setAttribute(USERNAME, username);
				return true;
			}
		}
		return false;
		*/
		// comment lines down here to NOT check login with db
		DatabaseInterface dbi = (DatabaseInterface) getServletContext().getAttribute(DBI);
		Boolean authenticated;
		try{
			authenticated = dbi.login(username, password);
		} catch(IllegalArgumentException e){
			return false;
		} catch(NoSuchUserException e){
			return false;
		} catch(SQLException e){
			System.out
			.println("Servlet: on login() catched SQLException-> "
					+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		}
		if (authenticated){
			HttpSession httpSession = getThreadLocalRequest().getSession(false);
			if (httpSession != null){
				httpSession.setAttribute(ISAUTHENTICATED, true);
				httpSession.setAttribute(USERNAME, username);
				return true;
			}
		}
		else{
			return false;
		}
		return authenticated;
	}

	/*
	 * comment this method for fake registration
	 */
	@Override
	public void registerUser(String username, String password) throws FatalException, DuplicateUserNameException{
		
		DatabaseInterface dbi = (DatabaseInterface) getServletContext().getAttribute(DBI);
		try {
			dbi.addNewUser(username, password);
		} catch (IllegalArgumentException e) {
			throw new FatalException();
		} catch (SQLException e) {
			System.out.println("Servlet: on registerUser() catched SQLException ->");
			e.printStackTrace();
			throw new FatalException();
		} catch (DuplicateUserNameException e) {
			throw e;
		}
	
	}

	@Override
	public boolean isUserRegistered(String username) throws IllegalArgumentException, FatalException{
		DatabaseInterface dbi = (DatabaseInterface) getServletContext().getAttribute(DBI);
		try {
			return dbi.isRegistered(username);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (SQLException e) {
			System.out.println("Servlet: on isUserRegistered() catched SQLException ->");
			e.printStackTrace();
			throw new FatalException();
		}
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

		try {

			GlobalTableManagerInterface gtm = (GlobalTableManagerInterface) getServletContext()
					.getAttribute(GTMI);
			return gtm.getTableList();
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on getTableList() catched RemoteException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		}
	}

	/**
	 * Create a new table FIXME: ottieni il punteggio dal DB
	 * 
	 * @throws MaxNumTableReachedException
	 * 
	 * @throws RemoteException
	 *             in case of internal errors
	 * @throws AllLTMBusyException
	 *             if catch AllLTMBusyException
	 */
	@Override
	public InitialTableStatus createTable() throws MaxNumTableReachedException,
			UserNotAuthenticatedException, FatalException {
		InitialTableStatus its = new InitialTableStatus();
		try {
			GlobalTableManagerInterface gtm = (GlobalTableManagerInterface) getServletContext()
					.getAttribute(GTMI);

			HttpSession httpSession = getThreadLocalRequest().getSession(false);
			if (httpSession == null) {
				return null;
			}
			if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
				throw new UserNotAuthenticatedException();
			}

			CometSession cometSession = CometServlet
					.getCometSession(httpSession);
			if (cometSession == null) {
				return null;
			}

			ServletNotificationsInterface sni = getServletNotificationsInterface(
					httpSession, cometSession);
			httpSession.setAttribute(SNI, sni);
			httpSession.getAttribute(USERNAME);
			TableInterface ti = gtm.createTable(USERNAME, sni);
			httpSession.setAttribute(TI, ti);
			UnicastRemoteObject.exportObject(sni);

			// FIXME: retrieve point from DB
			its.playerScores[0] = 99;

		} catch (RemoteException e) {
			System.out
					.println("Servlet: on createTable() catched RemoteException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException("GTMLookupName not reachable");
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
			throws FullTableException, NoSuchTableException,
			NoSuchServerException, DuplicateUserNameException,
			UserNotAuthenticatedException, FatalException {
		try {
			HttpSession httpSession = getThreadLocalRequest().getSession(false);
			if (httpSession == null) {
				return null;
			}
			if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
				throw new UserNotAuthenticatedException();
			}

			CometSession cometSession = CometServlet
					.getCometSession(httpSession);
			if (cometSession == null) {
				httpSession.invalidate();
				return null;
			}

			GlobalTableManagerInterface gtm = (GlobalTableManagerInterface) getServletContext()
					.getAttribute(GTMI);
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
		} catch (DuplicateUserNameException e) {
			throw e;
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on joinTable() catched RemoteException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (NoSuchLTMException e) {
			System.out
					.println("Servlet: on joinTable() catched NoSuchLTMException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new NoSuchServerException();
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
			// In questo caso lo username non è registrato nel DB
			// vedi TableInterface.joinTable
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
			GlobalTableManagerInterface gtm = (GlobalTableManagerInterface) getServletContext()
					.getAttribute(GTMI);
			ltmi = gtm.getLTMInterface(server);
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
			throw new NoSuchTableException();
		}
	}

	/**
	 * Open connection and bind httpSession with remote service interfaces
	 */
	@Override
	public void openCometConnection() {

		System.out.println("Servlet: Opening a Comet connession...");

		// Get the HTTP session for the browser
		final HttpSession httpSession = getThreadLocalRequest()
				.getSession(true);
		if (httpSession == null) {
			return;
		}
		System.out
				.println("Servlet: HttpSession opened " + httpSession.getId());
		// TODO: istanzia us solo SessionClosedListener su ServletContext invece
		// che uno per sessione
		httpSession.setAttribute("sessionClosedListener",
				new SessionClosedListener() {
					@Override
					public void onSessionClosed(HttpSession hSession) {
						System.out
								.println("Servlet: onSessionClosed() was called.");
						// Notify player left at the table
						TableInterface ti = (TableInterface) hSession
								.getAttribute(TI);
						if (ti == null)
							return;
						try {
							ti.leaveTable((String) hSession
									.getAttribute(USERNAME));
						} catch (RemoteException e) {
							System.out
									.println("Servlet: in SessionClosedListener catched RemoteException ->");
							e.printStackTrace();
						} catch (PlayerNotFoundException e) {
							// Ignore
							return;
						}

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