package mc.decodedlogic.craftrp.entity;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import mc.decodedlogic.craftrp.CraftRP;
import mc.decodedlogic.craftrp.event.PlayerInteractEntityEvent;
import mc.decodedlogic.craftrp.hologram.HologramMovement;
import mc.decodedlogic.craftrp.hologram.HologramVisibility;
import mc.decodedlogic.craftrp.hologram.RPHologram;
import mc.decodedlogic.craftrp.particle.ParticleEffects;
import mc.decodedlogic.craftrp.player.CRPPlayer;
import mc.decodedlogic.craftrp.player.CRPPlayerManager;
import mc.decodedlogic.craftrp.player.PlayerPointer;
import mc.decodedlogic.craftrp.util.BlockUtils;
import net.md_5.bungee.api.ChatColor;

public class CRPMoneyPrinter extends CRPPlaceableEntity {
    
    public static final String PRINTER_HOLOGRAM_NAME = "HG_Printer";
    
    // Temperature is in fahrenheit
    public static final int DEFAULT_TEMPERATURE = 72;
    public static final int SMOKE_TEMPERATURE = 105;
    public static final int EXPLODE_TEMPERATURE = 150;
    public static final int MINIMUM_TEMPERATURE = 65;
    
    public enum PrinterType {
        BRONZE("Bronze", ChatColor.GOLD, Material.ORANGE_CARPET, new int[] {5, 8, 10}),
        SILVER("Silver", ChatColor.GRAY, Material.LIGHT_GRAY_CARPET, new int[] {12, 16, 20}),
        GOLD("Gold", ChatColor.YELLOW, Material.YELLOW_CARPET, new int[] {22, 26, 30}),
        PLATINUM("Platinum", ChatColor.AQUA, Material.LIGHT_BLUE_CARPET, new int[] {32, 36, 40});
        
        private String name;
        private ChatColor textColor;
        private Material carpetType;
        
        private int[] moneyIvals;
        
        private PrinterType(String name, ChatColor textColor, Material carpetType, int[] moneyIvals) {
            this.name = name;
            this.textColor = textColor;
            this.carpetType = carpetType;
            this.moneyIvals = moneyIvals;
        }
        
        public String getName() {
            return this.name;
        }
        
        public ChatColor getTextColor() {
            return this.textColor;
        }
        
        public Material getCarpetType() {
            return this.carpetType;
        }
        
        public int getMoneyAtLevel(int level) {
            level = level - 1;
            return (level < moneyIvals.length) ? moneyIvals[level] : 0;
        }
        
    }
    
    public enum PrinterState {
        PRINTING("&7Printing..."),
        ERROR("&c&lError!"),
        STARTING("&aStarting...");
        
        private String name;
        
        private PrinterState(String name) {
            this.name = name;
        }
        
        public String getColoredName() {
            return ChatColor.translateAlternateColorCodes('&', this.name);
        }
        
        public String getName() {
            return this.name;
        }

    }
    
    private final PrinterType PRINTER_TYPE;
    private PrinterState state;
    private int level;
    private int temperature;
    
    private int money;
    
    private boolean hasCooler;
    
    private BukkitTask task;
    
    public CRPMoneyPrinter(PrinterType type, CRPPlayer owner) {
        super(CRPEntityType.PRINTER);
        this.name = "Money Printer";
        this.PRINTER_TYPE = type;
        this.state = PrinterState.STARTING;
        this.level = 1;
        this.temperature = DEFAULT_TEMPERATURE;
        this.money = 0;
        this.hasCooler = true;
        this.task = null;
        
        RPHologram rpHG = new RPHologram(hologramContainer);
        rpHG.setName(PRINTER_HOLOGRAM_NAME);
        rpHG.setPopupSound(RPHologram.DEFAULT_APPEAR_SOUND);
        rpHG.setVisibility(HologramVisibility.ALL_VIEWERS);
        rpHG.setMovement(HologramMovement.STATIONARY);
        
        hologramContainer.addHologram(PRINTER_HOLOGRAM_NAME, rpHG);
    }
    
