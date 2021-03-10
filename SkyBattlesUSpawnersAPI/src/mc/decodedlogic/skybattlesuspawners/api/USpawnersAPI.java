package mc.decodedlogic.skybattlesuspawners.api;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import mc.decodedlogic.skybattlesuspawners.USpawners;
import mc.decodedlogic.skybattlesuspawners.spawner.MobSpawner;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerManager;

public class USpawnersAPI {
    
    public final static String STACK_META_KEY = "StackAmount";
    public final static String ENT_SPAWNER_KEY = "USpawner";
    public final static String ENT_QUANTITY_KEY = "Quantity";
    public final static String ENT_UPGRADE_KEY = "Upgrade";
    
    /**
     * Sets the amount of items in a stack.
     * @param item - The item to update the stack size of.
     * @param amount - The amount to update to.
     * 
     * NOTE: If the amount of items in the {@link ItemStack} is 0 and the stack size is set to 0,
     * the {@link Item} will be removed with {@link Item#remove()}.
     */
    
    public static void setItemsInStack(@Nonnull Item item, int amount) {
        if(item == null || (item != null && item.getItemStack().getType() == Material.AIR)) return;
        
        // The plugin's main instance.
        final USpawners MAIN = USpawners.get();
        
        // Let's remove any existing stack data.
        if(item.hasMetadata(STACK_META_KEY)) {
            try {
                item.removeMetadata(STACK_META_KEY, MAIN);
            } catch (Exception e) {}
        }
        
        if(amount > 0) {
            item.setMetadata(STACK_META_KEY, new FixedMetadataValue(MAIN, amount));
        }else if(item.getItemStack().getAmount() == 0) {
            item.remove();
        }
    }
    
    /**
     * Returns the amount of items in a "stack" of items.
     * Always will return 0 <= n <= MAX_STACK_SIZE
     * @param {@link Item} The item to check.
     * @return
     */
    
    public static int getItemsInStack(@Nonnull Item item) {
        int amount = 0;
        
        if(isStackedItem(item)) {
            try {
                amount = item.getMetadata(STACK_META_KEY).get(0).asInt();
            } catch (NullPointerException e) {}
        }
        
        return amount;
    }
    
    /**
     * Checks a specified {@link Item} to see if it has stacking data.
     * @param The item to check.
     * @return
     */
    
    public static boolean isStackedItem(@Nonnull Item item) {
        if(item == null || (item != null && item.getItemStack().getType() == Material.AIR)) return false;
        
        return (item.hasMetadata(STACK_META_KEY) && item.getMetadata(STACK_META_KEY).size() > 0);
    }
    
    /**
     * Fetches the {@link MobSpawner} at the specified {@link Location}.
     * @param location - The location to check
     * @return
     */
    
    public static MobSpawner getMobSpawnerAt(@Nonnull Location location) {
        if(location == null) return null;
        
        // Let's fetch the spawner at the block's location.
        Optional<MobSpawner> o = SpawnerManager.getMobSpawners().stream()
                .filter(spawner -> spawner.getLocation().equals(location)).findAny();
        MobSpawner mSpawner = (o.isPresent()) ? o.get() : null;
        
        return mSpawner;
    }
    
    /**
     * Fetches the {@link MobSpawner} that the specified {@link Block} represents.
     * @param block - The block to check.
     * @return
     */
    
    public static MobSpawner getMobSpawnerFrom(@Nonnull Block block) {
        if(block == null || (block != null && block.getType() != Material.MOB_SPAWNER)) return null;
        return getMobSpawnerAt(block.getLocation());
    }
    
    /**
     * Utility metadata method to extract the first {@link MetadataValue} from the specified {@link Metadatable} object with the
     * specified key.
     * @param obj - The object that might have the specified metadata.
     * @param key - The name of the metadata key.
     * @return NULL or a valid MetadataValue instance.
     */
    
    public static MetadataValue getFirstValueInKey(@Nonnull Metadatable obj, @Nonnull String key) {
        if(obj == null || key == null) return null;
        
        MetadataValue value = null;
        
        if(obj.hasMetadata(key)) {
            try {
                value = obj.getMetadata(key).get(0);
            } catch (Exception e) {}
        }
        
        return value;
    }
    
    /**
     * Fetches the {@link StackedEntityData} from the specified {@link Entity} if stacking/mob spawner data exists.
     * @param entity - The entity to fetch the stacking data of.
     * @return StackedEntityData or NULL if no such data exists.
     */
    
    public static StackedEntityData getStackedEntityData(Entity entity) {
        final StackedEntityData DATA = new StackedEntityData(entity);
        return (DATA.getMobSpawner() == null) ? null : DATA;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////*
    /**
     *                                          UTILITY METHODS THAT FETCH SETTINGS
     */
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////*
    
    /**
     * Returns the radius in which entities will be stacked.
     * Returns -1 if the plugin hasn't been enabled yet.
     * @return
     */
    
    public static double getStackRadius() {
        // The plugin's main instance.
        final USpawners MAIN = USpawners.get();
        double radius = -1;
        
        if(MAIN != null) {
            radius = MAIN.getSettings().getStackRadius();
        }
        
        return radius;
    }
    
    /**
     * Returns the limit of entities in a stack.
     * Returns -1 if the plugin hasn't been enabled yet.
     * @return
     */
    
    public static int getEntityStackLimit() {
        // The plugin's main instance.
        final USpawners MAIN = USpawners.get();
        int limit = -1;
        
        if(MAIN != null) {
            limit = MAIN.getSettings().getEntityStackLimit();
        }
        
        return limit;
    }
    
    /**
     * Returns the limit of spawners in a stack.
     * Returns -1 if the plugin hasn't been enabled yet.
     * @return
     */
    
    public static int getSpawnerStackLimit() {
        // The plugin's main instance.
        final USpawners MAIN = USpawners.get();
        int limit = -1;
        
        if(MAIN != null) {
            limit = MAIN.getSettings().getSpawnerStackLimit();
        }
        
        return limit;
    }
    
    /**
     * Returns whether or not stacked mobs are passive.
     * @return
     */
    
    public static boolean areStackedMobsPassive() {
        // The plugin's main instance.
        final USpawners MAIN = USpawners.get();
        boolean passive = false;
        
        if(MAIN != null) {
            passive = MAIN.getSettings().areSpawnerMobsPassive();
        }
        
        return passive;
    }
    
    /**
     * Returns whether or not stacked health is used on stacked mobs.
     * @return
     */
    
    public static boolean doesUseStackedHealth() {
        // The plugin's main instance.
        final USpawners MAIN = USpawners.get();
        boolean stackedH = false;
        
        if(MAIN != null) {
            stackedH = MAIN.getSettings().doesUseStackedHealth();
        }
        
        return stackedH;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////*
    
}
