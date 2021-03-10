package mc.decodedlogic.skybattlesuspawners.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import mc.decodedlogic.skybattlesuspawners.Utils;
import mc.decodedlogic.skybattlesuspawners.logging.LocationLog;
import mc.decodedlogic.skybattlesuspawners.logging.ModificationRecord;
import mc.decodedlogic.skybattlesuspawners.logging.SpawnerLog;
import mc.decodedlogic.skybattlesuspawners.logging.Transaction;
import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerUpgrade;
import net.md_5.bungee.api.ChatColor;

public class MenuButton {
	
	public enum Data {
		WITHDRAW_ALL(0, 9, "WithdrawAll"),
		WITHDRAW_64(0, 10, "Withdraw64"),
		WITHDRAW_1(0, 11, "Withdraw1"),
		NEXT_PAGE(0, 13, "UpgradePage"), // This is the button that takes the user to the upgrades page.
		DEPOSIT_1(0, 15, "Deposit1"),
		DEPOSIT_64(0, 16, "Deposit64"),
		DEPOSIT_ALL(0, 17, "DepositAll"),
		
        RESET(1, 8, "Reset"),
        UPGRADE_SLOT(1, 12, "UpgradeSlot"),
		
		EMPTY_SLOT(1, 0, "EmptySlot"),
	    
	    OPEN_SPAWNER_LOGS(0, 0, ""),
	    CREATION_EVENT(1, 0, ""),
	    TRANSACTION_EVENT(1, 0, ""),
	    DELETION_EVENT(1, 0, "");
		
		private final int PAGE_INDEX;
		
		// The name within the config file.
		private final String CONFIG_NAME;
		private MaterialData iconData;
		private String name;
		private List<String> description;
		private int slot;
		private int amount;
		
		private Data(int pageIndex, int defaultSlot, String cfgName) {
			this.PAGE_INDEX = pageIndex;
			this.CONFIG_NAME = cfgName;
			this.slot = defaultSlot;
			this.amount = 1;
			this.iconData = null;
			this.name = null;
			this.description = null;
		}
		
		public void loadDataFrom(final ConfigurationSection section) {
			if(section == null) throw new NullPointerException("Section cannot be null!");
			
			// Attempt to fetch the slot from the section, if not found, continue using
			// the default slot.
			this.slot = section.getInt("Slot", this.slot);
			
			this.name = section.getString("Name");
			this.description = section.getStringList("Description");
			this.amount = section.getInt("Amount", this.amount);
			
			if(Arrays.asList("OPEN_SPAWNER_LOGS", "CREATION_EVENT", "TRANSACTION_EVENT", "DELETION_EVENT").contains(this.name())) return;
			
			// We only care about specified "icon" data for non-upgrade buttons.
			if(!this.name().startsWith("UPGRADE")) {
				String itemData = section.getString("Icon", "");
				
				if(itemData.isEmpty()) throw new IllegalArgumentException(String.format("Error parsing button \"%s\"'s data. "
						+ "You must specify an icon with its name and optional data. Ex: STAINED_GLASS_PANE:7", section.getName()));
				
				String[] split = itemData.split(":");
				String matName = null;
				byte data = 0;
				
				if(split.length > 0 && split.length <= 2) {
					matName = split[0];
					
					if(split.length == 2) {
						try {
							data = Byte.valueOf(split[1]);
						} catch (NumberFormatException e) {
							throw new IllegalArgumentException(String.format("Error parsing button \"%s\"'s data. "
							+ "Incorrect data argument for icon. Expected byte, got \"%s\".", section.getName(), split[1]));
						}
					}
					
					this.iconData = Utils.buildMaterialData(matName, data);
				}else if(split.length > 2) {
					throw new IllegalArgumentException(String.format("Error parsing button \"%s\"'s data. "
					+ "Too many arguments! Expected 2 at most!", section.getName()));
				}
				
			}
		}
		
		public int getPageIndex() {
			return this.PAGE_INDEX;
		}
		
		public String getConfigName() {
			return this.CONFIG_NAME;
		}
		
		public MaterialData getIconData() {
			return this.iconData;
		}
		
		public String getName() {
			return this.name;
		}
		
		/**
		 * Returns the raw description without formatting and variable replacements.
		 * @return
		 */
		
		public List<String> getDescription() {
			return this.description;
		}
		
		public int getSlot() {
			return this.slot;
		}
		
	}
	
	private final Menu MENU;
	private final Data BUTTON_DATA;
	private int slot;
	private int upgradeIndex;
	
	public MenuButton(final Menu menu, final Data buttonData) {
	    this(menu, buttonData, -1);
	}
	
	public MenuButton(Menu menu, Data buttonData, int upgradeIndex) {
        this.MENU = menu;
        this.BUTTON_DATA = buttonData;
        this.upgradeIndex = upgradeIndex;
        
        if(upgradeIndex != -1) {
            if(menu instanceof SpawnerMenu) {
                SpawnerMenu sMenu = (SpawnerMenu) menu;
                SpawnerUpgrade upgrade = sMenu.getSpawner().getType().getUpgrades().get(upgradeIndex);
                
                if(upgrade.getIconSlot() == buttonData.slot) {
                    this.slot = buttonData.slot + upgradeIndex;
                }else {
                    this.slot = upgrade.getIconSlot();
                }
            }else {
                this.slot = upgradeIndex;
            }
        }else {
            this.slot = buttonData.slot;
        }
	}
	
