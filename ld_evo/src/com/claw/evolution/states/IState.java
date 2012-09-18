package com.claw.evolution.states;

public interface IState {
	public void changeTo(IState nextState);
	public void changeFrom(IState prevState);
}
