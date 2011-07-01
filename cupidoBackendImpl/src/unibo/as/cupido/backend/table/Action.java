package unibo.as.cupido.backend.table;

/**
 * A generic action that can be executed sometime in the future.
 */
public interface Action {
	/**
	 * Executes this action.
	 */
	public void execute();
}
