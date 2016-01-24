import java.awt.Color;
import java.util.ArrayList;


public enum DefaultMap implements StoreLocator, SpawnLocator, BombSiteLocator {
	ICE_WORLD("Fy Iceworld", new DefaultPlayerCharacteristics(4.5, 0.9, 0.1), new Map.InitialDroppedWeapon[] {
			new Map.InitialDroppedWeapon(20.5, 2.5, 0d, BasicPrimaryGun.DefaultSubmachineGuns.P90.createGun()),
			new Map.InitialDroppedWeapon(6.5, 28.5, 0d, BasicPrimaryGun.DefaultSubmachineGuns.P90.createGun()),
			new Map.InitialDroppedWeapon(20.5, 4.5, 0d, BasicPrimaryGun.DefaultSubmachineGuns.MAC10.createGun()),
			new Map.InitialDroppedWeapon(6.5, 26.5, 0d, BasicPrimaryGun.DefaultSubmachineGuns.MAC10.createGun()),
			new Map.InitialDroppedWeapon(18.5, 4.5, 0d, BasicPrimaryGun.DefaultRifles.AWP.createGun()),
			new Map.InitialDroppedWeapon(8.5, 26.5, 0d, BasicPrimaryGun.DefaultRifles.AWP.createGun()),
			new Map.InitialDroppedWeapon(18.5, 2.5, 0d, BasicPrimaryGun.DefaultRifles.M4A1.createGun()),
			new Map.InitialDroppedWeapon(8.5, 28.5, 0d, BasicPrimaryGun.DefaultRifles.M4A1.createGun()),
			new Map.InitialDroppedWeapon(16.5, 4.5, 0d, BasicPrimaryGun.DefaultMachineGuns.M249.createGun()),
			new Map.InitialDroppedWeapon(10.5, 26.5, 0d, BasicPrimaryGun.DefaultMachineGuns.M249.createGun()),
			new Map.InitialDroppedWeapon(16.5, 2.5, 0d, BasicPrimaryGun.DefaultRifles.AUG.createGun()),
			new Map.InitialDroppedWeapon(10.5, 28.5, 0d, BasicPrimaryGun.DefaultRifles.AUG.createGun()),
			new Map.InitialDroppedWeapon(10.5, 4.5, 0d, BasicPrimaryGun.DefaultShotguns.XM1014.createGun()),
			new Map.InitialDroppedWeapon(16.5, 26.5, 0d, BasicPrimaryGun.DefaultShotguns.XM1014.createGun()),
			new Map.InitialDroppedWeapon(10.5, 2.5, 0d, BasicPrimaryGun.DefaultRifles.KRIEG552.createGun()),
			new Map.InitialDroppedWeapon(16.5, 28.5, 0d, BasicPrimaryGun.DefaultRifles.KRIEG552.createGun()),
			new Map.InitialDroppedWeapon(8.5, 4.5, 0d, BasicPrimaryGun.DefaultShotguns.M3.createGun()),
			new Map.InitialDroppedWeapon(18.5, 26.5, 0d, BasicPrimaryGun.DefaultShotguns.M3.createGun()),
			new Map.InitialDroppedWeapon(8.5, 2.5, 0d, BasicPrimaryGun.DefaultRifles.AK47.createGun()),
			new Map.InitialDroppedWeapon(18.5, 28.5, 0d, BasicPrimaryGun.DefaultRifles.AK47.createGun()),
			new Map.InitialDroppedWeapon(6.5, 4.5, 0d, BasicPrimaryGun.DefaultSubmachineGuns.TMP.createGun()),
			new Map.InitialDroppedWeapon(20.5, 26.5, 0d, BasicPrimaryGun.DefaultSubmachineGuns.TMP.createGun()),
			new Map.InitialDroppedWeapon(6.5, 2.5, 0d, BasicPrimaryGun.DefaultSubmachineGuns.MP5.createGun()),
			new Map.InitialDroppedWeapon(20.5, 28.5, 0d, BasicPrimaryGun.DefaultSubmachineGuns.MP5.createGun()),
	}, new Map.InitialSpectatorViewLocation(1.5, 0.5, 2.5, Math.PI * 17 / 9, -Math.PI / 24),
	new Map.Point3D[] {
			new Map.Point3D(6.5, 1.5, 0), new Map.Point3D(8.5, 1.5, 0), new Map.Point3D(10.5, 1.5, 0), new Map.Point3D(16.5, 1.5, 0), new Map.Point3D(18.5, 1.5, 0), new Map.Point3D(20.5, 1.5, 0),
			new Map.Point3D(6.5, 3.5, 0), new Map.Point3D(8.5, 3.5, 0), new Map.Point3D(10.5, 3.5, 0), new Map.Point3D(16.5, 3.5, 0), new Map.Point3D(18.5, 3.5, 0), new Map.Point3D(20.5, 3.5, 0),
	}, new Map.Point3D[] {
			new Map.Point3D(6.5, 27.5, 0), new Map.Point3D(8.5, 27.5, 0), new Map.Point3D(10.5, 27.5, 0), new Map.Point3D(16.5, 27.5, 0), new Map.Point3D(18.5, 27.5, 0), new Map.Point3D(20.5, 27.5, 0),
			new Map.Point3D(6.5, 29.5, 0), new Map.Point3D(8.5, 29.5, 0), new Map.Point3D(10.5, 29.5, 0), new Map.Point3D(16.5, 29.5, 0), new Map.Point3D(18.5, 29.5, 0), new Map.Point3D(20.5, 29.5, 0),
	}),
	LAVA("Lava", new DefaultPlayerCharacteristics(3d, 0.7, 0.505), null, null, new Map.Point3D[] {
			new Map.Point3D(2.5, 2.5, 0), new Map.Point3D(4.5, 2.5, 0), new Map.Point3D(7.5, 2.5, 0), new Map.Point3D(9.5, 2.5, 0)
	}, new Map.Point3D[] {
			new Map.Point3D(2.5, 8.5, 0), null, new Map.Point3D(4.5, 8.5, 0), new Map.Point3D(7.5, 8.5, 0), new Map.Point3D(9.5, 8.5, 0)
	}), DE_DUST2("De Dust 2", new DefaultPlayerCharacteristics(5.5, 1.1, 0.5), null, 
	new Map.InitialSpectatorViewLocation(20, 8.5, 5.5, Math.PI * 16 / 9, -Math.PI / 12),
	new Map.Point3D[] {
			new Map.Point3D(23.5, 64.5, 0),
			new Map.Point3D(23.5, 61.5, 0),
			new Map.Point3D(26.5, 64.5, 0),
			new Map.Point3D(26.5, 61.5, 0),
			new Map.Point3D(29.5, 64.5, 0),
			new Map.Point3D(29.5, 61.5, 0),
			new Map.Point3D(32.5, 64.5, 0),
			new Map.Point3D(32.5, 61.5, 0),
			new Map.Point3D(35.5, 64.5, 0),
			new Map.Point3D(35.5, 61.5, 0),
			new Map.Point3D(38.5, 64.5, 0),
			new Map.Point3D(38.5, 61.5, 0),
			new Map.Point3D(41.5, 61.5, 0)
	}, new Map.Point3D[] {
			new Map.Point3D(38.5, 15.5, 0),
			new Map.Point3D(41.5, 15.5, 0),
			new Map.Point3D(44.5, 15.5, 0),
			new Map.Point3D(47.5, 15.5, 0),
			new Map.Point3D(50.5, 15.5, 0),
			new Map.Point3D(41.5, 12.5, 0),
			new Map.Point3D(44.5, 12.5, 0),
			new Map.Point3D(47.5, 12.5, 0),
			new Map.Point3D(50.5, 12.5, 0),
			new Map.Point3D(41.5, 9.5, 0),
			new Map.Point3D(44.5, 9.5, 0),
			new Map.Point3D(47.5, 9.5, 0),
			new Map.Point3D(50.5, 9.5, 0)
	}), CS_ASSAULT("CS Assault", new DefaultPlayerCharacteristics(10d, 0.54, 0.36), null, null, new Map.Point3D[]{new Map.Point3D(13, 1, 0)}, new Map.Point3D[]{new Map.Point3D(15, 1, 0)
	}), ZOMBIELAND_RANDOM("Zombieland (Random)", new DefaultPlayerCharacteristics(3d, 1d, 0.1), null, null, new Map.Point3D[]{}, new Map.Point3D[]{});
	
	private final String NAME;
	private final DefaultPlayerCharacteristics DEFAULT_PLAYER_CHARACTERISTICS;
	private final Map.InitialDroppedWeapon[] INITIAL_DROPPED_WEAPONS;
	private final Map.InitialSpectatorViewLocation INITIAL_SPECTATOR_VIEW_LOCATION;
	private final Map.Point3D[] TERRORISTS_SPAWN;
	private final Map.Point3D[] COUNTER_TERRORISTS_SPAWN;
	
