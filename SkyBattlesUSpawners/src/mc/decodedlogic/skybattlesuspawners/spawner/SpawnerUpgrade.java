package mc.decodedlogic.skybattlesuspawners.spawner;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mc.decodedlogic.skybattlesuspawners.USpawners;
import mc.decodedlogic.skybattlesuspawners.Utils;
import mc.decodedlogic.skybattlesuspawners.menu.MenuButton;

public class SpawnerUpgrade {
    
    public static final SpawnerUpgrade DEFAULT = new SpawnerUpgrade() {
      public String getDisplayName() { return ""; }  
    };
	
	private String name;
	private Material icon;
	private Material drop;
	
	private String iconName;
	private List<String> iconDesc;
	private int iconSlot;
	
	private double costPerSpawner;
	private int spawnRatePerSpawner;
	
	protected int index;
	protected SpawnerType type;
	
	public SpawnerUpgrade() {
	    this.name = "Default";
	    this.icon = null;
	    this.drop = null;
	    this.costPerSpawner = 0.0d;
	    this.spawnRatePerSpawner = 0;
	    
	    this.iconName = MenuButton.Data.UPGRADE_SLOT.getName();
	    this.iconDesc = MenuButton.Data.UPGRADE_SLOT.getDescription();
	    this.iconSlot = MenuButton.Data.UPGRADE_SLOT.getSlot();
	    this.index = -1;
	    this.type = null;
	}
	
	public SpawnerUpgrade(Material icon, 
			double costPerSpawner, int spawnRatePerSpawner) {
	    this(icon, icon, costPerSpawner, spawnRatePerSpawner);
	}
	
	public SpawnerUpgrade(Material icon, 
			Material drop, double costPerSpawner, int spawnRatePerSpawner) {
		this.name = drop.toString();
		this.icon = icon;
		this.drop = drop;
		this.costPerSpawner = costPerSpawner;
		this.spawnRatePerSpawner = spawnRatePerSpawner;
		
        this.iconName = MenuButton.Data.UPGRADE_SLOT.getName();
        this.iconDesc = MenuButton.Data.UPGRADE_SLOT.getDescription();
        this.iconSlot = MenuButton.Data.UPGRADE_SLOT.getSlot();
        this.index = -1;
        this.type = null;
	}
	
	/**
	 * Generates an {@link ItemStack} representing this upgrade.
	 * @param numSpawners - The number of spawners the parent spawner has.
	 * @return
	 */
	
	public ItemStack generateItem(MobSpawner spawner) {
		ItemStack item = new ItemStack(this.icon, 1);
		ItemMeta meta = item.getItemMeta();
		
		meta.setDisplayName(Utils.color(replaceVariablesWithValues(Utils.replaceVariableWith(iconName, 
				"upgrade", getDisplayName()), spawner)));
		
		List<String> itemDesc = new ArrayList<String>();
		itemDesc.addAll(iconDesc);
		
		if(spawner.getUpgrade().equals(this)) {
		    itemDesc.addAll(USpawners.get().getSettings().getAlreadyPurchased());
		}
		
		for(int i = 0; i < itemDesc.size(); i++) {
			final String line = Utils.color(replaceVariablesWithValues(itemDesc.get(i), spawner));
			itemDesc.set(i, line);
		}
		
		meta.setLore(itemDesc);
		item.setItemMeta(meta);
		return item;
	}
	
	private String replaceVariablesWithValues(final String baseString, final MobSpawner spawner) {
		String result = Utils.replaceVariableWith(baseString, "amount", spawner.getSize());
		
		result = Utils.replaceVariableWith(result, "upgrade", getDisplayName());
		result = Utils.replaceVariableWith(result, "cost", calculateCost(spawner));
		result = Utils.replaceVariableWith(result, "spawnerType", Utils.makePrettyStringFromEnum(spawner.getType().toString(), false));
		return result;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDisplayName() {
	    return Utils.makePrettyStringFromEnum(this.name, false);
	}
	
	public void setIconName(String newIconName) {
	    this.iconName = newIconName;
	}
	
	public String getIconName() {
	    return this.iconName;
	}
	
	public void setIconDesc(List<String> newIconDesc) {
	    this.iconDesc = newIconDesc;
	}
	
	public List<String> getIconDesc() {
	    return new ArrayList<String>(iconDesc);
	}
	
	public void setIconSlot(int newSlot) {
	    this.iconSlot = newSlot;
	}
	
	public int getIconSlot() {
	    return this.iconSlot;
	}
	
	public int getIndex() {
	    return this.index;
	}
	
	public SpawnerType getSpawnerType() {
	    return this.type;
	}
	
	public Material getIcon() {
		return this.icon;
	}
	
	public Material getDrop() {
		return this.drop;
	}
	
	public double getBaseCost(int numSpawners) {
	    return costPerSpawner * numSpawners;
	}
	
	public double calculateCost(MobSpawner parent) {
	    int numSpawners = parent.size();
	    double baseCost = costPerSpawner * numSpawners;
	    double discount = 0.0d;
	    
	    if(index > 0) {
	        SpawnerUpgrade lower = type.getUpgrades().get(index-1);
	        
	        if(lower != null && parent.getUpgrade().equals(lower)) {
	            discount = lower.getBaseCost(numSpawners);
	        }
	    }
	    
		return baseCost - discount;
	}
	
	public double getCostPerSpawner() {
		return this.costPerSpawner;
	}
	
	public double getSpawnRatePerSpawner() {
		return this.spawnRatePerSpawner;
	}

}
