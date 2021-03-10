package mc.decodedlogic.craftrp.util.string;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RPString {
    
    public static final char VARIABLE_DELIMITER = '%';
    public static final char IGNORE = '\\';
    
    // The builder that spits out final string
    protected StringBuilder sb;
    protected Context ctxt;
    
    protected List<RPStringVariable> vars;
    
    public RPString(String msg) {
        this.vars = new ArrayList<RPStringVariable>();
        this.parse(msg);
    }
    
    private void parse(String msg) {
        if(isEmptyString(msg)) throw new NullPointerException("RPString cannot be null!");
        
        char lastChar = 0;
        
        // For handling variables
        StringBuilder vNameB = null;
        int vStartIndex = -1;
        int vEndIndex = -1;
        
        for(int i = 0; i < msg.length(); i++) {
            char c = msg.toCharArray()[i];
            
            if(lastChar != IGNORE) {
                
                if(c == VARIABLE_DELIMITER) {
                    // This is either where a variable begins or ends.
                    if(vNameB == null) {
                        // This is the beginning of a new variable.
                        vStartIndex = i;
                        vNameB = new StringBuilder();
                    }else {
                        // This is where a variable ends.
                        vEndIndex = i;
                        
                        String varName = vNameB.toString();
                        
                        // Let's verify that the discovered variable name isn't
                        // duplicated.
                        if(getVariableByName(varName) == null) {
                            // Great, let's add the variable!
                            RPStringVariable var = new RPStringVariable(this, varName, vStartIndex, vEndIndex);
                            vars.add(var);
                        }else {
                            throw new IllegalArgumentException("Variable name already in the string!");
                        }
                    }
                }
                
                if(c == VARIABLE_DELIMITER && vNameB == null) {
                    vStartIndex = i;
                    vNameB = new StringBuilder();
                }else if(vNameB != null) {
                    vNameB.append(c);
                }
            }
            
            lastChar = c;
        }
    }
    
    private RPStringVariable getVariableByName(String varName) {
        if(isEmptyString(varName)) throw new NullPointerException("Variable name cannot be null!");
        
        for(RPStringVariable var : vars) {
            if(var.getName().equalsIgnoreCase(varName)) {
                return var;
            }
        }
        
        return null;
    }
    
    public static final boolean isEmptyString(String str) {
        return (str == null || (str != null && str.isEmpty()));
    }
}
