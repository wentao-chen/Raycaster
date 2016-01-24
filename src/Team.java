import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.KeyStroke;


public final class Team implements ChatMessage.ChatMessageTarget, Serializable {
	private static final long serialVersionUID = 6968827070596665818L;
	
	/**
	 * The default team name for Spectators.
	 */
	public static final String SPECTATOR_NAME = "Spectators";
	/**
	 * The default team color for Spectators.
	 */
	public static final Color SPECTATOR_COLOR = Color.GRAY;
	
	private static final AtomicInteger ID_ASSIGNER = new AtomicInteger();

	public static final Store DEFAULT_TERRORISTS_STORE = new Store()
			.addCategory(Pistol.PistolType.PISTOL.getName(), KeyStroke.getKeyStroke("1"))
					.addItem(Pistol.createAllPistols()).store()
			.addCategory(BasicPrimaryGun.BasicPrimaryGunType.SHOTGUN.getName(), KeyStroke.getKeyStroke("2"))
					.addItem(BasicPrimaryGun.BasicPrimaryGunType.SHOTGUN.createAllGuns()).store()
			.addCategory(BasicPrimaryGun.BasicPrimaryGunType.SUBMACHINE_GUN.getName(), KeyStroke.getKeyStroke("3"))
					.addItem(BasicPrimaryGun.BasicPrimaryGunType.SUBMACHINE_GUN.createAllGuns()).store()
			.addCategory(BasicPrimaryGun.BasicPrimaryGunType.RIFLE.getName(), KeyStroke.getKeyStroke("4"))
					.addItem(BasicPrimaryGun.BasicPrimaryGunType.RIFLE.createAllGuns()).store()
			.addCategory(BasicPrimaryGun.BasicPrimaryGunType.MACHINE_GUN.getName(), KeyStroke.getKeyStroke("5"))
					.addItem(BasicPrimaryGun.BasicPrimaryGunType.MACHINE_GUN.createAllGuns()).store()
			.addItem(Gun.AmmoCategory.PRIMARY, KeyStroke.getKeyStroke("6"))
			.addItem(Gun.AmmoCategory.SECONDARY, KeyStroke.getKeyStroke("7"))
			.addCategory("Equipment", KeyStroke.getKeyStroke("8"))
				.addItem(Player.Kevlar.DEFAULT_KEVLAR, KeyStroke.getKeyStroke("1"))
				.addItem(Player.KevlarAndHelmet.DEFAULT_KEVLAR_AND_HELMET, KeyStroke.getKeyStroke("2"))
				.addItem(ThrowableWeapon.createDefaultFlashbang(), KeyStroke.getKeyStroke("3"))
				.addItem(ThrowableWeapon.createDefaultHEGrenade(), KeyStroke.getKeyStroke("4"))
				.addItem(ThrowableWeapon.createDefaultSmokeGrenade(), KeyStroke.getKeyStroke("5"))
				.addItem(Bench.DEFAULT_BENCH, KeyStroke.getKeyStroke("6")).store().lock();
	
	private static final File TERRORISTS_WIN_SOUND_FILE = new File(Main.TERRORISTS_WIN_FILE_LOCATION);
	public static final String TERRORISTS_WIN_MESSAGE = "Terrorists Win";
	public static final long TERRORISTS_WIN_MESSAGE_DURATION = 1700;
	
	private final int ID;
	private final String NAME;
	//private final Store TEAM_STORE; TODO
	private final Color TEAM_COLOR;
	private final boolean IS_BOMB_TEAM;
	private final boolean IS_DEFUSE_TEAM;
	private transient BufferedImage[] teamPlayerImages = null;
	
