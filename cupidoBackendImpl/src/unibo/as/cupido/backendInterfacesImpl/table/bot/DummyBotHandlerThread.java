package unibo.as.cupido.backendInterfacesImpl.table.bot;

import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.InitialTableStatus;

public class DummyBotHandlerThread extends AbstractBot implements Runnable {
	private final Bot bot;

	public DummyBotHandlerThread(Bot bot,
			InitialTableStatus initialTableStatus,
			TableInterface singleTableManager, String botName) {
		super(initialTableStatus, singleTableManager, botName);
		this.bot = bot;
	}

	@Override
	public void run() {

	}
}
