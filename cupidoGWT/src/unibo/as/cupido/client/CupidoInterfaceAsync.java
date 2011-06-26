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

	public void logout(AsyncCallback<Void> callback);

	public void isUserRegistered(String username, AsyncCallback<Boolean> callback);

	public void registerUser(String username, String password,
			AsyncCallback<Void> callback);

	public void login(String username, String password, AsyncCallback<Boolean> callback);

	public void getTableList(AsyncCallback<Collection<TableInfoForClient>> callback);

	public void createTable(AsyncCallback<InitialTableStatus> callback);

	public void joinTable(String server, int tableId,
			AsyncCallback<InitialTableStatus> callback);

	public void viewTable(String server, int tableId,
			AsyncCallback<ObservedGameStatus> callback);

	public void openCometConnection(AsyncCallback<Void> callback);

	public void leaveTable(AsyncCallback<Void> callback);

	public void playCard(Card card, AsyncCallback<Void> callback);

	public void passCards(Card[] cards, AsyncCallback<Void> callback);

	public void addBot(int position, AsyncCallback<String> callback);

	public void viewLastMessages(AsyncCallback<ChatMessage[]> callback);

	public void sendGlobalChatMessage(String message, AsyncCallback<Void> callback);

	public void sendLocalChatMessage(String message, AsyncCallback<Void> callback);

	public void destroySession(AsyncCallback<Void> callback);

	public void getMyRank(AsyncCallback<RankingEntry> callback);

	public void getLocalRank(AsyncCallback<ArrayList<RankingEntry>> callback);

	public void getTopRank(AsyncCallback<ArrayList<RankingEntry>> callback);
}