package com.claw.evolution.components;

import com.artemis.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public abstract class AComponent extends Component implements Poolable{
	@Override
	public void reset(){}
}
