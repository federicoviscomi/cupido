/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import unibo.as.cupido.common.interfaces.DatabaseInterface;
import unibo.as.cupido.common.interfaces.GlobalChatInterface;
import unibo.as.cupido.common.interfaces.GlobalTableManagerInterface;
import unibo.as.cupido.common.interfaces.LocalTableManagerInterface;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import net.zschech.gwt.comet.server.CometServlet;
import net.zschech.gwt.comet.server.CometSession;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.RankingEntry;
import unibo.as.cupido.common.structures.TableInfoForClient;
import unibo.as.cupido.common.exception.AllLTMBusyException;
import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.DuplicateViewerException;
import unibo.as.cupido.common.exception.FatalException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.MaxNumTableReachedException;
import unibo.as.cupido.common.exception.NoSuchLTMException;
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.exception.NoSuchServerException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.FullPositionException;
import unibo.as.cupido.common.exception.UserNotAuthenticatedException;
import unibo.as.cupido.common.exception.WrongGameStateException;
import unibo.as.cupido.common.database.DatabaseManager;
import unibo.as.cupido.client.CupidoInterface;
import unibo.as.cupido.shared.cometNotification.CardPassed;
import unibo.as.cupido.shared.cometNotification.CardPlayed;
import unibo.as.cupido.shared.cometNotification.GameEnded;
import unibo.as.cupido.shared.cometNotification.GameStarted;
import unibo.as.cupido.shared.cometNotification.NewLocalChatMessage;
import unibo.as.cupido.shared.cometNotification.NewPlayerJoined;
import unibo.as.cupido.shared.cometNotification.PlayerLeft;
import unibo.as.cupido.shared.cometNotification.PlayerReplaced;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Servlet implementation for Cupido Game.
 * @author Lorenzo Belli
 * 
 */
