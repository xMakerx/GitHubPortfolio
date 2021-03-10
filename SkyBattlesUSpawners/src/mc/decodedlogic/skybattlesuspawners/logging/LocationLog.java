package mc.decodedlogic.skybattlesuspawners.logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import mc.decodedlogic.skybattlesuspawners.USpawners;
import mc.decodedlogic.skybattlesuspawners.USpawnersLogger;
import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerManager;

public class LocationLog {
    
    ////////////////////////////////////////////////////////////////////////////////////////
    // Static methods, variables, etc
    ////////////////////////////////////////////////////////////////////////////////////////
    
    public static final String INSPECT_METADATA_KEY_NAME = "USpawnersInspect";
    
    public static Map<Location, LocationLog> logs;
    public static String logDir;
    public static USpawnersLogger notify;
    
    public static boolean loadingLogs;
    public static Map<Location, List<Record>> preloadCache;
    
    static {
        logs = new HashMap<Location, LocationLog>();
        logDir = File.separator + "logs";
        notify = new USpawnersLogger("LogManager");
        preloadCache = new HashMap<Location, List<Record>>();
        loadingLogs = true;
    }
    
    public static void setInspecting(@Nonnull Player p, boolean flag) {
        if(p == null) return;
        USpawners main = USpawners.get();
        
        if(p.hasMetadata(INSPECT_METADATA_KEY_NAME) && p.getMetadata(INSPECT_METADATA_KEY_NAME).size() > 0) {
            p.removeMetadata(INSPECT_METADATA_KEY_NAME, main);
        }
        
        p.setMetadata(INSPECT_METADATA_KEY_NAME, new FixedMetadataValue(main, flag));
    }
    
    public static boolean isInspecting(@Nonnull Player p) {
        if(p == null) return false;
        
        if(p.hasMetadata(INSPECT_METADATA_KEY_NAME) && p.getMetadata(INSPECT_METADATA_KEY_NAME).size() > 0) {
            return p.getMetadata(INSPECT_METADATA_KEY_NAME).get(0).asBoolean();
        }
        
        return false;
    }
    
    public static void loadAll() {
        logs.clear();
        
        final USpawners main = USpawners.get();
        final File dir = new File(main.getDataFolder() + logDir);
        
        if(!dir.exists()) {
            dir.mkdir();
            notify.info("Created log directory!");
        }else if(dir.isDirectory()) {
            
            new BukkitRunnable() {
                public void run() {
            
                    notify.info("Loading all logs...");
                    
                    try (Stream<Path> paths = Files.walk(Paths.get(dir.getPath()))) {
                        paths.filter(Files::isRegularFile)
                        .forEach(file -> {
                            String name = file.getFileName().toString();
                            String[] split = name.split("-");
                            
                            String worldName = "";
                            
                            boolean nextNumNegative = false;
                            int[] coords = new int[3];
                            int k = 0;
                            for(int i = 0; i < split.length; i++) {
                                String s = split[i];
                                if(i == 0) {
                                    worldName = s;
                                    continue;
                                }
                                
                                if(s.isEmpty()) {
                                    nextNumNegative = true;
                                    continue;
                                }else {
                                    if(s.endsWith(".yml")) {
                                        s = s.substring(0, s.indexOf(".yml"));
                                    }
                                    
                                    coords[k] = Integer.valueOf(s) * ((nextNumNegative) ? -1 : 1);
                                    nextNumNegative = false;
                                    
                                    if((k+1) == coords.length) break;
                                    k++;
                                }
                            }
                            
                            World w = main.getServer().getWorld(worldName);
                            
                            if(w != null) {
                                Location l = new Location(w, coords[0], coords[1], coords[2]);
                                LocationLog log = new LocationLog(l);
                                logs.put(l, log);
                            }
                        });
                    } catch (IOException e) {
                        notify.error("Failed to load logs!");
                        e.printStackTrace();
                    }
                    
                    notify.info("All done loading logs!");
                    
                    
                    new BukkitRunnable() {
                        
                        public void run() {
                            for(MobSpawner s : SpawnerManager.getMobSpawners()) {
                                LocationLog log = LocationLog.get(s.getLocation());
                                SpawnerLog sLog = log.getLatestSpawnerLog();
                                if(sLog == null) {
                                    log.handleOldSpawner();
                                }
                            }
                            
                                
                            for(Location l : preloadCache.keySet()) {
                                LocationLog log = LocationLog.get(l);
                                
                                if(log != null) {
                                    for(Record r : preloadCache.get(l)) {
                                        log.addRecord(r);
                                    }
                                }
                            }
                            
                            preloadCache.clear();
                        }
                        
                    }.runTask(main);
                    
                    
                    LocationLog.loadingLogs = false;
                }
            }.runTaskAsynchronously(main);
        }
    }
    
    public static void saveAll() {
        for(LocationLog log : logs.values()) log.save();
        notify.info("Saved all logs!");
    }
    
