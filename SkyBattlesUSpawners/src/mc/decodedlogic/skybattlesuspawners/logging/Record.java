package mc.decodedlogic.skybattlesuspawners.logging;

import java.util.UUID;

import javax.annotation.Nonnull;

public abstract class Record {
    
    protected final char PREFIX;
    protected final String UUID_USER;
    protected final long TIMESTAMP;
    
    public Record(@Nonnull String uuid, long timestamp, char prefix) {
        this.UUID_USER = uuid;
        this.TIMESTAMP = timestamp;
        this.PREFIX = prefix;
    }
    
    public Record(@Nonnull UUID uuid, char prefix) {
        this.UUID_USER = uuid.toString();
        this.TIMESTAMP = System.currentTimeMillis();
        this.PREFIX = prefix;
    }
    
    /**
     * Fetches the prefix of this record type.
     * @return
     */
    
    public char getPrefix() {
        return this.PREFIX;
    }
    
    public UUID getUUID() {
        if(UUID_USER.equalsIgnoreCase("server")) {
            return null;
        }
        
        return UUID.fromString(UUID_USER);
    }
    
    /**
     * Fetches when this occurred in milliseconds.
     * See {@link System#currentTimeMillis()}
     * @return
     */
    
    public long getTimestamp() {
        return this.TIMESTAMP;
    }
    
    public final String toString(@Nonnull String uniqueData) {
        return String.format("%s{%s,%s,%s}", PREFIX, TIMESTAMP, uniqueData, UUID_USER);
    }
    
    public String toString() {
        throw new NullPointerException("You must provide unique data from a record!");
    }
    
}
