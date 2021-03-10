package mc.decodedlogic.gucciislandtop.valuable;

public class Attribute extends Commodity {
	
	protected final String cfgName;
	
	public Attribute(String name, String cfgName) {
		super(name);
		this.cfgName = cfgName;
	}
	
	public String getConfigName() {
		return this.cfgName;
	}

}
