package net.stridefactions.foundation.gui;

import net.stridefactions.foundation.MoneyPot;
import net.stridefactions.foundation.StrideItem;
import net.stridefactions.foundation.StrideSound;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class MoneyPotMenu extends Menu {

	public MoneyPotMenu(Player player) {
		super(player);
		this.inv = Bukkit.createInventory(null, 27, settings.getMessage("menuTitle"));
	}
	
	public void show() {
		for(int i = 0; i < inv.getSize(); i++) {
			this.inv.setItem(i, null);
		}
		
		// Let's create the inventory.
		this.inv.setItem(4, StrideItem.PROGRESS.buildItem());
		
		final ItemStack add_1 = StrideItem.ADD_1.buildItem();
		final ItemStack add_5 = StrideItem.ADD_5.buildItem();
		final ItemStack add_25 = StrideItem.ADD_25.buildItem();
		
		this.inv.setItem(18, add_1.clone());
		this.inv.setItem(19, add_5.clone());
		this.inv.setItem(20, add_25.clone());
		this.inv.setItem(22, StrideItem.ADD_1000.buildItem());
		this.inv.setItem(24, add_25.clone());
		this.inv.setItem(25, add_5.clone());
		this.inv.setItem(26, add_1.clone());
		
		if(!opened) {
			this.player.openInventory(this.inv);
			this.player.getWorld().playSound(player.getLocation(), StrideSound.GUI_OPEN.getSound(), 1F, 1F);
			this.opened = true;
		}
		
		new BukkitRunnable() {
			
			public void run() {
				player.updateInventory();
			}
			
		}.runTaskLater(instance, 1L);
	}
	
	public void handleDonation(int amount) {
		final Sound donation = StrideSound.MONEY_SENT.getSound();
		if(instance.getEconomy().getBalance(player) >= amount) {
			instance.getEconomy().withdrawPlayer(player, amount);
			MoneyPot.setBalance(MoneyPot.getBalance() + amount, false);
			
			// Let's create the message.
			String msg = settings.getString("moneySent");
			msg = msg.replaceAll("\\{payment\\}", String.valueOf(amount));
			settings.sendMessage(player, msg);
			
			if(donation != null) {
				player.getWorld().playSound(player.getLocation(), donation, 1F, 1F);
			}
			
			show();
		}else {
			settings.sendMessage(player, settings.getString("cantAfford"));
		}
	}
	
	public void click(final InventoryClickEvent evt) {
		final int slot = evt.getSlot();
		
		if(slot == 18 || slot == 26) {
			// Add 1
			handleDonation(1);
		}else if(slot == 19 || slot == 25) {
			// Add 5
			handleDonation(5);
		}else if(slot == 20 || slot == 24) {
			// Add 25
			handleDonation(25);
		}else if(slot == 22) {
			// Add 1000
			handleDonation(1000);
		}
	}
	
}
