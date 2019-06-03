package com.coginvasion.stride.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.coginvasion.stride.barrier.Barrier;

public abstract class BarrierEvent extends Event {
	
	private final Barrier barrier;
	private static final HandlerList handlers = new HandlerList();
	
	public BarrierEvent(final Barrier bar) {
		this.barrier = bar;
	}
	
	public Barrier getBarrier() {
		return this.barrier;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
        return handlers;
    }

}
