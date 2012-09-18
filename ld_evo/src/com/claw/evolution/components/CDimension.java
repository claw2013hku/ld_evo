package com.claw.evolution.components;

public class CDimension extends AComponent {
	/**width in pixels**/
	public float width;
	/**height in pixels**/
	public float height;
	/**rotation in degrees**/
	public float rotation;
	
	public CDimension()
	{
		reset();
	}

	@Override
	public void reset() {
		width = 0;
		height = 0;
		rotation = 0;
	}
}