    public static void addRecord(@Nonnull Location l, @Nonnull Record r) {
        if(l == null || r == null) return;
        
        if(loadingLogs) {
            List<Record> cache = preloadCache.getOrDefault(l, new ArrayList<Record>());
            
            cache.add(r);
            preloadCache.put(l, cache);
        }else {
            LocationLog log = get(l);
            if(log != null) log.addRecord(r);
        }
        
    }
    
    public static LocationLog get(@Nonnull Location l) {
        LocationLog log = null;
        l = new Location(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
        log = logs.get(l);
        
        if(log == null && !LocationLog.loadingLogs) {
            log = new LocationLog(l);
            logs.put(l, log);
        }
        
        return log;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////
    
    private final USpawnersLogger NOTIFY;
    private final Location LOCATION;
    
    private File logFile;
    private FileConfiguration conf;
    
    private List<SpawnerLog> spawnerLogs;
    private List<Record> allRecords;
    
    private boolean canAddTransactions;
    
    public LocationLog(Location location) {
        this.LOCATION = location;
        this.NOTIFY = new USpawnersLogger("LocationLog-" + getFilename());
        this.spawnerLogs = new ArrayList<SpawnerLog>();
        this.allRecords = new ArrayList<Record>();
        
        USpawners main = USpawners.get();
        this.logFile = new File(main.getDataFolder() + File.separator + "logs" + File.separator + getFilename());
        boolean exists = logFile.exists();
        
        if(!exists)
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                NOTIFY.error("Failed to create new LocationLog.");
                e.printStackTrace();
            }
        
        this.conf = YamlConfiguration.loadConfiguration(logFile);
        this.canAddTransactions = false;
        if(exists) populate();
    }
    
    public void addRecord(@Nonnull Record r) {
        if(r == null) return;
        
        if(r instanceof ModificationRecord) {
            ModificationRecord modRecord = (ModificationRecord) r;
            SpawnerLog l = null;
            
            if(modRecord.isPlace) {
                l = new SpawnerLog();
                l.setCreationRecord(modRecord);
                spawnerLogs.add(l);
                
                if(spawnerLogs.size() > 9) {
                    spawnerLogs.remove(0);
                }
                
                this.canAddTransactions = true;
            }else {
                l = spawnerLogs.get(spawnerLogs.size()-1);
                l.setDeletionRecord(modRecord);
                this.canAddTransactions = false;
            }
            
            l.records.add(modRecord);
        }else if(canAddTransactions) {
            SpawnerLog l = spawnerLogs.get(spawnerLogs.size()-1);
            
            if(l != null) {
                l.records.add(r);
            }else {
                NOTIFY.warning("Current spawner log not found?");
                return;
            }
        }
        
        allRecords.add(r);
        NOTIFY.debug("Added record!");
        save();
    }
    
    public void handleOldSpawner() {
        SpawnerLog l = new SpawnerLog();
        ModificationRecord createRecord = new ModificationRecord("server", -1, true);
        spawnerLogs.add(l);
        
        addRecord(createRecord);
    }
    
    public SpawnerLog getLatestSpawnerLog() {
        SpawnerLog l = null;
        
        if(spawnerLogs.size() > 0) {
            l = spawnerLogs.get(spawnerLogs.size()-1);
        }
        
        return l;
    }
    
    public void save() {
        List<String> strRecords = new ArrayList<String>();
        
        for(Record r : allRecords) {
            strRecords.add(r.toString());
        }
        
        conf.set("records", strRecords);
        
        try {
            conf.save(logFile);
        } catch (IOException e) {
            System.out.printf("Failed to save LocationLog %s!", getFilename());
            e.printStackTrace();
        }
    }
    
    public void delete() {
        this.spawnerLogs.clear();
        this.allRecords.clear();
        
        conf = null;
        logFile.delete();
    }
    
    private void populate() {
        List<String> strRecords = conf.getStringList("records");
        
        for(String str : strRecords) {
            char prefix = str.charAt(0);
            
            if(prefix == 'T') {
                addRecord(Transaction.from(str));
            }else {
                ModificationRecord r = ModificationRecord.from(str);
                if(r != null) addRecord(r);
            }
        }
        
        boolean changes = false;
        
        int newRecordIndex = 1;
        for(int i = 0; i < spawnerLogs.size()-1; i++) {
            SpawnerLog l = spawnerLogs.get(i);
            
            newRecordIndex += l.records.size();
            
            if(l.getDeletionRecord() == null) {
                // Let's add a deletion for an unknown time.
                ModificationRecord r = new ModificationRecord("server", -1, false);
                l.setDeletionRecord(r);
                l.records.add(r);
                changes = true;
                
                allRecords.add(newRecordIndex, r);
            }
            
            newRecordIndex += 2;
        }
        
        if(changes) save();
    }
    
    public Location getLocation() {
        return this.LOCATION;
    }
    
    public List<SpawnerLog> getSpawnerLogs() {
        return this.spawnerLogs;
    }
    
    public List<Record> getAllRecords() {
        return this.allRecords;
    }
    
    public String getFilename() {
        
        return String.format("%s-%s-%s-%s.yml", LOCATION.getWorld().getName(), 
                LOCATION.getBlockX(), 
                LOCATION.getBlockY(), 
                LOCATION.getBlockZ());
    }
}
