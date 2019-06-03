package com.coginvasion.stride.barrier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.coginvasion.stride.StrideBarriers;
import com.coginvasion.stride.TextUtil;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;

public class BarrierSession {
	
	final Player player;
	final BarrierType type;
	
	private Location l1;
	private Location l2;
	private SessionState state;
	private boolean closeRequested;
	
	private List<Block> outline;
	
	public BarrierSession(final Player player, final BarrierType type) {
		this.player = player;
		this.type = type;
		this.l1 = null;
		this.l2 = null;
		this.state = SessionState.SELECTING;
		this.closeRequested = false;
		this.outline = new ArrayList<Block>();
		
		StrideBarriers.getBarrierManager().addBarrierSession(player.getUniqueId(), this);
		beginTimeoutTask();
	}
	
	public void startConstructing() {
		outline = BarrierUtil.getOutlineBlocks(l1, l2);
		setState(SessionState.CONSTRUCTING);
		Collections.sort(outline, new Comparator<Block>(){
			@Override
			public int compare(Block block1, Block block2){
				return Double.compare(block1.getY(), block2.getY());
			}

			@Override
			public Comparator<Block> reversed() {
				return null;
			}

			@Override
			public Comparator<Block> thenComparing(
					Comparator<? super Block> arg0) {
				return null;
			}

			@Override
			public <U extends Comparable<? super U>> Comparator<Block> thenComparing(
					Function<? super Block, ? extends U> arg0) {
				return null;
			}

			@Override
			public <U> Comparator<Block> thenComparing(
					Function<? super Block, ? extends U> arg0,
					Comparator<? super U> arg1) {
				return null;
			}

			@Override
			public Comparator<Block> thenComparingDouble(
					ToDoubleFunction<? super Block> arg0) {
				return null;
			}

			@Override
			public Comparator<Block> thenComparingInt(
					ToIntFunction<? super Block> arg0) {
				return null;
			}

			@Override
			public Comparator<Block> thenComparingLong(
					ToLongFunction<? super Block> arg0) {
				return null;
			}
		});
		
		new BukkitRunnable() {
			int blocksBuilt = 0;
			
			@SuppressWarnings("deprecation")
			public void run() {
				
				if(blocksBuilt == 0) {
					TextUtil.sendTitle(player, ChatColor.GREEN + type.getColor().name(), 
						StrideBarriers.getSettings().getMessage("barrierStarted"), 
						1, 4, 1);
				}
				
				for(int i = 0; i < 5; i++) {
					if(blocksBuilt < outline.size()) {
						float perct = Float.valueOf(blocksBuilt) / Float.valueOf(outline.size());
						final int greenSquares = Math.round(6 * perct);
						String progressTxt = StrideBarriers.getSettings().getString("progress").concat(" ");
						for(int g = 0; g < greenSquares; g++) {
							progressTxt = progressTxt.concat("&a▮&r ");
						}
						
						for(int r = 0; r < (6 - greenSquares); r++) {
							progressTxt = progressTxt.concat("&c▮&r ");
						}
						
						TextUtil.sendActionBarMsg(player, ChatColor.translateAlternateColorCodes('&', progressTxt));
						
						final Block b = outline.get(blocksBuilt);
						b.setTypeIdAndData(Material.STAINED_CLAY.getId(), type.getColor().getData(), true);
						b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, Material.STAINED_CLAY.getId());
						blocksBuilt += 1;
					}else {
						break;
					}
				}
				
				if(blocksBuilt == outline.size()) {
					this.cancel();
					TextUtil.sendTitle(player, 
							ChatColor.GREEN + type.getColor().name(),
							StrideBarriers.getSettings().getMessage("barrierComplete"), 
							1, 4, 1);
					
					// Let's create the Barrier object.
					final Faction plyFaction = MPlayer.get(player).getFaction();
					final Barrier barrier = new Barrier(plyFaction.getId(), type, l1, l2, type.getDurability());
					StrideBarriers.getBarrierManager().addBarrier(plyFaction.getId(), barrier);
					StrideBarriers.getBarrierManager().removeBarrierSession(player.getUniqueId());
					player.playSound(player.getLocation(), StrideBarriers.getSettings().getBarrierBuiltSound(), 1F, 1F);
				}
			}
			
		}.runTaskTimer(StrideBarriers.getInstance(), 20L, 20L);
	}
	
	public void beginTimeoutTask() {
		new BukkitRunnable() {
			
			public void run() {
				if(player != null && !isCloseRequested() && getState() == SessionState.SELECTING) {
					player.sendMessage(StrideBarriers.getSettings().getMessage("selectionTimeout"));
					StrideBarriers.getBarrierManager().removeBarrierSession(player.getUniqueId());
				}
				
				this.cancel();
			}
			
		}.runTaskLater(StrideBarriers.getInstance(), 20L * 180);
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public BarrierType getBarrierType() {
		return this.type;
	}
	
	public void setFirstCorner(final Location l1) {
		this.l1 = l1;
	}
	
	public Location getFirstCorner() {
		return this.l1;
	}
	
	public void setSecondCorner(final Location l2) {
		this.l2 = l2;
	}
	
	public Location getSecondCorner() {
		return this.l2;
	}
	
	public void setState(final SessionState state) {
		this.state = state;
	}
	
	public SessionState getState() {
		return this.state;
	}
	
	public void setCloseRequested(final boolean flag) {
		this.closeRequested = flag;
	}
	
	public boolean isCloseRequested() {
		return this.closeRequested;
	}
	
	public List<Block> getBlocks() {
		return this.outline;
	}
}
