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

import com.google.gwt.animation.client.Animation;

public abstract class SimpleAnimation implements GWTAnimation {

	private int ms;

	public SimpleAnimation(int ms) {
		this.ms = ms;
	}

	@Override
	public int duration() {
		return ms;
	}

	public abstract void onUpdate(double progress);

	/**
	 * The default implementation is empty. Callers can override this if needed.
	 */
	@Override
	public void onStart() {
	}

	/**
	 * The default implementation is empty. Callers can override this if needed.
	 */
	@Override
	public void onComplete() {
	}

	@Override
	public void run(final AnimationCompletedListener listener) {
		onStart();
		final SimpleAnimation simpleAnimation = this;
		Animation animation = new Animation() {

			@Override
			protected void onUpdate(double progress) {
				simpleAnimation.onUpdate(progress);
			}

			@Override
			protected void onComplete() {
				super.onComplete();
				simpleAnimation.onComplete();
				listener.onComplete();
			}
		};
		animation.run(ms);
	}
}
