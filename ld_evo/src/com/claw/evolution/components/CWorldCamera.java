package com.claw.evolution.components;

import com.badlogic.gdx.graphics.Camera;

public class CWorldCamera extends AComponent{
	public Camera camera;
	
	public CWorldCamera()
	{
		camera = null;
	}
	
	public void reset()
	{
		camera = null;
	}
}
