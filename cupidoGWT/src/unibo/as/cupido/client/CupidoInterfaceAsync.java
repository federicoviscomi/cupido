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

import java.util.ArrayList;
import java.util.Collection;

import unibo.as.cupido.common.structures.Card;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.RankingEntry;
import unibo.as.cupido.common.structures.TableInfoForClient;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CupidoInterfaceAsync {

	void logout(AsyncCallback<Void> callback);

	void isUserRegistered(String username, AsyncCallback<Boolean> callback);

	void registerUser(String username, String password,
			AsyncCallback<Void> callback);

	void login(String username, String password, AsyncCallback<Boolean> callback);

	void getTableList(AsyncCallback<Collection<TableInfoForClient>> callback);

	void createTable(AsyncCallback<InitialTableStatus> callback);

	void joinTable(String server, int tableId,
			AsyncCallback<InitialTableStatus> callback);

	void viewTable(String server, int tableId,
			AsyncCallback<ObservedGameStatus> callback);

	void openCometConnection(AsyncCallback<Void> callback);

	void leaveTable(AsyncCallback<Void> callback);

	void playCard(Card card, AsyncCallback<Void> callback);

	void passCards(Card[] cards, AsyncCallback<Void> callback);

	void addBot(int position, AsyncCallback<String> callback);

	void viewLastMessages(AsyncCallback<ChatMessage[]> callback);

	void sendGlobalChatMessage(String message, AsyncCallback<Void> callback);

	void sendLocalChatMessage(String message, AsyncCallback<Void> callback);

	void destroySession(AsyncCallback<Void> callback);

	void getMyRank(AsyncCallback<RankingEntry> callback);

	void getLocalRank(AsyncCallback<ArrayList<RankingEntry>> callback);

	void getTopRank(AsyncCallback<ArrayList<RankingEntry>> callback);
}