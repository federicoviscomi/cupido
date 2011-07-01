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

public interface ScreenManager {

	/**
	 * Shows the about screen instead of the current one.
	 */
	public void displayAboutScreen(String username);

	/**
	 * Shows the general error screen instead of the current one.
	 * 
	 * @param caught
	 *            the exception that generated the error.
	 */
	public void displayGeneralErrorScreen(Throwable caught);

	/**
	 * Shows the loading screen instead of the current one.
	 */
	public void displayLoadingScreen();

	/**
	 * Shows the login screen instead of the current one.
	 */
	public void displayLoginScreen();

	/**
	 * Shows the main menu screen instead of the current one.
	 */
	public void displayMainMenuScreen(String username);

	/**
	 * Shows the table screen (as a viewer) instead of the current one.
	 * 
	 * @param observedGameStatus
	 */
	public void displayObservedTableScreen(String username,
			ObservedGameStatus observedGameStatus);

	/**
	 * Shows the login screen instead of the current one.
	 */
	public void displayRegistrationScreen();

	/**
	 * Shows the scores' screen instead of the current one.
	 */
	public void displayScoresScreen(String username,
			ArrayList<RankingEntry> topRanks, ArrayList<RankingEntry> localRanks);

	/**
	 * Shows the table list screen instead of the current one.
	 */
	public void displayTableListScreen(String username,
			Collection<TableInfoForClient> tableCollection);

	/**
	 * Shows the table screen (as a player) instead of the current one.
	 * 
	 * @param inititalTableStatus
	 */
	public void displayTableScreen(String username, boolean isOwner,
			InitialTableStatus inititalTableStatus, int userScore);

	public void setListener(CometMessageListener listener);
}
