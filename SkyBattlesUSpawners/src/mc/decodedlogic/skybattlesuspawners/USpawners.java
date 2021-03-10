package mc.decodedlogic.skybattlesuspawners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.gmail.filoghost.holographicdisplays.HolographicDisplays;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.wasteofplastic.askyblock.ASkyBlock;

import mc.decodedlogic.skybattlesuspawners.USpawnersLogger.LogLevel;
import mc.decodedlogic.skybattlesuspawners.api.StackedEntityData;
import mc.decodedlogic.skybattlesuspawners.api.USpawnersAPI;
import mc.decodedlogic.skybattlesuspawners.event.listener.BlockEvents;
import mc.decodedlogic.skybattlesuspawners.event.listener.EntityEvents;
import mc.decodedlogic.skybattlesuspawners.event.listener.MenuEvents;
import mc.decodedlogic.skybattlesuspawners.logging.LocationLog;
import mc.decodedlogic.skybattlesuspawners.logging.SpawnerLog;
import mc.decodedlogic.skybattlesuspawners.menu.MenuManager;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerManager;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerType;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerUpgrade;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

public class USpawners extends JavaPlugin implements CommandExecutor, TabExecutor {

    private static USpawners instance;

    private USpawnersLogger notify;
    private LogLevel logLevel;
    private Economy economy;
    private HolographicDisplays hgDisplaysPlugin;
    private WorldGuardPlugin worldGuardPlugin;
    
    private ASkyBlock asbPlugin = null;

    private Settings settings;
    
    // SDAKLJFKJDSHSKFHSKDGF I hate the way ASkyBlock was designed
    // that made this necessary.
    private BukkitTask waitForASBTask;
    
    // Task to clear entities every 5 minutes.
    private BukkitTask clearEntitiesTask;

    public void onEnable() {
        USpawners.instance = this;

        this.logLevel = LogLevel.INFO;
        this.notify = new USpawnersLogger("USpawners", logLevel);

        Plugin vault = this.getServer().getPluginManager().getPlugin("Vault");
        if(vault != null && vault instanceof Vault) {
            notify.info(ChatColor.GREEN + String.format("Successfully hooked into Vault [%s]", 
                    vault.getDescription().getVersion()));

            final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

            if(rsp != null && rsp.getProvider() != null) {
                this.economy = rsp.getProvider();

                final Plugin econPlugin = this.getServer().getPluginManager().getPlugin(economy.getName());
                final String ver = (econPlugin == null) ? "UNKNOWN" : econPlugin.getDescription().getVersion();
                notify.info(ChatColor.GREEN + String.format("Successfully hooked into %s [%s]", 
                        economy.getName(), ver));
            }else {
                notify.error("No economy plugin found! You must install one to use this plugin!");
                setEnabled(false);
                return;
            }

        }else {
            notify.error("Could not hook into Vault! Is it installed correctly?");
            setEnabled(false);
            return;
        }

        Plugin hgDisplays = this.getServer().getPluginManager().getPlugin("HolographicDisplays");
        if(hgDisplays != null && hgDisplays instanceof HolographicDisplays) {
            this.hgDisplaysPlugin = (HolographicDisplays)hgDisplays;
            notify.info(ChatColor.GREEN + String.format("Successfully hooked into HolographicDisplays [%s]", 
                    hgDisplaysPlugin.getDescription().getVersion()));
        }else {
            this.hgDisplaysPlugin = null;
            notify.warning("Could not hook into HolographicDisplays! "
                    + "Is it installed correctly? Hologram functionality has been disabled.");
        }

        Plugin worldGuard = this.getServer().getPluginManager().getPlugin("WorldGuard");
        if(worldGuard != null && worldGuard instanceof WorldGuardPlugin) {
            this.worldGuardPlugin = (WorldGuardPlugin)worldGuard;
            notify.info(ChatColor.GREEN + String.format("Successfully hooked into WorldGuard [%s]", 
                    worldGuardPlugin.getDescription().getVersion()));
        }else {
            this.worldGuardPlugin = null;
            notify.warning("Could not hook into WorldGuard! "
                    + "Is it installed correctly? Region checking functionality has been disabled.");
        }
        
        this.waitForASkyblock();
    }
    
