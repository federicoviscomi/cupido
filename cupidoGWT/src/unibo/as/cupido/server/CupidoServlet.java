package unibo.as.cupido.server;

import java.net.InetAddress;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.LocalTableManagerInterface;
import unibo.as.cupido.backendInterfaces.GlobalTableManagerInterface.Table;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.ChatMessage;
import unibo.as.cupido.backendInterfaces.common.FullTableException;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PositionFullException;
import unibo.as.cupido.client.CupidoInterface;
import unibo.as.cupido.client.GlobalChatInterface;
import unibo.as.cupido.client.TableInterface;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CupidoServlet extends RemoteServiceServlet implements CupidoInterface, GlobalChatInterface, TableInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	
	public CupidoServlet() {
		GlobalTableManagerInterface.ServletNotifcationsInterface a;
		// UnicastRemoteObject a;
	}

	@Override
	public ChatMessage[] viewLastMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendMessage(String message) {
		// TODO Auto-generated method stub

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

	@Override
	public InitialTableStatus createTable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InitialTableStatus joinTable(String server, int tableId) throws FullTableException, NoSuchTableException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObservedGameStatus viewTable(String server, int tableId) throws NoSuchTableException {
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

}
