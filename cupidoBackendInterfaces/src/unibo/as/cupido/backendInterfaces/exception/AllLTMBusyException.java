package unibo.as.cupido.backendInterfaces.exception;

import java.io.Serializable;

public class AllLTMBusyException extends Exception implements Serializable {

	public AllLTMBusyException(String string) {
		super(string);
	}

	public AllLTMBusyException() {
		// 
	}

	private static final long serialVersionUID = 1L;

}