    public void waitForASkyblock() {
        this.waitForASBTask = new BukkitRunnable() {
            
            public void run() {
                Plugin asb = null;
                
                long startTimeMs = System.currentTimeMillis();
                long lastPollMsgTime = 0;
                
                while(!(asbPlugin != null && asbPlugin.getGrid() != null)) {
                    
                    long elapsedTime = (System.currentTimeMillis() - startTimeMs);
                    
                    if(elapsedTime > 60000) {
                        notify.error("Could not hook into ASkyBlock after 1 minute! Is it installed correctly?");
                        setEnabled(false);
                        return;
                    }
                    
                    if(asbPlugin == null) {
                        // We need to setup our reference to ASkyBlock.
                        asb = getServer().getPluginManager().getPlugin("ASkyBlock");
                        
                        if(asb != null && asb instanceof ASkyBlock) {
                            asbPlugin = (ASkyBlock) asb;
                        }
                        
                    }
                    
                    if((System.currentTimeMillis() - lastPollMsgTime) > 10000) {
                        notify.info(ChatColor.YELLOW + "Polling ASkyBlock...");
                        lastPollMsgTime = System.currentTimeMillis();
                    }
                }
                
                finishLoading();
                this.cancel();
            }
            
        }.runTaskAsynchronously(this);
    }
    
    public void finishLoading() {
        USpawners t = this;
        new BukkitRunnable() {
            
            public void run() {
                waitForASBTask = null;
                
                notify.info(ChatColor.GREEN + String.format("Successfully hooked into ASkyBlock [%s]", 
                        asbPlugin.getDescription().getVersion()));
                
                try {
                    settings = new Settings(t);
                } catch (Exception e) {
                    notify.error(String.format("An error occurred while loading settings. %s(%s).", e.getClass().getName(), e.getMessage()));
                    setEnabled(false);
                    return;
                }
                
                // Let's load up our logs!
                LocationLog.loadAll();
                
                try {
                    SpawnerManager.loadStoredSpawners();
                } catch (Exception e) {
                    notify.error(String.format("An error occurred while loading existing spawners. %s(%s).", e.getClass().getName(), e.getMessage()));
                    setEnabled(false);
                    return;
                }

                getServer().getPluginManager().registerEvents(new BlockEvents(), t);
                getServer().getPluginManager().registerEvents(new EntityEvents(t), t);
                getServer().getPluginManager().registerEvents(new MenuEvents(), t);

                // Let's register our command.
                getCommand("uspawners").setExecutor(t);
                
                // Let's register our clear task.
                long cooldown = (20L * 60) * 5; // 5 minutes
                clearEntitiesTask = new BukkitRunnable() {
                    
                    public void run() {
                        clearAllStackedEntities();
                    }
                    
                }.runTaskTimer(t, cooldown, cooldown);
            }
            
        }.runTask(this);
    }
    
    public void clearAllStackedEntities() {
        int cleared = 0;
        
        for(World w : getServer().getWorlds()) {
        
            for(Entity ent : w.getEntities()) {
                StackedEntityData sd = USpawnersAPI.getStackedEntityData(ent);
                
                if(ent.getType() == EntityType.DROPPED_ITEM) {
                    if(USpawnersAPI.getItemsInStack((Item) ent) > 0) {
                        ent.remove();
                        cleared++;
                        continue;
                    }
                }else if(sd != null) {
                    ent.remove();
                    cleared++;
                }
            }
        }
        
        final String CLEAR_MSG = Utils.color(settings.getClearedEntities());
        getServer().getOnlinePlayers().stream().filter(p -> p.isOp()).forEach(p -> p.sendMessage(CLEAR_MSG));
        
        notify.info(ChatColor.GREEN + String.format("Successfully cleared %s stacked entities!", cleared));
    }

    public void onDisable() {
        
        // Let's clear one last time.
        clearAllStackedEntities();
        
        SpawnerManager.cleanup();
        MenuManager.closeAllMenus();
        LocationLog.saveAll();
        
        if(clearEntitiesTask != null) {
            this.clearEntitiesTask.cancel();
            this.clearEntitiesTask = null;
        }
        
        if(waitForASBTask != null) {
            this.waitForASBTask.cancel();
            this.waitForASBTask = null;
        }
    }

