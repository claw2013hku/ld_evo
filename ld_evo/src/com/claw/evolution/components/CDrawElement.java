package com.claw.evolution.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pools;

public class CDrawElement extends AComponent{
	public TextureRegion textureRegion;
	//public TextureRegion derivedRegion;
	public float originX, originY;
	public float scaleX, scaleY;
	
	public CDrawElement()
	{
		textureRegion = null;
		originX = 0;
		originY = 0;
		scaleX = 1;
		scaleY = 1;
	}
	
	public CDrawElement setOriginFromTextureRegion()
	{
		originX = textureRegion.getRegionWidth() / 2;
		originY = textureRegion.getRegionHeight() / 2;
		return this;
	}

	@Override
	public void reset() {
		if(textureRegion != null)
		{
			Pools.free(textureRegion);
		}
		originX = 0;
		originY = 0;
		scaleX = 1;
		scaleY = 1;
	}
}
