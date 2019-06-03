package com.coginvasion.stride.event;

import com.coginvasion.stride.barrier.Barrier;

public class BarrierDestroyedEvent extends BarrierEvent {
	
	private final boolean removed;
	
	public BarrierDestroyedEvent(final Barrier bar, final boolean removed) {
		super(bar);
		this.removed = removed;
	}
	
	public boolean wasRemoved() {
		return this.removed;
	}
}
