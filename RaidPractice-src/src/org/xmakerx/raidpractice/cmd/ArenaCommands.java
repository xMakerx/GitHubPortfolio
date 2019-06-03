package org.xmakerx.raidpractice.cmd;

import java.util.HashMap;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.xmakerx.raidpractice.Localizer;
import org.xmakerx.raidpractice.RaidPractice;
import org.xmakerx.raidpractice.arena.Arena;
import org.xmakerx.raidpractice.arena.ArenaFlag;
import org.xmakerx.raidpractice.arena.ArenaManager;
import org.xmakerx.raidpractice.arena.Game;
import org.xmakerx.raidpractice.arena.Spawn;
import org.xmakerx.raidpractice.arena.Spawn.SpawnType;

import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

public class ArenaCommands {
	
	final RaidPractice instance;
	final ArenaManager arenaManager;
	final Localizer localizer;
	private final HashMap<Player, Arena> editSessions;
	
	public ArenaCommands(final RaidPractice main) {
		this.instance = main;
		this.arenaManager = main.getArenaManager();
		this.localizer = main.getLocalizer();
		this.editSessions = new HashMap<Player, Arena>();
	}
	
	private void startEditSession(final Player player, final Arena arena) {
		editSessions.put(player, arena);
		player.sendMessage(localizer.getMessage(getArenaMessage(arenaManager.correctSpaces(arena.getName()), "editSessionStarted")));
	}
	
	public Arena getArenaEditing(final Player player) {
		return editSessions.get(player);
	}
	
