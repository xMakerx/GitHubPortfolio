package mc.decodedlogic.craftrp.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.NotImplementedException;

import static mc.decodedlogic.craftrp.util.string.RPString.isEmptyString;

public abstract class RPVariable implements Denominable {
    
    protected final String NAME;
    protected final boolean IS_GLOBAL;
    
    protected List<String> desc;
    protected List<RPVariableInstance> instances;
    
    public RPVariable(String name) {
        if(isEmptyString(name)) throw new NullPointerException("Variable name cannot be null!");
        this.NAME = name;
        this.IS_GLOBAL = false;
        this.desc = new ArrayList<String>();
        this.instances = new ArrayList<RPVariableInstance>();
    }
    
    /**
     * This cannot be used.
     */
    
    public void setName(String newName) { throw new NotImplementedException("Variable names are final!"); }
    
    @Override
    public String getName() {
        return this.NAME;
    }
    
    @Override
    public void setDescription(List<String> newDesc) {
        this.desc = newDesc;
    }
    
    @Override
    public List<String> getDescription() {
        return this.desc;
    }

}
