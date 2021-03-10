package mc.decodedlogic.craftrp.util;

import java.util.List;

public interface Denominable {
    
    /**
     * The name of this object
     * @param The new name
     */
    
    public void setName(String newName);
    
    /**
     * Fetches the name of this object
     * @return
     */
    public String getName();
    
    /**
     * Sets the description of this object
     * @param description
     */
    
    public void setDescription(List<String> description);
    
    /**
     * Fetches the description of this object
     * @return
     */
    
    public List<String> getDescription();
}
