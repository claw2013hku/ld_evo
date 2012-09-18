package com.claw.evolution.systems;

import com.claw.evolution.events.IEvent;

public interface IEventHandler {
	boolean handleEvent(IEvent event);
}
