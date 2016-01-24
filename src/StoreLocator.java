import java.io.Serializable;


public interface StoreLocator extends Serializable {
	
	public boolean isStoreLocation(Map map, double x, double y, double z, Player player);
	
	/**
	 * Called when a player requests the store.
	 * @param player the player requesting the store
	 * @param action the action executed after the store is closed with the items bought being passed as an argument
	 * @param isAutobuy {@code true} if auto buy is requested; otherwise, {@code false}
	 */
	public void openStore(Player player, ProjectionPlane.DialogDisposedAction<StoreItem[]> action, boolean isAutobuy);
	
	public static class EveryWhereStoreLocator implements StoreLocator {
		private static final long serialVersionUID = -9176374053480547180L;

		@Override
		public boolean isStoreLocation(Map map, double x, double y, double z, Player player) {
			return true;
		}

		@Override
		public void openStore(Player player, ProjectionPlane.DialogDisposedAction<StoreItem[]> action, boolean isAutobuy) {
			Store.DEFAULT_STORE.openStore(player, action, isAutobuy);
		}
	}
}
