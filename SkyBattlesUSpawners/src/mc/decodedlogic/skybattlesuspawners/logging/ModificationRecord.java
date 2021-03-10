package mc.decodedlogic.skybattlesuspawners.logging;

import java.util.UUID;

import javax.annotation.Nonnull;

public class ModificationRecord extends Record {
    
    public static ModificationRecord from(@Nonnull String data) {
        ModificationRecord record = null;

        try {
            char prefix = data.charAt(0);
            boolean isPlace = (prefix == 'P') ? true : false;
            
            String[] split = data.split(",");
            split[0] = split[0].substring(2);
            
            String uuidSect = split[split.length-1];
            String uuid = uuidSect.substring(0, uuidSect.length()-1);
            long timestamp = Long.parseLong(split[0]);
            
            record = new ModificationRecord(uuid, timestamp, isPlace);
        } catch (Exception e) {
            System.out.println("Error processing ModificationRecord data.");
            e.printStackTrace();
        }
        
        return record;
    }
    
    
    protected boolean isPlace;
    
    public ModificationRecord(@Nonnull String uuid, long timestamp, boolean isPlace) {
        super(uuid, timestamp, (isPlace) ? 'P' : 'D');
        this.isPlace = isPlace;
    }
    
    public ModificationRecord(@Nonnull UUID uuid, boolean isPlace) {
        super(uuid, (isPlace) ? 'P' : 'D');
        this.isPlace = isPlace;
    }
    
    public ModificationRecord(boolean isPlace) {
        super("server", System.currentTimeMillis(), (isPlace) ? 'P' : 'D');
        this.isPlace = isPlace;
    }
    
    /**
     * P prefix means placed, D prefix means destroyed.
     */
    
    public String toString() {
        return super.toString("CHANGE");
    }

}
