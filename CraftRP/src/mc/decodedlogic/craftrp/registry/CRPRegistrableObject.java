package mc.decodedlogic.craftrp.registry;

public interface CRPRegistrableObject {
    
    public void onRegistered(long assignedID);
    
    public void onUnregistered();
    
    public default boolean isRegistered() {
        return getID() != -1;
    }
    
    public long getID();
    
}
