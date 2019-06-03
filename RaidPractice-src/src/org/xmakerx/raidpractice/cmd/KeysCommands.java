package org.xmakerx.raidpractice.cmd;

import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.xmakerx.raidpractice.Localizer;
import org.xmakerx.raidpractice.RaidPractice;
import org.xmakerx.raidpractice.Settings;
import org.xmakerx.raidpractice.arena.ArenaManager;

public class KeysCommands {
	
	final RaidPractice instance;
	final Settings settings;
	final Localizer localizer;
	final ArenaManager arenaMgr;
	
	public KeysCommands(final RaidPractice main) {
		this.instance = main;
		this.settings = main.getSettings();
		this.localizer = main.getLocalizer();
		this.arenaMgr = instance.getArenaManager();
	}
	
	public boolean handleCommand(CommandSender sender, List<String> args) {
		Player player = null;
		
		if(sender instanceof Player) {
			player = (Player)sender;
		}
		
		if(args.size() == 0) {
			if(sender.hasPermission("rp.keys.balance") && player != null) {
				final int keys = instance.getStatsDatabase().getKeys(player);
				String msg = localizer.getMessage("keyBalance");
				msg = msg.replaceAll("\\{player\\}", player.getDisplayName());
				msg = msg.replaceAll("\\{keys\\}", String.valueOf(keys));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
			}else if(player == null) {
				sender.sendMessage(localizer.getMessage("mustBePlayer"));
			}else {
				sender.sendMessage(localizer.getMessage("noPermission"));
			}
			
			return true;
		}else if(args.size() == 1) {
			// Look up the balance of a player.
			if(sender.hasPermission("rp.keys.balance")) {
				final Player user = Bukkit.getPlayer(args.get(0));
				
				if(user != null) {
					final int keys = instance.getStatsDatabase().getKeys(user);
					String msg = localizer.getMessage("keyBalance");
					msg = msg.replaceAll("\\{player\\}", user.getDisplayName());
					msg = msg.replaceAll("\\{keys\\}", String.valueOf(keys));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
				}else {
					sender.sendMessage(localizer.getMessage("playerNotFound"));
				}
			}else {
				sender.sendMessage(localizer.getMessage("noPermission"));
			}
			
			return true;
		}else if(args.size() == 3 && args.get(0).equalsIgnoreCase("give")) {
			if(sender.hasPermission("rp.keys.give")) {
				final Player user = Bukkit.getPlayer(args.get(1));
				
				if(user != null) {
					try {
						// They entered a valid integer, let's give the keys.
						int keysToGive = Integer.parseInt(args.get(2));
						instance.getStatsDatabase().setKeys(user, instance.getStatsDatabase().getKeys(user) + keysToGive);
						instance.getStatsDatabase().save();
						String msg = localizer.getMessage("keysAdded");
						msg = msg.replaceAll("\\{player\\}", user.getDisplayName());
						msg = msg.replaceAll("\\{keys\\}", String.valueOf(keysToGive));
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
					}catch(NumberFormatException e) {
						// They did not enter an integer, let them know.
						String info = localizer.getString("badFlagArg");
						info = info.replaceAll("\\{type\\}", Integer.class.getSimpleName());
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', info));
					}
				}else {
					sender.sendMessage(localizer.getMessage("playerNotFound"));
				}
			}
		}
		
		
		
		return false;
	}
}
