package unibo.as.cupido.backendInterfaces.common;

import java.io.Serializable;

public class TableInfoForClient implements Serializable {
	private static final long serialVersionUID = -3002842357423083821L;
	public String owner;
	public int freePosition;
	public TableDescriptor tableDescriptor;

	public TableInfoForClient() {
		//
	}

	public TableInfoForClient(String owner, int freePosition,
			TableDescriptor tableDescriptor) {
		this.owner = owner;
		this.freePosition = freePosition;
		this.tableDescriptor = tableDescriptor;
	}

	/**
	 * A Table is uniquely identified by two things: the server it's managed by,
	 * the unique id the Table has within that server. This method is used by an
	 * hash map in the TableManager
	 */
	@Override
	public int hashCode() {
		return tableDescriptor.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return tableDescriptor.equals(((TableInfoForClient) o).tableDescriptor);
	}

	@Override
	public String toString() {
		return "[owner=" + owner + ", free position=" + freePosition
				+ ", server=" + tableDescriptor.ltmId + ", table id="
				+ tableDescriptor.id + "]";
	}
}
