package com.claw.evolution.components;

public class CWorldActor extends AComponent {
	public enum Type{PLAYER, OTHERS, LAST}
	public Type type;
	public CWorldActor()
	{
		reset();
	}
	
	@Override
	public void reset() {
		type = Type.PLAYER;
	}
}
