package unibo.as.cupido.backendInterfaces.common;

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
