package mc.decodedlogic.craftrp.ownable;

import java.util.HashSet;

public interface Owner {
    
    public String getName();
    
    public default void addProperty(Property property) {
        if(property != null) getProperties().add(property);
    }
    
    public default void removeProperty(Property property) {
        if(property != null) getProperties().remove(property);
    }
    
    public HashSet<Property> getProperties();
}
