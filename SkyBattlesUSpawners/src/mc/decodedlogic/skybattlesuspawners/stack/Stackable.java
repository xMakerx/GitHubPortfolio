package mc.decodedlogic.skybattlesuspawners.stack;

import mc.decodedlogic.skybattlesuspawners.USpawners;

public abstract class Stackable {
    
    public static final String METADATA_KEY_NAME = "StackableData";
    public static final int DEFAULT_MAX_STACK = 999;
    public static double stackRadius = 0.0d;
    
    public static double getStackRadius() {
        if(stackRadius == 0.0d) {
            stackRadius = USpawners.get().getSettings().getStackRadius();
        }
        
        return stackRadius;
    }
	
	protected int size = 1;
	protected int maxSize = DEFAULT_MAX_STACK;
	protected int id;
	
	/**
	 * Instantiates a new Stackable object with a size of 1
	 * and a max size equal to DEFAULT_MAX_STACK
	 */
	
	public Stackable() {
	    this(1, DEFAULT_MAX_STACK);
	    this.id = -1;
	}
	
	/**
	 * Instantiates a new Stackable object with the specified size
	 * and maximum size.
	 * @param size - The size to start with.
	 * @param maxSize - The maximum size allowed by this stack.
	 */
	
	public Stackable(int size, int maxSize) {
        this.maxSize = maxSize;
	    this.size = size;
	    this.id = -1;
	}
	
	public void sizeChanged(int oldSize, int newSize) {}
	public void maxSizeChanged(int oldMaxSize, int newMaxSize) {}
	
    /**
     * Sets the new maximum size of this stack. If 'newMaxSize' is less
     * than the current max size, it will adjust the current stack size to
     * fit within those constraints.
     * @param newMaxSize - The new maximum size for this stack.
     */
    
    public void setMaxSize(int newMaxSize) {
        if(newMaxSize < maxSize) {
            setSize(newMaxSize);
        }
        
        this.maxSizeChanged(maxSize, newMaxSize);
        this.maxSize = newMaxSize;
    }
    
    /**
     * Fetches the maximum stack size.
     * @return
     */
    
    public int getMaxSize() {
        return this.maxSize;
    }
	
	/**
	 * Sets the size of this stack. If 'newSize' is greater than
	 * the maximum size, the maximum size will be used instead.
	 * @param newSize - The new size to set.
	 */
	
	public void setSize(int newSize) {
	    int oldSize = size;
	    this.size = (newSize <= maxSize) ? newSize : maxSize;
	    this.sizeChanged(oldSize, size);
	}
	
	/**
	 * Fetches the size of this stack
	 * @return
	 */
	
	public int getSize() {
	    return this.size;
	}
	
    /**
     * Fetches the size of this stack
     * @return
     */
	
	public int size() {
	    return getSize();
	}
	
	/**
	 * Returns whether or not this stack is empty
	 * @return
	 */
	
	public boolean empty() {
	    return this.size == 0;
	}
	
	/**
	 * Returns whether or not this stack is full.
	 * @return
	 */
	
	public boolean full() {
	    return this.size == this.maxSize;
	}
	
}