    public void generate() {
        if(origin.getBlock().isEmpty()) {
            origin.getBlock().setType(Material.LOOM);
        }
        
        Block above = new Location(origin.getWorld(), origin.getBlockX(), 
                origin.getBlockY()+1.0, origin.getBlockZ()).getBlock();
        
        above.setType(PRINTER_TYPE.getCarpetType());
        
        blocks.add(above);
        
        CRPEntityManager.registerEntity(this);
        
        Location l = above.getLocation().clone();
        l.add(0.5, 1.1, 0.5);
        Hologram hg = HologramsAPI.createHologram(CraftRP.get(), l);
        hg.getVisibilityManager().setVisibleByDefault(false);
        hg.appendTextLine(PRINTER_TYPE.getTextColor() + PRINTER_TYPE.getName() 
            + " " + ChatColor.DARK_GREEN + name);
        hg.appendTextLine(state.getColoredName());
        hg.appendTextLine(ChatColor.GREEN + String.format("%s*F", temperature));
        
        RPHologram rpHG = hologramContainer.getHologramByName(PRINTER_HOLOGRAM_NAME);
        rpHG.setOrigin(l);
        rpHG.setHologram(hg);
        
        startTask();
    }
    
    public void startTask() {
        Block b = origin.getBlock();
        long startTime = System.currentTimeMillis();
        
        this.task = new BukkitRunnable() {
            
            long lastPlayTime = 0;
            long nextPlayTime = 1000;
            
            long nextUpdateTime = 0;
            long nextHeatTime = 0;
            
            long nextSmokeSoundTime = 0;
            long timeStartSmoking = -1;
            long timeLastCoolerChanceIncrease = -1;
            long purchaseCoolerTime = -1;
            long nextCoolTime = -1;
            
            int moneyAccumlatedSinceCoolerPurchase = 0;
            int moneyAccumulatedCoolerBreak = -1;
            double coolerBreakChance = 0.00;
            
            Location front = BlockUtils.getFrontCenter(b);
            Location back = BlockUtils.getRearCenter(b);
            
            public void run() {
                if(b == null) destroy();
                
                long now = System.currentTimeMillis();
                
                if(state == PrinterState.STARTING) {
                    setState(PrinterState.PRINTING);
                }else if(state == PrinterState.PRINTING) {
                    long timeSinceStart = (now - startTime);
                    double secondsPrinting = ((double) timeSinceStart) / 1000d;
                    
                    if(hasCooler) {
                        if(purchaseCoolerTime == -1) {
                            purchaseCoolerTime = now;
                            
                            // Let's calculate the maximum amount of money that can be printed before this
                            // cooler breaks.
                            int minsUntilBreak = 10 + ((level-1) * 5);
                            int moneyInASecond = PRINTER_TYPE.getMoneyAtLevel(level) * 2;
                            
                            moneyAccumulatedCoolerBreak = (moneyInASecond * 60) * minsUntilBreak;
                            coolerBreakChance = 0.00;
                            
                            // Let's calculate the time until we can activate the cooler.
                            int secondsUntilStart = 20 - (level-1) * 10;
                            nextCoolTime = now + (1000 * secondsUntilStart);
                        }
                        
                        if(now >= nextCoolTime) {
                            double secondsWithCooler = ((double) (now - purchaseCoolerTime)) / 1000d;
                            int bound = (int) (0.95d * Math.log(secondsWithCooler));
                            int delta = ThreadLocalRandom.current().nextInt(1, bound);
                            delta = (delta > 12) ? 12 : delta;
                            
                            if((temperature - delta) < MINIMUM_TEMPERATURE) {
                                temperature = MINIMUM_TEMPERATURE;
                            }else {
                                temperature -= delta;
                            }
                            
                            nextCoolTime = now + ThreadLocalRandom.current().nextInt(4500, 6250);
                            b.getWorld().playSound(origin, Sound.ENTITY_EGG_THROW, 0.35F, 0.85F);
                        }
                        
                        double chance = ThreadLocalRandom.current().nextDouble();
                        boolean tooMuchMoney = moneyAccumlatedSinceCoolerPurchase >= moneyAccumulatedCoolerBreak;
                        
                        double breakChance = ((double) moneyAccumlatedSinceCoolerPurchase) / moneyAccumulatedCoolerBreak;
                        coolerBreakChance += breakChance;
                        
                        if(tooMuchMoney || chance < coolerBreakChance) {
                            // Uh-oh, it's not their lucky day.
                            b.getWorld().playSound(origin, Sound.BLOCK_METAL_BREAK, 1.5F, 1.0F);
                            hasCooler = false;
                            
                            purchaseCoolerTime = -1;
                            moneyAccumulatedCoolerBreak = -1;
                            moneyAccumlatedSinceCoolerPurchase = 0;
                            timeLastCoolerChanceIncrease = -1;
                            coolerBreakChance = 0.00;
                            nextCoolTime = -1;
                        }
                    }
                    
                    
                    if(now > nextHeatTime && secondsPrinting > 10) {
                        int bound = (int) (1.25d * Math.log(secondsPrinting));
                        temperature += ThreadLocalRandom.current().nextInt(1, bound);
                        
                        nextHeatTime = now + ThreadLocalRandom.current().nextInt(6000, 9250);
                    }
                    
                    money += PRINTER_TYPE.getMoneyAtLevel(level);
                    if(hasCooler) moneyAccumlatedSinceCoolerPurchase += PRINTER_TYPE.getMoneyAtLevel(level);
                    
                    BlockData data = Material.BLACK_CONCRETE.createBlockData();
                    b.getWorld().spawnParticle(Particle.BLOCK_DUST, front, 20, data);
                    
                    // Percentage progress to blowing up
                    double perct = ((double) temperature) / EXPLODE_TEMPERATURE;
                    
                    if(temperature >= SMOKE_TEMPERATURE) {
                        
                        float smokePerct = ((float) (temperature - SMOKE_TEMPERATURE)) / ((float) (EXPLODE_TEMPERATURE - SMOKE_TEMPERATURE));
                        
                        ParticleEffects.drawDustAt(back, Color.GRAY, 6 + (6*smokePerct), 4 + (int) Math.ceil(36*smokePerct));
                        
                        if(smokePerct > 0.65F) {
                            int numFlames = 3 + (int) Math.ceil(20 * smokePerct);
                            b.getWorld().playEffect(origin, Effect.MOBSPAWNER_FLAMES, numFlames);
                            
                            if(now > nextSmokeSoundTime) {
                                b.getWorld().playSound(origin, Sound.BLOCK_FIRE_AMBIENT, 0.5F + (0.5F * smokePerct), 1.0F);
                                nextSmokeSoundTime = (now + 2500) - (int) Math.ceil(2000 * smokePerct);
                            }
                        }
                        
                        if(timeStartSmoking == -1) {
                            timeStartSmoking = now;
                        }else {
                            double secondsSmoking = (double) (now - timeStartSmoking) / 1000d;
                            double secondsSinceLastIncrease = (timeLastCoolerChanceIncrease > 0) ? (double) (now - timeLastCoolerChanceIncrease) / 1000d : 0;
                            
                            if((secondsSmoking - secondsSinceLastIncrease) % 30 == 0) {
                                timeLastCoolerChanceIncrease = now;
                                coolerBreakChance += 0.05;
                            }
                        }
                        
                    }else {
                        double secondsSmoking = (double) (now - timeStartSmoking) / 1000d;
                        int incrs = (int) Math.ceil(secondsSmoking / 30);
                        
                        coolerBreakChance -= 0.05 * incrs;
                        
                        // Reset the time we starting smoking to -1.
                        if(timeStartSmoking != -1) {
                            timeStartSmoking = -1;
                        }
                    }
                    
                    if(now > (lastPlayTime + nextPlayTime)) {
                        b.getWorld().playSound(origin, Sound.BLOCK_COMPARATOR_CLICK, 0.5F, 1.0F);
                        lastPlayTime = now;
                        
                        int bound = 4200;
                        
                        bound -= (3950 * perct);
                        
                        int origin = (bound > 1500) ? 1500 : bound-1;
                        
                        nextPlayTime = ThreadLocalRandom.current().nextInt(origin, bound);
                    }
                }else {
                    if(isRegistered()) {
                        b.getWorld().createExplosion(origin, 8.0f, false, false);
                        destroy();
                        return;
                    }
                }
                
                if(now > nextUpdateTime && state != PrinterState.ERROR) {
                    update();
                    nextUpdateTime = now + 1500;
                }
            }
            
        }.runTaskTimer(CraftRP.get(), 10L, 10L);
    }
    
