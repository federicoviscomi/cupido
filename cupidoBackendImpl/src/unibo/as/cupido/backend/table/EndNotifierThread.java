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

public class EndNotifierThread extends Thread {

	private final SingleTableManager stm;
	final Object lock = new Object();
	boolean gameEnded = false;
	boolean gameEndedPrematurely = false;

	public EndNotifierThread(SingleTableManager stm) {
		this.stm = stm;
	}

	@Override
	public void run() {
		try {
			synchronized (lock) {
				while (!gameEnded && !gameEndedPrematurely) {
					lock.wait();
				}
				if (gameEnded) {
					stm.notifyGameEnded();
				} else {
					stm.notifyGameEndedPrematurely();
				}
			}
		} catch (InterruptedException e) {
			// TODO 
			e.printStackTrace();
		}
	}
}
