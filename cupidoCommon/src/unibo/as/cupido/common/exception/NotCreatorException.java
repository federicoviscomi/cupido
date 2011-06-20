package unibo.as.cupido.common.exception;

/**
 * Thrown by the TableManager when a user attemps to add a bot in a table he is
 * not the creator of.
 * 
 * @author cane
 * 
 */
public class NotCreatorException extends Exception {
	private static final long serialVersionUID = 1L;
	public NotCreatorException(){
	}
	public NotCreatorException(String string) {
		super(string);
	}

}
