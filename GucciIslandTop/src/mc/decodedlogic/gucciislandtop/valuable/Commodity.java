package mc.decodedlogic.gucciislandtop.valuable;

public abstract class Commodity {
	
	protected String name;
	protected double worth;
	protected int quantity;
	
	public Commodity(String name) {
		this.setName(name);
		this.setWorth(0.0);
		this.setQuantity(0);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setWorth(double worth) {
		this.worth = worth;
	}
	
	public double getWorth() {
		return this.worth;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public int getQuantity() {
		return this.quantity;
	}
	
}
