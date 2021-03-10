package mc.decodedlogic.gucciislandtop.valuable;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

public class Valuable extends Commodity {
	
	private List<Attribute> attributes;
	private final MaterialData materialData;
	private MaterialData iconData;
	private String headName;
	private int slot;
	
	public Valuable(MaterialData data) {
		super(null);
		
		this.attributes = new ArrayList<Attribute>();
		this.materialData = data;
		this.iconData = null;
		this.headName = null;
		this.slot = -1;
	}
	
	public double calculateOverallWorth() {
		double worthPerObject = worth * quantity;
		
		for(Attribute attr : attributes) {
			worthPerObject += (attr.getWorth() * attr.getQuantity());
		}
		
		return worthPerObject;
	}
	
	public void addAttribute(Attribute attr) {
		this.attributes.add(attr);
	}
	
	public List<Attribute> getAttributes() {
		return this.attributes;
	}
	
	@SuppressWarnings("deprecation")
	public boolean isValuable(Block block) {
		return (block.getType() == materialData.getItemType() && block.getData() == materialData.getData());
	}
	
	public MaterialData getMaterialData() {
		return this.materialData;
	}
	
	public void setIconData(MaterialData data) {
		this.iconData = data;
	}
	
	public MaterialData getIconData() {
		return this.iconData;
	}
	
	public void setHeadName(String name) {
		this.headName = name;
	}
	
	public String getHeadName() {
		return this.headName;
	}
	
	public void setSlot(int slot) {
		this.slot = slot;
	}
	
	public int getSlot() {
		return this.slot;
	}

}
