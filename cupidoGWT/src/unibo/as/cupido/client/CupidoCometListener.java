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

package unibo.as.cupido.client;

import java.io.Serializable;
import java.util.List;

import net.zschech.gwt.comet.client.CometListener;

/**
 * This class allows to dynamically change the handlers of the various Comet
 * notifications, using the setListener() method.
 * 
 * @author Marco Poletti
 */
public class CupidoCometListener implements CometListener {

	private CometListener impl;

	public CupidoCometListener(CometListener x) {
		setListener(x);
	}

	public void setListener(CometListener x) {
		impl = x;
	}

	@Override
	public void onConnected(int heartbeat) {
		impl.onConnected(heartbeat);
	}

	@Override
	public void onDisconnected() {
		impl.onDisconnected();
	}

	@Override
	public void onError(Throwable exception, boolean connected) {
		impl.onError(exception, connected);
	}

	@Override
	public void onHeartbeat() {
		impl.onHeartbeat();
	}

	@Override
	public void onRefresh() {
		impl.onRefresh();
	}

	@Override
	public void onMessage(List<? extends Serializable> messages) {
		impl.onMessage(messages);
	}
}
