package com.coginvasion.stride;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.coginvasion.stride.barrier.Barrier;
import com.coginvasion.stride.barrier.BarrierManager;
import com.coginvasion.stride.barrier.BarrierSession;
import com.coginvasion.stride.barrier.BarrierUtil;
import com.coginvasion.stride.barrier.SessionState;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;

public class Commands implements CommandExecutor {
	
	final StrideBarriers instance;
	final Settings settings;
	final BarrierManager barMgr;
	
	public Commands() {
		this.instance = StrideBarriers.getInstance();
		this.settings = StrideBarriers.getSettings();
		this.barMgr = StrideBarriers.getBarrierManager();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLbl, String[] args) {
		final Player player;
		
		if(sender instanceof Player) {
			player = (Player) sender;
		}else {
			player = null;
			sender.sendMessage(settings.getMessage("mustBePlayer"));
			return true;
		}
		
		if(cmd.getName().equalsIgnoreCase("barriers")) {
			if(args.length == 0) {
				StrideBarriers.getMenuManager().addMenu(player, new BarriersMenu(instance, player));
			}else if(args.length == 1 && args[0].equalsIgnoreCase("construct")) {
				final BarrierSession session = barMgr.getBarrierSession(player.getUniqueId());
				if(session == null) {
					player.sendMessage(settings.getMessage("noSession"));
					return true;
				}else if(session.getFirstCorner() == null || session.getSecondCorner() == null) {
					player.sendMessage(settings.getMessage("incompleteSelection"));
					return true;
				}
				
				if(BarrierUtil.canBuild(player, BarrierUtil.getOutlineBlocks(session.getFirstCorner(), 
						session.getSecondCorner()))) {
					final double cost = barMgr.calculateCost(session);
					StrideBarriers.getEconomy().withdrawPlayer(player, cost);
					String deduction = settings.getString("amtDeducted");
					deduction = deduction.replaceAll("\\{cost\\}", String.valueOf(cost));
					player.sendMessage(settings.color(deduction));
					barMgr.removeWand(player);
					session.startConstructing();
				}
				
			}else if(args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
				final BarrierSession session = barMgr.getBarrierSession(player.getUniqueId());
				if(session == null) {
					player.sendMessage(settings.getMessage("noSession"));
				}else if(session.getState() == SessionState.SELECTING) {
					session.setCloseRequested(true);
					barMgr.removeBarrierSession(player.getUniqueId());
					player.sendMessage(settings.getMessage("sessionCanceled"));
				}else {
					player.sendMessage(settings.getMessage("barrierInProgress"));
				}
				
				return true;
			}else if(args.length == 1 && args[0].equalsIgnoreCase("destroy")) {
				final Faction plyF = MPlayer.get(player).getFaction();
				final Barrier barrier = barMgr.getBarrier(plyF.getId());
				
				if(barrier == null) {
					player.sendMessage(settings.getMessage("noBarrier"));
					return true;
				}
				
				if(plyF.getLeader().equals(MPlayer.get(player))) {
					barrier.setDurability(0, true);
					barMgr.removeBarrier(plyF.getId());
				}else {
					player.sendMessage(settings.getMessage("mustBeLeader"));
					return true;
				}
			}
		}
		
		return false;
	}
	
}
