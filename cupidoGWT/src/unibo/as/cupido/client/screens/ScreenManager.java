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

package unibo.as.cupido.client.screens;

import java.util.ArrayList;
import java.util.Collection;

import unibo.as.cupido.client.CometMessageListener;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.RankingEntry;
import unibo.as.cupido.common.structures.TableInfoForClient;

/**
 * The interface implemented by the global screen manager.
 */
public interface ScreenManager {

	/**
	 * Shows the about screen instead of the current one.
	 * 
	 * @param username The username of the current user.
	 * 
	 * @see AboutScreen
	 */
	public void displayAboutScreen(String username);

	/**
	 * Shows the general error screen instead of the current one.
	 * 
	 * @param caught
	 *            the exception that generated the error.
	 *            
	 * @see GeneralErrorScreen
	 */
	public void displayGeneralErrorScreen(Throwable caught);

	/**
	 * Shows the loading screen instead of the current one.
	 * 
	 * @see LoadingScreen
	 */
	public void displayLoadingScreen();

	/**
	 * Shows the login screen instead of the current one.
	 * 
	 * @see LoginScreen
	 */
	public void displayLoginScreen();

	/**
	 * Shows the main menu screen instead of the current one.
	 * 
	 * @param username The username of the current user.
	 * 
	 * @see MainMenuScreen
	 */
	public void displayMainMenuScreen(String username);

	/**
	 * Shows the table screen (as a viewer) instead of the current one.
	 * 
	 * @param username The username of the current user.
	 * @param observedGameStatus Contains information about the current state of the game.
	 * 
	 * @see ObservedTableScreen
	 */
	public void displayObservedTableScreen(String username,
			ObservedGameStatus observedGameStatus);

	/**
	 * Shows the login screen instead of the current one.
	 */
	public void displayRegistrationScreen();

	/**
	 * Shows the scores' screen instead of the current one.
	 * 
	 * @param username The username of the current user.
	 * @param topRanks The <code>RankingEntry</code> objects referring to the top 10 users.
	 * @param localRanks The <code>RankingEntry</code> objects referring to the current user
	 *             and to users with similar ranks.
	 * 
	 * @see ScoresScreen
	 */
	public void displayScoresScreen(String username,
			ArrayList<RankingEntry> topRanks, ArrayList<RankingEntry> localRanks);

	/**
	 * Shows the table list screen instead of the current one.
	 * 
	 * @param username The username of the current user.
	 * @param tableCollection The list of tables available for joining and/or viewing.
	 * 
	 * @see TableListScreen
	 */
	public void displayTableListScreen(String username,
			Collection<TableInfoForClient> tableCollection);

	/**
	 * Shows the table screen (as a player) instead of the current one.
	 * 
	 * @param username The username of the current user.
	 * @param isOwner Specifies whether or not the current user is the creator of this table.
	 * @param initialTableStatus Contains information about the current state of the table.
	 * @param userScore The global score of the current user.
	 * 
	 * @see TableScreen
	 */
	public void displayTableScreen(String username, boolean isOwner,
			InitialTableStatus initialTableStatus, int userScore);

	/**
	 * Changes the listener used to handle comet messages received
	 * from the servlet.
	 * 
	 * @param listener The listener that will handle comet messages.
	 */
	public void setListener(CometMessageListener listener);
}