	private String getArenaMessage(final String arenaName, final String messageName) {
		String message = localizer.getString(messageName);
		message = message.replaceAll("\\{name\\}", arenaManager.correctUnderscores(arenaName));
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public boolean handleCommand(CommandSender sender, List<String> args) {
		Player player = null;
		
		if(sender instanceof Player) {
			player = (Player)sender;
		}
		
		if(args.size() == 0) {
			// Let's send the help information.
			return true;
		}else if(args.size() > 0) {
			if(args.get(0).equalsIgnoreCase("create")) {
				if(player == null) {
					sender.sendMessage(localizer.getColoredString("mustBePlayer"));
					return false;
				}else if(player.hasPermission("rp.create")) {
					if(args.size() == 1) {
						sender.sendMessage(localizer.getMessage("mustSpecifyArenaName"));
						return true;
					}else if(args.size() == 2) {
						final String arenaName = args.get(1);
						if(!arenaManager.isNameAvailable(arenaName)) {
							sender.sendMessage(localizer.getMessage("nameUnavailable"));
							return true;
						}else {
							final Selection sel = instance.getWorldEdit().getSelection(player);
							// This happens if they don't have a selection or an incomplete selection.
							if(sel == null || sel.getMinimumPoint() == null || sel.getMaximumPoint() == null) {
								sender.sendMessage(localizer.getMessage("mustHaveSelection"));
								return true;
							}else {
								// Let's make this arena.
								final ProtectedCuboidRegion region = new ProtectedCuboidRegion(String.format("Arena_%s", 
										arenaManager.correctSpaces(arenaName)), sel.getNativeMinimumPoint().toBlockVector(), 
								sel.getNativeMaximumPoint().toBlockVector());
								WGBukkit.getRegionManager(player.getWorld()).addRegion(region);
								
								final Arena arena = new Arena(instance, arenaManager.correctUnderscores(arenaName), player.getWorld(), region);
								arenaManager.createArena(arena, player);
								sender.sendMessage(localizer.getMessage(getArenaMessage(arena.getName(), "arenaCreated")));
								
								// Let's start an edit session on this arena.
								startEditSession(player, arena);
								return true;
							}
						}
					}
				}else {
					sender.sendMessage(localizer.getMessage("noPermission"));
					return false;
				}
			}else if(args.get(0).equalsIgnoreCase("blacklist")) {
				if(player == null) {
					sender.sendMessage(localizer.getColoredString("mustBePlayer"));
					return false;
				}else if(player.hasPermission("rp.create")) {
					// Let's make sure we're editing an arena.
					final Arena arena = getArenaEditing(player);
					if(arena == null) {
						sender.sendMessage(localizer.getMessage("notEditingArena"));
						return true;
					}else {
						final Selection sel = instance.getWorldEdit().getSelection(player);
						// This happens if they don't have a selection or an incomplete selection.
						if(sel == null || sel.getMinimumPoint() == null || sel.getMaximumPoint() == null) {
							sender.sendMessage(localizer.getMessage("mustHaveSelection"));
							return true;
						}else {
							// Let's make this arena.
							final String regionName = String.format("Arena_%s_Blacklist_%s", arenaManager.correctSpaces(arena.getName()),
									String.valueOf(arena.getBlacklist().size() + 1));
							final ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionName, 
									sel.getNativeMinimumPoint().toBlockVector(), 
							sel.getNativeMaximumPoint().toBlockVector());
							WGBukkit.getRegionManager(player.getWorld()).addRegion(region);
							arena.blacklistRegion(region);
							arenaManager.saveArena(arena);
							sender.sendMessage(localizer.getMessage("blacklistedSelection"));
							return true;
						}
					}
				}
			}else if(args.get(0).equalsIgnoreCase("factionsclaim")) {
				if(player == null) {
					sender.sendMessage(localizer.getColoredString("mustBePlayer"));
					return false;
				}else if(player.hasPermission("rp.create")) {
					// Let's make sure we're editing an arena.
					final Arena arena = getArenaEditing(player);
					if(arena == null) {
						sender.sendMessage(localizer.getMessage("notEditingArena"));
						return true;
					}else {
						final Selection sel = instance.getWorldEdit().getSelection(player);
						// This happens if they don't have a selection or an incomplete selection.
						if(sel == null || sel.getMinimumPoint() == null || sel.getMaximumPoint() == null) {
							sender.sendMessage(localizer.getMessage("mustHaveSelection"));
							return true;
						}else {
							// Let's make this arena.
							final String regionName = String.format("Arena_%s_Claim_%s", arenaManager.correctSpaces(arena.getName()),
									String.valueOf(arena.getBlacklist().size() + 1));
							final ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionName, 
									sel.getNativeMinimumPoint().toBlockVector(), 
							sel.getNativeMaximumPoint().toBlockVector());
							WGBukkit.getRegionManager(player.getWorld()).addRegion(region);
							arena.claimRegion(region);
							arenaManager.saveArena(arena);
							sender.sendMessage(localizer.getMessage("factionsClaimed"));
							return true;
						}
					}
				}
			}else if(args.get(0).equalsIgnoreCase("update") && args.size() == 1) {
				if(player == null) {
					sender.sendMessage(localizer.getColoredString("mustBePlayer"));
					return false;
				}else if(player.hasPermission("rp.create")) {
					// Let's make sure we're editing an arena.
					final Arena arena = getArenaEditing(player);
					if(arena == null) {
						sender.sendMessage(localizer.getMessage("notEditingArena"));
						return true;
					}else {
						arenaManager.createSchematic(arena);
						sender.sendMessage(localizer.getMessage("updatedSchematic"));
						return true;
					}
				}
			}else if(args.get(0).equalsIgnoreCase("delete")) {
				if(player == null) {
					sender.sendMessage(localizer.getColoredString("mustBePlayer"));
					return false;
				}else if(player.hasPermission("rp.delete")) {
					if(args.size() == 1) {
						sender.sendMessage(localizer.getMessage("mustSpecifyArenaName"));
						return true;
					}else if(args.size() == 2) {
						final String arenaName = args.get(1);
						final Arena arena = arenaManager.getArenaByName(arenaName);
						if(arena == null) {
							sender.sendMessage(localizer.getMessage("arenaNonExistant"));
							return true;
						}else {
							arenaManager.delArena(arena);
							sender.sendMessage(localizer.getMessage(getArenaMessage(arenaName, "arenaDeleted")));
							return true;
						}
					}
				}else {
					sender.sendMessage(localizer.getMessage("noPermission"));
					return false;
				}
			}else if(args.get(0).equalsIgnoreCase("mkspawn")) {
				if(player == null) {
					sender.sendMessage(localizer.getColoredString("mustBePlayer"));
					return false;
				}else if(player.hasPermission("rp.create")) {
					
					// Let's make sure we're editing an arena.
					final Arena arena = getArenaEditing(player);
					if(arena == null) {
						sender.sendMessage(localizer.getMessage("notEditingArena"));
						return true;
					}
					
					// Make sure we specified a type.
					if(args.size() == 1) {
						sender.sendMessage(localizer.getMessage("noSpawnType"));
						return true;
					}else {
						// Let's make sure the player specified a valid spawn type.
						final String type = args.get(1);
						SpawnType sType = null;
						
						// Correcting a bug when attempting to do #valueOf.
						for(final SpawnType spawnType : SpawnType.values()) {
							if(spawnType.name().equalsIgnoreCase(type.toUpperCase())) {
								sType = spawnType;
							}
						}
						
						if(sType != null) {
							final Spawn spawn = arenaManager.createSpawn(arena, player.getLocation(), sType);
							arenaManager.saveArena(arena);
							sender.sendMessage(localizer.getMessage(getArenaMessage(spawn.getName(), "spawnCreated")));
							return true;
						}else {
							sender.sendMessage(localizer.getMessage("invalidType"));
							return true;
						}
					}
				}else {
					sender.sendMessage(localizer.getMessage("noPermission"));
					return false;
				}
			}else if(args.get(0).equalsIgnoreCase("delspawn")) {
				return true;
			}else if(args.get(0).equalsIgnoreCase("set")) {
				if(player == null) {
					sender.sendMessage(localizer.getColoredString("mustBePlayer"));
					return false;
				}
				if(args.size() == 2 || args.size() == 3) {
					final Arena arena = getArenaEditing(player);
					if(player.hasPermission("rp.create")) {
						// Let's make sure we're editing an arena.
						if(arena == null) {
							sender.sendMessage(localizer.getMessage("notEditingArena"));
							return true;
						}
						if(args.size() == 2 && args.get(1).equalsIgnoreCase("gear")) {
							player.updateInventory();
							final Inventory inv = Bukkit.createInventory(null, 36);
							for(int i = 0; i < 36; i++) {
								final ItemStack item = player.getInventory().getContents()[i];
								inv.setItem(i, item);
							}
							arena.setGear(inv);
							sender.sendMessage(localizer.getMessage("setGear"));
							arenaManager.saveArena(arena);
							return true;
						}else if(args.size() == 3) {
							final String flagName = args.get(1);
							final ArenaFlag flag = arena.getFlag(flagName);
							final String value = args.get(2);
							boolean setValue = false;
							
							if(flag != null) {
								if(flag.getValue() instanceof Boolean) {
									if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
										flag.setValue(Boolean.valueOf(value));
										setValue = true;
									}
								}else if(flag.getValue() instanceof Integer) {
									try {
										Integer.parseInt(value);
										flag.setValue(Integer.parseInt(value));
										setValue = true;
									}catch(NumberFormatException e) {
										// We'll handle this later.
									}
								}else if(flag.getValue() instanceof String) {
									try {
										String.valueOf(value);
										flag.setValue(String.valueOf(value));
										setValue = true;
									}catch(NullPointerException e) {
										e.printStackTrace();
									}
								}
								if(!setValue) {
									String info = localizer.getString("badFlagArg");
									info = info.replaceAll("\\{type\\}", flag.getValue().getClass().getSimpleName());
									sender.sendMessage(localizer.getMessage(ChatColor.translateAlternateColorCodes('&', info)));
								}else {
									String info = localizer.getString("setFlag");
									info = info.replaceAll("\\{flag\\}", flag.getAliases()[0]);
									info = info.replaceAll("\\{value\\}", value);
									sender.sendMessage(localizer.getMessage(ChatColor.translateAlternateColorCodes('&', info)));
									arenaManager.saveArena(arena);
									final Game game = arenaManager.getGameFromArena(arena);
									if(game != null) {
										game.updateVariables();
									}
								}
							}else {
								sender.sendMessage(localizer.getMessage("flagNonExistant"));
							}
							return true;
						}
					}else {
						sender.sendMessage(localizer.getMessage("noPermission"));
						return false;
					}
				}
			}else if(args.get(0).equalsIgnoreCase("edit")) {
				if(player == null) {
					sender.sendMessage(localizer.getColoredString("mustBePlayer"));
					return false;
				}
				if(player.hasPermission("rp.create")) {
					if(args.size() == 2) {
						final String arenaName = args.get(1);
						final Arena arena = arenaManager.getArenaByName(arenaName);
						if(arena == null) {
							sender.sendMessage(localizer.getMessage("arenaNonExistant"));
							return true;
						}else {
							if(hasEditSession(player) && editSessions.get(player).equals(arena)) {
								editSessions.remove(player);
								sender.sendMessage(localizer.getMessage("editSessionStopped"));
							}else {
								startEditSession(player, arena);
							}
							return true;
						}
					}else {
						sender.sendMessage(localizer.getMessage("mustSpecifyArenaName"));
						return true;
					}
				}else {
					sender.sendMessage(localizer.getMessage("noPermission"));
					return false;
				}
			}
		}
		sender.sendMessage(localizer.getMessage("commandNotFound"));
		return false;
	}
	
	public boolean hasEditSession(final Player player) {
		return editSessions.containsKey(player);
	}
}
