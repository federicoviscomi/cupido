package unibo.as.cupido.common.exception;

/**
 * Used to signal that specified viewer is missing from a table
 */
public class NoSuchViewerException extends Exception {

	public NoSuchViewerException(String viewerName) {
		super(viewerName);
	}

	private static final long serialVersionUID = 1L;
}