    public void destroy() {
        reset();
        task.cancel();
        CRPEntityManager.unregisterEntity(this);
    }
    
    public void update() {
        RPHologram rpHG = hologramContainer.getHologramByName(PRINTER_HOLOGRAM_NAME);
        Hologram hg = rpHG.getHologram();
        
        ChatColor[] fanColors = {
                ChatColor.GREEN,
                ChatColor.YELLOW,
                ChatColor.GOLD,
                ChatColor.RED
        };
        
        int colorIndex = 0;
        
        if(temperature >= EXPLODE_TEMPERATURE) {
            setState(PrinterState.ERROR);
            colorIndex = 3;
        }else if(temperature > 120) {
            colorIndex = 3;
        }else if(temperature > 100) {
            colorIndex = 2;
        }else if(temperature > 80) {
            colorIndex = 1;
        }
        
        hg.clearLines();
        hg.appendTextLine(PRINTER_TYPE.getTextColor() + PRINTER_TYPE.getName() 
        + " " + ChatColor.DARK_GREEN + name);
        hg.appendTextLine("$" + money);
        hg.appendTextLine(fanColors[colorIndex] + String.format("%s*F", temperature));
        
        viewers.stream().forEach(v -> {
            CRPPlayer p = CRPPlayerManager.getCRPPlayerFromId(v);
            if(p != null) {
                rpHG.update(p);
            }
        });

    }
    
