package mc.decodedlogic.skybattlesuspawners;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import com.wasteofplastic.askyblock.ASkyBlock;
import com.wasteofplastic.askyblock.Island;

import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerType;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerUpgrade;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;

public class Utils {
	
	public static final boolean LOWERCASE_ENUMS = false;
	
	/**
	 * Clears the specified mob's goal and target selectors.
	 * @param Entity entity
	 * @return true/false whether disabled.
	 */
	
	public static boolean disableMobAI(Entity entity) {
	    if(entity == null || (entity != null && entity.isDead())) return false;
	    
	    try {
	    
    	    // Use NMS to fetch the EntityCreature
    	    EntityCreature c = (EntityCreature) ((EntityInsentient) ((CraftEntity) entity).getHandle());
    	    
    	    List<?> l = new ArrayList<>();
    	    
    	    try {
    	        // Let's clear the goal and target selectors.
    	        Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
    	        bField.setAccessible(true);
    	        
    	        Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
    	        cField.setAccessible(true);
    	        
    	        bField.set(c.goalSelector, l);
    	        bField.set(c.targetSelector, l);
    	        
    	        cField.set(c.goalSelector, l);
    	        cField.set(c.targetSelector, l);
    	        
    	    }catch (Exception e) {
    	        return false;
    	    }
	    } catch (Exception e) { return false; }
	    
	    return true;
	}
	
	public static String makePrettyStringFromEnum(final String enumString, boolean allCaps) {
		String[] split = enumString.split("_");
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < split.length; i++) {
			final String s = split[i];
			
			if(!allCaps) {
				sb.append(s.substring(0, 1) + s.substring(1).toLowerCase());
			}else {
				sb.append(s);
			}
			
			if((i+1) < split.length) {
				sb.append(" ");
			}
		}
		