public class CupidoServlet extends RemoteServiceServlet implements
		CupidoInterface {

	private static final long serialVersionUID = 1L;

	private static final String CONFIGURATION_FILE = "servlet.config";
	private static final String CONFIGURATION_FILE2 = "webapps/cupido/servlet.config";
	/*
	 * hostname and port of registry, default is 127.0.0.1:1099 GTMLookupName
	 * and GCLookupName are name for registry lookup Values of GTMLookupName is
	 * defined in GlobalTableManagerInterface Values of GCLookupName is defined
	 * in GlobalChatInterface
	 */
	private static String registryHost = "127.0.0.1";
	private static int registryPort = 1099;
	private static final String GTMLookupName = GlobalTableManagerInterface.GTM_RMI_NAME;
	private static final String GCLookupName = GlobalChatInterface.GLOBAL_CHAT_RMI_NAME;

	/*
	 * Names used in httpSession attributes
	 */
	private static final String SNI = "servletNotificationInterface";
	private static final String TI = "tableInterfaces";
	private static final String USERNAME = "username";
	private static final String ISAUTHENTICATED = "isAuthenticated";
	/*
	 * Names used in servlet context attributes
	 */
	private static final String GCI = "globalChatInterface";
	private static final String GTMI = "globalTableManagerInterface";
	private static final String DBI = "databaseInterface";
	private static final String SCL = "sessionClosedListener";

	/**
	 * Number of entry to show in top rank list.
	 */
	private static final int NUMTOPRANKENTRIES = 10;

	/**
	 * Saves registry lookups in servlet context, and initialize DBmanager.
	 * Read configuration file
	 */
	@Override
	public void init(ServletConfig config) {
		String DBHostname = null;
		InputStream is=null, is1 =null, is2 = null;
		try{
		is1 = new FileInputStream(CONFIGURATION_FILE);
		}catch(FileNotFoundException e){
		}
		try{
			is2 = new FileInputStream(CONFIGURATION_FILE2);
			}catch(FileNotFoundException e){
		}
		is = (is1!=null?is1:is2);
		try {
			super.init(config);	
			Properties prop = new Properties();
			prop.load(is);
			CupidoServlet.registryHost = prop.getProperty(
					"servlet.registryHost", registryHost);
			CupidoServlet.registryPort = Integer.parseInt(prop.getProperty(
					"servlet.registryPort", String.valueOf(registryPort)));
			DBHostname = prop.getProperty("servlet.DBhostname");

		} catch (ServletException e) {
			System.out.println("Servlet: on init() catched ServletException->"
					+ e.getMessage());
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out
					.println("Servlet: on init() catched FileNotFoundException");
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			System.out.println("Servlet: on init() catched IOException");
			e.printStackTrace();
			System.exit(1);
		}

		Registry registry;
		GlobalChatInterface gci = null;
		GlobalTableManagerInterface gtmi = null;
		try {
			registry = LocateRegistry.getRegistry(registryHost, registryPort);
			gci = (GlobalChatInterface) registry.lookup(GCLookupName);
			gtmi = (GlobalTableManagerInterface) registry.lookup(GTMLookupName);
		} catch (RemoteException e) {
			System.out.println("Servlet: on init() catched RemoteException->"
					+ e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} catch (NotBoundException e) {
			System.out.println("Servlet: on init() catched NotBoundException->"
					+ e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		getServletContext().setAttribute(GCI, gci);
		getServletContext().setAttribute(GTMI, gtmi);
		DatabaseInterface dbi = null;
		try {
			dbi = new DatabaseManager(DBHostname);
		} catch (SQLException e1) {
			System.out.println("Servlet: on init() catched SQLException ->");
			e1.printStackTrace();
			System.exit(1);
		}
		getServletContext().setAttribute(DBI, dbi);
		getServletContext().setAttribute(SCL, new SessionClosedListener() {
			@Override
			public void onSessionClosed(HttpSession hSession) {
				// Notify player left at the table
				TableInterface ti = (TableInterface) hSession.getAttribute(TI);
				try {
					if (ti != null){
						ti.leaveTable((String) hSession.getAttribute(USERNAME));
					}
				} catch (RemoteException e) {
					System.out
							.println("Servlet: in SessionClosedListener catched RemoteException ->");
					e.printStackTrace();
				} catch (NoSuchPlayerException e) {
					// Nothing to do.
				} catch (GameInterruptedException e) {
					// Nothing to do.
				}
				hSession.removeAttribute(TI);
				hSession.removeAttribute(SNI);
			}
		});
		System.out.println("Servlet inited");
	}

	/**
	 * Implements here action to perform when notified by the table. A
	 * ServletNotificationsInterface object is bounded to a single httpSession.
	 * 
	 * @param hSession
	 *            HttpSession of the bounded client
	 * @param ctSession
	 *            CometSession of the bounded client
	 * 
	 * @return a ServletNotificationsInterface object bounded with the given
	 *         HttpSession and CometSession
	 */
	private ServletNotificationsInterface getServletNotificationsInterface(
			final HttpSession hSession, final CometSession cSession) {
		return new ServletNotificationsInterface() {
			private HttpSession httpSession = hSession;
			private CometSession cometSession = cSession;

			/**
			 * Notify PlayerLeft at the client
			 * 
			 * @see PlayerLeft
			 */
			@Override
			public void notifyPlayerLeft(String name) {
				if (httpSession == null || cometSession == null) {
					System.out.println("SerletNotInterf: session null");
					return;
				}
				cometSession.enqueue(new PlayerLeft(name));
			}

			/**
			 * Notify NewPlayerJoined at the client.
			 * 
			 * @see NewPlayerJoined
			 */
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

			/**
			 * Notify NewPlayerJoined at the client.
			 * 
			 * @see NewPlayerJoined
			 */
			@Override
			public void notifyPlayerReplaced(String botName, int position) {
				if (httpSession == null || cometSession == null) {
					System.out.println("SerletNotInterf: session null");
					return;
				}
				PlayerReplaced x = new PlayerReplaced();
				x.name = botName;
				x.position = position;
				System.out.println("Servlet: on notifyPlayerReplaced() sending  PlayerReplaced");
				cometSession.enqueue(x);
			}

			/**
			 * Notify CardPlayed at the client.
			 * 
			 * @see CardPlayed
			 */
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

			/**
			 * Notify CardPassed at the client.
			 * 
			 * @see CardPassed
			 */
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

			/**
			 * Notify NewLocalChatMessage at the client.
			 * 
			 * @see NewLocalChatMessage
			 */
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

			/**
			 * Notify GameStarted at the client.
			 * 
			 * @see GameStarted
			 */
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

			/**
			 * Notify GameEnded at the client, and remove attribute bindings in
			 * httpSession.
			 * 
			 * @see GameEnded
			 */
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

	/**
	 * CupidoSessionListener use this interface.
	 * 
	 * @see CupidoSessionListener
	 */
	public interface SessionClosedListener {
		/**
		 * HttpListener call this method when httpSession is closing
		 */
		public void onSessionClosed(HttpSession httpSession);
	}

	/**
	 * Show the last {@link GlobalChatInterface#MESSAGE_NUMBER} messages of the global chat. Client poll
	 * this method
	 */
	@Override
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
			e.printStackTrace();
			throw new FatalException();
		}
	}

	/**
	 * Send a message in global chat.
	 * 
	 * @throw FatalException if catch {@link RemoteException}
	 * @see GlobalChatInterface#sendMessage(ChatMessage)
	 */
	@Override
	public void sendGlobalChatMessage(String message)
			throws IllegalArgumentException, UserNotAuthenticatedException,
			FatalException {
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			return;
		}
		if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
			throw new UserNotAuthenticatedException();
		}
		//Case added for performance
		if (message.length() > GlobalChatInterface.MAX_CHAT_MESSAGE_LENGTH)
			throw new IllegalArgumentException();
		
		GlobalChatInterface gci = (GlobalChatInterface) getServletContext()
				.getAttribute(GCI);
		try {
			ChatMessage m = new ChatMessage(
					(String) httpSession.getAttribute(USERNAME), message);
			gci.sendMessage(m);
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on sendGlobalChatMessage() catched RemoteException-> "
							+ e.getMessage());
			e.printStackTrace();
			throw new FatalException();
		}
	}

	/**
	 * Send Message in Local chat.
	 * 
	 * @throws FatalException
	 *             if catch {@link RemoteException}
	 * @see TableInterface#sendMessage(ChatMessage)
	 */
	@Override
	public void sendLocalChatMessage(String message)
			throws IllegalArgumentException, UserNotAuthenticatedException,
			FatalException, NoSuchTableException, GameInterruptedException {

		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			return;
		}
		if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
			throw new UserNotAuthenticatedException();
		}
		//Case added for performance
		if (message.length() > GlobalChatInterface.MAX_CHAT_MESSAGE_LENGTH)
			throw new IllegalArgumentException();
		
		TableInterface ti = (TableInterface) httpSession.getAttribute(TI);
		if (ti == null) {
			System.out.println("Servlet: on sendLocalChatMessage() ti == null");
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
			e.printStackTrace();
			throw new FatalException();
		}
	}

	/**
	 * Binds httpSession with attribute {@link USERNAME} and
	 * {@link ISAUTHENTICATED}. Errors must be non-informative.
	 * 
	 * @throws FatalException
	 *             if catch a {@link SQLException}
	 * @see DatabaseInterface#login(String, String)
	 */
	@Override
	public boolean login(String username, String password)
			throws FatalException {
		DatabaseInterface dbi = (DatabaseInterface) getServletContext()
				.getAttribute(DBI);
		Boolean authenticated;
		try {
			authenticated = dbi.login(username, password);
		} catch (IllegalArgumentException e) {
			return false;
		} catch (NoSuchUserException e) {
			return false;
		} catch (SQLException e) {
			System.out.println("Servlet: on login() catched SQLException-> "
					+ e.getMessage());
			e.printStackTrace();
			throw new FatalException();
		}
		if (authenticated) {
			HttpSession httpSession = getThreadLocalRequest().getSession(false);
			if (httpSession != null) {
				httpSession.setAttribute(ISAUTHENTICATED, true);
				httpSession.setAttribute(USERNAME, username);
				return true;
			}
		} else {
			return false;
		}
		return authenticated;
	}

	/**
	 * Calls {@link DatabaseInterface#addNewUser(String, String)}
	 * 
	 * @throws FatalException
	 *             if catch {@link SQLException} or
	 *             {@link IllegalArgumentException}
	 * @see DatabaseInterface#addNewUser(String, String)
	 */
	@Override
	public void registerUser(String username, String password)
			throws FatalException, DuplicateUserNameException, IllegalArgumentException {

		DatabaseInterface dbi = (DatabaseInterface) getServletContext()
				.getAttribute(DBI);
		try {
			dbi.addNewUser(username, password);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (SQLException e) {
			System.out
					.println("Servlet: on registerUser() catched SQLException ->");
			e.printStackTrace();
			throw new FatalException();
		}
	}

	/**
	 * Check in the database if the user with name {@link username} is already
	 * registered.
	 * 
	 * @see DatabaseInterface#contains(String)
	 * @throws FatalException
	 *             if catch {@link SQLException}
	 */
	@Override
	public boolean isUserRegistered(String username)
			throws IllegalArgumentException, FatalException {
		DatabaseInterface dbi = (DatabaseInterface) getServletContext()
				.getAttribute(DBI);
		try {
			return dbi.contains(username);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (SQLException e) {
			System.out
					.println("Servlet: on isUserRegistered() catched SQLException ->");
			e.printStackTrace();
			throw new FatalException();
		}
	}

	/**
	 * Perform logout operation.
	 */
	@Override
	public void logout() {
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession != null) {
			httpSession.setAttribute(ISAUTHENTICATED, false);
			httpSession.removeAttribute(USERNAME);
		}
	}

	/**
	 * Retrieve table List from
	 * {@link GlobalTableManagerInterface#getTableList()}.
	 * 
	 * @throws FatalException
	 *             if catch {@link RemoteException}
	 */
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
			e.printStackTrace();
			throw new FatalException();
		}
	}

	/**
	 * Create a new table. Binds httpSession with its
	 * {@link ServletNotificationInterface} and {@link TableInterface}
	 * 
	 * @throws RemoteException
	 *             if catch {@link RemoteException} or
	 *             {@link IllegalArgumentException}
	 * @throws MaxNumTableReachedException
	 *             if catch {@link AllLTMBusyException}
	 * 
	 * @see GlobalTableManagerInterfaces#createTable(String,
	 *      ServletNotificationInterface)
	 */
	@Override
	public InitialTableStatus createTable() throws MaxNumTableReachedException,
			UserNotAuthenticatedException, FatalException {
		InitialTableStatus its = new InitialTableStatus();
		its.opponents = new String[3];
		its.playerScores = new int[3];
		its.whoIsBot = new boolean[3];
		its.opponents[0] = null;
		its.opponents[1] = null;
		its.opponents[2] = null;
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
			UnicastRemoteObject
					.exportObject((ServletNotificationsInterface) httpSession
							.getAttribute(SNI));
			String username = (String) httpSession.getAttribute(USERNAME);
			TableInterface ti = gtm.createTable(username, sni);
			httpSession.setAttribute(TI, ti);
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on createTable() catched RemoteException-> "
							+ e.getMessage());
			e.printStackTrace();
			throw new FatalException("GTMLookupName not reachable");
		} catch (AllLTMBusyException e) {
			throw new MaxNumTableReachedException();
		} catch (IllegalArgumentException e) {
			System.out
					.println("Servlet: on createTable catched IllegalArgumentException ->"
							+ e.getMessage());
			e.printStackTrace();
			throw new FatalException();
		}
		return its;
	}

	/**
	 * Binds httpSession with its
	 * {@link ServletNotificationInterface} and {@link TableInterface}
	 * 
	 * @param ltmId
	 *            identifier of LTM
	 * @param tableId
	 *            table identifier
	 * @return InitialTableStatus state of the match not yet started
	 * @see TableInterface#joinTable(String, ServletNotificationsInterface)
	 * @throws FatalException
	 *             if catch {@link RemoteException} or
	 *             {@link IllegalStateException} or
	 *             {@link IllegalSArgumentException} or
	 *             {@link NoSuchUserException} or
	 *             {#link AccessException}
	 */
	@Override
	public InitialTableStatus joinTable(String ltmId, int tableId)
			throws FullTableException, NoSuchTableException,
			NoSuchServerException, DuplicateUserNameException,
			UserNotAuthenticatedException, FatalException,
			GameInterruptedException {
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
			ServletNotificationsInterface sni = getServletNotificationsInterface(
					httpSession, cometSession);
			UnicastRemoteObject.exportObject(sni);
			httpSession.setAttribute(SNI, sni);
			return ti.joinTable((String) httpSession.getAttribute(USERNAME),
					sni);

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
			e.printStackTrace();
			throw new FatalException();
		} catch (NoSuchLTMException e) {
			System.out
					.println("Servlet: on joinTable() catched NoSuchLTMException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new NoSuchServerException(ltmId);
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
		} catch (NoSuchUserException e) {
			System.out
					.println("Servlet: on joinTable() catched NoSuchUserException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		}
	}

	/**
	 * Binds httpSession with its {@link ServletNotificationInterface} and
	 * {@link TableInterface}
	 * 
	 * @throws FatalException
	 *             if catch {@link AccessException} or {@link RemoteException}
	 *             or {@link DuplicateViewerException}.
	 * @see TableInterface#viewTable(String, ServletNotificationsInterface)
	 */
	@Override
	public ObservedGameStatus viewTable(String server, int tableId)
			throws NoSuchTableException, UserNotAuthenticatedException,
			FatalException, NoSuchServerException, WrongGameStateException,
			GameInterruptedException {

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
			e.printStackTrace();
			throw new FatalException();
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on viewTable() catched RemoteException1-> "
							+ e.getMessage());
			e.printStackTrace();
			throw new FatalException();
		} catch (NoSuchLTMException e) {
			throw new NoSuchServerException(server);
		}
		try {
			TableInterface ti = ltmi.getTable(tableId);
			httpSession.setAttribute(TI, ti);
			CometSession cometSession = CometServlet
					.getCometSession(httpSession);
			ServletNotificationsInterface sni = getServletNotificationsInterface(
					httpSession, cometSession);
			UnicastRemoteObject.exportObject(sni);
			httpSession.setAttribute(SNI, sni);
			return ti.viewTable((String) httpSession.getAttribute(USERNAME),
					sni);
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on viewTable() catched RemoteException2-> "
							+ e.getMessage());
			e.printStackTrace();
			throw new FatalException();
		} catch (NoSuchTableException e) {
			throw e;
		} catch (DuplicateViewerException e) {
			throw new FatalException();
		}
	}

	/**
	 * User leaves a table. Unbounds httpSession with its
	 * {@link ServletNotificationInterface} and {@link TableInterface}.
	 * 
	 * @see TableInterface#leaveTable(String)
	 * 
	 * @throws FatalException
	 *             if catch RemoteException
	 */
	@Override
	public void leaveTable() throws UserNotAuthenticatedException,
			NoSuchTableException, FatalException,
			GameInterruptedException {
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
			ti.leaveTable((String) httpSession.getAttribute(USERNAME));
		} catch (RemoteException e) {
			System.out
					.println("Servlet: onLeaveTable catched RemoteException ->"
							+ e.getMessage());
			e.printStackTrace();
			throw new FatalException();
		} catch (NoSuchPlayerException e) {
			throw new NoSuchTableException();
		}
		httpSession.removeAttribute(TI);
		httpSession.removeAttribute(SNI);
	}

	/**
	 * User play a card. Call {@link TableInterface#playCard()}
	 * 
	 * @throws FatalException
	 *             if catch {@link NoSuchPlayerException}
	 */
	@Override
	public void playCard(Card card) throws IllegalMoveException,
			FatalException, IllegalArgumentException, NoSuchTableException,
			UserNotAuthenticatedException, GameInterruptedException,
			WrongGameStateException {
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
			throw new NoSuchTableException();
		}

		try {
			ti.playCard((String) httpSession.getAttribute(USERNAME), card);
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on playCard() catched RemoteException ->"
							+ e.getMessage());
			e.printStackTrace();
			throw new FatalException();
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (IllegalMoveException e) {
			throw e;
		} catch (NoSuchPlayerException e) {
			throw new FatalException();
		}
	}

	/**
	 * Player pass three cards.
	 * 
	 * @see TableInterface#passCards(String, Card[])
	 * 
	 * @throws FatalException
	 *             if catch NoSuchPlayerException or RemoteException
	 */
	@Override
	public void passCards(Card[] cards) throws IllegalStateException,
			IllegalArgumentException, NoSuchTableException,
			UserNotAuthenticatedException, FatalException,
			GameInterruptedException, WrongGameStateException {
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
			e.printStackTrace();
			throw new FatalException();
		} catch (NoSuchPlayerException e) {
			throw new FatalException();
		}
	}

	/**
	 * @throws FatalException
	 *             if catch {@link RemoteException}
	 * @see TableInterface#addBot(String, int)
	 */
	@Override
	public String addBot(int position) throws FullPositionException,
			NotCreatorException, IllegalArgumentException,
			UserNotAuthenticatedException, FatalException,
			NoSuchTableException, GameInterruptedException {

		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		assert httpSession != null;
		if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
			throw new UserNotAuthenticatedException();
		}
		TableInterface ti = (TableInterface) httpSession.getAttribute(TI);
		if (ti == null) {
			throw new NoSuchTableException();
		}
		try {
			return ti.addBot((String) httpSession.getAttribute(USERNAME),
					position);
		} catch (RemoteException e) {
			System.out
					.println("Servlet: on addBot() catched RemoteException-> "
							+ e.getMessage());
			e.printStackTrace();
			throw new FatalException();
		}
	}

	/**
	 * Open Comet connection.
	 * 
	 * @see CometSession
	 */
	@Override
	public void openCometConnection() {
		final HttpSession httpSession = getThreadLocalRequest()
				.getSession(true);
		if (httpSession == null) {
			return;
		}
		System.out
				.println("Servlet: HttpSession opened " + httpSession.getId());
		httpSession.setAttribute(ISAUTHENTICATED, Boolean.FALSE);
		// Create the Comet session for the browser
		CometServlet.getCometSession(httpSession);
	}

	/**
	 * Calls {@link DatabaseInterface#getMyRank(int)}
	 * 
	 * @throws FatalException
	 *             if catch IllegalArgumentException or SQLException or
	 *             NoSuchUserException
	 */
	@Override
	public RankingEntry getMyRank() throws FatalException,
			UserNotAuthenticatedException {
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			return null;
		}
		if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
			throw new UserNotAuthenticatedException();
		}
		DatabaseInterface dbi = (DatabaseInterface) getServletContext()
				.getAttribute(DBI);
		try {
			String name = (String) httpSession.getAttribute(USERNAME);
			return dbi.getUserRank(name);
		} catch (IllegalArgumentException e) {
			System.out
					.println("Servlet: on getMyRank() catched IllegalArgumentException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (SQLException e) {
			System.out
					.println("Servlet: on getMyRank() catched SQLException-> "
							+ e.getMessage());
			e.printStackTrace();
			throw new FatalException();
		} catch (NoSuchUserException e) {
			System.out
					.println("Servlet: on getMyRank() catched NoSuchUserException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		}
	}

	/**
	 * RankingEntry of top {@link NUMTOPRANKENTRIES} players in the global
	 * ranking. Calls {@link DatabaseInterface#getTopRank(int)}
	 * 
	 * @throws FatalException
	 *             if catch SQLException or IllegalArgumentException
	 */
	@Override
	public ArrayList<RankingEntry> getTopRank()
			throws UserNotAuthenticatedException, FatalException {
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			return null;
		}
		if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
			throw new UserNotAuthenticatedException();
		}
		DatabaseInterface dbi = (DatabaseInterface) getServletContext()
				.getAttribute(DBI);
		try {
			return dbi.getTopRank(NUMTOPRANKENTRIES);
		} catch (IllegalArgumentException e) {
			System.out
					.println("Servlet: on getTopRank() catched IllegalArgumentException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (SQLException e) {
			System.out
					.println("Servlet: on getTopRank() catched SQLException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		}
	}

	/**
	 * Calls {@link DatabaseInterface#getLocalRank(String)}
	 * 
	 * @throws FatalException
	 *             when catch IllegalArgumentException or SQLException or
	 *             NoSuchUserException
	 */
	@Override
	public ArrayList<RankingEntry> getLocalRank()
			throws UserNotAuthenticatedException, FatalException {
		HttpSession httpSession = getThreadLocalRequest().getSession(false);
		if (httpSession == null) {
			return null;
		}
		if (!(Boolean) httpSession.getAttribute(ISAUTHENTICATED)) {
			throw new UserNotAuthenticatedException();
		}
		DatabaseInterface dbi = (DatabaseInterface) getServletContext()
				.getAttribute(DBI);
		try {
			return dbi
					.getLocalRank((String) httpSession.getAttribute(USERNAME));
		} catch (IllegalArgumentException e) {
			System.out
					.println("Servlet: on getLocalRank() catched IllegalArgumentException-> "
							+ e.getMessage());
			// e.printStackTrace();
			throw new FatalException();
		} catch (SQLException e) {
			System.out
					.println("Servlet: on getLocalRank() catched SQLException-> "
							+ e.getMessage());
			e.printStackTrace();
			throw new FatalException();
		} catch (NoSuchUserException e) {
			//DEAD CODE
			System.out
					.println("Servlet: on getLocalRank() catched NoSuchUserException-> "
							+ e.getMessage());
			e.printStackTrace();
			throw new FatalException();
		}
	}

	/**
	 * Invalidate httpSession.
	 */
	@Override
	public void destroySession() {
		getThreadLocalRequest().getSession(false).invalidate();
	}
}