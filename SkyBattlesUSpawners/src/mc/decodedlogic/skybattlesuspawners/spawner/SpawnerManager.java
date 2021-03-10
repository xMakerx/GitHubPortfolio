package mc.decodedlogic.skybattlesuspawners.spawner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import mc.decodedlogic.skybattlesuspawners.USpawners;
import mc.decodedlogic.skybattlesuspawners.USpawnersLogger;
import mc.decodedlogic.skybattlesuspawners.event.MobSpawnerRegisterEvent;
import mc.decodedlogic.skybattlesuspawners.event.MobSpawnerUnregisterEvent;

public class SpawnerManager {
	
	// Map containing spawner id -> MobSpawner instance pairs.
	private static final Map<Integer, MobSpawner> SPAWNERS;
	
	private static File storageDir;
	
	private static USpawnersLogger notify;
	
	static {
		SPAWNERS = new HashMap<Integer, MobSpawner>();
		storageDir = null;
		notify = new USpawnersLogger("SpawnerManager");
	}
	
	/**
	 * Allocates and returns a new id between 1-999999.
	 * @return
	 */
	
	private static int allocateId() {
		int id = -1;
		
		do {
			id = ThreadLocalRandom.current().nextInt(1, 10000000);
		} while (SPAWNERS.keySet().contains(id));
		
		return id;
	}
	
	public static void storeSpawnerData(final MobSpawner spawner) {
		if(SPAWNERS.keySet().contains(spawner.getId())) {
			verifyStorageDirectoryExists();
			final File spawnerFile = new File(storageDir.getAbsolutePath() + "/" + getSpawnerFilename(spawner));
			
			if(!spawnerFile.exists()) {
				try {
					spawnerFile.createNewFile();
				} catch (IOException e) {
					notify.error(String.format("An error occurred while creating MobSpawner data file. Error: %s", e.getMessage()));
					e.printStackTrace();
					return;
				}
			}
			
			final YamlConfiguration config = YamlConfiguration.loadConfiguration(spawnerFile);
			int upgradeIndex = -1;
			
			if(spawner.getUpgrade() != null) {
				upgradeIndex = spawner.getType().getUpgrades().indexOf(spawner.getUpgrade());
			}
			
			config.set("id", spawner.getId());
			config.set("upgrade", upgradeIndex);
			config.set("quantity", spawner.getSize());
			
			final ConfigurationSection locationSection = config.createSection("location");
			final Location location = spawner.getLocation();
			locationSection.set("world", location.getWorld().getName());
			locationSection.set("x", location.getBlockX());
			locationSection.set("y", location.getBlockY());
			locationSection.set("z", location.getBlockZ());
			
			try {
				config.save(spawnerFile);
			} catch (IOException e) {
				notify.error(String.format("An error occurred while saving MobSpawner. Error: %s", e.getMessage()));
				e.printStackTrace();
			}
		}
	}
	
	public static void loadStoredSpawners() {
		verifyStorageDirectoryExists();
		try {
			List<File> filesInFolder = Files.walk(Paths.get(storageDir.getAbsolutePath()))
			        .filter(Files::isRegularFile)
			        .map(Path::toFile)
			        .collect(Collectors.toList());
			
			List<File> deleteFiles = new ArrayList<File>();
			
			for(File spawnerFile : filesInFolder) {
				final YamlConfiguration config = YamlConfiguration.loadConfiguration(spawnerFile);
				int spawnerId = config.getInt("id");
				int upgradeIndex = config.getInt("upgrade");
				int quantity = config.getInt("quantity");
				
				final ConfigurationSection locationSection = config.getConfigurationSection("location");
				String worldName = locationSection.getString("world");
				int blockX = locationSection.getInt("x");
				int blockY = locationSection.getInt("y");
				int blockZ = locationSection.getInt("z");
				
				if(Bukkit.getWorld(worldName) != null) {
					Location location = new Location(Bukkit.getWorld(worldName), blockX, blockY, blockZ);
					Block b = (location != null) ? location.getBlock() : null;
					
					if(quantity > 0 && (b != null && b.getState() instanceof CreatureSpawner)) {
					    new MobSpawner(location, spawnerId, upgradeIndex, quantity);
					    
					    /* Handled in log loader
					    LocationLog log = LocationLog.get(location);
					    SpawnerLog sLog = log.getLatestSpawnerLog();
					    if(sLog == null) {
					        log.handleOldSpawner();
					    }*/
					}else {
						deleteFiles.add(spawnerFile);
					}
				}
			}
			
			for(File deleteFile : deleteFiles) {
				deleteSpawnerFile(deleteFile);
			}

		} catch (IOException e) {
			notify.error(String.format("An error occurred while deleting MobSpawner data. Error: %s", e.getMessage()));
			e.printStackTrace();
		}
	}
	
	public static void deleteSpawnerFile(final File spawnerFile) {
		if(!spawnerFile.exists()) return;
		
		spawnerFile.delete();
	}
	
	private static void verifyStorageDirectoryExists() {
		final File storageDir = new File(USpawners.get().getDataFolder() + "/spawners");
		SpawnerManager.storageDir = storageDir;
		
		if(!storageDir.exists()) {
			storageDir.mkdirs();
		}
	}
	
	private static String getSpawnerFilename(final MobSpawner spawner) {
		return String.format("spawner-%d.yml", spawner.getId());
	}
	
	public static File getSpawnerFile(final MobSpawner spawner) {
		return new File(storageDir.getAbsolutePath() + "/" + getSpawnerFilename(spawner));
	}
	
	/**
	 * Registers a new {@link MobSpawner}. This is meant for brand-new spawners
	 * that we don't have any data on.
	 * @param spawner
	 * @return Returns an allocated id (see: {@link #allocateId()}) or -1 if registration failed.
	 */
	
	public static int registerNew(final MobSpawner spawner) {
		int spawnerId = -1;
		if(!SPAWNERS.values().contains(spawner)) {
			spawnerId = allocateId();
			SPAWNERS.put(spawnerId, spawner);
			USpawners.get().callEvent(new MobSpawnerRegisterEvent(spawner));
		}
		
		return spawnerId;
	}
	
	/**
	 * Registers a {@link MobSpawner} so we can start keeping track of it again.
	 * This is meant for spawners that are being loaded from a file.
	 * @param spawner
	 * @return Whether or not the mob spawner was registered.
	 */
	
	public static boolean register(final MobSpawner spawner) {
		boolean registered = !SPAWNERS.containsKey(spawner.getId());
		
		if(registered) {
			SPAWNERS.put(spawner.getId(), spawner);
			USpawners.get().callEvent(new MobSpawnerRegisterEvent(spawner));
		}
		
		return registered;
	}
	
	public static boolean unregister(final MobSpawner spawner) {
		if(spawner == null) return false;
		boolean unregistered = SPAWNERS.values().contains(spawner);
		
		SPAWNERS.remove(spawner.getId());
		
		MobSpawnerUnregisterEvent unregisterEvt = new MobSpawnerUnregisterEvent(spawner);
		
		USpawners.get().callEvent(unregisterEvt);
		unregistered = !unregisterEvt.isCancelled();
		
		return unregistered;
	}
	
	public static void cleanup() {
		for(MobSpawner spawner : SPAWNERS.values()) {
			storeSpawnerData(spawner);
			spawner.cleanup();
		}
		
		SPAWNERS.clear();
	}
	
	public static MobSpawner getSpawner(int spawnerId) {
		return SPAWNERS.get(spawnerId);
	}
	
	public static Collection<MobSpawner> getMobSpawners() {
		return SPAWNERS.values();
	}
	
}
