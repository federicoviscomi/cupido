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

/**
 * An interface implemented by GUI animations.
 */
public interface GWTAnimation {

	/**
	 * @return The duration of the animation, in milliseconds.
	 */
	public int duration();
	
	/**
	 * Interrupts the animation, if it is running.
	 */
	public void cancel();

	/**
	 * Start the animation. When it finishes, the callback is invoked.
	 */
	public void run(AnimationCompletedListener listener);

	/**
	 * This is called just before starting the animation.
	 */
	public void onStart();

	/**
	 * This is called when the animation is finished, but before calling the
	 * listener.
	 */
	public void onComplete();

	static public interface AnimationCompletedListener {
		public void onComplete();
	}
}
