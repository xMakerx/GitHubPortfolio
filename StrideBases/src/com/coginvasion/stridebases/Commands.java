package com.coginvasion.stridebases;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
	
	final StrideBases instance;
	final Settings settings;
	final MenuManager menuMgr;
	
	public Commands(final StrideBases main) {
		this.instance = main;
		this.settings = StrideBases.getSettings();
		this.menuMgr = StrideBases.getMenuManager();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLbl, String[] args) {
		final Player player;
		
		if(sender instanceof Player) {
			player = (Player) sender;
		}else {
			player = null;
		}
		
		if(cmd.getName().equalsIgnoreCase("bases")) {
			if(args.length == 0 && player != null) {
				menuMgr.addMenu(player, new BasesMenu(instance, player));
			}else if(args.length == 0 && player == null) {
				sender.sendMessage(settings.getMessage("mustBePlayer"));
				return true;
			}else if(args.length == 3 && args[0].equalsIgnoreCase("add")) {
				
				if(!sender.hasPermission("stridebases.add")) {
					sender.sendMessage(settings.getMessage("noPermission"));
					return true;
				}
				
				final Player user = Bukkit.getPlayer(args[1]);
				final String base = args[2];
				
				if(user == null) {
					sender.sendMessage(settings.getMessage("playerNotFound"));
					return true;
				}else {
					String giveMsg = settings.getString("gaveBase");
					giveMsg = giveMsg.replaceAll("\\{player\\}", user.getDisplayName());
					
					Base baseObj = null;
					
					for(final Base tBase : settings.getBases()) {
						if(tBase.getCodeName().equalsIgnoreCase(base)) {
							baseObj = tBase;
							break;
						}
					}
					
					if(baseObj == null) {
						sender.sendMessage(settings.getMessage("baseNotFound"));
						return true;
					}
					
					settings.addOwnedBase(user.getUniqueId(), baseObj);
					giveMsg = giveMsg.replaceAll("\\{base\\}", base);
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', giveMsg));
				}
			}
		}else if(cmd.getName().equalsIgnoreCase("bundo") && args.length == 0) {
			if(player == null) {
				sender.sendMessage(settings.getMessage("mustBePlayer"));
				return true;
			}else {
				final BaseBuildSession session = settings.getBaseBuildSession(player.getUniqueId());
				if(session == null) {
					sender.sendMessage(settings.getMessage("cantUndo"));
					return true;
				}else if(session != null && session.isBaseComplete()) {
					session.restorePreviousData();
				}else {
					sender.sendMessage(settings.getMessage("mustWait"));
				}
			}
		}
		
		return false;
	}
}
