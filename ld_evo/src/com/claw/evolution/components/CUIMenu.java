package com.claw.evolution.components;

public class CUIMenu extends AComponent {
	public enum MenuType {IN_GAME_LAUNCHED, IN_GAME_MENU, MAIN}
	public MenuType type;
	public boolean shown;
	public CUIMenu()
	{
		reset();
	}
	@Override
	public void reset() {
		type = MenuType.IN_GAME_LAUNCHED;
		shown = false;
	}
}
