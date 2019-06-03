package org.xmakerx.raidpractice.arena;

public class ArenaFlag {
	
	private final String[] aliases;
	protected Object value;
	
	public ArenaFlag(final Object defaultValue, final String[] aliases) {
		this.aliases = aliases;
		this.value = defaultValue;
	}
	
	public void setValue(final Object value) {
		this.value = value;
	}
	
	public Object getValue() {
		return this.value;
	}
	
	public boolean isAlias(final String operation) {
		for(String alias : aliases) {
			if(operation.equalsIgnoreCase(alias)) {
				return true;
			}
		}
		return false;
	}
	
	public String[] getAliases() {
		return this.aliases;
	}
}
