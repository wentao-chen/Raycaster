import java.io.Serializable;

/**
 * An immutable class representing an attack that may inflict damage to a player.
 * @author Wentao
 *
 */
class AttackEvent implements Serializable {
	private static final long serialVersionUID = 628554548377429376L;

	/**
	 * The duration of the display of an attack event.
	 */
	static final long HIT_DISPLAY_TIME = 100;
	
	private final long HIT_TIME;
	private final double INCOMING_DIRECTION;
	
	/**
	 * Creates an attack event of an attack from a certain direction.
	 * @param incomingDirection the direction of the attack in radians
	 */
	AttackEvent(double incomingDirection) {
		HIT_TIME = System.currentTimeMillis();
		INCOMING_DIRECTION = incomingDirection;
	}
	
	/**
	 * Gets the time of the attack.
	 * @return the time of the attack
	 */
	long getHitTime() {
		return HIT_TIME;
	}
	
	/**
	 * Gets the direction of the attack in radians.
	 * @return the direction of the attack in radians
	 */
	public double getIncomingDirection() {
		return INCOMING_DIRECTION;
	}
}
