package unibo.as.cupido.backend.table;

import java.util.Arrays;

import unibo.as.cupido.backend.table.bot.NonRemoteBot;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;

public abstract class AsynchronousMessage {



	public static class BotActivateMessage extends AsynchronousMessage {
		public final int position;

		public BotActivateMessage(int position) {
			super(MessageType.BOT_ACTIVATE);
			this.position = position;
		}

		@Override
		public String toString() {
			return "[" + this.getClass().getSimpleName() + "]";
		}
	}

	public static class BotPassCardsMessage extends AsynchronousMessage {
		public final NonRemoteBot nonRemoteBot;
		public final int position;

		public BotPassCardsMessage(NonRemoteBot nonRemoteBot, int position) {
			super(MessageType.BOT_PASS);
			this.nonRemoteBot = nonRemoteBot;
			this.position = position;
		}

		@Override
		public String toString() {
			return "[" + this.getClass().getSimpleName() + "]";
		}
	}

	public static class BotPlayMessage extends AsynchronousMessage {
		public final NonRemoteBot nonRemoteBot;
		public final int position;

		public BotPlayMessage(NonRemoteBot nonRemoteBot, int position) {
			super(MessageType.BOT_PLAY);
			this.nonRemoteBot = nonRemoteBot;
			this.position = position;
		}

		@Override
		public String toString() {
			return "[" + this.getClass().getSimpleName() + "]";
		}
	}

	public static class EndGameMessage extends AsynchronousMessage {
		public EndGameMessage() {
			super(MessageType.END_GAME);
		}

		@Override
		public String toString() {
			return "[" + this.getClass().getSimpleName() + "]";
		}
	}

	public static class EndGamePrematurelyMessage extends AsynchronousMessage {
		public EndGamePrematurelyMessage() {
			super(MessageType.END_GAME_PREMATURELY);
		}

		@Override
		public String toString() {
			return "[" + this.getClass().getSimpleName() + "]";
		}
	}

	public enum MessageType {
		ADD_PLAYER, END_GAME_PREMATURELY, REPLACE_PLAYER, PLAYER_LEAVE, PASS_CARDS, PLAY_CARD, SEND_LOCAL_CHAT_MESSAGE, ALL_PASSED_CARDS, END_GAME, BOT_PLAY, BOT_ACTIVATE, BOT_PASS
	}

	public static class PlayerLeaveMessage extends AsynchronousMessage {

		public final String playerName;

		public PlayerLeaveMessage(String playerName) {
			super(MessageType.PLAYER_LEAVE);
			this.playerName = playerName;
		}

		@Override
		public String toString() {
			return "[" + this.getClass().getSimpleName() + ": player "
					+ playerName + "]";
		}

	}

	public static class PlayerPassCardsMessage extends AsynchronousMessage {

		final String playerName;
		final int position;
		final Card[] cards;

		public PlayerPassCardsMessage(String playerName, int position,
				Card[] cards) {
			super(MessageType.PASS_CARDS);
			this.playerName = playerName;
			this.position = position;
			this.cards = cards;
		}

		@Override
		public String toString() {
			return "[" + this.getClass().getSimpleName() + ": player name "
					+ playerName + ", position " + position + ", cards "
					+ Arrays.toString(cards) + "]";
		}
	}

	public static class PlayerPlayCardsMessage extends AsynchronousMessage {

		public final String playerName;
		public final int position;
		public final Card card;

		public PlayerPlayCardsMessage(String playName, int position, Card card) {
			super(MessageType.PLAY_CARD);
			this.playerName = playName;
			this.position = position;
			this.card = card;
		}

		@Override
		public String toString() {
			return "[" + this.getClass().getSimpleName() + ": player name "
					+ playerName + ", card " + card + ", position " + position
					+ "]";
		}

	}

	public static class ReplacePlayerMessage extends AsynchronousMessage {

		public final String playerName;
		public final int position;

		public ReplacePlayerMessage(String playerName, int position) {
			super(MessageType.REPLACE_PLAYER);
			this.playerName = playerName;
			this.position = position;
		}

		@Override
		public String toString() {
			return "[" + this.getClass().getSimpleName() + ": player name "
					+ playerName + ", position " + position + "]";
		}

	}

	public static class SendLocalChatMessage extends AsynchronousMessage {

		public final ChatMessage message;

		public SendLocalChatMessage(ChatMessage message) {
			super(MessageType.SEND_LOCAL_CHAT_MESSAGE);
			this.message = message;
		}

		@Override
		public String toString() {
			return "[" + this.getClass().getSimpleName() + ": message "
					+ message + "]";
		}
	}

	public final MessageType type;

	public AsynchronousMessage(MessageType type) {
		this.type = type;
	}
}
