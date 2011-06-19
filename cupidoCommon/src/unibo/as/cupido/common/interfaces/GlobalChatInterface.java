package unibo.as.cupido.common.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import unibo.as.cupido.common.structures.ChatMessage;

/**
 * This interface is used by the Servlet. The Servlet is not notified when a
 * message is sent to the global chat. Instead the Servlet pools the global chat
 * component.
 * <p>
 * TODO how do we justify this choice?
 * <p>
 * The global chat component is a remote RMI object who is registered in the
 * same remote registry of the GTM and is bounded to name
 * <code>globalChatName</code>
 * 
 * @author
 * 
 */
public interface GlobalChatInterface extends Remote {

	public static int MESSAGE_NUMBER = 10;

	public static final String globalChatName = "globalChat";

	/**
	 * @return the last MESSAGE_NUMBER messages
	 */
	public ChatMessage[] getLastMessages() throws RemoteException;

	/**
	 * Send a message to the global chat.
	 * 
	 * @param message
	 *            contains user name of the user who wants to send a message to
	 *            the global chat and the message he wants to send
	 */
	public void sendMessage(ChatMessage message) throws RemoteException;

}
