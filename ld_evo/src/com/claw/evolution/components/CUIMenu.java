package com.claw.evolution.components;

public class CUIMenu extends AComponent {
	//Add / modify MenuTypes, each type of menu is unique i.e. not allowed to have 2 main menus, instead derive more menu types e.g. SUB_MENU.
	public enum MenuType {IN_GAME_LAUNCHED, IN_GAME_MENU, MAIN, SAMPLE}
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
