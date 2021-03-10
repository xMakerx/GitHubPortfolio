package net.stridefactions.foundation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.stridefactions.foundation.gui.MoneyPotMenu;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public class Commands implements CommandExecutor {
	
	final StrideFoundation instance;
	
	public Commands() {
		this.instance = StrideFoundation.getInstance();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLbl, String[] args) {
		final Player player = (sender instanceof Player) ? (Player) sender : null;
		final Settings settings = instance.getSettings();
		
		if(player == null && args.length == 0 || 
				player == null && args.length > 0 && !args[0].equalsIgnoreCase("reset")) {
			settings.sendMessage(sender, settings.getString("mustBePlayer"));
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("pot") || cmd.getName().equalsIgnoreCase("foundation")) {
			if(sender.hasPermission("stride.moneypot.use")) {
				if(args.length == 0 && player != null) {
					instance.getMenuManager().addMenu(player, new MoneyPotMenu(player));
				}else if(args.length == 1) {
					if(Arrays.asList("sethologram", "delhologram", "reset", "setblock").contains(args[0]) && sender.hasPermission("stride.moneypot.admin")) {
						if(player != null && Arrays.asList("sethologram", "delhologram", "setblock").contains(args[0])) {
							if(args[0].equalsIgnoreCase("sethologram")) {
								final Location loc = new Location(player.getWorld(), player.getLocation().getX(), 
									player.getLocation().getY() + settings.getHologramHeight(), player.getLocation().getZ());
								
								final Hologram hologram = HologramsAPI.createHologram(instance, loc);
								final List<String> lore = new ArrayList<String>(settings.getHologramLines());
								
								for(String line : lore) {
									line = line.replaceAll("\\{progress\\}", MoneyPot.getProgressString());
									line = line.replaceAll("\\{progressBar\\}", MoneyPot.getProgressBar());
									line = line.replaceAll("\\{goal\\}", String.valueOf(MoneyPot.getMaxBalance()));
									hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', line));
								}
								
								HologramManager.registerHologram(hologram);
								settings.sendMessage(sender, settings.getString("hologramCreated"));
							}else if(args[0].equalsIgnoreCase("delhologram")) {
								
								boolean foundHologram = false;
								for(final Hologram hologram : HologramManager.getHolograms()) {
									final Location potentialLoc = new Location(player.getWorld(), player.getLocation().getX(),
										player.getLocation().getY() + settings.getHologramHeight(), player.getLocation().getZ());
									
									if(hologram.getLocation().equals(potentialLoc)) {
										HologramManager.removeHologram(hologram);
										settings.sendMessage(player, settings.getString("hologramDeleted"));
										foundHologram = true;
										break;
									}
								}
								
								if(!foundHologram) {
									settings.sendMessage(player, settings.getString("noHolograms"));
								}
								
							}else if(args[0].equalsIgnoreCase("setblock")) {
								instance.getData().setEffectBlock(player.getLocation());
								settings.sendMessage(player, settings.getString("setBlock"));
							}
						}else if(args[0].equalsIgnoreCase("reset")) {
							MoneyPot.setBalance(0, true);
							settings.sendMessage(player, settings.getString("moneyPotReset"));
						}else if(player == null) {
							settings.sendMessage(sender, settings.getString("mustBePlayer"));
						}
					}else {
						settings.sendMessage(sender, settings.getString("noPermission"));
					}
				}
			}else if(player != null && !player.hasPermission("stride.moneypot.use")) {
				settings.sendMessage(player, settings.getString("noPermission"));
			}
			
			return true;
		}
		
		return false;
	}
}
