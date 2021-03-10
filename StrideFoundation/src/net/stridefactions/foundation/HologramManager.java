package net.stridefactions.foundation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.stridefactions.foundation.gui.Menu;

import com.gmail.filoghost.holographicdisplays.api.Hologram;

public class HologramManager {
	
	private static HashSet<Hologram> holograms = new HashSet<Hologram>();
	
	public static void updateHolograms() {
		for(final Hologram hologram : holograms) {
			hologram.clearLines();
			final List<String> lore = new ArrayList<String>(StrideFoundation.getInstance().getSettings().getHologramLines());
			for(String line : lore) {
				line = line.replaceAll("\\{progress\\}", MoneyPot.getProgressString());
				line = line.replaceAll("\\{progressBar\\}", MoneyPot.getProgressBar());
				line = line.replaceAll("\\{goal\\}", String.valueOf(MoneyPot.getMaxBalance()));
				hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', line));
			}
		}
		
		for(final UUID uuid : StrideFoundation.getInstance().getMenuManager().getMenus().keySet()) {
			final Menu menu = StrideFoundation.getInstance().getMenuManager().getMenus().get(uuid);
			menu.show();
		}
	}
	
	/**
	 * Register a hologram to the manager.
	 * @param Hologram hologram
	 */
	
	public static void registerHologram(final Hologram hologram) {
		holograms.add(hologram);
	}
	
	/**
	 * Remove a hologram from the manager.
	 * @param Hologram hologram
	 */
	
	public static void removeHologram(final Hologram hologram) {
		hologram.clearLines();
		hologram.delete();
		holograms.remove(hologram);
	}
	
	/**
	 * Clear and get rid of all the created holograms.
	 */
	
	public static void clear() {
		for(final Hologram hologram : holograms) {
			hologram.clearLines();
			hologram.delete();
		}
		
		holograms.clear();
	}
	
	public static HashSet<Hologram> getHolograms() {
		return HologramManager.holograms;
	}
}
