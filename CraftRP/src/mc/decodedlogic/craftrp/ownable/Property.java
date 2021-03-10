package mc.decodedlogic.craftrp.ownable;

import java.util.List;
import java.util.stream.Collectors;

import lib.decodedlogic.core.internal.MethodResult;
import mc.decodedlogic.craftrp.job.Job;
import mc.decodedlogic.craftrp.player.CRPPlayer;

public interface Property {
    
    // These integers are returned as output
    public static final int ALREADY_OWNER = 0;
    public static final int OWNER_ADDED = 1;
    public static final int OWNER_REMOVED = 2;
    public static final int TOO_MANY_OWNERS = 3;
    public static final int ACTION_DISALLOWED = 4;
    
    public void onOwnerAdded(Owner owner);
    public void onOwnerRemoved(Owner owner);
    
    public default boolean canAddOwner(Owner owner) {
        return true;
    }
    
    public default boolean canRemoveOwner(Owner owner) {
        return true;
    }
    
    /**
     * Attempts to add the specified {@link Owner} to this property.
     * @param owner - The owner to attempt to add.
     * @return MethodResult with an output value containing an integer
     * that specifies what has occurred. See integers declared as final ints.
     * 
     * Calls {@link #onOwnerAdded(Owner)} upon successful add.
     */
    
    public default MethodResult addOwner(Owner owner) {
        return modifyOwner(owner, true);
    }
    
    /**
     * Attempts to remove the specified {@link Owner} from this property.
     * @param owner - The owner to attempt to remove.
     * @return MethodResult with an output value containing an integer
     * that specifies what has occurred. See integers declared as final ints.
     * 
     * Calls {@link #onOwnerRemove(Owner)} upon successful add.
     */
    
    public default MethodResult removeOwner(Owner owner) {
        return modifyOwner(owner, false);
    }
    
    /**
     * Modifies the specified {@link Owner} by either adding or removing them
     * based on the boolean given.
     * @param owner - The owner to modify
     * @param isAdd - Whether or not to add the owner to the list of owners.
     * @return
     */
    
    public default MethodResult modifyOwner(Owner owner, boolean isAdd) {
        List<Owner> owners = getOwners();
        
        MethodResult result = new MethodResult();
        result.setSuccessful(false);
        
        if(owner != null) {
            boolean alreadyOwns = isOwner(owner);
            
            if(alreadyOwns) {
                
                if(!isAdd) {
                    if(!canRemoveOwner(owner)) {
                        result.setOutput(ACTION_DISALLOWED);
                        return result;
                    }
                    
                    // Remove the owner
                    owners.remove(owner);
                    result.setOutput(OWNER_REMOVED);
                    result.setSuccessful(true);
                    owner.removeProperty(this);
                    onOwnerRemoved(owner);
                }else {
                    result.setOutput(ALREADY_OWNER);
                }
                
                return result;
            }
            
            if(isAdd && !canAddAnotherOwner()) {
                result.setOutput(TOO_MANY_OWNERS);
                return result;
            }
            
            if(isAdd) {
                
                if(!canAddOwner(owner)) {
                    result.setOutput(ACTION_DISALLOWED);
                    return result;
                }
                
                owners.add(owner);
                result.setOutput(OWNER_ADDED);
                result.setSuccessful(true);
                owner.addProperty(this);
                onOwnerAdded(owner);
            }
        }
        
        return result;
    }
    
    public default void removeAllOwners() {
        List<Owner> owners = getOwners();
        
        for(Owner owner : owners) {
            owner.removeProperty(this);
            onOwnerRemoved(owner);
        }
        
        owners.clear();
    }
    
    public default boolean isOwner(Owner owner) {
        List<Owner> owners = getOwners();
        
        if(owner == null) return false;
        
        if(owners.contains(owner)) return true;
        
        if(owner instanceof CRPPlayer) {
            List<Owner> ownedByJob = owners.stream().filter(o -> o instanceof Job).collect(Collectors.toList());
            CRPPlayer p = (CRPPlayer) owner;
            
            for(Owner org : ownedByJob) {
                Job job = (Job) org;
                
                if(p.getJob() == job) return true;
            }
        }
        
        return false;
    }
    
    public default boolean canAddAnotherOwner() {
        List<Owner> owners = getOwners();
        int maxOwners = getMaximumOwners();
        return (maxOwners == -1 || (owners.size() < maxOwners));
    }
    
    public void setMaximumOwners(int newMaxOwners);
    public int getMaximumOwners();
    
    public List<Owner> getOwners();
    
    public PropertyType getPropertyType();
}
