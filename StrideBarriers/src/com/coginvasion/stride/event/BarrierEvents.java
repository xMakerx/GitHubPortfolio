package com.coginvasion.stride.event;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import com.coginvasion.stride.Settings;
import com.coginvasion.stride.StrideBarriers;
import com.coginvasion.stride.barrier.Barrier;
import com.coginvasion.stride.barrier.BarrierManager;
import com.coginvasion.stride.barrier.BarrierUtil;

public class BarrierEvents implements Listener {
	
	final StrideBarriers instance;
	final Settings settings;
	final BarrierManager barMgr;
	
	public BarrierEvents(final StrideBarriers main) {
		this.instance = main;
		this.settings = StrideBarriers.getSettings();
		this.barMgr = StrideBarriers.getBarrierManager();
	}
	
	@EventHandler
	public void onEntityExplode(final EntityExplodeEvent evt) {
		final Entity ent = evt.getEntity();
		final List<Block> blocks = evt.blockList();
		final HashSet<Location> addBlocks = new HashSet<Location>();
		
		// Let's make sure this is PrimedTNT.
		if(ent instanceof TNTPrimed) {
			
			for(final Barrier bar : barMgr.getBarriers()) {
				
				boolean durChanged = false;
				
				for(final Block b : blocks) {
					if(bar.containsBlock(b)) {
						addBlocks.add(b.getLocation());
						if(!durChanged) {
							bar.setDurability(bar.getDurability() - 1, false);
							durChanged = true;
						}
						evt.setCancelled(true);
						break;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityChangeBlock(final EntityChangeBlockEvent evt) {
		final Entity ent = evt.getEntity();
		
		if(ent instanceof FallingBlock) {
			final FallingBlock block = (FallingBlock) ent;
			if(block.getCustomName() != null && block.getCustomName().equalsIgnoreCase("BARRIER_RUBBLE")) {
				evt.setCancelled(true);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBarrierDestroyed(final BarrierDestroyedEvent evt) {
		final Barrier bar = evt.getBarrier();
		
		for(final Location corner : BarrierUtil.getCorners(bar.getFirstCorner(), bar.getSecondCorner())) {
			final Firework fw = (Firework) corner.getWorld().spawnEntity(corner, EntityType.FIREWORK);
			final FireworkMeta meta = fw.getFireworkMeta();
			meta.addEffect(FireworkEffect.builder()
				.trail(true)
				.with(Type.BALL_LARGE).withColor(Color.LIME)
				.withColor(Color.WHITE)
			.build());
			fw.setFireworkMeta(meta);
		}
		
		for(final Block b : bar.getBlocks()) {
			b.setType(Material.AIR);
			final FallingBlock block = b.getWorld().spawnFallingBlock(b.getLocation(), Material.STAINED_CLAY, bar.getType().getColor().getData());
			
			block.setHurtEntities(false);
			block.setCustomName("BARRIER_RUBBLE");
			block.setVelocity(new Vector(0.5, 0.5, 0.5));
			block.setDropItem(false);
		}
		
		StrideBarriers.getBarrierManager().removeBarrier(bar.getFactionId());
		StrideBarriers.getBarrierManager().saveData();
		
		if(!evt.wasRemoved()) {
			bar.getFirstCorner().getWorld().playSound(bar.getFirstCorner(), settings.getBarrierDeathSound(), 1F, 1F);
		}else {
			final Location firstLoc = bar.getFirstCorner();
			final Location secondLoc = bar.getSecondCorner();
			final Location strike = new Location(firstLoc.getWorld(), (firstLoc.getX() + secondLoc.getX()) / 2, firstLoc.getY() - 1, (firstLoc.getZ() + secondLoc.getZ()) / 2);
			strike.getWorld().spigot().strikeLightning(strike, false);
		}
	}
}