	public Team(String name, Store teamStore, Color teamColor, boolean isBombTeam, boolean isDefuseTeam, String... teamPlayerImagePaths) {
		if (name == null) throw new IllegalArgumentException("name cannot be null");
		if (teamColor == null) throw new IllegalArgumentException("color cannot be null");
		ID = ID_ASSIGNER.getAndIncrement();
		NAME = name;
		//TEAM_STORE = teamStore;TODO
		TEAM_COLOR = teamColor;
		IS_BOMB_TEAM = isBombTeam;
		IS_DEFUSE_TEAM = isDefuseTeam;
		this.teamPlayerImages = new BufferedImage[teamPlayerImagePaths.length];
		for (int i = 0; i < teamPlayerImagePaths.length; i++) {
			this.teamPlayerImages[i] = Main.getImage(teamPlayerImagePaths[i], Color.WHITE);
		}
	}
	
	/**
	 * Gets the unique id number of the team.
	 * @return the id number of the team
	 */
	public int getID() {
		return ID;
	}
	
	/**
	 * Gets the team name.
	 * @return the team name
	 */
	public String getName() {
		return NAME;
	}
	
	public void openStore(Player buyer, ProjectionPlane.DialogDisposedAction<StoreItem[]> action, boolean isAutobuy) {
		Store.DEFAULT_STORE.openStore(buyer, action, isAutobuy);
		/*
		if (TEAM_STORE != null) {TODO
			TEAM_STORE.openStore(buyer.getMoney(), action);
		}*/
	}
	
	/**
	 * Gets the team color.
	 * @return the team color
	 */
	public Color getColor() {
		return TEAM_COLOR;
	}
	
	/**
	 * Checks if the team allows its players to carry bombs.
	 * @return {@code true} if the team allows bombs; otherwise {@code false}
	 */
	public boolean isBombTeam() {
		return IS_BOMB_TEAM;
	}
	
	/**
	 * Checks if the team allows its players to defuse bombs.
	 * @return {@code true} if the team allows defusing bombs; otherwise {@code false}
	 */
	public boolean isDefuseTeam() {
		return IS_DEFUSE_TEAM;
	}

	@Override
	public String getRecieverName() {
		return "Team (" + getName() + ") Chat";
	}

	@Override
	public Color getMessageColor() {
		return getColor();
	}

	@Override
	public boolean willRecieveMessage(Player p) {
		return p != null && equals(p.getTeam());
	}

	public BufferedImage getTeamPlayerImage() {
		return this.teamPlayerImages.length > 0 ? this.teamPlayerImages[(int) Math.floor(Math.random() * this.teamPlayerImages.length)] : Team.DefaultTeams.COUNTER_TERRORISTS.getTeam().getTeamPlayerImage();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeInt(this.teamPlayerImages.length);
		for (BufferedImage i : this.teamPlayerImages) {
	        ImageIO.write(i, "png", out);
		}
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.teamPlayerImages = new BufferedImage[in.readInt()];
        for (int i = 0; i < this.teamPlayerImages.length; i++) {
            this.teamPlayerImages[i] = ImageIO.read(in);
        }
    }

	@Override
	public String toString() {
		return getName();
	}
	
	public static void playTerroristsWinSound() {
		ProjectionPlane.playSoundFile(TERRORISTS_WIN_SOUND_FILE, 0d);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ID;
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
		Team other = (Team) obj;
		if (ID != other.ID)
			return false;
		return true;
	}
	
	public enum DefaultTeams {
		TERRORISTS(new Team("Terrorists", DEFAULT_TERRORISTS_STORE, Color.RED, true, true, Main.TERRORIST_IMAGE_PATH, Main.TERRORIST2_IMAGE_PATH)),
		COUNTER_TERRORISTS(new Team("Counter-Terrorists", Store.DEFAULT_STORE, Color.BLUE, false, true, Main.COUNTER_TERRORIST_IMAGE_PATH)),
		COUNTER_ZOMBIES(new Team("Rick Grimes and Friends", Store.DEFAULT_STORE, Color.GREEN, false, false, Main.COUNTER_TERRORIST_IMAGE_PATH)),
		ZOMBIES(new Team("Zombies", null, new Color(139, 69, 19), false, false, Main.ZOMBIE_IMAGE_PATH));
		
		private final Team TEAM;
		
		DefaultTeams(Team team) {
			TEAM = team;
		}
		
		public Team getTeam() {
			return TEAM;
		}
	}
}