package liberty.maverick.dragonscale;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import liberty.maverick.dragonscale.pickaxe.DragonScalePickaxe;
import liberty.maverick.dragonscale.pickaxe.LevelData;
import net.md_5.bungee.api.ChatColor;

public class Commands implements CommandExecutor {
	
	final DragonScale main;
	final DragonScaleDatabase database;
	
	public Commands(final DragonScale mainInstance) {
		this.main = mainInstance;
		this.database = main.getSystemDatabase();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLbl, String[] args) {
		
		if(cmdLbl.equalsIgnoreCase("pickaxe")) {
			if(sender.hasPermission("pickaxe.admin") || sender.isOp()) {
				if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
					// Let's handle the reload command.
					main.getSettings().loadConfigAndSettings();
					
					for(Player player : main.getServer().getOnlinePlayers()) {
						database.getPickaxeData(player.getUniqueId().toString()).processReload();
					}
					
					sender.sendMessage(ChatColor.DARK_GRAY + "Reloaded config!");
					return true;

				}else if(args.length > 2) {
					// Let's handle the data editing command.
					final UUID uuid = getPlayerFromArg(sender, args[1]);
					if(uuid == null) return true;
					
					final DragonScalePickaxe pickaxe = database.getPickaxeData(uuid.toString());
					
					if(!database.getData().containsKey(uuid.toString())) {
						// Let's temporarily keep track of the offline player's pickaxe instance
						// for saving.
						database.getData().put(uuid.toString(), pickaxe);
					}
					
					if(args.length == 2 && args[0].equalsIgnoreCase("give")) {
						pickaxe.getWielder().getInventory().addItem(pickaxe.generatePickaxe(main.getSettings()));
						
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
								String.format("&aSuccessfully gave &e%s&a their pickaxe!", 
								args[1])));
						return true;
					}else if(args.length == 4) {
						final boolean updateXP = (args[2].equalsIgnoreCase("xp") || args[2].equalsIgnoreCase("exp"));
						if(!updateXP && !args[2].equalsIgnoreCase("level")) return false;
						
						int amount = 0;
						
						try {
							amount = Integer.parseInt(args[3]);
						} catch (NumberFormatException e) {
							return false;
						}
						
						if(!updateXP && amount < 1) {
							amount = 1;
						}else if(!updateXP && amount > main.getSettings().getMaxLevel()) {
							amount = main.getSettings().getMaxLevel();
						}
						
						String message = null;
						
						if(args[0].equalsIgnoreCase("set")) {
							if(updateXP) {
								pickaxe.setExp(amount);
							}else {
								pickaxe.setLevel(amount);
								pickaxe.setExp(0);
							}
							
							message = String.format("&aSuccessfully set &e%s&a's &b%s &ato &b%s&a!", args[1], 
									(updateXP) ? "xp" : "level", amount);
		
						}else if(args[0].equalsIgnoreCase("add")) {
							if(updateXP) {
								if((pickaxe.getExp() + amount) > pickaxe.getLevelData().getLevelUpExp()) {
									final int[] data = computeProperLevelDataFromAddition(pickaxe.getLevel(), pickaxe.getExp(), amount);
									pickaxe.setLevel(data[1]);
									pickaxe.setExp(data[0]);
								}else {
									pickaxe.setExp(pickaxe.getExp() + amount);
								}
							}else {
								pickaxe.setLevel(pickaxe.getLevel() + amount);
								pickaxe.setExp(0);
							}
							
							message = String.format("&aSuccessfully added &b%s %s &ato &e%s&a!", amount, (updateXP) ? "xp" : "levels", 
									args[1]);
							
						}else if(args[0].equalsIgnoreCase("remove")) {
							if(updateXP) {
								if((pickaxe.getExp() - amount) < 0) {
									final int[] data = computeProperLevelDataFromSubtraction(pickaxe.getLevel(), pickaxe.getExp(), amount);
									pickaxe.setLevel(data[1]);
									pickaxe.setExp(data[0]);
								}else {
									pickaxe.setExp(pickaxe.getExp() - amount);
								}
							}else {
								pickaxe.setLevel(pickaxe.getLevel() - amount);
								pickaxe.setExp(0);
							}
							
							message = String.format("&aSuccessfully removed &b%s %s &afrom &e%s&a!", amount, (updateXP) ? "xp" : "levels", 
									args[1]);
						}else {
							return false;
						}
						
						pickaxe.update();
						database.savePlayerData(uuid.toString());
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
						
						// Let's stop keeping track of an offline player's data.
						if(!main.getServer().getOfflinePlayer(uuid).isOnline()) {
							database.getData().remove(uuid.toString());
						}
						
						return true;
					}
				}
			}else {
				sender.sendMessage(ChatColor.RED + "Sorry, you don't have permission to run that command.");
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Recursive function that calculates the correct level and exp to "land" on.
	 * @param currentLevel - Either the start level or the level currently "stepping" on from the last cycle.
	 * @param currentExp - Either the start exp or the exp currently on from the last cycle.
	 * @param rmvExp - The remaining exp to remove.
	 * @return int[] with two items, the first one is the xp and the second is the level.
	 */
	
	public int[] computeProperLevelDataFromSubtraction(int currentLevel, int currentExp, int rmvExp) {
		if((currentExp - rmvExp) > 0) return new int[] {(currentExp - rmvExp), currentLevel};
		if(currentLevel - 1 == 0) return new int[] {0, 1};

		final LevelData levelData = new LevelData(currentLevel - 1);
		rmvExp = rmvExp - currentExp;
		if((levelData.getLevelUpExp() - rmvExp) < 0) {
			int newCurrExp = levelData.getLevelUpExp();
			return computeProperLevelDataFromSubtraction(currentLevel - 1, newCurrExp, rmvExp);
		}
			
		return new int[] {(levelData.getLevelUpExp() - rmvExp), levelData.getLevel()};
	}
	
	/**
	 * Recursive function that calculates the correct level and exp to "land" on.
	 * @param currentLevel - Either the start level or the level currently "stepping" on from the last cycle.
	 * @param currentExp - Either the start exp or the exp currently on from the last cycle.
	 * @param addExp - The remaining exp to add.
	 * @return int[] with two items, the first one is the xp and the second is the level.
	 */
	
	public int[] computeProperLevelDataFromAddition(int currentLevel, int currentExp, int addExp) {
		final LevelData currLevelData = new LevelData(currentLevel);
		if((currentExp + addExp) < currLevelData.getLevelUpExp() || currLevelData.getLevelUpExp() == -1) return new int[] {(currentExp + addExp), currentLevel};

		final LevelData levelData = new LevelData(currentLevel + 1);
		addExp -= (currLevelData.getLevelUpExp() - currentExp);
		if(addExp > levelData.getLevelUpExp()) {
			int newCurrExp = 0;
			return computeProperLevelDataFromAddition(currentLevel + 1, newCurrExp, addExp);
		}
			
		return new int[] {addExp, levelData.getLevel()};
	}
	
	public UUID getPlayerFromArg(final CommandSender sender, final String input) {
		@SuppressWarnings("deprecation")
		final OfflinePlayer offPlayer = main.getServer().getOfflinePlayer(input);
		UUID foundId = null;
		
		if(offPlayer.hasPlayedBefore()) {
			foundId = offPlayer.getUniqueId();
		}else {
			sender.sendMessage(ChatColor.RED + "Could not find the specified player!");
		}
		
		return foundId;
	}

}
