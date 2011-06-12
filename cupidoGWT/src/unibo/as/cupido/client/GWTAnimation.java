package unibo.as.cupido.client;

public interface GWTAnimation {

	/**
	 * @return The duration of the animation, in milliseconds.
	 */
	public int duration();

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
