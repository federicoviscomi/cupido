package unibo.as.cupido.client;

import java.io.Serializable;
import java.util.List;

import net.zschech.gwt.comet.client.CometListener;

/**
 * This class allows to dynamically change the handlers of the various Comet notifications,
 * using the setListener() method.
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