	public void setSlot(int slot) {
		this.slot = slot;
	}
	
	public int getSlot() {
		return this.slot;
	}
	
	@SuppressWarnings("deprecation")
	public void generate() {
		final Inventory inv = this.MENU.getCraftInventory();
		
		MaterialData iconData = this.BUTTON_DATA.getIconData();
		SpawnerUpgrade upgrade = null;
		MobSpawner spawner = null;
		
        if(BUTTON_DATA == Data.OPEN_SPAWNER_LOGS) {
            LogMenu lMenu = ((LogMenu) MENU);
            LocationLog log = lMenu.getLog();
            SpawnerLog sLog = log.getSpawnerLogs().get(slot);
            
            ItemStack icon = new ItemStack(Material.MOB_SPAWNER, 1);
            
            String unknown = "&cUnknown";
            String active = "&aActive";
            String creation = "&aCreation: %s";
            String deletion = "&cDeletion: %s";
            
            ModificationRecord cRecord = sLog.getCreationRecord();
            ModificationRecord dRecord = sLog.getDeletionRecord();
            
            if(cRecord == null || (cRecord != null && cRecord.getTimestamp() == -1)) {
                creation = String.format(creation, unknown);
            }else {
                creation = String.format(creation, String.format("&7[&e%s&7]", Utils.prettyFormatTime(cRecord.getTimestamp())));
            }
            
            if(dRecord == null) {
                deletion = String.format(deletion, active);
            }else {
                deletion = String.format(deletion, String.format("&7[&e%s&7]", Utils.prettyFormatTime(dRecord.getTimestamp())));
            }
            
            Utils.setItemNameAndDesc(icon, String.format("&7MobSpawner %s", slot+1), 
                    Arrays.asList(creation, deletion, "&7&o(( Click to view logs! ))"));
            
            inv.setItem(slot, icon);
        }else if(BUTTON_DATA == Data.CREATION_EVENT) {
            LogMenu lMenu = ((LogMenu) MENU);
            SpawnerLog sLog = lMenu.getSpawnerLog();
            
            ItemStack icon = new ItemStack(Material.NETHER_STAR, 1);
            String creator = "&7&l- &aBy: %s";
            String creation = "&7&l- &aTime: %s";
            String unknown = "&cUnknown";
            String server = "&2Server";
            
            ModificationRecord cRecord = sLog.getCreationRecord();
            if(cRecord != null) {
                boolean serverCreated = (cRecord.getTimestamp() == -1);
                String playerName = (!serverCreated) ? Bukkit.getOfflinePlayer(cRecord.getUUID()).getName() : server;
                String time = (serverCreated) ? unknown : Utils.prettyFormatTime(cRecord.getTimestamp());
                
                creator = String.format(creator, playerName);
                creation = String.format(creation, String.format("&7[&e%s&7]", time));
            }
            
            Utils.setItemNameAndDesc(icon, "&a&lPlace Event", Arrays.asList(creator, creation));
            
            inv.setItem(slot, icon);
        }else if(BUTTON_DATA == Data.DELETION_EVENT) {
            LogMenu lMenu = ((LogMenu) MENU);
            SpawnerLog sLog = lMenu.getSpawnerLog();
            
            ItemStack icon = new ItemStack(Material.BARRIER, 1);
            String creator = "&7&l- &aBy: %s";
            String creation = "&7&l- &aTime: %s";
            String unknown = "&cUnknown";
            String server = "&2Server";
            
            ModificationRecord cRecord = sLog.getDeletionRecord();
            if(cRecord != null) {
                boolean serverCreated = (cRecord.getTimestamp() == -1);
                String playerName = (!serverCreated) ? Bukkit.getOfflinePlayer(cRecord.getUUID()).getName() : server;
                String time = (serverCreated) ? unknown : Utils.prettyFormatTime(cRecord.getTimestamp());
                
                creator = String.format(creator, playerName);
                creation = String.format(creation, String.format("&7[&e%s&7]", time));
            }
            
            Utils.setItemNameAndDesc(icon, "&c&lBreak Event", Arrays.asList(creator, creation));
            
            inv.setItem(slot, icon);
        }else if(BUTTON_DATA == Data.TRANSACTION_EVENT) {
            LogMenu lMenu = ((LogMenu) MENU);
            
            SpawnerLog sLog = lMenu.getSpawnerLog();
            int index = slot + (lMenu.getPageIndex() - 1) * 35;
            
            Material mat = null;
            String creator = "&7&l- &r&aBy: %s";
            String creation = "&7&l- &r&aTime: %s";
            String unknown = "&cUnknown";
            String server = "&2Server";
            
            String upgradeLine = "&7&l- &r&aUpgrade: &e%s";
            String cost = "&7&l- &r&aCost: &7$%s";
            String spawners = "&7&l- &r&aSpawners: &7%s";
            
            Transaction r = (Transaction) sLog.getRecords().get(index);
            
            boolean serverCreated = (r.getTimestamp() == -1);
            String playerName = (!serverCreated) ? ChatColor.YELLOW + Bukkit.getOfflinePlayer(r.getUUID()).getName() : server;
            String time = (serverCreated) ? unknown : Utils.prettyFormatTime(r.getTimestamp());
            
            creator = String.format(creator, playerName);
            creation = String.format(creation, String.format("&7[&e%s&7]", time));
            cost = String.format(cost, Utils.prettyFormatDouble(r.getCost()));
            spawners = String.format(spawners, r.getDelta());
            
            List<String> desc = new ArrayList<String>();
            String evtName = "";
            
            if(r.getType() == Transaction.SPAWNER_UPGRADE) {
                if(r.getPurchasedUpgrade() != null) {
                    mat = r.getPurchasedUpgrade().getIcon();
                    upgradeLine = String.format(upgradeLine, r.getPurchasedUpgrade().getDisplayName());
                }else {
                    mat = Material.EMERALD;
                    upgradeLine = String.format(upgradeLine, unknown);
                }
                
                evtName = "&a&lUpgrade Purchased";
                desc.add(upgradeLine);
                desc.add(spawners);
                desc.add(cost);
            }else if(r.getType() == Transaction.SPAWNER_RESET) {
                mat = Material.BUCKET;
                
                evtName = "&a&lSpawner Reset";
                desc.add(spawners);
                desc.add(cost);
            }else if(r.getType() == Transaction.SPAWNER_DEPOSIT) {
                mat = Material.EMPTY_MAP;
                
                evtName = "&a&lSpawner Deposit";
                desc.add(spawners);
            }else if(r.getType() == Transaction.SPAWNER_WITHDRAW) {
                mat = Material.FISHING_ROD;
                
                evtName = "&a&lSpawner Withdraw";
                desc.add(spawners);
            }
            
            desc.add(creator);
            desc.add(creation);
            
            ItemStack icon = new ItemStack(mat, 1);
            Utils.setItemNameAndDesc(icon, evtName, desc);
            
            inv.setItem(slot, icon);
        }else {
		
    		if(MENU instanceof SpawnerMenu) {
    		    SpawnerMenu sMenu = ((SpawnerMenu) MENU);
    		    upgrade = sMenu.getSpawner().getUpgrade();
    		    spawner = sMenu.getSpawner();
    		}
    		
    		ItemStack icon = null;
    		
    		if(iconData == null) {
    			// This must be an upgrade button.
    			try {
    				upgrade = spawner.getType().getUpgrades().get(upgradeIndex);
    				icon = upgrade.generateItem(spawner);
    			} catch (IndexOutOfBoundsException | NullPointerException e) {
    				throw e;
    			}
    		}else {
    		    icon = new ItemStack(iconData.getItemType(), BUTTON_DATA.amount);
    	        icon.setDurability((short) iconData.getData());
    	        final ItemMeta meta = icon.getItemMeta();
    	        
    	        String baseName = replaceVariablesWithValues(this.BUTTON_DATA.getName(), upgrade);
    	        meta.setDisplayName((BUTTON_DATA == Data.EMPTY_SLOT) ? Utils.color(baseName) : Utils.mkDisplayReady(baseName));
    	        
    	        final List<String> description = new ArrayList<String>();
    	        description.addAll(this.BUTTON_DATA.getDescription());
    	        
    	        for(int i = 0; i < description.size(); i++) {
    	            String line = description.get(i);
    	            line = Utils.mkDisplayReady(replaceVariablesWithValues(line, upgrade));
    	            description.set(i, line);
    	        }
    	        
    	        meta.setLore(description);
    	        icon.setItemMeta(meta);
    		}
    		
    		inv.setItem(slot, icon);
        }
	}
	
	public String replaceVariablesWithValues(final String baseString, final SpawnerUpgrade upgrade) {
	    if(MENU instanceof SpawnerMenu) {
    		final MobSpawner spawner = ((SpawnerMenu) MENU).getSpawner();
    		String result = Utils.replaceVariableWith(baseString, "amount", spawner.getSize());
    		
    		String upgradeName = (upgrade == SpawnerUpgrade.DEFAULT) ? upgrade.getName() : upgrade.getDisplayName();
    		result = Utils.replaceVariableWith(result, "spawnerType", Utils.makePrettyStringFromEnum(spawner.getType().toString(), false));
    		result = Utils.replaceVariableWith(result, "upgrade", upgradeName);
    		if(upgrade != null) result = Utils.replaceVariableWith(result, "cost", upgrade.calculateCost(spawner));
    		return result;
	    }
	    
	    return baseString;
	}
	
	public Menu getMenu() {
		return this.MENU;
	}
	
	public int getUpgradeIndex() {
	    return this.upgradeIndex;
	}
	
	public Data getButtonData() {
		return this.BUTTON_DATA;
	}
	
}
