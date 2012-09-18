package com.claw.evolution.components;

import com.artemis.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IdentityMap;

public class CWorldActorPositionMap extends AComponent {
	public IdentityMap<Entity, Vector2> map = new IdentityMap<Entity, Vector2>();

	@Override
	public void reset() {
		map.clear();
	}
}
