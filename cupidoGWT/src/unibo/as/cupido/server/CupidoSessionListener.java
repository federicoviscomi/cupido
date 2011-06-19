package unibo.as.cupido.server;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import unibo.as.cupido.server.CupidoServlet.SessionClosedListener;

/**
 * Listen to event oh httpSession.
 *
 */
public class CupidoSessionListener implements HttpSessionListener {
	/*
	 * Name used in servlet context. Value of SCL can be found in CupidoServlet
	 */
	private static final String SCL = "sessionClosedListener";
	@Override
	public void sessionCreated(HttpSessionEvent e) {
		System.out.println("DumbSessionListener: in sessionCreated().");
		// Expire after 20 seconds.
		e.getSession().setMaxInactiveInterval(20);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent x) {
		Object untypedListener = x.getSession().getServletContext().getAttribute(
				SCL);
		if (untypedListener == null)
			return;
		if (!(untypedListener instanceof SessionClosedListener))
			return;
		SessionClosedListener listener = (SessionClosedListener) untypedListener;
		listener.onSessionClosed(x.getSession());
		System.out.println("CupidoSessionListener (Servlet): destroyed httpSession "+x.getSession().getId());
	}

}
