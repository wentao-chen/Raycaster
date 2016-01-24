import java.io.Serializable;


public final class DefaultPlayerCharacteristics implements Serializable {
	private static final long serialVersionUID = 7585335124187701534L;
	
	private final Double SPEED;
	private final Double JUMP_HEIGHT;
	private final Double STEP_HEIGHT;
	
	public DefaultPlayerCharacteristics() {
		this(null, null, null);
	}
	
	public DefaultPlayerCharacteristics(Double speed, Double jumpHeight, Double stepHeight) {
		SPEED = speed != null ? Math.abs(speed) : null;
		JUMP_HEIGHT = jumpHeight != null ? Math.abs(jumpHeight) : null;
		STEP_HEIGHT = stepHeight != null ? Math.abs(stepHeight) : null;
	}

	public Double getSpeed() {
		return SPEED;
	}
	
	public Double getJumpHeight() {
		return JUMP_HEIGHT;
	}
	
	public Double getStepHeight() {
		return STEP_HEIGHT;
	}
	
	public void setToDefaultPlayer(Player player) {
		if (getSpeed() != null) {
			player.setSpeed(getSpeed());
		}
		if (getJumpHeight() != null) {
			player.setMaxJumpHeight(getJumpHeight());
		}
		if (getStepHeight() != null) {
			player.setStepHeight(getStepHeight());
		}
	}
}
