package unibo.as.cupido.server;

import javax.servlet.http.HttpSession;

import net.zschech.gwt.comet.server.CometServlet;
import net.zschech.gwt.comet.server.CometSession;
import unibo.as.cupido.backendInterfaces.common.Card;
import unibo.as.cupido.backendInterfaces.common.FullTableException;
import unibo.as.cupido.backendInterfaces.common.InitialTableStatus;
import unibo.as.cupido.backendInterfaces.common.NoSuchTableException;
import unibo.as.cupido.backendInterfaces.common.ObservedGameStatus;
import unibo.as.cupido.backendInterfaces.common.PositionFullException;
import unibo.as.cupido.client.CupidoInterface;
import unibo.as.cupido.shared.cometNotification.NewLocalChatMessage;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CupidoServlet extends RemoteServiceServlet implements CupidoInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String[] viewLastMessages() {
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
		
        CometSession cometSession = (CometSession) getServletContext().getAttribute("cometSession");
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

	@Override
	public InitialTableStatus createTable() {
		// TODO Auto-generated method stub
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

        // Get or create the Comet session for the browser
        CometSession cometSession = CometServlet.getCometSession(httpSession);
        
        getServletContext().setAttribute("cometSession", cometSession);
		
		// FIXME: Use the correct username.
        getServletContext().setAttribute("username", "pippo");

        System.out.println("Servlet: Comet connession opened.");
	}
}
