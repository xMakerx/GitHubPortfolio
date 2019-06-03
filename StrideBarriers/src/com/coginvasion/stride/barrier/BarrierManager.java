package com.coginvasion.stride.barrier;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.coginvasion.stride.StrideBarriers;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;

public class BarrierManager implements Listener {
	
	final StrideBarriers instance;
	final HashMap<String, Barrier> barriers;
	final HashMap<UUID, BarrierSession> barrierSessions;
	final File configFile;
	final YamlConfiguration config;
	final ConfigurationSection barSection;
	
	public BarrierManager() {
		this.instance = StrideBarriers.getInstance();
		this.barriers = new HashMap<String, Barrier>();
		this.barrierSessions = new HashMap<UUID, BarrierSession>();
		this.configFile = new File(instance.getDataFolder() + "/data.yml");
		
		boolean newFile = false;
		
		if(!configFile.exists()) {
			try {
				configFile.createNewFile();
				newFile = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		this.config = YamlConfiguration.loadConfiguration(configFile);
		
		if(newFile) config.createSection("barriers");
		this.barSection = config.getConfigurationSection("barriers");
		loadData();
	}
	
	public void loadData() {
		for(final String bar : barSection.getKeys(false)) {
			final ConfigurationSection section = barSection.getConfigurationSection(bar);
			
			// The faction id
			final String fId = section.getString("faction");
			final String typeStr = section.getString("type");
			final String worldStr = section.getString("world");
			final int durability = section.getInt("durability");
			
			final ConfigurationSection minSection = section.getConfigurationSection("firstCorner");
			final double minX = minSection.getDouble("x");
			final double minY = minSection.getDouble("y");
			final double minZ = minSection.getDouble("z");
			
			final ConfigurationSection maxSection = section.getConfigurationSection("secondCorner");
			final double maxX = maxSection.getDouble("x");
			final double maxY = maxSection.getDouble("y");
			final double maxZ = maxSection.getDouble("z");
			
			final World world = Bukkit.getWorld(worldStr);
			
			if(world == null) {
				instance.getLogger().severe(String.format("Failed to find World %s. Could not load Barrier %s.", worldStr, bar));
				continue;
			}
			
			final Location min = new Location(world, minX, minY, minZ);
			final Location max = new Location(world, maxX, maxY, maxZ);
			
			BarrierType type = null;
			
			for(final BarrierType iType : StrideBarriers.getSettings().getBarrierTypes()) {
				if(iType.getColor().name().equalsIgnoreCase(typeStr)) {
					type = iType;
					break;
				}
			}
			
			if(type == null) {
				instance.getLogger().severe(String.format("Failed to load BarrierType %s. Could not load Barrier %s.", typeStr, bar));
				continue;
			}
			
			final Barrier barrier = new Barrier(fId, type, min, max, durability);
			this.barriers.put(fId, barrier);
		}
	}
	
	public void saveData() {
		for(final String bar : barSection.getKeys(false)) {
			barSection.set(bar, null);
		}
		
		int index = 1;
		for(final Barrier barrier : barriers.values()) {
			final ConfigurationSection section = barSection.createSection(String.format("barrier_%s", String.valueOf(index)));
			section.set("faction", barrier.getFactionId());
			section.set("type", barrier.getType().getColor().name());
			section.set("world", barrier.getFirstCorner().getWorld().getName());
			section.set("durability", barrier.getDurability());
			
			final ConfigurationSection minSection = section.createSection("firstCorner");
			minSection.set("x", barrier.getFirstCorner().getX());
			minSection.set("y", barrier.getFirstCorner().getY());
			minSection.set("z", barrier.getFirstCorner().getZ());
			
			final ConfigurationSection maxSection = section.createSection("secondCorner");
			maxSection.set("x", barrier.getSecondCorner().getX());
			maxSection.set("y", barrier.getSecondCorner().getY());
			maxSection.set("z", barrier.getSecondCorner().getZ());
			
			index += 1;
		}
		
		save();
	}
	
	public void save() {
		try {
			this.config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addBarrier(final String factionId, final Barrier barrier) {
		this.barriers.put(factionId, barrier);
	}
	
	public void removeBarrier(final String factionId) {
		if(barriers.containsKey(factionId)) {
			this.barriers.remove(factionId);
		}
	}
	
	public Barrier getBarrier(final String factionId) {
		return barriers.get(factionId);
	}
	
	public Collection<Barrier> getBarriers() {
		return this.barriers.values();
	}
	
	public void addBarrierSession(final UUID uuid, final BarrierSession session) {
		this.barrierSessions.put(uuid, session);
	}
	
	public void removeBarrierSession(final UUID uuid) {
		if(barrierSessions.containsKey(uuid)) {
			this.barrierSessions.remove(uuid);
			
			final Player player = Bukkit.getPlayer(uuid);
			if(player == null) return;
			removeWand(player);
		}
	}
	
	public void removeWand(final Player player) {
		// Remove the edit wand.
		for(int i = 0; i < player.getInventory().getSize(); i++) {
			final ItemStack item = player.getInventory().getItem(i);
			if(item != null && item.isSimilar(StrideBarriers.getSettings().getBarrierWand())) {
				player.getInventory().setItem(i, null);
				player.updateInventory();
				break;
			}
		}
	}
	
	public BarrierSession getBarrierSession(final UUID uuid) {
		return this.barrierSessions.get(uuid);
	}
	
	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent evt) {
		final Player player = evt.getPlayer();
		removeBarrierSession(player.getUniqueId());
	}
	
	@EventHandler
	public void onItemDrop(final PlayerDropItemEvent evt) {
		final ItemStack item = evt.getItemDrop().getItemStack();
		final Player player = evt.getPlayer();
		
		if(item.isSimilar(StrideBarriers.getSettings().getBarrierWand())) {
			player.sendMessage(StrideBarriers.getSettings().getMessage("cantDrop"));
			evt.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryOpen(final InventoryOpenEvent evt) {
		final Player player = (Player) evt.getPlayer();
		
		for(final ItemStack item : player.getInventory().getContents()) {
			if(item != null && item.isSimilar(StrideBarriers.getSettings().getBarrierWand())) {
				player.sendMessage(StrideBarriers.getSettings().getMessage("inventoryOpen"));
				evt.setCancelled(true);
				break;
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(final InventoryClickEvent evt) {
		final ItemStack item = evt.getCurrentItem();
		
		if(item != null && item.isSimilar(StrideBarriers.getSettings().getBarrierWand())) {
			evt.setCancelled(true);
		}
	}
	
	public TextComponent constructComponent(final String text) {
		final TextComponent part = new TextComponent(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', text)));
		if(text.charAt(0) == '&') {
			part.setColor(ChatColor.getByChar(text.charAt(1)));
		}
		
		if(text.contains("&l")) {
			part.setBold(true);
		}else if(text.contains("&n")) {
			part.setUnderlined(true);
		}else if(text.contains("&o")) {
			part.setItalic(true);
		}else if(text.contains("&m")) {
			part.setStrikethrough(true);
		}else if(text.contains("&k")) {
			part.setObfuscated(true);
		}
		
		return part;
	}
	
	public double calculateCost(final BarrierSession session) {
		return session.getBarrierType().getCost() + BarrierUtil.getOutlineBlocks(session.getFirstCorner(), 
			session.getSecondCorner()).size();
	}
	
	public void sendConfirmation(final Player player) {
		String invoiceMsg = StrideBarriers.getSettings().getString("cancelConfirmation");
		String confirm = StrideBarriers.getSettings().getString("confirm");
		String cancel = StrideBarriers.getSettings().getString("cancel");
		
		String firstPart = invoiceMsg.substring(0, invoiceMsg.indexOf("{confirm}"));
		String middleTxt = invoiceMsg.substring(firstPart.length(), invoiceMsg.indexOf("{cancel}"));
		int previousLength = middleTxt.length();
		middleTxt = middleTxt.replaceAll("\\{confirm\\}", "");
		invoiceMsg = invoiceMsg.substring(firstPart.length() + previousLength, invoiceMsg.length());
		invoiceMsg = invoiceMsg.replaceAll("\\{cancel\\}", "");
		
		final TextComponent confirmCmpt = constructComponent(confirm);
		confirmCmpt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/barriers destroy"));
		confirmCmpt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Click to ").append("CONFIRM").color(ChatColor.GREEN).create()));
		
		final TextComponent cancelCmpt = constructComponent(cancel);
		cancelCmpt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/barriers"));
		cancelCmpt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Click to ").append("CANCEL").color(ChatColor.GREEN).create()));
		
		final TextComponent base = constructComponent(firstPart);
		final TextComponent middle = constructComponent(middleTxt);
		final TextComponent end = constructComponent(invoiceMsg);
		
		base.addExtra(confirmCmpt);
		base.addExtra(middle);
		base.addExtra(cancelCmpt);
		base.addExtra(end);
		
		player.spigot().sendMessage(base);
	}
	
	public void sendInvoice(final Player player) {
		if(getBarrierSession(player.getUniqueId()) == null) return;
		String invoiceMsg = StrideBarriers.getSettings().getString("invoice");
		invoiceMsg = invoiceMsg.replaceAll("\\{price\\}", String.valueOf(calculateCost(getBarrierSession(player.getUniqueId()))));
		String confirm = StrideBarriers.getSettings().getString("confirm");
		String cancel = StrideBarriers.getSettings().getString("cancel");
		
		String firstPart = invoiceMsg.substring(0, invoiceMsg.indexOf("{confirm}"));
		String middleTxt = invoiceMsg.substring(firstPart.length(), invoiceMsg.indexOf("{cancel}"));
		int previousLength = middleTxt.length();
		middleTxt = middleTxt.replaceAll("\\{confirm\\}", "");
		invoiceMsg = invoiceMsg.substring(firstPart.length() + previousLength, invoiceMsg.length());
		invoiceMsg = invoiceMsg.replaceAll("\\{cancel\\}", "");
		
		final TextComponent confirmCmpt = constructComponent(confirm);
		confirmCmpt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/barriers construct"));
		confirmCmpt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Click to ").append("CONFIRM").color(ChatColor.GREEN).create()));
		
		final TextComponent cancelCmpt = constructComponent(cancel);
		cancelCmpt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/barriers cancel"));
		cancelCmpt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Click to ").append("CANCEL").color(ChatColor.GREEN).create()));
		
		final TextComponent base = constructComponent(firstPart);
		final TextComponent middle = constructComponent(middleTxt);
		final TextComponent end = constructComponent(invoiceMsg);
		
		base.addExtra(confirmCmpt);
		base.addExtra(middle);
		base.addExtra(cancelCmpt);
		base.addExtra(end);
		
		player.spigot().sendMessage(base);
	}
	
	@EventHandler
	public void onPlayerInteract(final PlayerInteractEvent evt) {
		final Player player = evt.getPlayer();
		final ItemStack item = evt.getPlayer().getItemInHand();
		final BarrierSession session = getBarrierSession(player.getUniqueId());
		
		if(item != null && session != null) {
			if(evt.getAction() == Action.LEFT_CLICK_BLOCK && item.isSimilar(StrideBarriers.getSettings().getBarrierWand())) {
				session.setFirstCorner(evt.getClickedBlock().getLocation());
				player.sendMessage(StrideBarriers.getSettings().getMessage("firstPoint"));
				evt.setCancelled(true);
				if(session.getFirstCorner() != null && session.getSecondCorner() != null) {
					sendInvoice(player);
				}
			}else if(evt.getAction() == Action.RIGHT_CLICK_BLOCK && item.isSimilar(StrideBarriers.getSettings().getBarrierWand())) {
				session.setSecondCorner(evt.getClickedBlock().getLocation());
				player.sendMessage(StrideBarriers.getSettings().getMessage("secondPoint"));
				evt.setCancelled(true);
				if(session.getFirstCorner() != null && session.getSecondCorner() != null) {
					sendInvoice(player);
				}
			}
		}
		
	}
	
	@EventHandler
	public void onBlockBreak(final BlockBreakEvent evt) {
		final Block b = evt.getBlock();
		final Player player = evt.getPlayer();
		
		for(final Barrier bar : barriers.values()) {
			if(bar.containsBlock(b)) {
				evt.setCancelled(true);
				player.sendMessage(StrideBarriers.getSettings().getMessage("cantBreak"));
				break;
			}
		}
		
		if(!evt.isCancelled()) {
			for(final BarrierSession bs : barrierSessions.values()) {
				for(final Block block : bs.getBlocks()) {
					if(block.getLocation().equals(b.getLocation())) {
						evt.setCancelled(true);
						player.sendMessage(StrideBarriers.getSettings().getMessage("cantBreak"));
						break;
					}
				}
			}
		}
		
	}
	
    private Block getBlockDirectlyBehindPlayer(Player player) {
        // Won't get NPEs from me.
        if (player == null) {
            return null;
        }
        // Get player's location, but at head height. Add 1 to y.
        Location location = player.getLocation().add(0, 1, 0);
       // Get the player's direction and invert it on the x and z axis to get the opposite direction.
        Vector direction = location.getDirection().multiply(new Vector(-1, 0, -1));
 
       // Return the block at the location opposite of the direction the player is looking, 1 block forward.
        return player.getWorld().getBlockAt(location.add(direction));
    }
	
	@EventHandler
	public void onPlayerMove(final PlayerMoveEvent evt) {
		final Player player = evt.getPlayer();
		final Faction faction = MPlayer.get(player).getFaction();
		final Location from = evt.getFrom();
		final Location move = evt.getTo();
		Location loc = null;
		Location testLoc = null;
		
		if(from.distance(move) > 0 && !player.hasPermission("stridebarriers.admin")) {
			for(final Barrier bar : barriers.values()) {
				final int mX = move.getBlockX();
				final int mZ = move.getBlockZ();
				
				if(bar.getFactionId().equalsIgnoreCase(faction.getId())) {
					continue;
				}
				
				while(loc == null) {
					if(testLoc == null && move.getBlock().getType() == Material.STAINED_CLAY) {
						loc = move;
					}else if(testLoc == null) {
						testLoc = new Location(move.getWorld(), mX, move.getBlockY() - 1, mZ);
					}else {
						testLoc = new Location(move.getWorld(), testLoc.getBlockX(), testLoc.getBlockY() - 1, testLoc.getBlockZ());
					}
					
					if(testLoc.getBlock().getType() != Material.AIR) {
						loc = testLoc;
					}
				}
				
				if(bar.containsBlock(loc.getBlock())) {
					final Vector knockback = player.getLocation().getDirection().multiply(-2);
					final Vector knockForward = player.getLocation().getDirection().multiply(2);
					Vector vel = knockback;
					final Block behind = getBlockDirectlyBehindPlayer(player);
					
					if(bar.containsBlock(behind)) {
						vel = knockback;
					}else {
						vel = knockForward;
					}
					
					player.teleport(from);
					player.sendMessage(StrideBarriers.getSettings().getMessage("cantEnter"));
				}
			}
		}
	}
}
