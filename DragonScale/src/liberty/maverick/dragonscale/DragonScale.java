package liberty.maverick.dragonscale;

import java.lang.reflect.Field;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import liberty.maverick.dragonscale.DragonScaleLogger.LogLevel;
import liberty.maverick.dragonscale.event.MineEventListener;
import liberty.maverick.dragonscale.event.PickaxeEventListener;
import liberty.maverick.dragonscale.event.PlayerEventListener;
import liberty.maverick.dragonscale.lootbox.LootBoxFactory;
import liberty.maverick.dragonscale.util.DragonUtils;
import liberty.maverick.dragonscale.util.GlowEnchantment;
import net.md_5.bungee.api.ChatColor;

public class DragonScale extends JavaPlugin {
	
	private DragonScaleLogger logger;
	
	// WorldGuard instance. Useful for managing protections.
	private WorldGuardPlugin worldGuard = null;
	
	// The LootBoxFactory instance for loot box stuff!
	private LootBoxFactory lootBoxFactory = null;
	
	private DragonScaleSettings settings = null;
	
	private DragonScaleDatabase database = null;
	
	// This is the default log level of the plugin.
	// This can be changed through the config file.
	private LogLevel logLevel = LogLevel.INFO;
	
	public static DragonScale singleton;
	
    public void onEnable() {
		// Let's instantiate our logger instance.
		logger = new DragonScaleLogger(this, "Core");
		
		// Let's try to hook into WorldGuard.
		Plugin wgPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
		
		if(wgPlugin != null && wgPlugin instanceof WorldGuardPlugin) {
			worldGuard = (WorldGuardPlugin) wgPlugin;
			logger.info(ChatColor.GREEN + String.format("Successfully hooked into WorldGuard [%s]!", 
					worldGuard.getDescription().getVersion()));
		}else {
			logger.info(ChatColor.YELLOW + "WorldGuard could not be found!!! "
					+ "Running without verifying build/destroy permissions.");
		}
		
		// Let's setup our utility class.
		DragonUtils.setup(this, worldGuard);
		
		lootBoxFactory = new LootBoxFactory(this);
		settings = new DragonScaleSettings(this);
		database = new DragonScaleDatabase(this);
		
		DragonScale.singleton = this;
		this.registerEvents();
		
		// Let's try to load the data of players already online.
		for(Player player : getServer().getOnlinePlayers()) {
			database.loadPlayerData(player.getUniqueId().toString());
		}
		
		// Let's setup our commands.
		getCommand("pickaxe").setExecutor(new Commands(this));
		
        try {
            final Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            
            try {
             // Let's register our glow enchantment.
                Enchantment.registerEnchantment(new GlowEnchantment());
            } catch (IllegalArgumentException e) {}
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void onDisable() {
		if(database != null) {
			database.saveAllDataAndClear();
		}
	}
	
	private void registerEvents() {
		getServer().getPluginManager().registerEvents(new MineEventListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
		getServer().getPluginManager().registerEvents(new PickaxeEventListener(this), this);
	}
	
	public void disable() {
		if(isEnabled()) {
			logger.info(ChatColor.YELLOW + "Disabling plugin...");
			setEnabled(false);
		}
	}
	
	/**
	 * Fetches the {@link WorldGuardPlugin} instance if one is running on the server.
	 * @return WorldGuardPlugin instance or null.
	 */
	
	public WorldGuardPlugin getWorldGuard() {
		return this.worldGuard;
	}
	
	/**
	 * Fetches the {@link LootBoxFactory} instance for generating loot boxes.
	 * @return LootBoxFactory instance or null.
	 */
	
	public LootBoxFactory getLootBoxFactory() {
		return this.lootBoxFactory;
	}
	
	/**
	 * Fetches the {@link DragonScaleSettings} instance.
	 * @return DragonScaleSettings instance or null.
	 */
	
	public DragonScaleSettings getSettings() {
		return this.settings;
	}
	
	/**
	 * Fetches the {@link DragonScaleDatabase} instance.
	 * @return DragonScaleDatabase instance or null.
	 */
	
	public DragonScaleDatabase getSystemDatabase() {
		return this.database;
	}
	
	/**
	 * Fetches the {@link DragonScaleLogger} instance associated with this plugin.
	 * @return DragonScaleLogger instance or null.
	 */
	
	public DragonScaleLogger getSystemLogger() {
		return this.logger;
	}
	
	/**
	 * Fetches the {@link LogLevel} the plugin is set at.
	 * @return Default return is {@link LogLevel.INFO}.
	 */
	
	public LogLevel getLogLevel() {
		return this.logLevel;
	}
	
}
