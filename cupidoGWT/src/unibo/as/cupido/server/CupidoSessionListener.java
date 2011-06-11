package unibo.as.cupido.server;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import unibo.as.cupido.server.CupidoServlet.SessionClosedListener;

public class CupidoSessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent e) {
		System.out.println("DumbSessionListener: in sessionCreated().");
		// Expire after 20 seconds.
		e.getSession().setMaxInactiveInterval(20);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent x) {
		System.out.println("DumbSessionListener: in sessionDestroyed().");
		Object untypedListener = x.getSession().getAttribute("sessionClosedListener");
		if (untypedListener == null)
			return;
		if (!(untypedListener instanceof SessionClosedListener))
			return;
		SessionClosedListener listener = (SessionClosedListener) untypedListener;
		listener.onSessionClosed();
	}

}
