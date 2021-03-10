package net.stridefactions.foundation;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public class Data {
	
	final StrideFoundation instance;
	final File dataFile;
	final YamlConfiguration config;
	final ConfigurationSection dataSection;
	
	private Location effectBlock;
	
	public Data() {
		this.instance = StrideFoundation.getInstance();
		this.dataFile = new File(instance.getDataFolder() + "/data.yml");
		
		boolean needsSetup = !dataFile.exists();
		
		if(!dataFile.exists()) {
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
				instance.getLogger().severe(String.format("Failed to create data.yml. Error: %s.", e.getMessage()));
			}
		}
		
		this.config = YamlConfiguration.loadConfiguration(dataFile);
		this.dataSection = (needsSetup) ? config.createSection("data") : config.getConfigurationSection("data");
		
		if(needsSetup) {
			dataSection.createSection("holograms");
			dataSection.createSection("effectBlock");
			dataSection.set("moneyPot", 0.0D);
			save();
		}
	}
	
	public void saveData() {
		for(final String key : dataSection.getKeys(false)) {
			dataSection.set(key, null);
		}
		
		if(MoneyPot.getBalance() >= MoneyPot.getMaxBalance()) {
			MoneyPot.setBalance(0, true);
		}
		
		dataSection.set("moneyPot", MoneyPot.getBalance());
		
		final ConfigurationSection holograms = dataSection.createSection("holograms");
		int i = 1;
		
		for(final Hologram hologram : HologramManager.getHolograms()) {
			final ConfigurationSection hologramSection = holograms.createSection(String.format("hologram_%d", i));
			hologramSection.set("world", hologram.getWorld().getName());
			hologramSection.set("x", hologram.getLocation().getX());
			hologramSection.set("y", hologram.getLocation().getY());
			hologramSection.set("z", hologram.getLocation().getZ());
			i += 1;
		}
		
		if(effectBlock != null) {
			final ConfigurationSection block = dataSection.createSection("effectBlock");
			block.set("world", effectBlock.getWorld().getName());
			block.set("x", effectBlock.getBlockX());
			block.set("y", effectBlock.getBlockY());
			block.set("z", effectBlock.getBlockZ());
		}
		
		save();
	}
	
	public void save() {
		try {
			config.save(dataFile);
		} catch (IOException e) {
			instance.getLogger().severe(String.format("Failed to save data.yml."));
		}
	}
	
	public void loadData() {
		final ConfigurationSection holograms = dataSection.getConfigurationSection("holograms");
		for(final String hologramKey : holograms.getKeys(false)) {
			final ConfigurationSection hologram = holograms.getConfigurationSection(hologramKey);
			final String worldName = hologram.getString("world");
			final World world;
			
			try {
				world = instance.getServer().getWorld(worldName);
			}catch (NullPointerException e) {
				instance.getLogger().severe(String.format("Could not load hologram inside World %s.", worldName));
				continue;
			}
			
			final double x, y, z;
			x = (double) hologram.get("x");
			y = (double) hologram.get("y");
			z = (double) hologram.get("z");
			
			HologramManager.registerHologram(HologramsAPI.createHologram(instance, new Location(world, x, y, z)));
		}
		
		if(dataSection.getConfigurationSection("effectBlock") != null) {
			final ConfigurationSection block = dataSection.getConfigurationSection("effectBlock");
			final String worldName = block.getString("world");
			final int x = block.getInt("x"), y = block.getInt("y"), z = block.getInt("z");
			final World world;
			
			try {
				world = instance.getServer().getWorld(worldName);
			}catch (NullPointerException e) {
				instance.getLogger().severe(String.format("Could not load effect block inside World %s.", worldName));
				return;
			}
		
			this.effectBlock = new Location(world, x, y, z);
		}
		
		final double balance = dataSection.getDouble("moneyPot");
		MoneyPot.setBalance(balance, true);
	}
	
	public void setEffectBlock(final Location loc) {
		this.effectBlock = loc;
	}
	
	public Location getEffectBlock() {
		return this.effectBlock;
	}
}
