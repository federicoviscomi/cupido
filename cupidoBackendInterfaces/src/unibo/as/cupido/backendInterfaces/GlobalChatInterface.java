package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import unibo.as.cupido.backendInterfaces.common.ChatMessage;

/**
 * 
 * used by the Servlet
 * implemented by the global chat component
 * the Servlet polls the global chat 
 * 
 * @author
 * 
 */
public interface GlobalChatInterface extends Remote {

	public static int MESSAGE_NUMBER = 10;

	/**
	 * 
	 * 
	 * 
	 * @return the last MESSAGE_NUMBER messages 
	 */
	public ChatMessage[] getLastMessages() throws RemoteException;

	/**
	 * @param message
	 */
	public void sendMessage(ChatMessage message) throws RemoteException;

}
