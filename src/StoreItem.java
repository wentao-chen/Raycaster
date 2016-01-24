import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;


public interface StoreItem extends Serializable {
	
	/**
	 * Gets the name of the store item. This is the name displayed in the store.
	 * @return the name of the item
	 */
	public String getName();
	
	/**
	 * Gets the cost of the item.
	 * @return the cost of the item
	 */
	public int getCost();
	
	/**
	 * Gets an image of the item. The image is displayed in the store.
	 * @return an image of the item
	 */
	public BufferedImage getImage();
	
	/**
	 * Gets a list of the information displayed by the store about the item. Each {@code String} is a distinct line.
	 * @return a list of information to be displayed by the store
	 */
	public ArrayList<String> getStoreInformation();
	
	/**
	 * Gets the item the that can be carried by a player after the item is bought or {@code null} if no items can be held.
	 * @return the item that can be carried after the item is bought.
	 */
	public HoldItem getHoldItem();
	
	/**
	 * Called after the item is bought by a player.
	 * @param buyer the player who bought the item
	 */
	public void itemBought(Player buyer);

	/**
	 * Gets a copy of the current store item instance. The copy will be passed to the buyer while the original store item remains in the store.
	 * The method {@link #equals(Object)} with the returned object evaluate {@code true}. Ideally, {@code ==} should return {@code false} but this condition
	 * is not required.
	 * @return a copy of the current store item
	 */
	public StoreItem getItemCopy();
}
