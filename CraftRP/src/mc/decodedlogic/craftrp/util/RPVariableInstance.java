package mc.decodedlogic.craftrp.util;

import mc.decodedlogic.craftrp.util.string.RPString;

public class RPVariableInstance {
    
    // The index where the variable begins in the string.
    protected final int START_INDEX;
    
    // The index where the variable ends in the string.
    protected final int END_INDEX;
    
    public RPVariableInstance(int varStartIndex, int varEndIndex) {
        this.START_INDEX = varStartIndex;
        this.END_INDEX = varEndIndex;
    }
    
    /**
     * The index of the first character indicating this variable's section of the main string.
     * This would be the variable delimiter. See: {@link RPString#VARIABLE_DELIMITER}
     * @return (0 <= n < PARENT STRING LENGTH)
     */
    
    public int getStartIndex() {
        return this.START_INDEX;
    }
    
    /**
     * The index of the last character indicating this variable's section of the main string.
     * This would be the variable delimiter. See: {@link RPString#VARIABLE_DELIMITER}
     * @return (0 <= n < PARENT STRING LENGTH)
     */
    
    public int getEndIndex() {
        return this.END_INDEX;
    }

}