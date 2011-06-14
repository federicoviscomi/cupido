package unibo.as.cupido.backendInterfaces.exception;

/**
 * Thrown by the TableManager when a user attemps to add a bot in a table he is
 * not the creator of.
 * 
 * @author cane
 * 
 */
public class NotCreatorException extends Exception {

	public NotCreatorException(String string) {
		super(string);
	}

}
