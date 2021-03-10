package mc.decodedlogic.skybattlesuspawners.logging;

import java.util.UUID;

import javax.annotation.Nonnull;

import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerType;
import mc.decodedlogic.skybattlesuspawners.spawner.SpawnerUpgrade;

public class Transaction extends Record {
    
    ////////////////////////////////////////////////////////////////////////////////////////
    // Static methods, variables, etc
    ////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Spawners were deposited into a MobSpawner stack.
     * {@link getPurchasedUpgrade()} = NULL.
     * {@link getDelta()} = # of spawners deposited.
     */
    
    public static final int SPAWNER_DEPOSIT = 0;
    
    /**
     * Spawners were withdrawn from a MobSpawner stack.
     * {@link getPurchasedUpgrade()} = NULL.
     * {@link getDelta()} = # of spawners withdrawn.
     */
    
    public static final int SPAWNER_WITHDRAW = 1;
    
    /**
     * A spawner upgrade transaction was completed.
     * {@link getPurchasedUpgrade()} != NULL.
     * {@link getDelta()} = # of spawners upgraded. Contributes to cost.
     */
    
    public static final int SPAWNER_UPGRADE = 2;
    
    /**
     * A spawner was reset.
     * {@link getPurchasedUpgrade()} != NULL.
     * {@link getDelta()} = 0
     */
    
    public static final int SPAWNER_RESET = 3;
    
    public static Transaction from(@Nonnull String data) {
        if(data == null || (data != null && data.isEmpty())) return null;
        
        Transaction t = null;
        
        try {
            String[] chunks = data.split(",");
            chunks[0] = chunks[0].substring(2);
            long timestamp = Long.parseLong(chunks[0]);
            int type = Integer.parseInt(chunks[1]);
            double cost = Double.parseDouble(chunks[2]);
            int delta = Integer.parseInt(chunks[3]);
            
            String uuidSect = chunks[chunks.length-1];
            String uuid = uuidSect.substring(0, uuidSect.length()-1);
            
            String[] uS = chunks[4].split(":");
            SpawnerUpgrade upgrade = null;
            int upgradeIndex = Integer.parseInt(uS[0]);
            short upgradeData = Short.parseShort(uS[1]);
            
            if(upgradeIndex != -1) {
                for(SpawnerType sType : SpawnerType.values()) {
                    if(sType.getData() == upgradeData) {
                        upgrade = sType.getUpgrades().get(upgradeIndex);
                        break;
                    }
                }
            }
            
            t = new Transaction(uuid, type, cost, upgrade, delta, timestamp);
        } catch (Exception e) {
            System.out.println("An error occurred while processing Transaction data.");
            e.printStackTrace();
        }
        
        return t;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////
    
    private final int TYPE;
    private final double COST;
    
    // The upgrade purchased if one was purchased.
    private final SpawnerUpgrade UPGRADE;
    
    // Variable that could equal how many spawners deposited, withdrew, or upgraded.
    private final int DELTA;
    
    public Transaction(@Nonnull UUID user, int type, double cost, int delta) {
        super(user, 'T');
        this.TYPE = type;
        this.COST = cost;
        this.DELTA = delta;
        this.UPGRADE = null;
    }
    
    public Transaction(String user, int type, double cost, int delta, long timestamp) {
        super(user, timestamp, 'T');
        this.TYPE = type;
        this.COST = cost;
        this.DELTA = delta;
        this.UPGRADE = null;
    }
    
    public Transaction(UUID user, int type, double cost, @Nonnull SpawnerUpgrade upgrade, int delta) {
        super(user, 'T');
        this.TYPE = type;
        this.COST = cost;
        this.DELTA = delta;
        this.UPGRADE = upgrade;
    }
    
    public Transaction(String user, int type, double cost, @Nonnull SpawnerUpgrade upgrade, int delta, long timestamp) {
        super(user, timestamp, 'T');
        this.TYPE = type;
        this.COST = cost;
        this.DELTA = delta;
        this.UPGRADE = upgrade;
    }
    
    /**
     * The type of transaction completed. See {@link SPAWNER_DEPOSIT}, {@link SPAWNER_WITHDRAW}, {@link SPAWNER_UPGRADE}.
     * @return
     */
    
    public int getType() {
        return this.TYPE;
    }
    
    /**
     * How much the transaction cost.
     * @return
     */
    
    public double getCost() {
        return this.COST;
    }
    
    /**
     * Fetches the {@link SpawnerUpgrade} that was purchased if one was purchased.
     * Returns NULL if an upgrade wasn't purchased.
     * 
     * NOTE: NULL does <b>NOT</b> mean DEFAULT. NULL = NULL, NO UPGRADE PURCHASED. 
     * See {@link SpawnerUpgrade.DEFAULT} if you think DEFAULT = NULL.
     * @return
     */
    
    public SpawnerUpgrade getPurchasedUpgrade() {
        return this.UPGRADE;
    }
    
    /**
     * This variable could hold how many spawners were deposited, withdrew, or upgraded.
     * Depends on the transaction that took place.
     * @return
     */
    
    public int getDelta() {
        return this.DELTA;
    }
    
    /**
     * Converts to a string in the following format:
     * 
     * T at the beginning indicates its a transaction.
     * 
     * T{TIMESTAMP (in milliseconds), TYPE (int, see {@link getType()}), COST (double), 
     * DELTA (int, see {@link getDelta()}), 
     * UPGRADE INDEX:UPGRADE TYPE DATA (int, see {@link SpawnerUpgrade#getIndex()} returns -1 if none purchased.)}
     * 
     * Upgrade Type Data is {@link SpawnerType#getData()}
     */
    
    public String toString() {
        int upgradeIndex = (UPGRADE != null) ? UPGRADE.getIndex() : -1;
        int upgradeType = (UPGRADE != null) ? UPGRADE.getSpawnerType().getData() : -1;
        String uniqueData = String.format("%s,%s,%s,%s:%s", TYPE, COST, DELTA, upgradeIndex, upgradeType);
        
        return super.toString(uniqueData);
    }
    
}
