package com.claw.evolution.components;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class CUIActor extends AComponent implements Comparable<CUIActor> {
	public Actor actor;
	public int zOrder;
	
	public CUIActor()
	{
		actor = null;
		zOrder = 0;
	}
	
	@Override
	public void reset() {
		actor = null;
		zOrder = 0;
	}
	
	@Override
	public int compareTo(CUIActor arg0) {
		return zOrder - arg0.zOrder;
	}
}
