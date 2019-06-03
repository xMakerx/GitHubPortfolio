package com.coginvasion.stridebases;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BasesMenu extends Menu {
	
	final Settings settings;
	LinkedHashMap<Integer, Base> baseSlots;
	boolean opened;

	public BasesMenu(StrideBases main, Player player) {
		super(main, player);
		this.inv = Bukkit.createInventory(null, 54, StrideBases.getSettings().getMessage("menuTitle"));
		this.settings = StrideBases.getSettings();
		this.baseSlots = new LinkedHashMap<Integer, Base>();
		this.opened = false;
	}

	@Override
	public void show() {
		this.inv.clear();
		
		int initSlot = 11;
		
		this.baseSlots.clear();
		final HashMap<Base, Boolean> ownedBases = settings.getOwnedBases(player);
		
		if(ownedBases != null) {
			for(Map.Entry<Base, Boolean> entry : ownedBases.entrySet()) {
				final Base base = entry.getKey();
				final boolean built = entry.getValue();
				final ItemStack item;
				final String loreLine;
				
				if(built) {
					item = settings.getBuiltBaseItem();
					loreLine = settings.getMessage("alreadyBuilt");
				}else {
					item = settings.getBuildBaseItem();
					loreLine = settings.getMessage("clickToBuild");
				}
				
				final ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', base.getName()));
				meta.setLore(Arrays.asList(loreLine));
				item.setItemMeta(meta);
				
				baseSlots.put(initSlot, base);
				inv.setItem(initSlot, item);
				if(Arrays.asList(15, 24, 33).contains(initSlot)) {
					initSlot += 5;
				}else {
					initSlot += 1;
				}
			}
		}
		
		if(baseSlots.size() > 0) {
			for(int i = 0; i < (15 - (baseSlots.size())); i++) {
				inv.setItem(initSlot, settings.getEmptySlotItem().clone());
				if(Arrays.asList(15, 24, 33).contains(initSlot)) {
					initSlot += 5;
				}else {
					initSlot += 1;
				}
			}
		}else {
			final ItemStack item = settings.getBuiltBaseItem();
			final ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(settings.getMessage("noBases"));
			item.setItemMeta(meta);
			inv.setItem(22, item);
		}
		
		inv.setItem(48, settings.getBackPageItem().clone());
		inv.setItem(49, settings.getDonateItem().clone());
		inv.setItem(50, settings.getNextPageItem().clone());
		
		if(!opened) {
			player.openInventory(this.inv);
			opened = true;
		}
		
		player.updateInventory();
	}

	@Override
	public void click(InventoryClickEvent evt) {
		final int slot = evt.getSlot();
		final Base base = baseSlots.get(slot);
		
		if(base != null) {
			final boolean used = settings.getOwnedBases(player).get(base);
			if(!used) {
				base.build(player, player.getLocation());
				player.closeInventory();
			}
		}
	}

}
