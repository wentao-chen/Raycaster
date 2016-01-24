import java.io.Serializable;


public interface PlayerMovementController extends Serializable {
	
	public void movePlayer(Player p, long timePassed);

}
