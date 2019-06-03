package com.coginvasion.stride.barrier;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

import com.coginvasion.stride.StrideBarriers;
import com.coginvasion.stride.TextUtil;
import com.coginvasion.stride.event.BarrierDestroyedEvent;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;

public class Barrier {
	
	final String factionId;
	final BarrierType type;
	final Location l1;
	final Location l2;
	final List<Block> blocks;
	int durability;
	
	public Barrier(final String fId, final BarrierType type, final Location l1,
			final Location l2, final int durability) {
		this.factionId = fId;
		this.type = type;
		this.l1 = l1;
		this.l2 = l2;
		this.blocks = BarrierUtil.getOutlineBlocksOfType(l1, l2, type);
		this.durability = durability;
	}
	
	public String getFactionId() {
		return this.factionId;
	}
	
	public Faction getFaction() {
		final FactionColl coll = FactionColl.get();
		if(coll.containsId(factionId)) {
			return coll.get(factionId);
		}
		return null;
	}
	
	public BarrierType getType() {
		return this.type;
	}
	
	public Location getFirstCorner() {
		return this.l1;
	}
	
	public Location getSecondCorner() {
		return this.l2;
	}
	
	public boolean containsBlock(final Block b) {
		for(final Block block : blocks) {
			if(block.getLocation().equals(b.getLocation())) {
				return true;
			}
		}
		return false;
	}
	
	public List<Block> getBlocks() {
		return this.blocks;
	}
	
	public void setDurability(final int durability, boolean forceRemove) {
		this.durability = durability;
		
		if(durability <= 0) {
			// Man down, man down! Let the server know we're toast.
			StrideBarriers.getInstance().getServer().getPluginManager().callEvent(new BarrierDestroyedEvent(this, forceRemove));
			final Faction f = FactionColl.get().get(factionId);
			for(final MPlayer mPlayer : f.getMPlayers()) {
				final Player player = Bukkit.getPlayer(mPlayer.getUuid());
				if(player != null) {
					String fStr = StrideBarriers.getSettings().getString("factionName");
					fStr = fStr.replaceAll("\\{faction\\}", f.getName());
					TextUtil.sendTitle(player, 
						StrideBarriers.getSettings().color(fStr), 
						StrideBarriers.getSettings().getMessage("barrierDestroyed"), 
					1, 2, 1);
				}
			}
		}else {
			final Faction f = FactionColl.get().get(factionId);
			for(final MPlayer mPlayer : f.getMPlayers()) {
				final Player player = Bukkit.getPlayer(mPlayer.getUuid());
				if(player != null) {
					String dmgStr = StrideBarriers.getSettings().getString("barrierDamaged");
					dmgStr = dmgStr.replaceAll("\\{health\\}", String.valueOf(durability));
					dmgStr = dmgStr.replaceAll("\\{maxHealth\\}", String.valueOf(type.getDurability()));
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', dmgStr));
				}
			}
		}
	}
	
	public int getDurability() {
		return this.durability;
	}
}
