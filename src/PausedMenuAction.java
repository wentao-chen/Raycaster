	
enum PausedMenuAction {
	RESUME("Resume", true), NEW_GAME("New Game", null), NEW_SERVER("New Server", false), FIND_SERVERS("Find Servers", null), CREATE_ITEM("Create Item", false), SETTINGS("Settings", null), QUIT("Quit", null);
	
	private final String NAME;
	private final Boolean SHOW_IFF_GAME_RUNNING;
	
	PausedMenuAction(String name, Boolean showIffGameRunning) {
		NAME = name;
		SHOW_IFF_GAME_RUNNING = showIffGameRunning;
	}
	
	public String getName() {
		return NAME;
	}
	
	public Boolean showIffGameRunning() {
		return SHOW_IFF_GAME_RUNNING;
	}
}
