package com.claw.evolution.events;

public class ButtonEvent implements IEvent {
	public enum ButtonType{RESTART, EXIT, RESUME, IN_GAME_MENU, INVALID, DEBUG, ADD_PLAYER, REMOVE_PLAYER, START, START_OPENING}
	public ButtonType type = ButtonType.INVALID;
	
	@Override
	public void reset() {
		type = ButtonType.INVALID;
	}
}
