package com.claw.evolution.components;

import com.badlogic.gdx.scenes.scene2d.Stage;

public class CUI extends AComponent{
	public Stage stage;
	public CUI()
	{
		stage = null;
	}
	
	public void reset()
	{
		stage = null;
	}
}
