package online.coginvasion.spider;

import static online.coginvasion.spider.SpiderSolitaire.core;

public class Ratio {
	
	private final float A;
	private final boolean HORIZONTAL;
	
	public Ratio(float a, boolean isHorizontal) {
		this.A = a;
		this.HORIZONTAL = isHorizontal;
	}
	
	public float get() {
		float rel = (HORIZONTAL) ? core.getStage().getViewport().getWorldWidth() : core.getStage().getViewport().getWorldHeight();
		return rel * A;
	}
	
}
