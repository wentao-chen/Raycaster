import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Set;


public interface HoldItem extends Serializable {

	public String getName();
	public DroppedItem getDropItem();
	public void drawImage(Graphics g, int screenWidth, int screenHeight, Color color);
	public Double getCrossHairsFocus(Player p);
	
	/**
	 * Resets the item to its initial state. Any properties not used to define the object should be reseted to its initial value. All equal objects should have equal states after this method is called.
	 */
	public void reset();
	public double getSpeedMultiplier();
	public boolean isDropable(Player player);
	public HoldItemSlot getHoldSlot();
	public boolean canHoldTacticalShield();
	public void stopCurrentAction();
	
	/**
	 * Called when the hold item is no longer the main hold item.
	 * @param player the player switching the item
	 */
	public void itemSwitched(Player player);
	
	public void keyPressed(int keyCode, Player player);
	public void keyReleased(int keyCode, Player player);
	public void keyTyped(int keyCode, Player player);
	public void checkKeys(Set<Integer> pressedKeys, Player player);

	public void mouseClicked(MouseEvent e, Player player);
	public void mousePressed(MouseEvent e, Player player);
	public void mouseReleased(MouseEvent e, Player player);
	public void checkButtons(Set<Integer> pressedButtons, Player player);
	
	public static class HoldItemSlot implements Serializable {
		private static final long serialVersionUID = 3900136302047998432L;

		private String SLOT_NAME;
		private Integer HOT_KEY;
		private Integer CAPACITY;
		
		public HoldItemSlot(String slotName, Integer hotKey, Integer capacity) {
			SLOT_NAME = slotName;
			HOT_KEY = hotKey;
			CAPACITY = capacity;
		}
		
		public String getName() {
			return SLOT_NAME;
		}
		
		public Integer getHotKey() {
			return HOT_KEY;
		}
		
		public Integer getCapacity() {
			return CAPACITY;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((CAPACITY == null) ? 0 : CAPACITY.hashCode());
			result = prime * result + ((HOT_KEY == null) ? 0 : HOT_KEY.hashCode());
			result = prime * result + ((SLOT_NAME == null) ? 0 : SLOT_NAME.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			HoldItemSlot other = (HoldItemSlot) obj;
			if (CAPACITY == null) {
				if (other.CAPACITY != null)
					return false;
			} else if (!CAPACITY.equals(other.CAPACITY))
				return false;
			if (HOT_KEY == null) {
				if (other.HOT_KEY != null)
					return false;
			} else if (!HOT_KEY.equals(other.HOT_KEY))
				return false;
			if (SLOT_NAME == null) {
				if (other.SLOT_NAME != null)
					return false;
			} else if (!SLOT_NAME.equals(other.SLOT_NAME))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "HoldItemSlot [SLOT_NAME=" + SLOT_NAME + ", HOT_KEY=" + HOT_KEY + ", CAPACITY=" + CAPACITY + "]";
		}
	}

}
