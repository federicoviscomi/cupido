package unibo.as.cupido.backendInterfaces.common;

public class TableDescriptor {
	public int id;

	public String ltmId;

	public TableDescriptor(String server, int id) {
		this.ltmId = server;
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		TableDescriptor otd = (TableDescriptor) o;
		return (otd.id == this.id && otd.ltmId.equals(this.ltmId));
	}

	@Override
	public int hashCode() {
		return (id + ltmId).hashCode();
	}
}
