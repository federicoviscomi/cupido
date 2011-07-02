package unibo.as.cupido.common.exception;

import java.io.Serializable;

/**
 * Used when trying to add a viewer in a table but the table already contains a
 * viewer with same name
 */
public class DuplicateViewerException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;

	public DuplicateViewerException(String viewerName) {
		super(viewerName);
	}
	
	public DuplicateViewerException() {
		//
	}
}
