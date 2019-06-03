package org.xmakerx.raidpractice.arena;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.xmakerx.raidpractice.RaidPractice;
import org.xmakerx.raidpractice.arena.Game.GameState;
import org.xmakerx.raidpractice.arena.Spawn.SpawnType;
import org.xmakerx.raidpractice.util.ConfigUtils;
import org.xmakerx.raidpractice.util.InventoryUtils;
import org.xmakerx.raidpractice.util.SchematicManager;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

@SuppressWarnings("deprecation")
public class ArenaManager {
	
	final RaidPractice instance;
	final File configFile;
	final YamlConfiguration config;
	final ConfigurationSection arenasSection;
	boolean configGenerated;
	
	private final HashSet<Arena> arenas;
	private final HashSet<Game> games;
	
	public ArenaManager(final RaidPractice main) {
		this.instance = main;
		this.arenas = new HashSet<Arena>();
		this.games = new HashSet<Game>();
		this.configFile = new File(main.getDataFolder() + "/arenas.yml");
		this.configGenerated = false;
		
		// Let's make sure the arenas directory is created.
		// This is where our schematics will be saved.
		
		File arenasDir = new File(main.getDataFolder() + "/arenas");
		if(!arenasDir.exists()) {
			// Let's create the directory.
			arenasDir.mkdirs();
		}
		
		// Load arenas.yml
		if(!configFile.exists()) {
			try {
				configFile.createNewFile();
				configGenerated = true;
			} catch (IOException e) {
				instance.getLogger().severe("Could not generate arenas.yml. Disabling...");
				instance.disable();
			}
		}
		// Let's load up the arenas file as a config.
		this.config = YamlConfiguration.loadConfiguration(configFile);
		
		// Let's get the arenas section.
		if(config.getConfigurationSection("arenas") == null) {
			// The section doesn't exist, let's create it.
			config.createSection("arenas");
			save();
		}
		this.arenasSection = config.getConfigurationSection("arenas");
		
		if(!configGenerated) {
			// The file already existed, let's load our arenas.
			this.loadArenas();
		}
	}
	
	private void save() {
		try {
			ConfigUtils.save(configFile, config);
		} catch (IOException e) {
			instance.getLogger().severe(String.format("Failed to save arenas.yml. Error: %s.", e.getMessage()));
		}
	}
	
	/**
	 * Replaces spaces with underscores.
	 * @param operation
	 * @return
	 */
	
	public String correctSpaces(final String operation) {
		return operation.replaceAll("\\s","_");
	}
	
	/**
	 * Replaces underscores in a string to spaces.
	 * @param operation
	 * @return
	 */
	
	public String correctUnderscores(final String operation) {
		return operation.replaceAll("\\_", " ");
	}
	
	/**
	 * Check if a name is available for an arena.
	 * @param name
	 * @return true/false flag
	 */
	
