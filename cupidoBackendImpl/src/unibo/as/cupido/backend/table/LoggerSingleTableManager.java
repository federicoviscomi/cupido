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

package unibo.as.cupido.backend.table;

import java.rmi.RemoteException;
import java.util.Arrays;

import unibo.as.cupido.common.exception.DuplicateUserNameException;
import unibo.as.cupido.common.exception.DuplicateViewerException;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.IllegalMoveException;
import unibo.as.cupido.common.exception.NoSuchPlayerException;
import unibo.as.cupido.common.exception.NoSuchUserException;
import unibo.as.cupido.common.exception.NotCreatorException;
import unibo.as.cupido.common.exception.FullPositionException;
import unibo.as.cupido.common.interfaces.ServletNotificationsInterface;
import unibo.as.cupido.common.interfaces.TableInterface;
import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;

/**
 * An STM that does nothing but log. This is used by replacement bot prior to
 * activation.
 * 
 * @see PlayersManager
 */
public final class LoggerSingleTableManager implements TableInterface {

	/** the default instance of this class */
	public static final TableInterface defaultInstance = new LoggerSingleTableManager();

	/**
	 * Called just once to create {@link #defaultInstance}. It is not necessary
	 * to make other instance of this.
	 */
	private LoggerSingleTableManager() {
		//
	}

	@Override
	public String addBot(String userName, int position)
			throws FullPositionException, RemoteException,
			IllegalArgumentException, NotCreatorException {
		throw new IllegalStateException(
				"a replacement bot should not call this");
	}

	@Override
	public InitialTableStatus joinTable(String userName,
			ServletNotificationsInterface snf) throws FullTableException,
			RemoteException, IllegalArgumentException,
			DuplicateUserNameException, NoSuchUserException {
		throw new IllegalStateException(
				"a replacement bot should not call this");
	}

	@Override
	public void leaveTable(String userName) throws RemoteException,
			NoSuchPlayerException {
		throw new IllegalStateException(
				"a replacement bot should not call this");
	}

	@Override
	public void passCards(String userName, Card[] cards)
			throws IllegalArgumentException, IllegalStateException,
			RemoteException {
		System.err.println("logger stm: passCards(" + userName + ", "
				+ Arrays.toString(cards) + ")");
	}

	@Override
	public void playCard(String userName, Card card)
			throws IllegalMoveException, RemoteException,
			IllegalArgumentException {
		System.err.println("logger stm: playCard(" + userName + ", " + card
				+ ")");
	}

	@Override
	public void sendMessage(ChatMessage message) throws RemoteException {
		throw new IllegalStateException(
				"a replacement bot should not call this");
	}

	@Override
	public ObservedGameStatus viewTable(String userName,
			ServletNotificationsInterface snf) throws RemoteException,
			DuplicateViewerException {
		throw new IllegalStateException(
				"a replacement bot should not call this");
	}

}
