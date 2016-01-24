import java.io.Serializable;


public interface BombSiteLocator extends Serializable {

	public boolean isBombSite(double x, double y, double z);
	
}