	DefaultMap(String name, DefaultPlayerCharacteristics defaultPlayerCharacteristics, Map.InitialDroppedWeapon[] initialDroppedWeapons, Map.InitialSpectatorViewLocation initialSpectatorViewLocation, Map.Point3D[] terroristsSpawn, Map.Point3D[] counterTerroristsSpawn) {
		NAME = name;
		DEFAULT_PLAYER_CHARACTERISTICS = defaultPlayerCharacteristics;
		INITIAL_DROPPED_WEAPONS = initialDroppedWeapons;
		INITIAL_SPECTATOR_VIEW_LOCATION = initialSpectatorViewLocation;
		TERRORISTS_SPAWN = terroristsSpawn;
		COUNTER_TERRORISTS_SPAWN = counterTerroristsSpawn;
	}
	
	public String getName() {
		return NAME;
	}
	
	public DefaultPlayerCharacteristics getDefaultPlayerCharacteristics() {
		return DEFAULT_PLAYER_CHARACTERISTICS;
	}
	
	public Map.InitialDroppedWeapon[] getInitialDroppedWeapons() {
		return INITIAL_DROPPED_WEAPONS;
	}
	
	public Map generateMap(DefaultTeamSet defaultTeamSet) {
		return generateMap(defaultTeamSet.getTeams());
	}
	
