package com.coginvasion.stridebases;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;

public class CheckedOutBase {
	
	final StrideBases instance;
	final UUID player;
	final Base base;
	
	public CheckedOutBase(final UUID player, final Base base) {
		this.instance = StrideBases.getInstance();
		this.player = player;
		this.base = base;
	}
	
	public boolean canBuildHere(final Player player) {
		final MPlayer mPlayer = MPlayer.get(player);
		final Location location = player.getLocation();
		int width = base.getSchematic().getWidth();
		int length = base.getSchematic().getLength();
		
		for(int x = location.getBlockX(); x <= location.getBlockX() + width; x++) {
			for(int z = location.getBlockZ(); z <= location.getBlockZ() + length; z++) {
				final Location loc = new Location(location.getWorld(), x, location.getBlockY(), z);
				final Faction f = BoardColl.get().getFactionAt(PS.valueOf(loc));
				
				if(!f.getMPlayers().contains(mPlayer)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public ItemStack getSkull() {
		final Settings settings = StrideBases.getSettings();
		final ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
		final SkullMeta meta = (SkullMeta) item.getItemMeta();
		meta.setOwner(settings.getSkullOwner());
		meta.setLore(settings.getSkullLore());
		meta.setDisplayName(settings.getSkullName());
		item.setItemMeta(meta);
		return item;
	}
	
	public UUID getUUID() {
		return this.player;
	}
	
	public Base getBase() {
		return this.base;
	}
}
