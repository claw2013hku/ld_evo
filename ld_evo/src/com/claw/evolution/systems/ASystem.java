package com.claw.evolution.systems;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.claw.evolution.events.IEvent;

public abstract class ASystem extends EntityProcessingSystem implements IDisposable, IEventHandler {
	long startFrameMS = 0;
	long frameTime = -1;
	
	public abstract String getName();
	
	public long getFrameTime()
	{
		return frameTime;
	}
	
	protected ASystem(Aspect aspect) {
		super(aspect);
	}

	@Override
	public abstract boolean handleEvent(IEvent event);

	@Override
	public abstract void dispose();

	@Override
	protected void process(Entity e) {
	}

	@Override
	protected void begin() {
		startFrameMS = System.currentTimeMillis();
	}

	@Override
	protected void end() {
		frameTime = System.currentTimeMillis() - startFrameMS;
	}
}
