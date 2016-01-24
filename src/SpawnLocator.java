import java.io.Serializable;


public interface SpawnLocator extends Serializable {

	/**
	 * Finds a location for a given player to spawn. The location should not conflict with the current state of the game. For example, an existing player should
	 * not be at the location. If {@code null} is returned, the location will be random.
	 * @param player the player to be spawned
	 * @return length-3 array that contains the coordinates of the spawning location of the player (0: x-coordinate, 1: y-coordinate, 2: the minimum height)
	 */
	public Map.Point3D getSpawnLocation(Player player);

	/**
	 * Finds a location for a given bot to spawn. The location should not conflict with the current state of the game. For example, an existing player should
	 * not be at the location. If {@code null} is returned, the location will be random.
	 * @param player the bot to be spawned
	 * @return length-3 array that contains the coordinates of the spawning location of the bot (0: x-coordinate, 1: y-coordinate, 2: the minimum height)
	 */
	public Map.Point3D getBotSpawnLocation(Player player);
	
	public static class RandomSpawner implements SpawnLocator {
		private static final long serialVersionUID = -3921275112957596528L;
		
		@Override
		public Map.Point3D getSpawnLocation(Player player) {
			return null;
		}
		@Override
		public Map.Point3D getBotSpawnLocation(Player player) {
			return null;
		}
	}
}
