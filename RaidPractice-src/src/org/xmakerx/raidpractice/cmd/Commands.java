package org.xmakerx.raidpractice.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.xmakerx.raidpractice.Localizer;
import org.xmakerx.raidpractice.RaidPractice;
import org.xmakerx.raidpractice.arena.Arena;
import org.xmakerx.raidpractice.arena.Game;
import org.xmakerx.raidpractice.arena.GamePlayer;
import org.xmakerx.raidpractice.arena.Game.GameState;

public class Commands implements CommandExecutor {
	
	final RaidPractice instance;
	final Localizer localizer;
	final ArenaCommands arenaCmds;
	final KeysCommands keysCmds;
	
	public Commands(final RaidPractice main) {
		this.instance = main;
		this.localizer = main.getLocalizer();
		this.arenaCmds = new ArenaCommands(main);
		this.keysCmds = new KeysCommands(main);
	}
	
	private List<String> createNewArgs(final String[] args) {
		final List<String> newArgs = new ArrayList<String>();
		for(int i = 1; i < args.length; i++) {
			newArgs.add(args[i]);
		}
		return newArgs;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLbl, String[] args) {
		Player player = null;
		if(cmd.getName().equalsIgnoreCase("rp") || cmd.getName().equalsIgnoreCase("raidpractice")) {
			if(sender instanceof Player) player = (Player)sender;
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("arena")) {
					return arenaCmds.handleCommand(sender, createNewArgs(args));
				}else if(args[0].equalsIgnoreCase("keys")) {
					return keysCmds.handleCommand(sender, createNewArgs(args));
				}else if(args[0].equalsIgnoreCase("join")) {
					if(player == null) {
						sender.sendMessage(localizer.getMessage("mustBePlayer"));
						return true;
					}
					if(args.length == 1) {
						sender.sendMessage(localizer.getMessage("mustSpecifyArenaName"));
						return true;
					}else if(args.length == 2) {
						final String arenaName = args[1];
						final Arena arena = instance.getArenaManager().getArenaByName(arenaName);
						final Game playerGame = instance.getArenaManager().getGameFromPlayer(player);
						if(playerGame != null) {
							sender.sendMessage(localizer.getMessage("alreadyInGame"));
							return true;
						}else {
							if(arena != null) {
								final Game game = instance.getArenaManager().getGameFromArena(arena);
								if(game != null) {
									game.attemptJoin(player);
									return true;
								}
							}else {
								sender.sendMessage(localizer.getMessage("arenaNonExistant"));
								return true;
							}
						}
					}
				}else if(args[0].equalsIgnoreCase("topplayer") && args.length == 1) {
					if(sender.hasPermission("rp.topplayer")) {
						final HashMap<OfflinePlayer, Integer> map = instance.getStatsDatabase().getTopPoints();
						String playerName = "N/A";
						int points = 0;
						
						for(final Map.Entry<OfflinePlayer, Integer> entry : map.entrySet()) {
							playerName = entry.getKey().getName();
							points = entry.getValue();
							break;
						}
						
						String msg = localizer.getString("topPlayer");
						msg = msg.replaceAll("\\{player\\}", playerName);
						msg = msg.replaceAll("\\{points\\}", String.valueOf(points));
						
						sender.sendMessage(localizer.getMessage("topPlayer"));
					}
				}else if(args[0].equalsIgnoreCase("leave") && args.length == 1) {
					if(player == null) {
						sender.sendMessage(localizer.getMessage("mustBePlayer"));
						return true;
					}
					final Game game = instance.getArenaManager().getGameFromPlayer(player);
					if(game != null) {
						game.leave(player);
						return true;
					}else {
						sender.sendMessage(localizer.getMessage("notInGame"));
						return true;
					}
				}else if(args[0].equalsIgnoreCase("sethome") && args.length == 1) {
					if(player == null) {
						sender.sendMessage(localizer.getMessage("mustBePlayer"));
						return true;
					}else if(!player.hasPermission("rp.sethome")) {
						sender.sendMessage(localizer.getMessage("noPermission"));
						return true;
					}
					final Game game = instance.getArenaManager().getGameFromPlayer(player);
					if(game != null) {
						final GamePlayer gPlayer = game.getGamePlayerFromPlayer(player);
						if(gPlayer != null) {
							if(game.getState() == GameState.IN_GAME) {
								gPlayer.setHome(player.getLocation());
								sender.sendMessage(localizer.getMessage("homeSet"));
								return true;
							}else {
								sender.sendMessage(localizer.getMessage("homeFailLobby"));
								return true;
							}
						}
					}else {
						sender.sendMessage(localizer.getMessage("notInGame"));
						return true;
					}
				}else if(args[0].equalsIgnoreCase("home") && args.length == 1) {
					if(player == null) {
						sender.sendMessage(localizer.getMessage("mustBePlayer"));
						return true;
					}else if(!player.hasPermission("rp.sethome")) {
						sender.sendMessage(localizer.getMessage("noPermission"));
						return true;
					}
					final Game game = instance.getArenaManager().getGameFromPlayer(player);
					if(game != null) {
						final GamePlayer gPlayer = game.getGamePlayerFromPlayer(player);
						if(gPlayer != null) {
							if(game.getState() == GameState.IN_GAME && gPlayer.getHome() != null) {
								player.teleport(gPlayer.getHome(), TeleportCause.PLUGIN);
								return true;
							}else {
								sender.sendMessage(localizer.getMessage("homeFailNone"));
								return true;
							}
						}
					}else {
						sender.sendMessage(localizer.getMessage("notInGame"));
						return true;
					}
				}else {
					sender.sendMessage(localizer.getMessage("commandNotFound"));
					return true;
				}
			}else {
				final List<String> helpMenu = instance.getLocalizer().getStringList("helpMenu");
				for(String line : helpMenu) {
					if(line.contains("perm")) {
						final String[] splitPerm = line.split("\\)");
						final String[] splitBegPerm = splitPerm[0].split("\\(");
						final String perm = splitBegPerm[1];
						
						// Let's remove the permission section and the original line.
						if(player.hasPermission(perm)) {
							// Add back the fixed line.
							line = line.replace(line.substring(line.indexOf("{"), line.indexOf("}") + 1), "");
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
						}
					}else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
					}
				}
				String versionMsg = String.format("&7RaidPractice [&b%s&7] for &aMinecraft 1.9.4", instance.getDescription().getVersion());
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', versionMsg));
				return true;
			}
		}
		return false;
	}
	
	public ArenaCommands getArenaCommands() {
		return this.arenaCmds;
	}
}
