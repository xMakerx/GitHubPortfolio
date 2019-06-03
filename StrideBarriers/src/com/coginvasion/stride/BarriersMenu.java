package com.coginvasion.stride;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.coginvasion.stride.barrier.BarrierSession;
import com.coginvasion.stride.barrier.BarrierType;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;

public class BarriersMenu extends Menu {
	
	final Settings settings;
	LinkedHashMap<Integer, BarrierType> slots;
	boolean opened;

	public BarriersMenu(StrideBarriers main, Player player) {
		super(main, player);
		this.inv = Bukkit.createInventory(null, 54, StrideBarriers.getSettings().getMessage("menuTitle"));
		this.settings = StrideBarriers.getSettings();
		this.slots = new LinkedHashMap<Integer, BarrierType>();
		this.opened = false;
	}
	
	public ChatColor getChatColor(final DyeColor color) {
		final HashMap<DyeColor, ChatColor> colors = new HashMap<DyeColor, ChatColor>();
		colors.put(DyeColor.GREEN, ChatColor.DARK_GREEN);
		colors.put(DyeColor.BLACK, ChatColor.BLACK);
		colors.put(DyeColor.LIME, ChatColor.GREEN);
		colors.put(DyeColor.PINK, ChatColor.DARK_PURPLE);
		colors.put(DyeColor.MAGENTA, ChatColor.DARK_PURPLE);
		colors.put(DyeColor.PURPLE, ChatColor.LIGHT_PURPLE);
		colors.put(DyeColor.BLUE, ChatColor.DARK_BLUE);
		colors.put(DyeColor.BROWN, ChatColor.GOLD);
		colors.put(DyeColor.CYAN, ChatColor.AQUA);
		colors.put(DyeColor.GRAY, ChatColor.DARK_GRAY);
		colors.put(DyeColor.LIGHT_BLUE, ChatColor.BLUE);
		colors.put(DyeColor.ORANGE, ChatColor.GOLD);
		colors.put(DyeColor.WHITE, ChatColor.WHITE);
		colors.put(DyeColor.YELLOW, ChatColor.YELLOW);
		colors.put(DyeColor.RED, ChatColor.DARK_RED);
		colors.put(DyeColor.SILVER, ChatColor.GRAY);
		return colors.get(color);
	}

	@Override
	public void show() {
		inv.clear();
		
		int initSlot = 11;
		
		final MPlayer mPlayer = MPlayer.get(player);
		final Faction plyFaction = MPlayer.get(player).getFaction();
		
		if(StrideBarriers.getBarrierManager().getBarrier(plyFaction.getId()) == null && plyFaction.getLeader() == mPlayer) {
			final ArrayList<BarrierType> types = StrideBarriers.getSettings().getBarrierTypes();
			
			for(final BarrierType type : types) {
				@SuppressWarnings("deprecation")
				final ItemStack item = new ItemStack(Material.STAINED_CLAY, 1, (byte) type.getColor().getData());
				final ItemMeta meta = item.getItemMeta();
				
				// The price message.
				String priceMsg = settings.getString("price");
				priceMsg = priceMsg.replaceAll("\\{price\\}", String.valueOf(type.getCost()));
				
				final ChatColor titleColor = getChatColor(type.getColor());
				meta.setDisplayName(titleColor + type.getColor().name());
				meta.setLore(Arrays.asList(settings.color(priceMsg)));
				item.setItemMeta(meta);
				
				slots.put(initSlot, type);
				inv.setItem(initSlot, item);
				
				if(Arrays.asList(15, 24, 33).contains(initSlot)) {
					initSlot += 5;
				}else {
					initSlot += 1;
				}
			}
			
			for(int i = 0; i < (15 - (slots.size())); i++) {
				inv.setItem(initSlot, settings.getEmptySlotItem().clone());
				if(Arrays.asList(15, 24, 33).contains(initSlot)) {
					initSlot += 5;
				}else {
					initSlot += 1;
				}
			}
			
			inv.setItem(49, settings.getInfoHeadItem().clone());
		}else {
			final ItemStack item = settings.getEmptySlotItem();
			final ItemMeta meta = item.getItemMeta();
			
			if(plyFaction.getLeader() == mPlayer) {
				meta.setDisplayName(settings.getMessage("barrierAlreadyBuilt"));
				
				final ItemStack rmv = settings.getRemoveItem();
				inv.setItem(40, rmv);
			}else {
				meta.setDisplayName(settings.getMessage("notLeader"));
			}
			item.setItemMeta(meta);
			item.setDurability((short) 14);
			inv.setItem(22, item);
		}
		
		if(!opened) {
			player.openInventory(inv);
			opened = true;
		}
		
		player.updateInventory();
	}

	@Override
	public void click(InventoryClickEvent evt) {
		final int slot = evt.getSlot();
		final BarrierType type = slots.get(slot);
		
		if(type != null) {
			final Economy econ = StrideBarriers.getEconomy();
			
			player.closeInventory();
			if(econ.getBalance(player) < type.getCost()) {
				player.sendMessage(settings.getMessage("cantAfford"));
			}else {
				if(player.getInventory().firstEmpty() == -1) {
					player.sendMessage(settings.getMessage("clearInventory"));
				}else {
					final ItemStack itemInHand = player.getInventory().getItemInHand();
					if(itemInHand != null) {
						player.getInventory().setItem(player.getInventory().firstEmpty(), itemInHand);
					}
					new BarrierSession(player, type);
					player.sendMessage(settings.getMessage("beginSelection"));
					player.getInventory().setItemInHand(StrideBarriers.getSettings().getBarrierWand().clone());
					player.updateInventory();
				}
			}
		}else if(evt.getCurrentItem() != null && evt.getCurrentItem().isSimilar(settings.getRemoveItem())) {
			StrideBarriers.getBarrierManager().sendConfirmation(player);
			StrideBarriers.getMenuManager().removeMenu(player);
		}
	}
}