	public boolean isNameAvailable(String name) {
		name = correctUnderscores(name);
		for(Arena arena : arenas) {
			if(arena.getName().equalsIgnoreCase(name)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Creates the schematic file for an arena.
	 * @param arena
	 * @return true/false flag.
	 */
	
	public boolean createSchematic(final Arena arena) {
		// Let's create a schematic with the region.
		BlockVector minVect = arena.getRegion().getMinimumPoint();
		BlockVector maxVect = arena.getRegion().getMaximumPoint();
		Location min = new Location(arena.getWorld(), minVect.getX(), minVect.getY(), minVect.getZ());
		Location max = new Location(arena.getWorld(), maxVect.getX(), maxVect.getY(), maxVect.getZ());
		arena.setSchematicName(correctSpaces(arena.getName()));
		try {
			new SchematicManager(instance.getWorldEdit(), arena.getWorld()).saveSchematic(new File(instance.getDataFolder() + String.format("/arenas/%s", correctSpaces(arena.getName()))), min, max);
			return true;
		} catch (DataException | IOException e) {
			instance.getLogger().severe(String.format("Failed to save schematic for Arena %s.", arena.getName()));
			e.printStackTrace();
			return false;
		}
	}
	
	public Spawn createSpawn(final Arena arena, final Location loc, final SpawnType type) {
		final int spawnNum = arena.getSpawns().size() + 1;
		final String spawnName = String.format("Spawn%s", String.format("%1$02d", spawnNum));
		final Spawn spawn = new Spawn(spawnName, loc, type);
		arena.addSpawn(spawn);
		return spawn;
	}
	
	/**
	 * Let's start keeping track of this arena
	 * and let's create egg spawns where dragon eggs are located.
	 * @param arena
	 */
	
	public void createArena(final Arena arena, final Player creator) {
		arenas.add(arena);
		
		final BlockVector minVect = arena.getRegion().getMinimumPoint();
		final BlockVector maxVect = arena.getRegion().getMaximumPoint();
		final Location min = new Location(arena.getWorld(), minVect.getX(), minVect.getY(), minVect.getZ());
		final Location max = new Location(arena.getWorld(), maxVect.getX(), maxVect.getY(), maxVect.getZ());
		int eggSpawnsCreated = 0;
		
		for(int x = min.getBlockX(); x <= max.getBlockX(); x++) {
			for(int y = min.getBlockY(); y <= max.getBlockY(); y++) {
				for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
					final Location blockLoc = new Location(arena.getWorld(), x, y, z);
					if(blockLoc.getBlock().getType() == Material.DRAGON_EGG) {
						// Let's create an egg spawn at this location.
						blockLoc.getBlock().setType(Material.AIR);
						createSpawn(arena, blockLoc, SpawnType.EGG);
						eggSpawnsCreated++;
					}
				}
			}
		}
		
		try {
			// Let's send the creator a message about how many egg spawns they made.
			String message = instance.getLocalizer().getString("createdEggSpawns");
			message = message.replaceAll("\\{spawns\\}", String.valueOf(eggSpawnsCreated));
			creator.sendMessage(instance.getLocalizer().getMessage(ChatColor.translateAlternateColorCodes('&', message)));
		}catch (NullPointerException e) {
			// We didn't pass a creator, no problem.
			// Let's just let the console know that we don't recommend using this method without a creator.
			instance.getLogger().warning("The method createArena() is expected to be used with a valid Player object passed to it. Please avoid passing null.");
		}
		saveArena(arena);
		
		final Game game = new Game(instance, arena);
		games.add(game);
	}
	
	/**
	 * Saves an arena into the arenas.yml file.
	 * @param arena
	 */
	
	public void saveArena(final Arena arena) {
		final String correctedName = correctSpaces(arena.getName());
		ConfigurationSection arenaSection = arenasSection.getConfigurationSection(correctedName);
		boolean isUpdate = true;
		
		if(arenaSection == null) {
			arenaSection = arenasSection.createSection(correctedName);
			isUpdate = false;
		}
		
		arenaSection.set("name", arena.getName());
		arenaSection.set("world", arena.getWorld().getName());
		
		// Let's save the minimum and maximum point.
		final BlockVector minVect = arena.getRegion().getMinimumPoint();
		final BlockVector maxVect = arena.getRegion().getMaximumPoint();
		final Location min = new Location(arena.getWorld(), minVect.getX(), minVect.getY(), minVect.getZ());
		final Location max = new Location(arena.getWorld(), maxVect.getX(), maxVect.getY(), maxVect.getZ());
		arenaSection.set("minPos", String.format("%s,%s,%s", min.getX(), min.getY(), min.getZ()));
		arenaSection.set("maxPos", String.format("%s,%s,%s", max.getX(), max.getY(), max.getZ()));
		
		// Only save the gear if it isn't null.
		if(arena.getGear() != null) {
			final String gear = InventoryUtils.toBase64(arena.getGear());
			arenaSection.set("gear", gear);
		}
		
		final Game game = getGameFromArena(arena);
		
		if(game != null && game.getState() != GameState.IN_GAME || game == null) {
			if(!isUpdate) {
				final boolean createdSchematic = createSchematic(arena);
				if(!createdSchematic) return;
			}else {
				final File schematicFile = new File(instance.getDataFolder() + String.format("/arenas/%s", correctSpaces(arena.getName())));
				if(!schematicFile.exists()) {
					instance.getLogger().info(String.format("Failed to load schematic for Arena %s. Generating a new one...", arena.getName()));
					if(!createSchematic(arena)) return;
				}
			}
		}
		
		arenaSection.set("schematic", arena.getSchematicName());
		
		// Let's save our blacklist.
		if(!isUpdate) arenaSection.createSection("blacklist");
		
		final ConfigurationSection blacklistSection = arenaSection.getConfigurationSection("blacklist");
		final HashSet<ProtectedCuboidRegion> blacklist = arena.getBlacklist();
		
		int i = 1;
		for(final ProtectedCuboidRegion region : blacklist) {
			final String name = String.format("region%s", String.format("%1$02d", i));
			final ConfigurationSection regionSection = blacklistSection.createSection(name);
			final BlockVector minPos = region.getMinimumPoint();
			final BlockVector maxPos = region.getMaximumPoint();
			regionSection.set("min", String.format("%s,%s,%s", minPos.getX(), minPos.getY(), minPos.getZ()));
			regionSection.set("max", String.format("%s,%s,%s", maxPos.getX(), maxPos.getY(), maxPos.getZ()));
			i += 1;
		}
		
		// Let's save our factions claims.
		if(!isUpdate) arenaSection.createSection("factionsClaims");
		
		final ConfigurationSection factionSection = arenaSection.getConfigurationSection("factionsClaims");
		final HashSet<ProtectedCuboidRegion> claims = arena.getClaims();
		int f = 1;
		for(final ProtectedCuboidRegion region : claims) {
			final String name = String.format("region%s", String.format("%1$02d", f));
			final ConfigurationSection regionSection = factionSection.createSection(name);
			final BlockVector minPos = region.getMinimumPoint();
			final BlockVector maxPos = region.getMaximumPoint();
			regionSection.set("min", String.format("%s,%s,%s", minPos.getX(), minPos.getY(), minPos.getZ()));
			regionSection.set("max", String.format("%s,%s,%s", maxPos.getX(), maxPos.getY(), maxPos.getZ()));
			f += 1;
		}
		
		// Let's save our flags.
		if(!isUpdate) arenaSection.createSection("flags");
		
		final ConfigurationSection flagsSection = arenaSection.getConfigurationSection("flags");
		for(ArenaFlag flag : arena.getFlags()) {
			flagsSection.set(flag.getAliases()[0], flag.getValue());
		}
		
		// Let's save our spawn points.
		ConfigurationSection spawnsSection = arenaSection.getConfigurationSection("spawns");
		if(spawnsSection != null) {
			arenaSection.set("spawns", null);
		}
		
		spawnsSection = arenaSection.createSection("spawns");
		
		for(Spawn spawn : arena.getSpawns()) {
			final ConfigurationSection spawnSection = spawnsSection.createSection(spawn.getName());
			spawnSection.set("type", spawn.getType().name());
			spawnSection.set("pos", String.format("%s,%s,%s", 
					spawn.getLocation().getX(), 
					spawn.getLocation().getY(), 
			spawn.getLocation().getZ()));
			spawnSection.set("pitch", spawn.getLocation().getPitch());
			spawnSection.set("yaw", spawn.getLocation().getYaw());
		}
		
		// Let's save our arena signs.
		ConfigurationSection signsSection = arenaSection.getConfigurationSection("signs");
		if(signsSection != null) {
			arenaSection.set("signs", null);
		}
		
		signsSection = arenaSection.createSection("signs");
		
		for(ArenaSign sign : arena.getArenaSigns()) {
			if(sign.getSign().getBlock().getState() instanceof Sign) {
				final Sign block = sign.getSign();
				signsSection.set(sign.getName(), String.format("%s,%s,%s", 
						block.getLocation().getX(), 
						block.getLocation().getY(), 
				block.getLocation().getZ()));
			}
		}
		
		save();
		
		if(game != null) game.updateSigns();
	}
	
	private void loadArenas() {
		
		// Work-around for a bug.
		if(instance.getArenaManager() == null) {
			instance.setArenaManager(this);
		}
		
		for(String arenaName : arenasSection.getKeys(false)) {
			final ConfigurationSection arenaSection = arenasSection.getConfigurationSection(arenaName);
			final String name = arenaSection.getString("name");
			final String worldName = arenaSection.getString("world");
			final String schemName = arenaSection.getString("schematic");
			final String base64gear = arenaSection.getString("gear");
			Inventory gear = null;
			
			// Let's attempt to load the gear.
			try {
				gear = InventoryUtils.fromBase64(base64gear);
			} catch (IOException | NullPointerException e) {
				instance.getLogger().severe(String.format("Failed to load gear for Arena %s.", name));
			}
			
			// Let's make sure the world exists.
			final World world = Bukkit.getWorld(worldName);
			if(world == null) {
				instance.getLogger().severe(String.format("World %s does not exist. Could not populate Arena %s.", worldName, name));
				continue;
			}
			
			// We need to load up the minimum and maximum points.
			final ArrayList<Double> minList = getPos(arenaSection.getString("minPos"));
			final ArrayList<Double> maxList = getPos(arenaSection.getString("maxPos"));
			final Vector min = new Vector(minList.get(0), minList.get(1), minList.get(2));
			final Vector max = new Vector(maxList.get(0), maxList.get(1), maxList.get(2));
			
			// Let's create the region again.
			final ProtectedCuboidRegion region = new ProtectedCuboidRegion(String.format("Arena_%s", correctSpaces(arenaName)),
					min.toBlockVector(), max.toBlockVector());
			WGBukkit.getRegionManager(world).addRegion(region);
			
			// Let's load up the arena spawns.
			final ConfigurationSection spawnsSection = arenaSection.getConfigurationSection("spawns");
			final HashSet<Spawn> spawns = new HashSet<Spawn>();
			
			for(String spawnName : spawnsSection.getKeys(false)) {
				// spawnName is the name of the spawn.
				final ConfigurationSection spawnSection = spawnsSection.getConfigurationSection(spawnName);
				final SpawnType spawnType = SpawnType.valueOf(spawnSection.getString("type"));
				
				// Let's get the location data.
				final ArrayList<Double> pos = getPos(spawnSection.getString("pos"));
				float sYaw = (float) spawnSection.getDouble("yaw");
				float sPitch = (float) spawnSection.getDouble("pitch");
				
				// Let's create the location object and the Spawn object.
				final Location sLoc = new Location(world, pos.get(0), pos.get(1), pos.get(2), sYaw, sPitch);
				final Spawn spawnPoint = new Spawn(spawnName, sLoc, spawnType);
				spawns.add(spawnPoint);
			}
			
			// We were able to load up everything, let's recreate the Arena object.
			final Arena arena = new Arena(instance, name, world, region, gear);
			arena.setSchematicName(schemName);
			
			// Add all the spawns back.
			for(Spawn spawn : spawns) {
				arena.addSpawn(spawn);
			}
			
			// Let's load up our flags.
			final ConfigurationSection flags = arenaSection.getConfigurationSection("flags");
			for(String flagName : flags.getKeys(false)) {
				final ArenaFlag flag = arena.getFlag(flagName);
				if(flag != null) {
					flag.setValue(flags.get(flagName));
				}else {
					flags.set(flagName, null);
				}
			}
			
			arenas.add(arena);
			
			// Let's load up our blacklist.
			final ConfigurationSection blacklistSection = arenaSection.getConfigurationSection("blacklist");
			int i = 1;
			for(String regionName : blacklistSection.getKeys(false)) {
				final ConfigurationSection regionSection = blacklistSection.getConfigurationSection(regionName);
				final ArrayList<Double> regionMin = getPos(regionSection.getString("min"));
				final ArrayList<Double> regionMax = getPos(regionSection.getString("max"));
				final String pcrName = String.format("Arena_%s_Blacklist_%s", name, i);
				final Vector minVect = new Vector(regionMin.get(0), regionMin.get(1), regionMin.get(2));
				final Vector maxVect = new Vector(regionMax.get(0), regionMax.get(1), regionMax.get(2));
				final ProtectedCuboidRegion blRegion = new ProtectedCuboidRegion(pcrName,
						minVect.toBlockVector(),
						maxVect.toBlockVector());
				WGBukkit.getRegionManager(world).addRegion(blRegion);
				arena.blacklistRegion(blRegion);
				i += 1;
			}
			
			// Let's load up our claims.
			final ConfigurationSection factionsSection = arenaSection.getConfigurationSection("factionsClaims");
			int f = 1;
			for(String regionName : factionsSection.getKeys(false)) {
				final ConfigurationSection regionSection = factionsSection.getConfigurationSection(regionName);
				final ArrayList<Double> regionMin = getPos(regionSection.getString("min"));
				final ArrayList<Double> regionMax = getPos(regionSection.getString("max"));
				final String pcrName = String.format("Arena_%s_Claim_%s", name, f);
				final Vector minVect = new Vector(regionMin.get(0), regionMin.get(1), regionMin.get(2));
				final Vector maxVect = new Vector(regionMax.get(0), regionMax.get(1), regionMax.get(2));
				final ProtectedCuboidRegion blRegion = new ProtectedCuboidRegion(pcrName,
						minVect.toBlockVector(),
						maxVect.toBlockVector());
				WGBukkit.getRegionManager(world).addRegion(blRegion);
				arena.claimRegion(blRegion);
				f += 1;
			}
			
			// Let's create the game object.
			final Game game = new Game(instance, arena);
			games.add(game);
			
			// We have to load up our signs after we create the Arena object.
			final ConfigurationSection signsSection = arenaSection.getConfigurationSection("signs");
			
			for(String signName : signsSection.getKeys(false)) {
				
				// Let's get the location data.
				final ArrayList<Double> signPos = getPos(signsSection.getString(signName));
				final Location loc = new Location(world, signPos.get(0), signPos.get(1), signPos.get(2));
				
				if(loc.getBlock().getState() instanceof Sign) {
					final ArenaSign sign = new ArenaSign(signName, instance, arena, (Sign) loc.getBlock().getState());
					arena.addArenaSign(sign);
				}else {
					System.out.println("NOT A SIGN");
				}
			}
		}
	}
	
	public ArrayList<Double> getPos(String posStr) {
		final String x = posStr.substring(0, posStr.indexOf(","));
		posStr = posStr.substring(x.length() + 1, posStr.length() - 1);
		final String y = posStr.substring(0, posStr.indexOf(","));
		posStr = posStr.substring(y.length() + 1, posStr.length() - 1);
		final String z = posStr.substring(0, posStr.length());
		return new ArrayList<Double>(Arrays.asList(Double.valueOf(x), Double.valueOf(y), Double.valueOf(z)));
	}
	
	public void addArena(final Arena arena) {
		this.arenas.add(arena);
	}
	
	public void delArena(final Arena arena) {
		final Game game = getGameFromArena(arena);
		if(game != null) {
			game.reset();
			games.remove(game);
		}
		arenasSection.set(correctSpaces(arena.getName()), null);
		new File(instance.getDataFolder() + String.format("/arenas/%s", correctSpaces(arena.getName()))).delete();
		
		for(final ArenaSign arenaSign : arena.getArenaSigns()) {
			arenaSign.getSign().getBlock().setType(Material.AIR);
		}
		
		final ProtectedCuboidRegion region = arena.getRegion();
		int minX = region.getMinimumPoint().getBlockX();
		int minY = region.getMinimumPoint().getBlockY();
		int minZ = region.getMinimumPoint().getBlockZ();
		int maxX = region.getMaximumPoint().getBlockX();
		int maxY = region.getMaximumPoint().getBlockY();
		int maxZ = region.getMaximumPoint().getBlockZ();
		
		for(int x = minX; x <= maxX; x++) {
			for(int y = minY; y <= maxY; y++) {
				for(int z = minZ; z <= maxZ; z++) {
					final Block block = new Location(arena.getWorld(), x, y, z).getBlock();
					block.setType(Material.AIR);
				}
			}
		}
		this.arenas.remove(arena);
	}
	
	/**
	 * Attempts to get the game a player is in.
	 * @param player
	 * @return a Game instance or null.
	*/
	
	public Game getGameFromPlayer(final Player player) {
		for(Game game : games) {
			if(game.isPlaying(player)) {
				return game;
			}
		}
		return null;
	}
	
	public Game getGameFromArena(final Arena arena) {
		for(Game game : games) {
			if(game.getArena().equals(arena)) {
				return game;
			}
		}
		return null;
	}
	
	public Arena getArenaByName(String name) {
		name = correctUnderscores(name);
		for(Arena arena : arenas) {
			if(arena.getName().equalsIgnoreCase(name)) {
				return arena;
			}
		}
		return null;
	}
	
	public boolean isBlacklisted(final Location loc) {
		for(Arena arena : getArenas()) {
			final HashSet<ProtectedCuboidRegion> regions = arena.getBlacklist();
			for(final ProtectedCuboidRegion region : regions) {
				if(region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isClaim(final Location loc) {
		for(Arena arena : getArenas()) {
			final HashSet<ProtectedCuboidRegion> regions = arena.getClaims();
			for(final ProtectedCuboidRegion region : regions) {
				if(region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public HashSet<Game> getGames() {
		return this.games;
	}
	
	public HashSet<Arena> getArenas() {
		return this.arenas;
	}
}
