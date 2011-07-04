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

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import unibo.as.cupido.server.CupidoServlet.SessionClosedListener;

/**
 * Listen to destoy event oh httpSession.
 *
 */
public class CupidoSessionListener implements HttpSessionListener {
	/*
	 * Name used in servlet context. Value of SCL can be found in CupidoServlet
	 */
	private static final String SCL = "sessionClosedListener";
	@Override
	public void sessionCreated(HttpSessionEvent e) {
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
