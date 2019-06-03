package com.coginvasion.stridebases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.coginvasion.stridebases.schematic.Schematic;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.massivecore.ps.PS;

public class Base {
	
	final StrideBases instance;
	final Base thisBase;
	private final String name;
	private final List<String> lore;
	private final Schematic schematic;
	
	private ArrayList<Block> allBlocks;
	
	public Base(final StrideBases main, final String name, final List<String> lore, final Schematic schematic) {
		this.instance = main;
		this.thisBase = this;
		this.name = name;
		this.lore = lore;
		this.schematic = schematic;
		
		this.allBlocks = new ArrayList<Block>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getCodeName() {
		return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));
	}
	
	public List<String> getLore() {
		return this.lore;
	}
	
	public Schematic getSchematic() {
		return this.schematic;
	}
	
	public boolean isValidRegion(final Location cornerLoc) {
		final Faction wilderness = FactionColl.get().getNone();
		final Location rCorner = new Location(cornerLoc.getWorld(), cornerLoc.getX() + schematic.getWidth(), 
				cornerLoc.getY(), cornerLoc.getZ());
		final Location tLCorner = new Location(cornerLoc.getWorld(), cornerLoc.getX(), cornerLoc.getY(),
				cornerLoc.getZ() + schematic.getLength());
		final Location tRCorner = new Location(rCorner.getWorld(), rCorner.getX(), rCorner.getY(),
				rCorner.getZ() + schematic.getLength());
		
		return BoardColl.get().getFactionAt(PS.valueOf(cornerLoc)).equals(wilderness) &&
			BoardColl.get().getFactionAt(PS.valueOf(rCorner)).equals(wilderness) &&
			BoardColl.get().getFactionAt(PS.valueOf(tLCorner)).equals(wilderness) &&
			BoardColl.get().getFactionAt(PS.valueOf(tRCorner)).equals(wilderness);
	}
	
	public void build(final Player player, final Location buildLoc) {
		final HashMap<Block, Integer> blocks = new HashMap<Block, Integer>();
		
		if(!isValidRegion(buildLoc)) {
			player.sendMessage(StrideBases.getSettings().getMessage("invalidRegion"));
			return;
		}else {
			player.sendMessage(StrideBases.getSettings().getMessage("undoInfo"));
		}
		
		for(int x = 0; x < schematic.getWidth(); x++){
			for (int y = 0; y < schematic.getHeight(); y++){
				for (int z = 0; z < schematic.getLength(); ++z){
					Location temp = buildLoc.clone().add(x, y, z);
					Block block = temp.getBlock();
					int index = y * schematic.getWidth() * schematic.getLength() + z * schematic.getWidth() + x;
					
					blocks.put(block, index);
					allBlocks.add(block);
				}
			}
		}
		
		final List<Block> orderedBlocks = new ArrayList<Block>();
		
		orderedBlocks.addAll(allBlocks);
		
		Collections.sort(orderedBlocks, new Comparator<Block>(){
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
		
		final BaseBuildSession session = StrideBases.getSettings().startBuildSession(player, this);
		
		final int size = orderedBlocks.size();
		final long delay = 1L;
		
		if(size > 0){
			new BukkitRunnable() {
				int blocksBuilt = 0;
				final int blocksPerTime = 10;
				
				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					
					if(blocksBuilt == 0) {
						TextUtil.sendTitle(player, 
							ChatColor.translateAlternateColorCodes('&', getName()), 
							StrideBases.getSettings().getMessage("buildingBegan"), 
							1, 4, 1);
					}
					
					for(int i = 0; i < blocksPerTime; i++) {
						if(blocksBuilt < size) {
							
							float perct = Float.valueOf(blocksBuilt) / Float.valueOf(size);
							final int greenSquares = Math.round(6 * perct);
							String progressTxt = StrideBases.getSettings().getString("progress").concat(" ");
							for(int g = 0; g < greenSquares; g++) {
								progressTxt = progressTxt.concat("&a▮&r ");
							}
							
							for(int r = 0; r < (6 - greenSquares); r++) {
								progressTxt = progressTxt.concat("&c▮&r ");
							}
							
							TextUtil.sendActionBarMsg(player, ChatColor.translateAlternateColorCodes('&', progressTxt));
							
							Block block = orderedBlocks.get(blocksBuilt);
							int otherIndex = blocks.get(block);
							int typeId = schematic.getBlocks()[otherIndex];
							byte data = schematic.getData()[otherIndex];
							
							if(typeId < 0) typeId += 256;
							if(data < 0) data += 256;
							
							if(!(block.getLocation().equals(buildLoc))) {
								final Location loc = block.getLocation();
								final Material type = Material.getMaterial(typeId);
								
								if(type != null) {
									loc.getWorld().playEffect(loc, Effect.STEP_SOUND, (type == Material.AIR ? block.getType().getId() : type.getId()));
									session.addPreviousData(loc, block.getTypeId(), block.getData());
									block.setTypeIdAndData(type.getId(), data, false);
								}
							}
							
							blocksBuilt += 1;
						} else {
							this.cancel();
							player.playSound(player.getLocation(), StrideBases.getSettings().getBaseCompleteSound(), 1F, 1F);
							TextUtil.sendTitle(player, 
									ChatColor.translateAlternateColorCodes('&', getName()), 
									StrideBases.getSettings().getMessage("buildingComplete"), 
									1, 4, 1);
							Bukkit.dispatchCommand(instance.getServer().getConsoleSender(), "/lagg clear");
							session.setBaseComplete(true);
							return;
						}
					}
				}
			}.runTaskTimer(instance, 20L, delay);
			StrideBases.getSettings().removeOwnedBase(player, this);
		}
	}
	
	public ArrayList<Block> getBlocks() {
		return this.allBlocks;
	}

}
