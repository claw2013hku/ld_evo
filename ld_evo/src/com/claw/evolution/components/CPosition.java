package com.claw.evolution.components;

/**
 * Example of a single component - a 2D position component with an x and a y
 * 
 * Note: all fields should be public, so you can direct-access them!
 * 
 * c.f. http://entity-systems.wikidot.com/rdbms-with-code-in-systems
 */
public class CPosition extends AComponent
{
	/**x and y in pixels**/
	public float x, y;
	
	public CPosition()
	{
		reset();
	}

	@Override
	public void reset() {
		x = 0;
		y = 0;
	}
}