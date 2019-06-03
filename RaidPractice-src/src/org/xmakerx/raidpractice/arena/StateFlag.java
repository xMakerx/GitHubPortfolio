package org.xmakerx.raidpractice.arena;

public class StateFlag extends ArenaFlag {

	public StateFlag(String[] aliases, boolean defaultValue) {
		super(defaultValue, aliases);
	}
	
	public void setValue(boolean flag) {
		this.value = flag;
	}
}