    public void disable() {
        setEnabled(false);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> compls = new ArrayList<String>();
        if(label.equalsIgnoreCase("uspawners")) {
            boolean givePerm = sender.hasPermission("uspawners.give") || sender.isOp();
            boolean clearPerm = sender.hasPermission("uspawners.clear") || sender.isOp();
                
            if(args.length == 1) {
                if(givePerm) compls.add("give");
                if(clearPerm) compls.add("clear");
            }else if(args.length > 1) {
                if(args[0].equalsIgnoreCase("give") && givePerm) {
                    if(args.length == 2) {
                        compls.add(ChatColor.stripColor(sender.getName()));
                        USpawners.get().getServer().getOnlinePlayers().stream().forEach(oP -> {
                            if(oP != sender) {
                                compls.add(ChatColor.stripColor(oP.getDisplayName()));
                            }
                        });
                    }else if(args.length == 3) {
                        for(SpawnerType type : SpawnerType.values()) {
                            compls.add(type.getName().toUpperCase());
                        }
                    }else if(args.length == 4) {
                        String sType = args[2];
                        SpawnerType type = null;
                        
                        try {
                            type = SpawnerType.valueOf(sType.toUpperCase());
                        } catch (IllegalArgumentException e) {}
                        
                        if(type != null) {
                            compls.add("DEFAULT");
                            
                            for(SpawnerUpgrade upgrade : type.getUpgrades()) {
                                compls.add(upgrade.getName().toUpperCase());
                            }
                        }
                    }else if(args.length == 5) {
                        compls.add("1");
                    }
                }else if(args[0].equalsIgnoreCase("clear") && clearPerm) {
                    if(args.length == 2) {
                        compls.add("5");
                    }
                }
            }
        }
        
        return compls;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(waitForASBTask != null) return true;
        if(label.equalsIgnoreCase("uspawners")) {
            if(args.length == 5 && args[0].equalsIgnoreCase("give")) {
                if(sender.hasPermission("uspawners.give") || sender.isOp()) {
                    final String playerName = args[1];
                    final String spawnerType = args[2];
                    final String upgradeName = args[3];
                    int amount = 1;

                    try {
                        amount = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        return false;
                    }

                    final Player player = Bukkit.getServer().getPlayer(playerName);

                    if(player != null) {
                        SpawnerType type = null;

                        for(SpawnerType sType : SpawnerType.values()) {
                            if(sType.getName().equalsIgnoreCase(spawnerType)) {
                                type = sType;
                                break;
                            }
                        }

                        if(type != null) {
                            SpawnerUpgrade sUpgrade = SpawnerUpgrade.DEFAULT;
                            
                            for(SpawnerUpgrade u : type.getUpgrades()) {
                                if(u.getName().equalsIgnoreCase(upgradeName)) {
                                    sUpgrade = u;
                                    break;
                                }
                            }
                            
                            int upgradeIndex = type.getUpgrades().indexOf(sUpgrade);
                            
                            if(sUpgrade != null) {
                                HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(Utils.generateSpawner(type.getEntityType(), amount, 
                                        upgradeIndex));

                                for(ItemStack leftover : leftovers.values()) {
                                    player.getLocation().getWorld().dropItemNaturally(player.getLocation(), leftover);
                                }

                                String chatMsg = settings.getGaveSpawners();
                                chatMsg = Utils.replaceVariableWith(chatMsg, "player", player.getName());
                                chatMsg = Utils.replaceVariableWith(chatMsg, "upgrade", sUpgrade.getName());
                                chatMsg = Utils.replaceVariableWith(chatMsg, "spawnerType", Utils.makePrettyStringFromEnum(type.getName(), false));
                                chatMsg = Utils.replaceVariableWith(chatMsg, "amount", amount);
                                sender.sendMessage(Utils.mkDisplayReady(chatMsg));
                                return true;
                            }else {
                                String chatMsg = settings.getUnknownData();
                                chatMsg = Utils.replaceVariableWith(chatMsg, "data", upgradeName);
                                sender.sendMessage(Utils.mkDisplayReady(chatMsg));
                                return true;
                            }
                        }else {
                            String chatMsg = settings.getUnknownData();
                            chatMsg = Utils.replaceVariableWith(chatMsg, "data", spawnerType);
                            sender.sendMessage(Utils.mkDisplayReady(chatMsg));
                            return true;
                        }
                    }else {
                        String chatMsg = settings.getPlayerNotFound();
                        chatMsg = Utils.replaceVariableWith(chatMsg, "player", playerName);
                        sender.sendMessage(Utils.mkDisplayReady(chatMsg));
                        return true;
                    }

                }else {
                    sender.sendMessage(Utils.mkDisplayReady(settings.getNoPermission()));
                    return true;
                }
            }else if(args.length == 2 && args[0].equalsIgnoreCase("clear")) {
                if(sender.hasPermission("uspawners.clear") || sender.isOp()) {
                    int radius = 1;
                    
                    final String invalidInput = ChatColor.RED + "Error: Invalid input. Must specify a radius between 1 and 999.";

                    try {
                        radius = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(invalidInput);
                        return true;
                    }

                    if(!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Error: You must be a player to execute that command!");
                        return true;
                    }
                    
                    if(radius < 1 || radius > 999) {
                        sender.sendMessage(invalidInput);
                        return true;
                    }
                    
                    Player p = (Player) sender;
                    int cleared = 0;
                    
                    for(Entity ent : p.getLocation().getWorld().getNearbyEntities(p.getLocation(), radius, radius, radius)) {
                        StackedEntityData sd = USpawnersAPI.getStackedEntityData(ent);
                        
                        if(ent.getType() == EntityType.DROPPED_ITEM) {
                            if(USpawnersAPI.getItemsInStack((Item) ent) > 0) {
                                ent.remove();
                                cleared++;
                                continue;
                            }
                        }else if(sd != null) {
                            ent.remove();
                            cleared++;
                        }
                    }

                    String msg = USpawners.get().getSettings().getClearCommandFeedback();
                    msg = Utils.replaceVariableWith(msg, "cleared", cleared);
                   
                    
                    p.sendMessage(Utils.color(msg));
                }else {
                    sender.sendMessage(Utils.mkDisplayReady(USpawners.get().getSettings().getNoPermission()));
                }

                return true;
            }else if(args.length == 1 && args[0].equalsIgnoreCase("inspect")) {
                if(sender.hasPermission("uspawners.inspect") || sender.isOp()) {
                    if(!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Error: You must be a player to execute that command!");
                        return true;
                    }
                    
                    Player p = (Player) sender;
                    
                    boolean inspecting = LocationLog.isInspecting(p);
                    LocationLog.setInspecting(p, !inspecting);
                    
                    if(!inspecting) {
                        String msg = USpawners.get().getSettings().getInspectorEnabled();
                        p.sendMessage(Utils.color(msg));
                    }else {
                        String msg = USpawners.get().getSettings().getInspectorDisabled();
                        p.sendMessage(Utils.color(msg));
                    }
                }else {
                    sender.sendMessage(Utils.mkDisplayReady(USpawners.get().getSettings().getNoPermission()));
                }
                
                return true;
            }else if(args.length == 2 && args[0].equalsIgnoreCase("radius")) {
                if(sender.hasPermission("uspawners.inspect") || sender.isOp()) {
                    int radius = 1;
                    if(!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Error: You must be a player to execute that command!");
                        return true;
                    }
                    
                    try {
                        radius = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Utils.color(USpawners.get().getSettings().getRadiusError()));
                        return true;
                    }
                    
                    if(radius < 1 || radius > 500) {
                        sender.sendMessage(Utils.color(USpawners.get().getSettings().getRadiusError()));
                        return true;
                    }
                    
                    Player p = (Player) sender;
                    Set<LocationLog> nearbyLogs = new HashSet<LocationLog>();
                    
                    // Let's use a different thread.
                    final int r = radius;
                    getServer().getScheduler().runTaskAsynchronously(this, () -> {
                       for(Location l : LocationLog.logs.keySet()) {
                           if(l.distance(p.getLocation()) <= r) {
                               nearbyLogs.add(LocationLog.logs.get(l));
                           }
                       }
                       
                       getServer().getScheduler().runTaskLater(USpawners.get(), () -> {
                           int i = 0;
                           int size = nearbyLogs.size();
                           sender.sendMessage("");
                           sender.sendMessage(Utils.color("&2-------------------------------------------------"));
                           sender.sendMessage("");
                           for(LocationLog log : nearbyLogs) {
                               String line = "&9MobSpawner&f<&e%s&f,&e%s&f,&e%s&f> - &6%s &7Records";
                               String line2 = "&7&l- &9Has Active Spawner: %s";
                               String line3 = "&a&lCLICK HERE TO VISIT";
                               String f = "&cfalse";
                               String t = "&atrue";
                               Location l = log.getLocation();
                               int x = l.getBlockX();
                               int y = l.getBlockY();
                               int z = l.getBlockZ();
                               line = String.format(line, x, y, z, log.getAllRecords().size());
                               
                               SpawnerLog latest = log.getLatestSpawnerLog();
                               boolean active = latest != null && latest.getDeletionRecord() == null;
                               
                               line2 = String.format(line2, (active) ? t : f);
                               
                               sender.sendMessage(Utils.color(line));
                               sender.sendMessage(Utils.color(line2));
                               
                               TextComponent click = new TextComponent();
                               click.setText(Utils.color(line3));
                               click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/uspawners view %s %s %s", x, y, z)));
                               
                               p.spigot().sendMessage(click);
                               
                               if((i+1) < size) {
                                   sender.sendMessage("");
                                   sender.sendMessage(Utils.color("&2-------------------------------------------------"));
                                   sender.sendMessage("");
                               }
                               
                               i++;
                           }
                       }, 1L);
                    });
                    
                }else {
                    sender.sendMessage(Utils.mkDisplayReady(USpawners.get().getSettings().getNoPermission()));
                }
                
                return true;
            }else if(args.length == 4 && args[0].equalsIgnoreCase("view")) {
                if(sender.hasPermission("uspawners.inspect") || sender.isOp()) {
                    if(!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Error: You must be a player to execute that command!");
                        return true;
                    }
                    
                    int x = 0;
                    int y = 0;
                    int z = 0;
                    
                    try {
                        x = Integer.parseInt(args[1]);
                        y = Integer.parseInt(args[2]);
                        z = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        return true;
                    }
                    
                    Player p = (Player) sender;
                    LocationLog log = LocationLog.get(new Location(p.getWorld(), x, y, z));
                    
                    if(log != null) {
                        int offsetX = ThreadLocalRandom.current().nextInt(-5, 5);
                        int offsetZ = ThreadLocalRandom.current().nextInt(-5, 5);
                        Location l = new Location(p.getWorld(), x+offsetX, y+2, z+offsetZ);
                        p.teleport(Utils.lookAt(l, log.getLocation()));
                        
                        getServer().getScheduler().runTaskLater(this, () -> {
                            p.playEffect(log.getLocation(), Effect.ENDER_SIGNAL, 30);
                        }, 5L);
                    }
                    
                }else {
                    sender.sendMessage(Utils.mkDisplayReady(USpawners.get().getSettings().getNoPermission()));
                }
                
                return true;
            }
        }

        return !settings.showDefaultCommandHelp();
    }

    public boolean callEvent(final Event evt) {
        if(evt == null) return false;

        if(isEnabled()) {
            getServer().getPluginManager().callEvent(evt);
            return true;
        }

        return false;
    }

    public void setLogLevel(LogLevel level) {
        this.logLevel = level;
    }

    public LogLevel getLogLevel() {
        return this.logLevel;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public HolographicDisplays getHolographicDisplays() {
        return this.hgDisplaysPlugin;
    }

    public WorldGuardPlugin getWorldGuard() {
        return this.worldGuardPlugin;
    }
    
    public ASkyBlock getASkyBlock() {
        return this.asbPlugin;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public USpawnersLogger getNotify() {
        return this.notify;
    }

    public static USpawners get() {
        return USpawners.instance;
    }
}
