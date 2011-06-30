package unibo.as.cupido.backend.table;

import static org.junit.Assert.*;

import java.rmi.RemoteException;

import org.junit.Test;

public class TestController {

	private int state = 0;
	boolean successful = true;

	protected void processFirstMessage() {
		successful = (successful && (state == 2));
		state++;
	}

	protected void processSecondMessage(ActionQueue controller) {
		successful = (successful && (state == 3));

		controller.interrupt();

		assertTrue(successful);
	}

	@Test
	public void testEnqueueNotification() {

		final ActionQueue controller = new ActionQueue();
		controller.start();

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			assertTrue(false);
		}

		controller.enqueue(new RemoteAction() {
			@Override
			public void onExecute() throws RemoteException {
				processFirstMessage();
			}
		});

		state++;

		controller.enqueue(new RemoteAction() {
			@Override
			public void onExecute() throws RemoteException {
				processSecondMessage(controller);
			}
		});

		state++;
	}
}
