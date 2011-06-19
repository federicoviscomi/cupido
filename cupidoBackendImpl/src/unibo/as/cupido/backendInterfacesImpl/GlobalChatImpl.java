package unibo.as.cupido.backendInterfacesImpl;

import java.rmi.RemoteException;
import java.util.concurrent.ArrayBlockingQueue;

import unibo.as.cupido.common.interfaces.GlobalChatInterface;
import unibo.as.cupido.common.structures.ChatMessage;

public class GlobalChatImpl implements GlobalChatInterface {

	private ArrayBlockingQueue<ChatMessage> messages;

	public GlobalChatImpl() {
		messages = new ArrayBlockingQueue<ChatMessage>(MESSAGE_NUMBER);
	}

	@Override
	public ChatMessage[] getLastMessages() throws RemoteException {
		return messages.toArray(new ChatMessage[messages.size()]);
	}

	@Override
	public void sendMessage(ChatMessage message) throws RemoteException {
		if (messages.size() == MESSAGE_NUMBER) {
			messages.remove();
		}
		messages.add(message);
		System.out.println("global chat message " + message);
	}

}
