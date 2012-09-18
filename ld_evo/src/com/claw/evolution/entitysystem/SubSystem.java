package com.claw.evolution.entitysystem;

import com.claw.evolution.events.IEvent;

/**
 * Standard design: c.f. http://entity-systems.wikidot.com/rdbms-with-code-in-systems
 */

public interface SubSystem
{
	public void processOneGameTick( float ms );
    
    /**
	 * Mostly used for debugging - check which system is firing, what order
	 * systems are firing in, etc
	 * 
	 * @return the human-readable name of this system
	 */
	public String getSimpleName();
	public void dispose();
	
	/** true to consume event **/
	public boolean handleEvent(IEvent event);
}