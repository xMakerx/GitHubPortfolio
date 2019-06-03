package org.xmakerx.raidpractice.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xmakerx.raidpractice.Database;
import org.xmakerx.raidpractice.RaidPractice;
import org.xmakerx.raidpractice.arena.Game;
import org.xmakerx.raidpractice.arena.GamePlayer;
import org.xmakerx.raidpractice.shop.Perk;
import org.xmakerx.raidpractice.shop.ShopData;
import org.xmakerx.raidpractice.shop.ShopItem;

public class ShopMenu extends Menu {
	
	final RaidPractice instance;
	final ShopData shopData;
	final Game game;
	final GamePlayer gPlayer;
	
	protected int itemSlot;
	protected final HashMap<Integer, Perk> perks;
	protected boolean opened;
	
	public ShopMenu(final RaidPractice main, final Player player) {
		super(player, Bukkit.createInventory(null, 54, main.getLocalizer().getColoredString("perkShop")));
		this.instance = main;
		this.shopData = instance.getShopData();
		this.itemSlot = 11;
		this.perks = new HashMap<Integer, Perk>();
		this.game = instance.getArenaManager().getGameFromPlayer(player);
		this.gPlayer = game.getGamePlayerFromPlayer(player);
		this.opened = false;
	}
	
	public void buildItem(final ShopItem itemData) {
		final ItemStack item = new ItemStack(itemData.getIcon(), 1);
		if(itemData.getMetadata().containsKey("data")) {
			item.setDurability(Short.valueOf(String.valueOf(itemData.getMetadata().get("data"))));
		}
		final ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemData.getName()));
		
		if(itemData.getDescription().size() > 0) {
			final ArrayList<String> lore = new ArrayList<String>();
			for(String line : itemData.getDescription()) {
				line = line.replaceAll("\\{keys\\}", String.valueOf(instance.getStatsDatabase().getKeys(viewer)));
				lore.add(ChatColor.translateAlternateColorCodes('&', line));
			}
			
			if(itemData.getCost() > 0 && !gPlayer.getPerks().contains(itemData)) {
				String costLine = instance.getLocalizer().getString("cost");
				costLine = costLine.replaceAll("\\{keys\\}", String.valueOf(itemData.getCost()));
				costLine = ChatColor.translateAlternateColorCodes('&', costLine);
				lore.add(costLine);
			}else if(itemData.getCost() > 0 && gPlayer.getPerks().contains(itemData)) {
				lore.add(instance.getLocalizer().getColoredString("alreadyPurchased"));
			}
			
			meta.setLore(lore);
		}
		
		item.setItemMeta(meta);
		
		if(itemData.getCodeName().equalsIgnoreCase("balance")) {
			this.ui.setItem(49, item);
		}else {
			this.ui.setItem(itemSlot, item);
			itemSlot += 1;
			if(new ArrayList<Integer>(Arrays.asList(16, 25, 34)).contains((itemSlot))) {
				itemSlot += 4;
			}
		}
	}
	
	public void show() {
		this.ui.clear();
		this.perks.clear();
		
		final HashMap<String, ShopItem> items = shopData.getItems();
		
		itemSlot = 11;
		
		int perks = 0;
		
		for(Map.Entry<String, ShopItem> entry : items.entrySet()) {
			final ShopItem itemData = entry.getValue();
			
			if(itemData.getCodeName().equalsIgnoreCase("emptySlot")) continue;
			
			if(itemData.isEnabled()) {
				if(itemData instanceof Perk) {
					perks += 1;
					this.perks.put(itemSlot, (Perk)itemData);
				}
				buildItem(itemData);
			}
		}
		
		for(int i = 0; i < (15 - perks); i++) {
			buildItem(items.get("emptySlot"));
		}
		
		if(!opened) {
			viewer.openInventory(this.ui);
			opened = true;
		}
		
		this.viewer.updateInventory();
	}

	@Override
	public void clickPerformed(InventoryClickEvent evt) {
		evt.setCancelled(true);
		final int slot = evt.getSlot();
		final Player player = (Player) evt.getWhoClicked();
		final Game game = instance.getArenaManager().getGameFromPlayer(player);
		
		if(game != null) {
			final GamePlayer gPlayer = game.getGamePlayerFromPlayer(player);
		
			if(perks.containsKey(slot)) {
				final Perk perk = perks.get(slot);
				
				final Database db = instance.getStatsDatabase();
				
				if(db.getKeys(player) >= perk.getCost()) {
					db.setKeys(player, db.getKeys(player) - perk.getCost());
					gPlayer.addPerk(perk);
					show();
				}
			}
		}
	}

	@Override
	public void closed() {
		// TODO Auto-generated method stub
		
	}
}
