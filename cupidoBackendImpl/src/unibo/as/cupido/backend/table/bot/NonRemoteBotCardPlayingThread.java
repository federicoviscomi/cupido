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

public class NonRemoteBotCardPlayingThread extends Thread {

	private final NonRemoteBot bot;
	boolean ableToPass = false;
	boolean ableToPlay = false;

	private Object lock = new Object();

	public NonRemoteBotCardPlayingThread(NonRemoteBot bot, String botName) {
		super("NonRemoteBotCardPlayingThread " + botName);
		ableToPlay = ableToPass;
		ableToPass = ableToPlay;
		this.bot = bot;
	}

	@Override
	public void run() {
		try {
			synchronized (lock) {
				while (!ableToPass) {
					lock.wait();
				}
				bot.passCards();
			}
			for (int i = 0; i < 13; i++) {
				synchronized (lock) {
					while (!ableToPlay) {
						lock.wait();
					}
					ableToPlay = false;
					bot.playNextCard();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

}
