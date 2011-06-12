package unibo.as.cupido.client;

import com.google.gwt.animation.client.Animation;

public abstract class SimpleAnimation implements GWTAnimation {

	private int ms;

	SimpleAnimation(int ms) {
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