    public PrinterType getPrinterType() {
        return this.PRINTER_TYPE;
    }
    
    public void setState(PrinterState newState) {
        this.state = newState;
    }
    
    public PrinterState getState() {
        return this.state;
    }
    
    public void setLevel(int newLevel) {
        this.level = newLevel;
    }
    
    public int getLevel() {
        return this.level;
    }
    
    public void setTemperature(int newTemperature) {
        this.temperature = newTemperature;
    }
    
    public int getTemperature() {
        return this.temperature;
    }
    

    @Override
    public void onPlayerInteract(CRPPlayer p, PlayerInteractEntityEvent evt) {
        evt.setCancelled(true);
    }

    @Override
    public void onPlayerPlace(CRPPlayer p, Location origin) {
        this.origin = origin;
        this.owner = new PlayerPointer(p.getPlayer());
        this.generate();
    }

    @Override
    public boolean canPlaceHere(CRPPlayer p, Location location) {
        Block above = new Location(location.getWorld(), location.getBlockX(), 
                location.getBlockY()+1.0, location.getBlockZ()).getBlock();
        
        return above.isEmpty();
    }

    @Override
    public void onPlayerFocusGained(CRPPlayer p) {
        super.onPlayerFocusGained(p);
        processHologramShow(p, PRINTER_HOLOGRAM_NAME);
    }

    @Override
    public void onPlayerFocusLost(CRPPlayer p) {
        super.onPlayerFocusLost(p);
    }
    
    public void onUnregistered() {
        super.onUnregistered();
        task.cancel();
    }
    
    public String getHelpInformation(CRPPlayer rpPlayer) {
        return "";
    }

}
