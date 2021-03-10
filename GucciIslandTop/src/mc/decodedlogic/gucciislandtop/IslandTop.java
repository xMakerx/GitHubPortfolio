package mc.decodedlogic.gucciislandtop;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.wasteofplastic.askyblock.ASkyBlock;

import mc.decodedlogic.gucciislandtop.IslandTopLogger.LogLevel;
import mc.decodedlogic.gucciislandtop.event.MenuEvents;
import mc.decodedlogic.gucciislandtop.menu.MenuManager;
import mc.decodedlogic.gucciislandtop.valuable.ValuableManager;
import mc.decodedlogic.skybattlesuspawners.USpawners;
import net.citizensnpcs.Citizens;
import net.md_5.bungee.api.ChatColor;

public class IslandTop extends JavaPlugin {

    private static IslandTop instance;

    private IslandTopLogger logger;
    private LogLevel logLevel;

    private USpawners uspawners;
    private ASkyBlock askyblock;
    private Citizens citizens;
    private Settings settings;
    private BukkitTask calcTask;

    static {
        instance = null;
    }

    @SuppressWarnings("deprecation")
    public void onEnable() {
        IslandTop.instance = this;
        logLevel = LogLevel.INFO;

        this.logger = new IslandTopLogger("IslandTop");

        final Plugin spawnersPlugin = getServer().getPluginManager().getPlugin("GucciUSpawners");
        if(spawnersPlugin != null && spawnersPlugin instanceof USpawners) {
            uspawners = ((USpawners) spawnersPlugin);
            logger.info(ChatColor.GREEN + String.format("Successfully hooked into %s [%s]!", 
                    uspawners.getDescription().getName(), 
                    uspawners.getDescription().getVersion()));
        }else {
            logger.error(ChatColor.RED + "Failed to hook into SkyBattlesUSpawners. Disabling...");
            setEnabled(false);
            return;
        }

        final Plugin skyblockPlugin = getServer().getPluginManager().getPlugin("ASkyBlock");
        if(skyblockPlugin != null && skyblockPlugin instanceof ASkyBlock) {
            askyblock = ((ASkyBlock) askyblock);
            logger.info(ChatColor.GREEN + String.format("Successfully hooked into %s [%s]!", 
                    skyblockPlugin.getDescription().getName(), 
                    skyblockPlugin.getDescription().getVersion()));
        }else {
            logger.error(ChatColor.RED + "Failed to hook into ASkyblock. Disabling...");
            setEnabled(false);
            return;
        }

        final Plugin citizensPlugin = getServer().getPluginManager().getPlugin("Citizens");
        if(citizensPlugin != null && citizensPlugin instanceof Citizens) {
            citizens = (Citizens) citizensPlugin;
            logger.info(ChatColor.GREEN + String.format("Successfully hooked into %s [%s]!", 
                    citizensPlugin.getDescription().getName(), 
                    citizensPlugin.getDescription().getVersion()));
        }else {
            logger.error(ChatColor.RED + "Failed to hook into Citizens. Disabling...");
            setEnabled(false);
            return;
        }



        this.settings = new Settings(this);

        // Let's setup our calculation task.
        calcTask = getServer().getScheduler().runTaskTimerAsynchronously(this, new BukkitRunnable() {

            public void run() {
                Utils.createTopIslands();
            }

        }, 200L, settings.getCalculationCooldown() * 20L);

        // Let's enable our events.
        getServer().getPluginManager().registerEvents(new MenuEvents(), this);
    }

    public void disable() {
        setEnabled(false);
    }

    public void onDisable() {
        if(this.calcTask != null) this.calcTask.cancel();
        Utils.endAllCalculationTasks();
        MenuManager.closeAll();
        ValuableManager.getRegisteredValuables().clear();
    }

    public void setLogLevel(LogLevel level) {
        this.logLevel = level;
    }

    public LogLevel getLogLevel() {
        return this.logLevel;
    }

    public USpawners getUSpawners() {
        return this.uspawners;
    }

    public ASkyBlock getASkyBlock() {
        return this.askyblock;
    }
    
    public Citizens getCitizens() {
        return this.citizens;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public static IslandTop get() {
        return IslandTop.instance;
    }

}