	public Map generateMap(Team... teams) {
		if (this == ICE_WORLD) {
			Bitmap wallBitmap = new Bitmap(Main.MISC_DIRECTORY + "/Wall.jpg", 50);
			Bitmap centerBlockBitmap = null;
			Color wallColor = new Color(240, 240, 240);
			Map map = new Map(27, 31, getName(), 24, wallColor, new Color(210, 210, 210), new Color(180, 180, 180), Map.DEFAULT_GRAVITY, this, this, getDefaultPlayerCharacteristics(), this, getInitialDroppedWeapons(), INITIAL_SPECTATOR_VIEW_LOCATION, wallColor, 3, teams);
			map.setBackgroundImage(Main.getImage(Main.MISC_DIRECTORY + "/IceMountains.jpg", Color.WHITE));
			double boundaryHeight = 3;
			Color halfWallColor = new Color(81, 71, 53);
			// ----------------------Corners-----------------------------
			map.addFence(new Fence.StraightFence.RectangularFence(1, 0, 0, 1, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(1, 31, 0, 30, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(26, 0, 27, 1, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(26, 31, 27, 30, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			// ----------------------Mid-Dividers-----------------------------
			map.addFence(new Fence.StraightFence.RectangularFence(12.5, 0, 13.5, 2, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(14.5, 0, 13.5, 2, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(12.5, 31, 13.5, 29, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(14.5, 31, 13.5, 29, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			// ----------------------Mid-Walls-----------------------------
			map.setWall(new Wall.Builder(0, 15).surfaceHeights(1).color(halfWallColor).topColor(halfWallColor).bitmap(wallBitmap).build());
			map.setWall(new Wall.Builder(1, 15).surfaceHeights(0.8).color(halfWallColor).topColor(halfWallColor).bitmap(wallBitmap).build());
			map.setWall(new Wall.Builder(25, 15).surfaceHeights(0.8).color(halfWallColor).topColor(halfWallColor).bitmap(wallBitmap).build());
			map.setWall(new Wall.Builder(26, 15).surfaceHeights(1).color(halfWallColor).topColor(halfWallColor).bitmap(wallBitmap).build());
			// ----------------------Side-Walls-----------------------------
			map.setWall(new Wall.Builder(3, 5).size(2, 1).surfaceHeights(1.3).color(halfWallColor).topColor(halfWallColor).bitmap(wallBitmap).build());
			map.setWall(new Wall.Builder(22, 5).size(2, 1).surfaceHeights(1.3).color(halfWallColor).topColor(halfWallColor).bitmap(wallBitmap).build());
			map.setWall(new Wall.Builder(3, 25).size(2, 1).surfaceHeights(1.3).color(halfWallColor).topColor(halfWallColor).bitmap(wallBitmap).build());
			map.setWall(new Wall.Builder(22, 25).size(2, 1).surfaceHeights(1.3).color(halfWallColor).topColor(halfWallColor).bitmap(wallBitmap).build());
			// ----------------------Center Block-----------------------------
			map.addFence(new Fence.StraightFence.RectangularFence(5, 5, 11.5, 5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(15.5, 5, 22, 5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(5, 26, 11.5, 26, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(15.5, 26, 22, 26, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			
			map.addFence(new Fence.StraightFence.RectangularFence(6, 14.5, 11.5, 14.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(15.5, 14.5, 21, 14.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(6, 16.5, 11.5, 16.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(15.5, 16.5, 21, 16.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			
			map.addFence(new Fence.StraightFence.RectangularFence(5, 5, 5, 13.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(12.5, 6, 12.5, 13.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(14.5, 6, 14.5, 13.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(22, 5, 22, 13.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(5, 26, 5, 17.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(12.5, 25, 12.5, 17.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(14.5, 25, 14.5, 17.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(22, 26, 22, 17.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			// ----------------------Center Block Corners-----------------------------
			map.addFence(new Fence.StraightFence.RectangularFence(11.5, 5, 12.5, 6, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(15.5, 5, 14.5, 6, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(11.5, 26, 12.5, 25, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(15.5, 26, 14.5, 25, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			
			map.addFence(new Fence.StraightFence.RectangularFence(5, 13.5, 6, 14.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(5, 17.5, 6, 16.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(22, 13.5, 21, 14.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(22, 17.5, 21, 16.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			
			map.addFence(new Fence.StraightFence.RectangularFence(15.5, 14.5, 14.5, 13.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(12.5, 13.5, 11.5, 14.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(15.5, 16.5, 14.5, 17.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(12.5, 17.5, 11.5, 16.5, boundaryHeight, 0, wallColor, wallColor, centerBlockBitmap));
			return map;
		} else if (this == LAVA) {
			Color wallColor = new Color(100, 100, 100);
			Map map = new Map(11, 11, getName(), 8, wallColor, Color.GRAY, new Color(50, 50, 50), Map.DEFAULT_GRAVITY, this, this, getDefaultPlayerCharacteristics(), this, getInitialDroppedWeapons(), INITIAL_SPECTATOR_VIEW_LOCATION, null, 0, teams);
			map.setWall(new Wall.Builder(0, 2).size(11, 2).surfaceHeights(0.5).color(wallColor).build());
			map.setWall(new Wall.Builder(0, 7).size(11, 2).surfaceHeights(0.5).color(wallColor).build());
			map.setWall(new Wall.Builder(0, 1).surfaceHeights(0.5).color(wallColor).build());
			map.setWall(new Wall.Builder(10, 1).surfaceHeights(0.5).color(wallColor).build());
			map.setWall(new Wall.Builder(0, 9).surfaceHeights(0.5).color(wallColor).build());
			map.setWall(new Wall.Builder(10, 9).surfaceHeights(0.5).color(wallColor).build());
			for (int y = 0; y < 11; y++) {
				for (int x = 0; x < 11; x++) {
					if (y == 0 || y == 10) {
						if (x >= 3 && x < 8) {
							map.setWall(new Wall.Builder(x, y).surfaceHeights(0.5, 2, 3.5 - Math.abs(5 - x) / 2d).color(wallColor).build());
						} else {
							map.setWall(new Wall.Builder(x, y).surfaceHeights(3.5 - Math.abs(5 - x) / 2d).color(wallColor).build());
						}
					} else if ((y == 1 || y == 9) && x >= 1 && x < 10) {
						map.setWall(new Wall.Builder(x, y).surfaceHeights(4.2 - Math.abs(5 - x) / 2d).color(wallColor).build());
					}
				}
			}
			map.setWall(new Wall.Builder(3, 1).surfaceHeights(0.5, 2, 3.2).color(wallColor).build());
			map.setWall(new Wall.Builder(5, 1).surfaceHeights(1.1, 1.6, 4.2).color(wallColor).glass(210).build());
			map.setWall(new Wall.Builder(7, 1).surfaceHeights(0.5, 2, 3.2).color(wallColor).build());
			map.setWall(new Wall.Builder(3, 9).surfaceHeights(0.5, 2, 3.2).color(wallColor).build());
			map.setWall(new Wall.Builder(5, 9).surfaceHeights(1.1, 1.6, 4.2).color(wallColor).glass(210).build());
			map.setWall(new Wall.Builder(7, 9).surfaceHeights(0.5, 2, 3.2).color(wallColor).build());
			map.setWall(new Wall.Builder(0, 4).size(11, 3).topColor(Color.ORANGE).color(wallColor).surfaceHeights(0.01).lavaDamage(5).build());
			return map;
		} else if (this == DE_DUST2) {
			double boundaryHeight = 9;
			double boundaryHeight2 = 7.5;
			Color boundaryColor = Color.WHITE;
			Color darkerTunnelColor = new Color(102, 102, 0);
			Color darkerTunnelFloorColor = new Color(189, 183, 107);
			Color doorColor = new Color(160, 82, 45);
			Color brownBoxColor = new Color(139, 69, 19);
			Color greenBoxColor = new Color(102, 204, 0);
			Bitmap doorBitmap = null;
			Map map = new Map(71, 67, getName(), null, new Color(200, 200, 0),  Color.LIGHT_GRAY, Map.DEFAULT_BACKGROUND_COLOR, Map.DEFAULT_GRAVITY * 2, this, this, getDefaultPlayerCharacteristics(), this, getInitialDroppedWeapons(), INITIAL_SPECTATOR_VIEW_LOCATION, boundaryColor, boundaryHeight, teams);
			// ----------------------CT mid-----------------------------
			Color cliffFloorColor = new Color(218, 165, 32);
			Color cliffColor = ProjectionPlane.getNewColorWithIntensity(cliffFloorColor, 0.15);
			map.setWall(new Wall.Builder(17, 5).size(19, 1).topColor(cliffFloorColor).color(cliffColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(17, 6).size(3, 1).topColor(cliffFloorColor).color(cliffColor).surfaceHeights(boundaryHeight2 * 3 / 4).build());
			map.setWall(new Wall.Builder(20, 6).size(4, 1).topColor(cliffFloorColor).color(cliffColor).surfaceHeights(boundaryHeight2 * 2 / 3).build());
			map.setWall(new Wall.Builder(24, 6).size(4, 1).topColor(cliffFloorColor).color(cliffColor).surfaceHeights(boundaryHeight2 / 2).build());
			map.setWall(new Wall.Builder(28, 6).size(5, 1).topColor(cliffFloorColor).color(cliffColor).surfaceHeights(boundaryHeight2 * 7 / 12).build());
			map.setWall(new Wall.Builder(17, 7).size(2, 1).topColor(cliffFloorColor).color(cliffColor).surfaceHeights(boundaryHeight2 * 2 / 3).build());
			map.setWall(new Wall.Builder(19, 7).size(4, 1).topColor(cliffFloorColor).color(cliffColor).surfaceHeights(boundaryHeight2 / 2).build());
			map.setWall(new Wall.Builder(29, 7).size(3, 1).topColor(cliffFloorColor).color(cliffColor).surfaceHeights(boundaryHeight2 / 3).build());
			map.setWall(new Wall.Builder(17, 16).size(14, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(16, 6).size(1, 2).color(boundaryColor).surfaceHeights(boundaryHeight2).build());

			map.setWall(new Wall.Builder(17, 8).size(1, 2).topColor(brownBoxColor).color(brownBoxColor).surfaceHeights(5).build());
			map.setWall(new Wall.Builder(18, 8).size(1, 2).topColor(brownBoxColor).color(brownBoxColor).surfaceHeights(4).build());
			map.setWall(new Wall.Builder(17, 10).size(2, 6).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(19, 14).size(2, 2).topColor(brownBoxColor).color(brownBoxColor).surfaceHeights(4.8).build());
			for (int x = 19; x < 21; x++) {
				map.setWall(new Wall.Builder(x, 8).size(1, 6).surfaceHeights(3 - (x - 18) / 4d).build());
			}
			for (int x = 21; x < 23; x++) {
				map.setWall(new Wall.Builder(x, 8).size(1, 8).surfaceHeights(3 - (x - 18) / 4d).build());
			}
			for (int x = 23; x < 29; x++) {
				map.setWall(new Wall.Builder(x, 7).size(1, 9).surfaceHeights(3 - (x - 18) / 4d).build());
			}
			map.setWall(new Wall.Builder(29, 8).size(1, 7).surfaceHeights(0.25).build());
			
			map.setWall(new Wall.Builder(29, 15).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(30, 15).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(36, 6).size(1, 6).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(36, 12).size(1, 6).color(boundaryColor).surfaceHeights(0, 3, boundaryHeight2).build());
			// ----------------------B-Back Platform-----------------------------
			map.setWall(new Wall.Builder(4, 0).size(1, 11).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(8, 0).size(1, 5).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(9, 4).size(1, 2).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(9, 6).size(1, 5).surfaceHeights(4).build());
			map.setWall(new Wall.Builder(5, 10).surfaceHeights(4).build());
			map.setWall(new Wall.Builder(8, 10).surfaceHeights(4).build());
			map.setWall(new Wall.Builder(5, 0).size(3, 10).surfaceHeights(3.5).build());
			map.setWall(new Wall.Builder(8, 5).size(1, 5).surfaceHeights(3.5).build());
			map.setWall(new Wall.Builder(6, 10).size(2, 1).surfaceHeights(3.25).build());
			map.setWall(new Wall.Builder(5, 1).size(3, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			// ----------------------B-Site-----------------------------
			map.setWall(new Wall.Builder(10, 6).size(4, 5).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(14, 6).size(2, 2).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(4, 11).size(12, 6).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(10, 5).size(5, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(15, 6).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(14, 8).topColor(greenBoxColor).color(greenBoxColor).surfaceHeights(4).build());
			map.setWall(new Wall.Builder(15, 8).topColor(greenBoxColor).color(greenBoxColor).surfaceHeights(5).build());
			map.setWall(new Wall.Builder(14, 9).size(2, 1).topColor(greenBoxColor).color(greenBoxColor).surfaceHeights(4).build());
			map.setWall(new Wall.Builder(14, 10).size(2, 1).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(11, 10).topColor(greenBoxColor).color(greenBoxColor).surfaceHeights(5).build());
			map.setWall(new Wall.Builder(16, 8).color(boundaryColor).surfaceHeights(5.5, 6.1, boundaryHeight2).build());
			map.setWall(new Wall.Builder(16, 9).size(1, 3).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(16, 12).size(1, 3).color(boundaryColor).surfaceHeights(3, 5.5, boundaryHeight2).build());
			map.addFence(new Fence.StraightFence.RectangularFence(16, 12, 17, 13.3, 5.5, 3, doorColor, doorColor, doorBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(16, 13.7, 17, 15, 5.5, 3, doorColor, doorColor, doorBitmap));
			map.setWall(new Wall.Builder(10, 17).size(4, 3).surfaceHeights(3).build());
			for (int i = 0; i < 5; i++) {
				map.setWall(new Wall.Builder(16 - i, 15 + i).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			}
			map.setWall(new Wall.Builder(10, 20).size(2, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(9, 17).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(9, 18).size(1, 4).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(3, 10).size(1, 7).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(4, 17).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(8, 17).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			// ----------------------Upper Tunnel-----------------------------
			map.setWall(new Wall.Builder(4, 17).size(1, 8).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(5, 17).size(2, 8).topColor(darkerTunnelFloorColor).surfaceHeights(3.2, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(7, 17).size(1, 8).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(2, 24).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(3, 24).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(8, 24).size(7, 1).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(14, 25).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(14, 26).topColor(darkerTunnelFloorColor).surfaceHeights(3.2, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(15, 26).size(6, 1).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(1, 25).size(1, 3).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(2, 25).size(2, 2).topColor(darkerTunnelFloorColor).surfaceHeights(5.2, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(4, 25).size(10, 2).topColor(darkerTunnelFloorColor).surfaceHeights(3.2, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(1, 27).size(1, 3).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(2, 27).size(17, 3).topColor(darkerTunnelFloorColor).surfaceHeights(3.2, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(1, 30).size(6, 1).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(6, 31).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(7, 32).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(7, 30).size(6, 2).topColor(darkerTunnelFloorColor).surfaceHeights(3.2, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(8, 32).size(1, 6).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(9, 32).size(2, 6).topColor(darkerTunnelFloorColor).surfaceHeights(3.2, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(11, 32).size(1, 6).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(12, 32).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(13, 31).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(13, 30).size(10, 1).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			// ----------------------Tunnels Stairs-----------------------------
			map.setWall(new Wall.Builder(19, 28).topColor(darkerTunnelFloorColor).surfaceHeights(4, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(20, 27).topColor(darkerTunnelFloorColor).surfaceHeights(4, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(19, 27).topColor(darkerTunnelFloorColor).surfaceHeights(3.2, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(19, 29).topColor(darkerTunnelFloorColor).surfaceHeights(2.7, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(20, 29).topColor(darkerTunnelFloorColor).surfaceHeights(2.2, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(20, 28).topColor(darkerTunnelFloorColor).surfaceHeights(1.7, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(21, 28).size(1, 2).topColor(darkerTunnelFloorColor).surfaceHeights(1.7, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(21, 27).topColor(darkerTunnelFloorColor).surfaceHeights(1.2, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(22, 27).size(1, 2).topColor(darkerTunnelFloorColor).surfaceHeights(1.2, 5.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(22, 29).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(23, 26).size(1, 4).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(21, 26).size(2, 1).topColor(darkerTunnelFloorColor).surfaceHeights(0.7, 2.7, boundaryHeight2).build());
			// ----------------------Lower Tunnel-----------------------------
			map.setWall(new Wall.Builder(21, 23).size(9, 3).topColor(darkerTunnelFloorColor).surfaceHeights(0.2, 2.7, boundaryHeight2).build());
			map.setWall(new Wall.Builder(20, 22).size(1, 4).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(21, 22).size(9, 1).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(24, 26).size(6, 1).topColor(darkerTunnelFloorColor).color(darkerTunnelColor).surfaceHeights(boundaryHeight2).build());
			// ----------------------Outside Tunnels-----------------------------
			map.setWall(new Wall.Builder(5, 38).size(4, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(9, 38).size(2, 1).color(boundaryColor).surfaceHeights(3.2, 5.7, boundaryHeight).build());
			map.setWall(new Wall.Builder(11, 38).size(5, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			for (int i = 0; i < 3; i++) {
				map.setWall(new Wall.Builder(i + 2, 41 - i).color(boundaryColor).surfaceHeights(boundaryHeight).build());
				map.setWall(new Wall.Builder(i + 3, 41 - i).size(15 - 2 * i, 1).surfaceHeights(3.2).build());
				map.setWall(new Wall.Builder(18 - i, 41 - i).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			}
			map.setWall(new Wall.Builder(2, 42).size(17, 1).surfaceHeights(3.1).build());
			map.setWall(new Wall.Builder(1, 42).size(1, 19).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(2, 43).size(17, 5).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(19, 42).size(1, 6).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(18, 48).size(1, 5).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(2, 48).size(16, 5).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(16, 52).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(17, 51).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			// ----------------------T-Spawn Ramp-----------------------------
			for (int y = 53; y < 61; y++) {
				map.setWall(new Wall.Builder(2, y).size(6, 1).surfaceHeights(3 + (y - 52) * 2 / 8d).build());
			}
			map.setWall(new Wall.Builder(8, 53).size(1, 8).surfaceHeights(5.7).build());
			map.setWall(new Wall.Builder(25, 53).size(1, 8).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(9, 53).size(6, 1).surfaceHeights(5.7).build());
			map.setWall(new Wall.Builder(9, 54).size(16, 7).surfaceHeights(5).build());
			map.setWall(new Wall.Builder(15, 53).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			for (int i = 0; i < 3; i++) {
				map.setWall(new Wall.Builder(17 - i, 56 - i).color(boundaryColor).surfaceHeights(boundaryHeight).build());
				map.setWall(new Wall.Builder(18 + i, 56 - i).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			}
			map.setWall(new Wall.Builder(21, 53).size(4, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			// ----------------------T-spawn-----------------------------
			map.setWall(new Wall.Builder(0, 61).size(2, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(0, 66).size(2, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(0, 62).size(2, 4).color(boundaryColor).surfaceHeights(5).build());
			map.setWall(new Wall.Builder(2, 61).size(23, 6).color(boundaryColor).surfaceHeights(5).build());
			map.setWall(new Wall.Builder(25, 61).size(1, 4).surfaceHeights(5, boundaryHeight - 1, boundaryHeight).build());
			map.setWall(new Wall.Builder(25, 65).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(26, 59).size(3, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(26, 66).size(3, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(26, 60).size(3, 6).surfaceHeights(5).build());
			for (int x = 29; x < 39; x++) {
				map.setWall(new Wall.Builder(x, 59).surfaceHeights(5.7 - (x - 29) * 0.2).build());
				map.setWall(new Wall.Builder(x, 60).size(1, 7).surfaceHeights(5 - (x - 29) * 0.2).build());
			}
			map.setWall(new Wall.Builder(38, 66).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			// ----------------------Outside Long-----------------------------
			map.setWall(new Wall.Builder(39, 59).size(9, 6).surfaceHeights(3).build());
			for (int x = 39; x < 48; x++) {
				map.setWall(new Wall.Builder(x, 65).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			}
			map.setWall(new Wall.Builder(47, 59).size(1, 6).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(48, 59).size(2, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(50, 59).size(4, 1).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(54, 59).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(50, 60).size(4, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(42, 47).size(13, 12).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(55, 47).size(1, 12).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			for (int i = 0; i < 3; i++) {
				map.setWall(new Wall.Builder(39 + i, 55 - i).color(boundaryColor).surfaceHeights(boundaryHeight).build());
				map.setWall(new Wall.Builder(39 + i, 56 - i).size(1, 3 + i).surfaceHeights(3).build());
			}
			map.setWall(new Wall.Builder(41, 49).size(1, 4).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(41, 47).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(48, 45).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(47, 43).size(1, 2).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			for (int i = 0; i < 3; i++) {
				map.setWall(new Wall.Builder(39 + i, 46 + i).color(boundaryColor).surfaceHeights(boundaryHeight).build());
				map.setWall(new Wall.Builder(39 + i, 45 + i).size(9, 1).surfaceHeights(3).build());
			}
			map.setWall(new Wall.Builder(39, 43).size(8, 2).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(41, 42).size(6, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(40, 41).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(39, 41).size(1, 2).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(40, 42).surfaceHeights(3).build());
			// ----------------------Long Doors-----------------------------
			map.addFence(new Fence.StraightFence.RectangularFence(50, 47, 52.3, 46, 5.5, 3, doorColor, doorColor, doorBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(52.7, 47, 55, 46, 5.5, 3, doorColor, doorColor, doorBitmap));
			map.setWall(new Wall.Builder(49, 37).size(1, 10).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(50, 37).size(5, 10).surfaceHeights(3, 5.5, boundaryHeight2).build());
			map.setWall(new Wall.Builder(55, 37).size(1, 10).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			// ----------------------Blue-----------------------------
			map.setWall(new Wall.Builder(49, 32).size(1, 5).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(50, 31).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(50, 32).size(1, 5).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(51, 30).size(11, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(51, 31).size(11, 6).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(53, 31).size(2, 2).topColor(brownBoxColor).color(brownBoxColor).surfaceHeights(5).build());
			// ----------------------Side of Pit-----------------------------
			map.setWall(new Wall.Builder(56, 37).size(5, 10).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(56, 47).size(5, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(61, 37).size(1, 7).surfaceHeights(3.7).build());
			// ----------------------Pit-----------------------------
			for (int y = 38; y < 44; y++) {
				map.setWall(new Wall.Builder(62, y).size(6, 1).surfaceHeights(3 - (y - 37) * 3 / 7d).build());
			}
			map.setWall(new Wall.Builder(61, 44).size(1, 3).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(62, 44).size(6, 3).surfaceHeights(0, 3, boundaryHeight2).build());
			map.setWall(new Wall.Builder(68, 44).size(1, 3).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(68, 31).size(1, 3).surfaceHeights(4.1).build());
			map.setWall(new Wall.Builder(68, 34).size(1, 2).surfaceHeights(3.2).build());
			map.setWall(new Wall.Builder(68, 36).size(1, 8).surfaceHeights(4.1).build());
			map.setWall(new Wall.Builder(69, 30).size(2, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(69, 31).size(2, 16).surfaceHeights(3.4).build());
			map.setWall(new Wall.Builder(62, 47).size(9, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			// ----------------------Suicide-----------------------------
			map.setWall(new Wall.Builder(30, 57).size(9, 2).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(29, 56).size(1, 3).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(34, 56).size(5, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(29, 46).size(1, 10).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(30, 47).size(3, 10).surfaceHeights(3, 5.5, boundaryHeight).build());
			map.setWall(new Wall.Builder(33, 56).surfaceHeights(3, 5.5, boundaryHeight).build());
			map.setWall(new Wall.Builder(33, 47).size(1, 9).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			for (int i = 0; i < 2; i++) {
				map.setWall(new Wall.Builder(30 - i, 46 - i).size(4 + 2 * i, 1).surfaceHeights(3).build());
			}
			map.setWall(new Wall.Builder(34, 46).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(28, 45).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			// ----------------------Top Mid-----------------------------
			map.setWall(new Wall.Builder(35, 45).size(4, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(27, 40).size(1, 5).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(28, 40).size(11, 5).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(28, 39).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(29, 38).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			for (int i = 0; i < 2; i++) {
				map.setWall(new Wall.Builder(30 - i, 38 + i).size(9 + i, 1).surfaceHeights(3).build());
			}
			// ----------------------Mid-----------------------------
			map.setWall(new Wall.Builder(30, 27).size(1, 11).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			for (int y = 27; y < 38; y++) {
				map.setWall(new Wall.Builder(31, y).size(5, 1).surfaceHeights((y - 26) * 3 / 12d).build());
			}
			map.setWall(new Wall.Builder(30, 24).size(1, 2).color(boundaryColor).surfaceHeights(0.2, 2.7, boundaryHeight).build());
			map.setWall(new Wall.Builder(30, 26).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			// ----------------------Mid Doors-----------------------------
			map.setWall(new Wall.Builder(29, 17).size(1, 5).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(30, 21).size(1, 3).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.addFence(new Fence.StraightFence.RectangularFence(31, 21, 33.3, 22, 3, 0, doorColor, doorColor, doorBitmap));
			map.addFence(new Fence.StraightFence.RectangularFence(33.7, 21, 36, 22, 3, 0, doorColor, doorColor, doorBitmap));
			map.setWall(new Wall.Builder(31, 21).size(5, 1).surfaceHeights(0, 3, boundaryHeight).build());
			map.setWall(new Wall.Builder(36, 18).size(1, 4).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			// ----------------------Catwalk-----------------------------
			map.setWall(new Wall.Builder(36, 23).size(1, 15).surfaceHeights(3.7).build());
			map.setWall(new Wall.Builder(37, 23).size(2, 15).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(39, 28).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(39, 29).size(1, 12).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(36, 22).size(11, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(39, 23).size(2, 1).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(40, 28).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(41, 23).size(6, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(39, 24).size(11, 4).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(41, 28).size(6, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(50, 26).size(2, 2).surfaceHeights(5).build());
			map.setWall(new Wall.Builder(50, 22).size(2, 4).surfaceHeights(3).build());
			for (int y = 21; y < 24; y++) {
				map.setWall(new Wall.Builder(47, y).size(2, 1).surfaceHeights(4.5 - (y - 20) * 1.5 / 4d).build());
				map.setWall(new Wall.Builder(49, y).surfaceHeights(5 - (y - 21) * 1.5 / 4d).build());
			}
			map.setWall(new Wall.Builder(46, 17).size(1, 5).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(50, 21).size(2, 1).surfaceHeights(5).build());
			map.setWall(new Wall.Builder(47, 28).size(5, 1).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(52, 18).size(1, 10).color(boundaryColor).surfaceHeights(boundaryHeight).build());
			map.setWall(new Wall.Builder(47, 17).size(5, 4).surfaceHeights(4.5).build());
			map.setWall(new Wall.Builder(52, 17).surfaceHeights(5.2).build());
			// ----------------------CT-spawn-----------------------------
			map.setWall(new Wall.Builder(38, 17).size(2, 1).surfaceHeights(0, 3, boundaryHeight2).build());
			map.setWall(new Wall.Builder(38, 18).surfaceHeights(0, 3, boundaryHeight2).build());
			map.setWall(new Wall.Builder(37, 11).size(1, 2).surfaceHeights(0, 3, boundaryHeight2).build());
			map.setWall(new Wall.Builder(37, 17).size(1, 2).surfaceHeights(1, 3, boundaryHeight2).build());
			map.setWall(new Wall.Builder(38, 11).size(2, 2).color(brownBoxColor).surfaceHeights(2, 3, boundaryHeight2).build());
			map.setWall(new Wall.Builder(37, 19).size(2, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(37, 10).size(3, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(37, 13).size(3, 4).surfaceHeights(0, 3, boundaryHeight2).build());
			map.setWall(new Wall.Builder(39, 18).size(7, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(40, 7).size(6, 11).surfaceHeights(0, 3, boundaryHeight2).build());
			map.setWall(new Wall.Builder(39, 7).size(1, 3).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(40, 6).size(7, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(46, 7).size(1, 10).surfaceHeights(0, 3, boundaryHeight2).build());
			map.setWall(new Wall.Builder(47, 7).size(5, 10).color(boundaryColor).surfaceHeights(0, 3, 4.5).build());
			map.setWall(new Wall.Builder(47, 4).size(15, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(52, 5).size(1, 5).color(boundaryColor).surfaceHeights(4.5).build());
			map.setWall(new Wall.Builder(52, 10).size(1, 3).surfaceHeights(5.2).build());
			map.setWall(new Wall.Builder(52, 13).size(1, 4).surfaceHeights(0, 3, 5.2).build());
			map.setWall(new Wall.Builder(46, 5).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(47, 5).size(2, 2).topColor(brownBoxColor).color(brownBoxColor).surfaceHeights(6.5).build());
			map.setWall(new Wall.Builder(49, 5).size(4, 2).surfaceHeights(4.5).build());
			// ----------------------CT Ramp-----------------------------
			map.setWall(new Wall.Builder(53, 11).size(2, 2).topColor(brownBoxColor).color(brownBoxColor).surfaceHeights(2).build());
			map.setWall(new Wall.Builder(53, 17).size(2, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			for (int x = 55; x < 58; x++) {
				map.setWall(new Wall.Builder(x, 11).size(1, 6).surfaceHeights((x - 54) * 3 / 7d).build());
			}
			for (int x = 58; x < 61; x++) {
				map.setWall(new Wall.Builder(x, 12).size(1, 5).surfaceHeights((x - 54) * 3 / 7d).build());
			}
			map.setWall(new Wall.Builder(55, 17).size(6, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			// ----------------------A-Site-----------------------------
			map.setWall(new Wall.Builder(53, 5).size(11, 5).surfaceHeights(4.5).build());
			map.setWall(new Wall.Builder(59, 10).size(5, 1).surfaceHeights(4.5).build());
			map.setWall(new Wall.Builder(53, 10).size(6, 1).surfaceHeights(5.2).build());
			map.setWall(new Wall.Builder(58, 11).size(6, 1).surfaceHeights(5.2).build());
			map.setWall(new Wall.Builder(64, 4).size(1, 8).surfaceHeights(5.2).build());
			map.setWall(new Wall.Builder(62, 3).size(2, 2).surfaceHeights(4.5).build());
			map.setWall(new Wall.Builder(64, 3).surfaceHeights(4.5).build());
			map.setWall(new Wall.Builder(61, 0).size(1, 4).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(62, 0).size(7, 3).surfaceHeights(4.5).build());
			map.setWall(new Wall.Builder(69, 0).size(1, 3).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			for (int y = 3; y < 9; y++) {
				map.setWall(new Wall.Builder(65, y).size(5, 1).surfaceHeights(4.5 - (y - 3) * 1.5 / 8d).build());
			}
			for (int y = 9; y < 11; y++) {
				map.setWall(new Wall.Builder(65, y).size(2, 1).topColor(greenBoxColor).color(greenBoxColor).surfaceHeights(6.5 - (y - 3) * 1.5 / 8d).build());
				map.setWall(new Wall.Builder(67, y).size(3, 1).surfaceHeights(4.5 - (y - 3) * 1.5 / 8d).build());
			}
			map.setWall(new Wall.Builder(70, 3).size(1, 8).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(65, 11).size(4, 1).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(69, 11).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			// ----------------------Long-----------------------------
			map.setWall(new Wall.Builder(69, 12).size(1, 3).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(61, 12).size(1, 6).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(61, 18).size(1, 12).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(68, 12).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(68, 13).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(68, 14).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(69, 15).size(2, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(69, 20).size(2, 1).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(68, 15).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(68, 16).size(3, 4).surfaceHeights(3).build());
			map.setWall(new Wall.Builder(68, 20).size(1, 11).color(boundaryColor).surfaceHeights(boundaryHeight2).build());
			map.setWall(new Wall.Builder(62, 12).size(6, 26).surfaceHeights(3).build());
			return map;
		} else if (this == CS_ASSAULT) {
			double boundaryHeight = 10;
			Color ventColor = new Color(119, 136, 153);
			Color ventColorDarker = new Color(79, 91, 102);
			Color ventColorBrighter = new Color(179, 204, 230);
			Color boundaryColor = new Color(200, 200, 200);
			Color darkerFloorColor = new Color(100, 100, 100);
			Color outsideWarehouseAlleyWallColor = new Color(70, 70, 70);
			Color brownFenceColor = new Color(205, 175, 149);
			Map map = new Map(50, 45, getName(), null, boundaryColor,  Color.LIGHT_GRAY, Map.DEFAULT_BACKGROUND_COLOR, Map.DEFAULT_GRAVITY * 2, this, this, getDefaultPlayerCharacteristics(), null, getInitialDroppedWeapons(), INITIAL_SPECTATOR_VIEW_LOCATION, Color.BLACK, boundaryHeight, teams);
			map.addFence(new Fence.StraightFence.RectangularFence(12, 0, 12, 3, 6, 0, outsideWarehouseAlleyWallColor, outsideWarehouseAlleyWallColor, null));
			map.addDoor(new Door(new Door.DoorStationaryPoint(12, 3), new Door.DoorEllipticalMovementController(12, 3, 1, 1, Math.PI * 3 / 2, 0, Compass.CircularDirection.COUNTER_CLOCKWISE, Compass.CircularDirection.CLOCKWISE), 2, 0, 1500, null, new Door.AutomaticallyClosingDoorTester.CubicalArea(11, 2, 0, 2, 2, 2), false, new Color(85, 107, 47), new Color(85, 107, 47), null));
			map.addFence(new Fence.StraightFence.RectangularFence(12, 3, 13, 3, 6, 2, outsideWarehouseAlleyWallColor, Color.LIGHT_GRAY, null));
			map.addFence(new Fence.StraightFence.RectangularFence(13, 3, 22, 3, 6, 0, outsideWarehouseAlleyWallColor, Color.LIGHT_GRAY, null));
			map.addLadder(new Ladder(19.5, 3, 0.3, 1 / 3d, 0, 6, 18, 1 / 33d, Color.BLACK));
			// ----------------------Warehouse Platform-----------------------------
			map.addFence(new Fence.StraightFence.RectangularFence(4, 3, 12, 3, 6, 0, Color.LIGHT_GRAY, Color.LIGHT_GRAY, null));
			map.setWall(new Wall.Builder(12, 4).size(4, 2).topColor(Color.BLUE).color(Color.BLUE).surfaceHeights(5.9).build());
			map.setWall(new Wall.Builder(4, 3).size(8, 7).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(0.5).build());
			map.setWall(new Wall.Builder(18, 3).size(4, 7).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(0.5).build());
			for (int y = 10; y < 16; y++) {
				map.setWall(new Wall.Builder(4, y).size(8, 1).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights((y - 9) / 7d + 0.5).build());
				map.setWall(new Wall.Builder(18, y).size(4, 1).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights((y - 9) / 7d + 0.5).build());
			}
			map.setWall(new Wall.Builder(4, 16).size(8, 5).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(1.5).build());
			map.setWall(new Wall.Builder(18, 16).size(4, 9).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(1.5).build());
			map.setWall(new Wall.Builder(2, 4).size(2, 18).color(darkerFloorColor).surfaceHeights(1.5, 3.3, 3.5).build());
			map.addFence(new Fence.StraightFence.RectangularFence(22, 3, 22, 25, 6, 0, Color.LIGHT_GRAY, Color.LIGHT_GRAY, null));
			map.addFence(new Fence.StraightFence.RectangularFence(18, 25, 22, 25, 6, 0, Color.LIGHT_GRAY, Color.LIGHT_GRAY, null));
			map.addFence(new Fence.StraightFence.RectangularFence(12, 25, 18, 25, 6, 1.2, Color.LIGHT_GRAY, Color.LIGHT_GRAY, null));
			map.addFence(new Fence.StraightFence.RectangularFence(18, 25, 22, 25, 6, 0, Color.LIGHT_GRAY, Color.LIGHT_GRAY, null));
			// ----------------------Warehouse Platform Stairs-----------------------------
			map.setWall(new Wall.Builder(0, 7).size(2, 2).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(3.5).build());
			for (int y = 9; y < 16; y++) {
				map.setWall(new Wall.Builder(0, y).size(2, 1).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights((16 - y) / 4d + 1.5).build());
			}
			map.setWall(new Wall.Builder(0, 16).size(2, 2).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(1.5).build());
			map.addFence(new Fence.StraightFence.RectangularFence(2, 9, 2, 16, 5.5, 0, Color.RED, darkerFloorColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(2, 16, 2, 18, 5.5, 3.5, darkerFloorColor, darkerFloorColor, null));
			map.setWall(new Wall.Builder(0, 4).size(2, 3).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(5.5).build());
			map.setWall(new Wall.Builder(2, 3).size(2, 1).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(5.5).build());
			map.setWall(new Wall.Builder(0, 18).size(2, 6).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(5.5).build());
			// ----------------------Warehouse Hostage Room Outside-----------------------------
			map.setWall(new Wall.Builder(4, 21).size(8, 4).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(1.5, 3.3, 3.5).build());
			map.setWall(new Wall.Builder(2, 22).size(2, 2).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(1.5, 3.3, 3.5).build());
			map.addFence(new Fence.StraightFence.RectangularFence(4, 21, 12, 21, 4, 3.5, darkerFloorColor, darkerFloorColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(4, 21, 12, 21, 5.5, 5, darkerFloorColor, darkerFloorColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(4, 21, 7, 21, 5, 4, darkerFloorColor, darkerFloorColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(8.5, 21, 9.5, 21, 5, 4, darkerFloorColor, darkerFloorColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(11, 21, 12, 21, 5, 4, darkerFloorColor, darkerFloorColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(12, 21, 12, 24, 5.5, 3.5, darkerFloorColor, darkerFloorColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(12, 24, 12, 25, 5.5, 4.2, darkerFloorColor, darkerFloorColor, null));
			// ----------------------Warehouse Hostage Room-----------------------------
			map.setWall(new Wall.Builder(4, 25).size(1, 2).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(3.5, 5.5, 5.6).build());
			map.setWall(new Wall.Builder(5, 25).size(6, 3).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(3.5, 5.5, 5.6).build());
			map.setWall(new Wall.Builder(9, 28).size(2, 3).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(3.5, 5.5, 5.6).build());
			map.setWall(new Wall.Builder(7, 29).size(2, 2).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(3.5, 5.5, 5.6).build());
			map.setWall(new Wall.Builder(5, 28).size(4, 1).color(boundaryColor).surfaceHeights(5.6).build());
			map.setWall(new Wall.Builder(11, 25).size(1, 10).color(boundaryColor).surfaceHeights(9).build());
			map.setWall(new Wall.Builder(7, 31).size(4, 1).color(boundaryColor).surfaceHeights(5.6).build());
			map.setWall(new Wall.Builder(6, 29).size(1, 2).color(boundaryColor).surfaceHeights(5.6).build());
			map.setWall(new Wall.Builder(2, 24).size(2, 1).color(boundaryColor).surfaceHeights(5.6).build());
			map.setWall(new Wall.Builder(3, 25).size(1, 2).color(boundaryColor).surfaceHeights(5.5).build());
			map.setWall(new Wall.Builder(4, 27).size(1, 1).color(boundaryColor).surfaceHeights(5.5).build());
			map.addFence(new Fence.StraightFence.RectangularFence(4, 25, 6, 25, 5.5, 0, darkerFloorColor, darkerFloorColor, null));
			Door.AutomaticallyClosingDoorTester hostageRoomDoorOpenArea = new Door.AutomaticallyClosingDoorTester.CubicalArea(4, 24, 3.5, 7, 2, 2);
			Door hostageRoomDoor1 = new Door(new Door.DoorLinearMovementController(4.5, 25, 6, 25), new Door.DoorLinearMovementController(6, 25, 7.5, 25), 5.5, 3.5, 1000, null, hostageRoomDoorOpenArea, false, boundaryColor, boundaryColor, null);
			Door hostageRoomDoor2 = new Door(new Door.DoorLinearMovementController(9, 25, 7.5, 25), new Door.DoorLinearMovementController(10.5, 25, 9, 25), 5.5, 3.5, 1000, null, hostageRoomDoorOpenArea, false, boundaryColor, boundaryColor, null);
			hostageRoomDoor1.addLinkedDoor(hostageRoomDoor2);
			hostageRoomDoor2.addLinkedDoor(hostageRoomDoor1);
			map.addDoor(hostageRoomDoor1);
			map.addDoor(hostageRoomDoor2);
			map.addFence(new Fence.StraightFence.RectangularFence(9, 25, 11, 25, 5.5, 0, darkerFloorColor, darkerFloorColor, null));
			// ----------------------Outside Warehouse Main Entrance-----------------------------
			map.setWall(new Wall.Builder(22, 25).size(2, 4).topColor(Color.RED).color(Color.RED).surfaceHeights(6).build());
			map.addLadder(new Ladder(23, 25, 0.3, 1 / 3d, 0, 6, 18, 1 / 33d, Color.BLACK));
			map.setWall(new Wall.Builder(27, 28).size(2, 4).topColor(Color.BLUE).color(Color.BLUE).surfaceHeights(4).build());
			map.addFence(new Fence.StraightFence.RectangularFence(18, 33, 18, 36, 2, 0, brownFenceColor, brownFenceColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(18, 33, 41, 33, 2, 0, brownFenceColor, brownFenceColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(41, 33, 41, 36, 2, 0, brownFenceColor, brownFenceColor, null));
			// ----------------------CT Spawn Area-----------------------------
			map.setWall(new Wall.Builder(26, 0).size(15, 3).topColor(boundaryColor).color(boundaryColor).surfaceHeights(10).build());
			map.setWall(new Wall.Builder(34, 3).size(6, 6).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(7).build());
			map.addLadder(new Ladder(40, 4.5, 0.3, 1 / 3d, 0, 7, 21, 1 / 33d, Color.BLACK));
			map.setWall(new Wall.Builder(33, 18).size(6, 9).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(7).build());
			map.setWall(new Wall.Builder(33, 27).size(2, 1).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(7).build());
			map.setWall(new Wall.Builder(37, 27).size(2, 1).topColor(darkerFloorColor).color(darkerFloorColor).surfaceHeights(7).build());
			map.setWall(new Wall.Builder(41, 0).size(5, 1).topColor(boundaryColor).color(boundaryColor).surfaceHeights(10).build());
			map.setWall(new Wall.Builder(46, 1).size(2, 2).topColor(boundaryColor).color(boundaryColor).surfaceHeights(10).build());
			map.setWall(new Wall.Builder(48, 3).size(2, 8).topColor(boundaryColor).color(boundaryColor).surfaceHeights(10).build());
			map.setWall(new Wall.Builder(48, 15).size(2, 12).topColor(boundaryColor).color(boundaryColor).surfaceHeights(10).build());
			map.setWall(new Wall.Builder(48, 31).size(2, 5).topColor(boundaryColor).color(boundaryColor).surfaceHeights(10).build());
			map.setWall(new Wall.Builder(44, 44).size(6, 1).topColor(boundaryColor).color(boundaryColor).surfaceHeights(10).build());
			// ----------------------Ramp-----------------------------
			map.setWall(new Wall.Builder(10, 36).size(20, 1).topColor(boundaryColor).color(boundaryColor).surfaceHeights(0, 39d / 11, 4.45).build());
			map.setWall(new Wall.Builder(10, 37).size(20, 4).topColor(boundaryColor).color(boundaryColor).surfaceHeights(0, 39d / 11, 3.9).build());
			map.setWall(new Wall.Builder(10, 41).size(20, 1).topColor(boundaryColor).color(boundaryColor).surfaceHeights(0, 39d / 11, 4.45).build());
			for (int x = 30; x < 40; x++) {
				map.setWall(new Wall.Builder(x, 36).size(1, 1).topColor(boundaryColor).color(boundaryColor).surfaceHeights(0, (39 - x) * 3.9 / 11, (40 - x) * 3.9 / 11 + 0.55).build());
				map.setWall(new Wall.Builder(x, 37).size(1, 4).topColor(boundaryColor).color(boundaryColor).surfaceHeights(0, (39 - x) * 3.9 / 11, (40 - x) * 3.9 / 11).build());
				map.setWall(new Wall.Builder(x, 41).size(1, 1).topColor(boundaryColor).color(boundaryColor).surfaceHeights(0, (39 - x) * 3.9 / 11, (40 - x) * 3.9 / 11 + 0.55).build());
			}
			map.setWall(new Wall.Builder(40, 36).size(1, 1).topColor(boundaryColor).color(boundaryColor).surfaceHeights(0.55).build());
			map.setWall(new Wall.Builder(40, 41).size(1, 1).topColor(boundaryColor).color(boundaryColor).surfaceHeights(0.55).build());
			// ----------------------Roof-----------------------------
			map.addCeiling(new Ceiling(12, 3, 6, 9, 1, boundaryColor, boundaryColor));
			map.addCeiling(new Ceiling(12, 24, 6, 9, 1, boundaryColor, boundaryColor));
			map.addCeiling(new Ceiling(21, 3, 6, 1, 22, boundaryColor, boundaryColor));
			map.addFence(new Fence.StraightFence.RectangularFence(12, 4, 21, 4, 6.25, 6, boundaryColor, boundaryColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(12, 24, 21, 24, 6.25, 6, boundaryColor, boundaryColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(21, 4, 21, 24, 6.25, 6, boundaryColor, boundaryColor, null));

			map.addCeiling(new Ceiling(12, 4, 6.25, 8, 1, boundaryColor, boundaryColor));
			map.addCeiling(new Ceiling(12, 23, 6.25, 8, 1, boundaryColor, boundaryColor));
			map.addCeiling(new Ceiling(20, 4, 6.25, 1, 20, boundaryColor, boundaryColor));
			map.addFence(new Fence.StraightFence.RectangularFence(12, 5, 20, 5, 6.5, 6.25, boundaryColor, boundaryColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(12, 23, 20, 23, 6.5, 6.25, boundaryColor, boundaryColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(20, 5, 20, 23, 6.5, 6.25, boundaryColor, boundaryColor, null));

			map.addCeiling(new Ceiling(12, 5, 6.5, 7, 1, boundaryColor, boundaryColor));
			map.addCeiling(new Ceiling(12, 22, 6.5, 7, 1, boundaryColor, boundaryColor));
			map.addCeiling(new Ceiling(19, 5, 6.5, 1, 18, boundaryColor, boundaryColor));
			map.addFence(new Fence.StraightFence.RectangularFence(12, 6, 19, 6, 6.75, 6.5, boundaryColor, boundaryColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(12, 22, 19, 22, 6.75, 6.5, boundaryColor, boundaryColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(19, 6, 19, 22, 6.75, 6.5, boundaryColor, boundaryColor, null));
			
			map.addCeiling(new Ceiling(13, 6, 6.75, 6, 16, boundaryColor, boundaryColor));
			map.addCeiling(new Ceiling(12, 6, 6.75, 1, 8, boundaryColor, boundaryColor));
			map.addCeiling(new Ceiling(12, 15, 6.75, 1, 7, boundaryColor, boundaryColor));
			map.addFence(new Fence.StraightFence.RectangularFence(12, 3, 12, 25, 9, 6.75, new Color(174, 137, 118), boundaryColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(12, 3, 12, 6, 6.75, 6, new Color(174, 137, 118), boundaryColor, null)); // TODO top vs top within distance but under height, reducing blocking top-fences covering vent, fence covering ramp
			map.addFence(new Fence.StraightFence.RectangularFence(12, 22, 12, 25, 6.75, 6, new Color(174, 137, 118), boundaryColor, null)); // TODO ladder on one side, 3d game, c++

			map.addCeiling(new Ceiling(0, 3, 7.2, 12, 22, boundaryColor, boundaryColor));
			// ----------------------Vent-----------------------------
			map.addFence(new Fence.StraightFence.RectangularFence(4, 14.15, 4, 14.85, 6.15, 3.5, new Color(255, 255, 255, 0), new Color(255, 255, 255, 0), null));
			map.addLadder(new Ladder(4, 14.5, 0.3, 1 / 3d, 3.5, 6.15, 2, 1 / 33d, Color.BLACK));
			map.addCeiling(new Ceiling(4, 14, 6.15, 9, 1, ventColorDarker, ventColorDarker));
			map.addCeiling(new Ceiling(4, 14, 6.75, 8, 1, ventColorDarker, ventColorDarker));
			map.addFence(new Fence.StraightFence.RectangularFence(4, 14, 13, 14, 6.75, 6.15, ventColor, ventColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(13, 14, 13, 15, 6.75, 6.15, ventColorBrighter, ventColorBrighter, null));
			map.addFence(new Fence.StraightFence.RectangularFence(4, 15, 8, 15, 6.75, 6.15, ventColor, ventColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(9, 15, 13, 15, 6.75, 6.15, ventColor, ventColor, null));
			
			map.addCeiling(new Ceiling(8, 15, 6.15, 1, 7, ventColorDarker, ventColorDarker));
			map.addCeiling(new Ceiling(8, 15, 6.75, 1, 8, ventColorDarker, ventColorDarker));
			map.addFence(new Fence.StraightFence.RectangularFence(8, 15, 8, 23, 6.75, 6.15, ventColor, ventColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(9, 15, 9, 23, 6.75, 6.15, ventColor, ventColor, null));
			map.addFence(new Fence.StraightFence.RectangularFence(8, 23, 9, 23, 6.75, 6.15, ventColorBrighter, ventColorBrighter, null));
			return map;
		} else if (this == ZOMBIELAND_RANDOM) {
			Color wallColor = Color.WHITE;
			Color centerColor = Color.BLACK;
			double mapEdgeWallHeight1 = 5;
			double mapEdgeWallHeight2 = 4;
			Map map = new Map((int) Math.floor(Math.random() * 7) * 2 + 11, (int) Math.floor(Math.random() * 7) * 2 + 11, getName(), null, new Color(200, 200, 0),  new Color(200, 200, 0), Map.DEFAULT_BACKGROUND_COLOR, Map.DEFAULT_GRAVITY * 2, this, this, getDefaultPlayerCharacteristics(), this, getInitialDroppedWeapons(), INITIAL_SPECTATOR_VIEW_LOCATION, wallColor, mapEdgeWallHeight1, teams);
			// ----------------------Edges-----------------------------
			for (int x = 0; x < map.getWidth(); x++) {
				map.setWall(new Wall.Builder(x, 0).color(wallColor).surfaceHeights(x % 2 == 0 ? mapEdgeWallHeight1 : mapEdgeWallHeight2).build());
				map.setWall(new Wall.Builder(x, map.getHeight() - 1).color(wallColor).surfaceHeights(x % 2 == 0 ? mapEdgeWallHeight1 : mapEdgeWallHeight2).build());
			}
			for (int y = 1; y < map.getHeight() - 1; y++) {
				map.setWall(new Wall.Builder(0, y).color(wallColor).surfaceHeights(y % 2 == 0 ? mapEdgeWallHeight1 : mapEdgeWallHeight2).build());
				map.setWall(new Wall.Builder(map.getWidth() - 1, y).color(wallColor).surfaceHeights(y % 2 == 0 ? mapEdgeWallHeight1 : mapEdgeWallHeight2).build());
			}
			// ----------------------Random Blocks-----------------------------
			ArrayList<Wall.Builder> builders = new ArrayList<Wall.Builder>();
			for (int i = 0; i < (map.getWidth() - 2) * (map.getHeight() - 2) * 0.3; i++) {
				int x = (int) Math.floor(Math.random() * (map.getWidth() - 2)) + 1;
				int y = (int) Math.floor(Math.random() * (map.getHeight() - 2)) + 1;
				Wall.Builder builder = new Wall.Builder(x, y).color(wallColor);
				double random = Math.random();
				if (isMapConnectedTo(map, Math.min(mapEdgeWallHeight1, mapEdgeWallHeight2), map.getWidth() / 2, map.getHeight() / 2, x, y)) {
					if (random < 0.15 && x >= 2 && x < map.getWidth() - 2 && y >= 2 && y < map.getHeight() - 2) {
						builder.surfaceHeights(0.01).topColor(Color.ORANGE).color(Color.ORANGE).lavaDamage(3);
					} else if (!isStoreLocation(map, x, y, 0, null)) {
						if (random < 0.95) {
							builder.surfaceHeights(map.getWall(x, y).getTopHeight(Integer.MAX_VALUE) + 2);
						} else {
							builder.surfaceHeights(map.getWall(x, y).getTopHeight(Integer.MAX_VALUE) + 2).glass(128);
						}
					}
					builders.add(builder);
				} else {
					i--;
				}
			}
			for (Wall.Builder b : builders) {
				map.setWall(b.build());
			}
			// ----------------------Center/Store-----------------------------
			map.setWall(new Wall.Builder(map.getWidth() / 2 - 1, map.getHeight() / 2 - 1).size(3, 3).topColor(centerColor).color(centerColor).surfaceHeights(0.01).build());
			return map;
		}
		return null;
	}
	
	private static boolean isMapConnectedTo(Map map, double minimumWallHeight, int connectedToX, int connectedToY, int additionalWallX, int additionalWallY) {
		boolean[][] mapWalls = new boolean[map.getHeight()][map.getWidth()];
		for (int y = 0; y < map.getHeight(); y++) {
			for (int x = 0; x < map.getWidth(); x++) {
				mapWalls[y][x] = map.getWall(x, y).getTopHeight(Integer.MAX_VALUE) >= minimumWallHeight || map.getWall(x, y).getLavaDamage() > 0;
			}
		}
		mapWalls[additionalWallY][additionalWallX] = true;
		return isMapConnectedTo(mapWalls, connectedToX, connectedToY);
	}
	
	private static boolean isMapConnectedTo(boolean[][] map, int x, int y) {
		boolean[][] checked = new boolean[map.length][map[0].length];
		checked[y][x] = true;
		return isMapConnectedTo(map, checked);
	}
	
	private static boolean isMapConnectedTo(boolean[][] map, boolean[][] checked) {
		boolean found = false;
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[y].length; x++) {
				if (!checked[y][x] && !map[y][x]) {
					if ((y > 0 && checked[y - 1][x]) || (y < map.length - 1 && checked[y + 1][x]) || (x > 0 && checked[y][x - 1]) || (x < map[y].length - 1 && checked[y][x + 1])) {
						found = true;
						checked[y][x] = true;
					}
				}
			}
		}
		if (found) {
			return isMapConnectedTo(map, checked);
		} else {
			for (int y = 0; y < map.length; y++) {
				for (int x = 0; x < map[y].length; x++) {
					if (!map[y][x] && !checked[y][x]) {
						return false;
					}
				}
			}
			return true;
		}
	}

	@Override
	public boolean isStoreLocation(Map map, double x, double y, double z, Player player) {
		if (player == null) {
			if (this == ICE_WORLD) {
				return x >= 11 && x < 16 && y >= 13 && y < 18;
			} else if (this == LAVA) {
				return (y >= 2 && y < 4) || (y >= 7 && y < 9);
			} else if (this == DE_DUST2) {
				throw new NullPointerException();
			} else if (this == CS_ASSAULT) {
				return true;
			} else if (this == ZOMBIELAND_RANDOM) {
				return x >= map.getWidth() / 2 - 1 && x <= map.getWidth() / 2 + 2 && y >= map.getHeight() / 2 - 1 && y <= map.getHeight() / 2 + 2;
			}
		} else if (!player.isSpectator()) {
			if (this == ICE_WORLD) {
				return isStoreLocation(map, x, y, z, null);
			} else if (this == LAVA) {
				return isStoreLocation(map, x, y, z, null);
			} else if (this == DE_DUST2) {
				if (map.getNumberOfTeams() > 0 && map.getTeam(0).equals(player.getTeam())) {
					return (int) x >= 22 && x <= 39 && (int) y >= 59 && (int) y <= 65;
				} else {
					return (int) x >= 36 && (int) x <= 52 && (int) y >= 8 && (int) y <= 19 && z < 3;
				}
			} else if (this == CS_ASSAULT) {
				return isStoreLocation(map, x, y, z, null);
			} else if (this == ZOMBIELAND_RANDOM) {
				return isStoreLocation(map, x, y, z, null);
			}
		}
		return false;
	}
	
	@Override
	public Map.Point3D getSpawnLocation(Player player) {
		if (this == ICE_WORLD || this == LAVA || this == DE_DUST2 || this == CS_ASSAULT) {
			if (player != null) {
				Map.Point3D[] locations = null;
				if (player.getGame().getMap().getNumberOfTeams() > 0 && player.getGame().getMap().getTeam(0).equals(player.getTeam())) {
					locations = TERRORISTS_SPAWN;
				} else {
					locations = COUNTER_TERRORISTS_SPAWN;
				}
				return getSpawnLocation(player, locations);
			}
		} else if (this == ZOMBIELAND_RANDOM) {
			int i = 0;
			Map.Point3D[] locations = null;
			if (Team.DefaultTeams.COUNTER_ZOMBIES.getTeam().equals(player.getTeam())) {
				locations = new Map.Point3D[9];
				for (int y = player.getGame().getMap().getHeight() / 2 - 1; y <= player.getGame().getMap().getHeight() / 2 + 1; y++) {
					for (int x = player.getGame().getMap().getWidth() / 2 - 1; x <= player.getGame().getMap().getWidth() / 2 + 1; x++) {
						locations[i++] = new Map.Point3D(x + 0.5, y + 0.5, 0);
					}
				}
			} else {
				locations = new Map.Point3D[(player.getGame().getMap().getWidth() + player.getGame().getMap().getHeight() - 1) * 2];
				for (int x = 0; x < player.getGame().getMap().getWidth(); x++) {
					locations[i++] = new Map.Point3D(x + 0.5, 0.5, player.getGame().getMap().getWall(x, 0).getTopHeight(Integer.MAX_VALUE));
					locations[i++] = new Map.Point3D(x + 0.5, player.getGame().getMap().getHeight() - 0.5, player.getGame().getMap().getWall(x, player.getGame().getMap().getHeight() - 1).getTopHeight(Integer.MAX_VALUE));
				}
				for (int y = 1; y < player.getGame().getMap().getHeight() - 1; y++) {
					locations[i++] = new Map.Point3D(0.5, y + 0.5, player.getGame().getMap().getWall(0, y).getTopHeight(Integer.MAX_VALUE));
					locations[i++] = new Map.Point3D(player.getGame().getMap().getWidth() - 0.5, y + 0.5, player.getGame().getMap().getWall(player.getGame().getMap().getWidth() - 1, y).getTopHeight(Integer.MAX_VALUE));
				}
			}
			return getSpawnLocation(player, locations);
		}
		return null;
	}
	
	private Map.Point3D getSpawnLocation(Player player, Map.Point3D... locations) {
		Map.Point3D[] locationsCopy = new Map.Point3D[locations.length];
		for (int i = 0; i < locations.length; i++) {
			locationsCopy[i] = locations[i];
		}
		for (int i = locationsCopy.length; i > 0; i--) {
			int chosen = (int) Math.floor(Math.random() * i);
			Map.Point3D point = locationsCopy[chosen];
			if (point != null && player.isMoveableTo(point.x, point.y, false, point.z, point.z + player.getDefaultHeight(), player.getGame(), true)) {
				return point;
			} else {
				locationsCopy[chosen] = locationsCopy[i - 1];
			}
		}
		return locationsCopy[(int) Math.floor(Math.random() * locationsCopy.length)];
	}
	
	@Override
	public Map.Point3D getBotSpawnLocation(Player player) {
		return getSpawnLocation(player);
	}

	@Override
	public void openStore(Player player, ProjectionPlane.DialogDisposedAction<StoreItem[]> action, boolean isAutobuy) {
		if (this == ICE_WORLD) {
			Store.DEFAULT_STORE.openStore(player, action, isAutobuy);
		} else if (this == LAVA || this == DE_DUST2 || this == CS_ASSAULT || this == ZOMBIELAND_RANDOM) {
			if (player.getTeam() != null) {
				player.getTeam().openStore(player, action, isAutobuy);
			}
		}
	}

	@Override
	public boolean isBombSite(double x, double y, double z) {
		if (this == ICE_WORLD) {
			return x >= 5;
		} else if (this == LAVA) {
			return false;
		} else if (this == DE_DUST2) {
			return (x >= 58 && x <= 64 && y >= 5 && y <= 11) || (x >= 10 && x <= 16 && y >= 6 && y <= 11);
		} else if (this == CS_ASSAULT) {
			return false;
		} else if (this == ZOMBIELAND_RANDOM) {
			return false;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public enum DefaultTeamSet {
		ZOMBIES_TEAM_SET(Team.DefaultTeams.COUNTER_ZOMBIES.getTeam(), Team.DefaultTeams.ZOMBIES.getTeam()), TERRORISTS_TEAM_SET(Team.DefaultTeams.TERRORISTS.getTeam(), Team.DefaultTeams.COUNTER_TERRORISTS.getTeam());
		
		private final Team[] TEAMS;
		
		DefaultTeamSet(Team... teams) {
			TEAMS = teams;
		}
		
		public Team[] getTeams() {
			return TEAMS;
		}
	}
}
