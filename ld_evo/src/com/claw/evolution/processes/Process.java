package com.claw.evolution.processes;

import com.badlogic.gdx.utils.Pool.Poolable;

public abstract class Process implements Poolable{
	public Process next;
	public abstract void update(float ms);
	public abstract boolean isDead();
}
