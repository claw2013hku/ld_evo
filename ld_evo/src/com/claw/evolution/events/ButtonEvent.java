package com.claw.evolution.events;

public class ButtonEvent implements IEvent {
	//Add / modify button types, note that button events should better be fired by buttons only.
	public enum ButtonType{RESTART, EXIT, RESUME, IN_GAME_MENU, INVALID, DEBUG, ADD_PLAYER, REMOVE_PLAYER, START, START_OPENING, SAMPLE, SAMPLE2}
	public ButtonType type = ButtonType.INVALID;
	
	@Override
	public void reset() {
		type = ButtonType.INVALID;
	}
}
