package unibo.as.cupido.backendInterfaces;

import java.rmi.Remote;


/**
 *  
 * 
 * @author cane
 *
 */
public interface GlobalChatInterface extends Remote{

	/**
	 * 
	 * @param message
	 */
	public void sendMessage(String message);
	
	/**
	 * 
	 * @return
	 */
	public String[] getLastMessages();
	
}