		return sb.toString();
	}
	
    public static void setItemNameAndDesc(ItemStack item, String name, List<String> desc) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.mkDisplayReady(name));
        
        List<String> lines = new ArrayList<String>();
        lines.addAll(desc);
        
        for(int i = 0; i < lines.size(); i++) {
            String l = lines.get(i);
            lines.set(i, Utils.mkDisplayReady(l));
        }
        
        meta.setLore(lines);
        item.setItemMeta(meta);
    }
    
    public static Location lookAt(Location loc, Location lookat) {
        //Clone the loc to prevent applied changes to the input loc
        loc = loc.clone();
        // Values of change in distance (make it relative)
        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }
        // Get the distance from dx/dz
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
        // Set pitch
        loc.setPitch((float) -Math.atan(dy / dxz));
        // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
        loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);
        return loc;
    }
	
	public static String prettyFormatDouble(final double number) {
	    double numMillions = (number / 1000000.0);

	    String formattedNum = null;
	    if(numMillions >= 1000.0) {
	    	formattedNum = String.format("%,.2fB", (number / 1000000000.0));
	    }else if(numMillions >= 1.0) {
	    	formattedNum = String.format("%,.2fM", (number / 1000000.0));
	    }else {
	    	formattedNum = String.format("%,.2f", number);
	    }
	    
	    return formattedNum;
	}
	
	public static String prettyFormatTime(final long milliseconds) {
	    if(milliseconds > 0) {
    	    Instant i = Instant.ofEpochMilli(milliseconds);
    	    ZonedDateTime zdt = ZonedDateTime.ofInstant(i, ZoneId.of("US/Eastern"));
    	    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
    	    return zdt.format(dtf);
	    } else {
	        return "&cUnknown";
	    }
	}
	
	public static String replaceVariableWith(final String baseString, final String variableName, 
			Object replacement) {
		
		if(replacement == null) {
			replacement = "";
		}
		
		String strReplacement = String.valueOf(replacement);
		
		try {
			Integer.parseInt(strReplacement);
		} catch (NumberFormatException e) {
			try {
				// If we're replacing a variable with a double, let's pretty format it.
				double amount = Double.parseDouble(strReplacement);
				strReplacement = Utils.prettyFormatDouble(amount);
			} catch (NumberFormatException e2) {}
		}
		
		final String variable = "%" + variableName + "%";
		int varIndex = baseString.indexOf(variable);
		String result = baseString;
		
		if(strReplacement.isEmpty() && varIndex != -1) {
			final List<String> startBlockChars = Arrays.asList("(", "{", "[");
			final List<String> endBlockChars = Arrays.asList(")", "}", "]");
			int oParenthesesIndex = findIndexOf(result, startBlockChars, varIndex-1, endBlockChars);
			int endParenthesesIndex = findIndexOf(result, endBlockChars, result.length() - 1, startBlockChars);
			
			String afterVariableStr = (endParenthesesIndex != -1) ? result.substring(endParenthesesIndex) : result.substring(varIndex);
			if(afterVariableStr.replaceAll("\\s","").isEmpty()) {
				int endIndex = oParenthesesIndex;
				
				if(result.substring(endIndex-1, endIndex).equals(" ")) {
					endIndex = endIndex - 1;
				}
				
				result = result.substring(0, endIndex);
				return result;
			}else if(oParenthesesIndex != -1 && endParenthesesIndex != -1) {
				result = result.substring(0, oParenthesesIndex) + result.substring(endParenthesesIndex+1);
				return result;
			}
		}
		
		return result.replaceAll("\\%" + variableName + "\\%", strReplacement);
	}
	
	private static int findIndexOf(String baseString, List<String> characters, 
			int startIndex, List<String> breakChars) {
		int index = startIndex;
		do {
			final String cChar = baseString.substring(index, index+1);
			if(characters.contains(cChar)) {
				return index;
			}else if(breakChars.contains(cChar) || cChar.equals("%")) {
				break;
			}
			
			index--;
		} while(index >= 0);
		
		return -1;
	}
	
	public static String color(String baseString) {
		return ChatColor.translateAlternateColorCodes('&', baseString);
	}
	
	@SuppressWarnings("deprecation")
	public static MaterialData buildMaterialData(final String materialName, byte data) {
		Material material = null;
		
		try {
			material = Material.valueOf(materialName.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(String.format("Material type \"%s\" could not be found! Double check that it's valid!", materialName));
		}
		
		return new MaterialData(material, data);
	}
	
	public static Island getIslandAt(final Location location) {
        ASkyBlock asb = USpawners.get().getASkyBlock();
        Island island = asb.getGrid().getIslandAt(location);
		
		return island;
	}
	
	public static boolean canAccessSpawner(final Player player, final MobSpawner spawner) {
		if(player == null || spawner == null) throw new NullPointerException("Must be given a valid Player and MobSpawner instance!");
		
		final Island spawnerIsland = getIslandAt(spawner.getLocation());
		
		return (spawnerIsland == null || (spawnerIsland != null && spawnerIsland.getMembers().contains(player.getUniqueId())));
	}
	
	public static ItemStack generateSpawner(final EntityType type, final int amount, final int upgradeIndex) {
		ItemStack spawner = new ItemStack(Material.MOB_SPAWNER, amount);
		final ItemMeta meta = spawner.getItemMeta();
		
		if(meta instanceof BlockStateMeta) {
			final BlockStateMeta bsm = (BlockStateMeta) meta;
			final BlockState state = bsm.getBlockState();
			
			if(state instanceof CreatureSpawner) {
				final CreatureSpawner cSpawner = (CreatureSpawner) state;
				cSpawner.setSpawnedType(type);
				bsm.setBlockState(cSpawner);
				
				final Settings settings = USpawners.get().getSettings();
				
				String displayName = settings.getSpawnerItemName();
				String upgradeName = "Default";
				
				SpawnerType sType = SpawnerType.getTypeFromEntityType(type);
				String typeName = (sType != null) ? sType.getName() : type.name();
				displayName = replaceVariableWith(displayName, "spawnerType", Utils.makePrettyStringFromEnum(typeName, false));
				
				if(upgradeIndex != -1) {
					try {
						SpawnerUpgrade upgrade = sType.getUpgrades().get(upgradeIndex);
						upgradeName = upgrade.getDisplayName();
					} catch (IndexOutOfBoundsException e) {}
				}
				
				displayName = replaceVariableWith(displayName, "upgrade", upgradeName);
				meta.setDisplayName(mkDisplayReady(displayName));
				
				final List<String> description = new ArrayList<String>();
				description.addAll(settings.getSpawnerItemDescription());
				
				for(int i = 0; i < description.size(); i++) {
					String line = description.get(i);
					line = replaceVariableWith(line, "spawnerType", Utils.makePrettyStringFromEnum(typeName, false));
					line = replaceVariableWith(line, "upgrade", upgradeName);
					line = mkDisplayReady(line);
					description.set(i, line);
				}
				
				meta.setLore(description);
				spawner.setItemMeta(meta);
				
				net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(spawner);
				NBTTagCompound nbt = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();
				nbt.set("Type", new NBTTagString(sType.name()));
				nbt.set("Upgrade", new NBTTagInt(upgradeIndex + 1));
				nbt.set("ench", new NBTTagList());
				nmsItem.setTag(nbt);
				
				spawner = CraftItemStack.asBukkitCopy(nmsItem);
			}
		}
		
		return spawner;
	}
	
	@SuppressWarnings("deprecation")
    public static boolean isSpawnerFor(SpawnerType type, SpawnerUpgrade upgrade, ItemStack spawner, boolean upgradeMustMatch) {
	    if(spawner == null || (spawner != null && spawner.getType() != Material.MOB_SPAWNER)) return false;
	    
	    net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(spawner);
        NBTTagCompound nbt = nmsItem.getTag();
        
        // If the item doesn't have any NBT data, it isn't one of our spawners.
        if(nbt == null) return false;
        
        // Fetch NBT data
        int itemUpgradeIndex = nbt.getInt("Upgrade") - 1;
        String itemTypeStr = nbt.getString("Type");
        
        SpawnerType itemType = null;
        
        if(nbt.hasKey("SilkSpawners")) {
            String ent = nbt.getCompound("SilkSpawners").getString("entity");
            System.out.println("Found entity type: " + ent);
            EntityType eT = null;
            
            for(EntityType e : EntityType.values()) {
                if(e.getName().equalsIgnoreCase(ent)) {
                    eT = e;
                    break;
                }
            }
            
            itemType = SpawnerType.getTypeFromEntityType(eT);
        }else {
            
            EntityType t = null;
            
            try {
                t = EntityType.valueOf(itemTypeStr);
            } catch (IllegalArgumentException e) {
                if(itemTypeStr.equalsIgnoreCase("ZOMBIE_PIGMAN")) {
                    t = EntityType.PIG_ZOMBIE;
                }else if(itemTypeStr.equalsIgnoreCase("MOOSHROOM")) {
                    t = EntityType.MUSHROOM_COW;
                }else {
                    t = EntityType.PIG;
                }
            }
            
            itemType = SpawnerType.getTypeFromEntityType(t);
        }
        
        // Verify the types are the same.
        if(itemType != type) return false;

        // Verify the upgrades are the same.
        if(upgradeMustMatch) {
            int spawnerUpgradeIndex = type.getUpgrades().indexOf(upgrade);
            
            if(spawnerUpgradeIndex != itemUpgradeIndex) return false;
        }
        
        // If nothing above failed, then we're in business!
        return true;
	}
	
	/**
	 * Colors and chops off trailing whitespace
	 * @param str
	 * @return
	 */
	
	public static String mkDisplayReady(String str) {
	    // Let's clear off trailing whitespace
	    if(str.length() > 2) {
    	    char lastChar = str.charAt(str.length()-1);
    	    if(isWhitespace(lastChar)) {
    	        int lastIndex = str.length()-2;
    	        for(int i = str.length()-2; i > -1; i--) {
    	            char c = str.charAt(i);
    	            
    	            if(!isWhitespace(c) && c != '&') {
    	                lastIndex = i;
    	                break;
    	            }
    	        }
    	        
    	        str = str.substring(0, lastIndex+1);
    	    }
	    }
	    
	    return color(str);
	}
	
	private static boolean isWhitespace(char c) {
	    return c == ' ' || Character.isWhitespace(c);
	}

}
