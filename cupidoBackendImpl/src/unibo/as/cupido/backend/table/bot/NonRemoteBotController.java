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

package unibo.as.cupido.backend.table.bot;

public class NonRemoteBotController extends Thread {

	NonRemoteBot bot;
	private boolean ableToPass = false;
	private boolean ableToPlay = false;
	Object lock = new Object();
	private boolean gameEnded = false;
	private final String botName;

	public NonRemoteBotController(NonRemoteBot bot, String botName) {
		super("NonRemoteBotController " + botName);
		this.botName = botName;
		if (bot == null || botName == null) {
			throw new IllegalArgumentException();
		}
		this.bot = bot;
	}

	public NonRemoteBotController(String botName) {
		super("NonRemoteBotController " + botName);
		this.botName = botName;
		if (botName == null) {
			throw new IllegalArgumentException();
		}
		this.bot = null;
	}

	@Override
	public void run() {
		try {
			synchronized (lock) {
				while (!ableToPass) {
					lock.wait();
				}
				if (gameEnded) {
					return;
				}
				if (bot != null) {
					bot.passCards();
				}
			}
			for (int i = 0; i < 13; i++) {
				System.out.println("non remote bot controller " + botName
						+ " turn " + i + " waiting ... ");
				synchronized (lock) {
					while (!ableToPlay) {
						lock.wait();
					}
					System.out.println("non remote bot controller " + botName
							+ " turn " + i + " notified ");
					if (gameEnded) {
						System.out.println("non remote bot controller "
								+ botName + " turn " + i + " game ended ");
						return;
					}
					ableToPlay = false;
					if (bot != null) {
						System.out.println("non remote bot controller "
								+ botName + " turn " + i
								+ " playing a card ... ");
						bot.playNextCard();
						System.out.println("non remote bot controller "
								+ botName + " turn " + i + " played ");
					} else {
						System.out
								.println("non remote bot controller "
										+ botName
										+ " turn "
										+ i
										+ " not playing a card because it is inactive ");
					}
				}
			}
		} catch (InterruptedException e) {
			//
		}
	}

	public void setAbleToPass() {
		synchronized (lock) {
			if (!ableToPass) {
				ableToPass = true;
				lock.notify();
			}
		}
	}

	public void setAbleToPlay() {
		synchronized (lock) {
			ableToPlay = true;
			lock.notify();
		}
	}

	public void setGameEnded() {
		synchronized (lock) {
			gameEnded = true;
			lock.notify();
		}
	}

}
