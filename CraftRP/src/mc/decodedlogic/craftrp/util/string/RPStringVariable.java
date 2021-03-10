package mc.decodedlogic.craftrp.util.string;

import java.util.ArrayList;
import java.util.List;

import mc.decodedlogic.craftrp.util.RPVariable;
import mc.decodedlogic.craftrp.util.RPVariableInstance;

public class RPStringVariable extends RPVariable {
    
    protected final RPString PARENT;
    
    protected List<RPVariableInstance> instances;
    
    protected final boolean IS_GLOBAL;
    
    protected String value;
    
    public RPStringVariable(RPString parent, String varName, int varBeginIndex, int varEndIndex) {
        super(varName);
        if(parent == null) throw new NullPointerException("RPString cannot be null!");
        this.PARENT = parent;
        this.instances = new ArrayList<RPVariableInstance>();
        this.instances.add(new RPVariableInstance(varBeginIndex, varEndIndex));
        this.IS_GLOBAL = false;
        
        // This is the value of this variable.
        this.value = null;
    }
    
    /**
     * Sets the value of this variable.
     * @param newValue
     */

    public void setValue(String newValue) {
        this.value = newValue;
    }
    
    /**
     * Gets the value of this variable.
     * @return
     */
    
    public String getValue() {
        return this.value;
    }
    
    public RPString getParent() {
        return this.PARENT;
    }
    
    public String getName() {
        return this.NAME;
    }
    
    /**
     * Returns whether or not this variable is global. If it's a global variable then that means that the variable
     * is always replaced.
     * @return
     */
    
    public boolean isGlobal() {
        return this.IS_GLOBAL;
    }
    
}