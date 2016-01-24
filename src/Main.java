import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class Main {
	// TODO deny team change
	
	// TODO other spectator cams
	// TODO console editing static variables - ChatMessage and Game.RecentEvents : display time - Main class variables : Game.friendlyFire, default freeze time
	// TODO no bomb and riot shield
	
	// TODO main message, personalized messages, sending messages, bomb has been dropped/picked up
	// TODO store prevent buying riot shield and defuse kit
	// TODO dropable defuse kit
	// TODO terrorists store
	
	public static final String NAME = "Raycaster";
	public static final int VERSION_MAJOR = 1;
	public static final int VERSION_MINOR = 0;
	public static final int VERSION_PATCH = 4;
	
	static final String RESOURCES_DIRECTORY = "res";
	static final String MISC_DIRECTORY = RESOURCES_DIRECTORY + "/Misc";
	
	public static final BufferedImage BICEPS_IMAGE = getImage(MISC_DIRECTORY + "/Biceps.png", Color.WHITE);
	public static final String BENCH_IMAGE_PATH = MISC_DIRECTORY + "/Bench.png";
	public static final BufferedImage BACKGROUND_WALL_PAPER = getImage(MISC_DIRECTORY + "/BackgroundWallpaper.jpg", Color.WHITE);
	public static final ImageIcon FRAME_ICON = new ImageIcon(getImage(MISC_DIRECTORY + "/Icon.png", null));
	public static final String ZOMBIE_IMAGE_PATH = MISC_DIRECTORY + "/Zombie.jpg";
	public static final String TERRORIST_IMAGE_PATH = MISC_DIRECTORY + "/Terrorist.gif";
	public static final String TERRORIST2_IMAGE_PATH = MISC_DIRECTORY + "/Terrorist2.gif";
	public static final String COUNTER_TERRORIST_IMAGE_PATH = MISC_DIRECTORY + "/CounterTerrorist.png";
	public static final String BREAK_GLASS_SOUND_FILE_PATH = MISC_DIRECTORY + "/breakglass.wav";
	public static final String TERRORISTS_WIN_FILE_LOCATION = MISC_DIRECTORY + "/TerroristsWin.wav";
	public static final String HEADSHOT_ICON_IMAGE_PATH = MISC_DIRECTORY + "/HeadshotIcon.png";
	
	public static final String PLAYER_TERMINATION_SOUND_PATH = MISC_DIRECTORY + "/playerdeath1.wav";
	public static final String[] PLAYER_STEP_SOUND_PATHS = {MISC_DIRECTORY + "/step1.wav", MISC_DIRECTORY + "/step2.wav", MISC_DIRECTORY + "/step3.wav", MISC_DIRECTORY + "/step4.wav"};
	
	public static final String DEFAULT_FONT = "segou ui";
	public static final Font SCORE_FONT = new Font(DEFAULT_FONT, Font.PLAIN, 10);
	public static final Font GAME_MAIN_MESSAGE_FONT = new Font(DEFAULT_FONT, Font.BOLD, 10);
	public static final Font CHAT_FONT = new Font(DEFAULT_FONT, Font.PLAIN, 10);
	public static final Font SPECTATOR_INFO_FONT = new Font(DEFAULT_FONT, Font.BOLD, 12);
	public static final Font RECENT_EVENTS_FONT = new Font(DEFAULT_FONT, Font.PLAIN, 14);
	public static final Font PAUSE_MENU_CLICKABLE_ITEMS_FONT = new Font(DEFAULT_FONT, Font.BOLD, 20);
	public static final Font PAUSE_MENU_GAME_MODE_NAME_FONT = new Font(DEFAULT_FONT, Font.ITALIC, 15);
	public static final Font PAUSE_MENU_MAP_NAME_FONT = new Font(DEFAULT_FONT, Font.ITALIC, 12);
	
	public static final int NUMBER_OF_RECENT_CHAT_MESSAGES = 4;
	
	public static final RandomNames RANDOM_NAMES = new RandomNames(MISC_DIRECTORY + "/RandomMaleNames.txt");
	public static final String ERROR_LOG_FILE_PATH = MISC_DIRECTORY + "/ErrorLog.txt";
	
	public static final double TARGET_FPS = 10;
	
	private static boolean agreeToTerms = false;
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (UnsupportedLookAndFeelException e) {
		}
		// if (!loadTermsAndConditions()) return;
		ProjectionPlane.getSingleton().start();
	}
	
	public static boolean loadTermsAndConditions() {
		final JDialog DIALOG = new JDialog((Frame) null, "Notice", true);
		DIALOG.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		DIALOG.getContentPane().setLayout(new BorderLayout());
		JTextArea textArea = new JTextArea();
		textArea.setText(""
				+ "This program was inspired by Counter-Strike developed by Valve Corporation. Some parts of the program may resemble scenes from Counter-Strike.\n\n"
				+ "If you intend to sue for copyright infringement, immediately terminate this program.\n\n"
				+ "This program may contain intense violence and is not suitable for all ages. Discretion is advised. The developer of this program is not liable for any damages or harm resulting from the use of this program.\n\n"
				+ "By clicking the \"I Agree\" button below, you have read and agreed to the conditions specified in this text box."
				+ "Privacy\n"
				+ "The developer of this program does not care about your privacy. By clicking the \"I Agree\" button below, you give permission to the developers unlimited use of your personal information.");
		textArea.setBorder(BorderFactory.createTitledBorder("Notice"));
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane areaScrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		DIALOG.getContentPane().add(areaScrollPane, BorderLayout.CENTER);
		JButton button = new JButton("I Agree");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Main.agreeToTerms = true;
				DIALOG.dispose();
			}
		});
		DIALOG.getContentPane().add(button, BorderLayout.SOUTH);
		DIALOG.setMinimumSize(new Dimension(400, 200));
		DIALOG.setVisible(true);
		return Main.agreeToTerms;
	}
	
	public static String getVersion() {
		return VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_PATCH;
	}
	
	public static void logError(Exception e) {
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Main.ERROR_LOG_FILE_PATH, true)));
		    out.println(System.currentTimeMillis());
		    e.printStackTrace(out);
		    out.close();
		} catch (IOException ex) {
		}
	}
	
	public static void logError(String s) {
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Main.ERROR_LOG_FILE_PATH, true)));
		    out.println(System.currentTimeMillis());
		    out.println(s);
		    out.close();
		} catch (IOException e) {
		}
	}
	
	/**
	 * Convenience method for {@link #getImage(String, Color, int, int)} with the default width and height {@code 50}.
	 * @param filePath the file path for the image
	 * @param defaultColor the default color if an image is not created or {@code null} to return a {@code null} image if one is not created
	 * @return the image from the file
	 * @see #getImage(String, Color, int, int)
	 */
	public static BufferedImage getImage(String filePath, Color defaultColor) {
		return getImage(filePath, defaultColor, 50, 50);
	}
	
	/**
	 * Creates a buffered image for an image file. If the default color is not null, a non-null value will be returned.
	 * @param filePath the file path for the image
	 * @param defaultColor the default color if an image is not created or {@code null} to return a {@code null} image if one is not created
	 * @param defaultWidth the default width of the image
	 * @param defaultHeight  the default height of the image
	 * @return the image from the file
	 */
	public static BufferedImage getImage(String filePath, Color defaultColor, int defaultWidth, int defaultHeight) {
		BufferedImage picture = null;
		try {
			picture = ImageIO.read(new File(filePath));
		} catch (IOException e) {
		} catch (NullPointerException e) {
		}
		if (picture == null && defaultColor != null) {
			picture = new BufferedImage(Math.abs(defaultWidth), Math.abs(defaultHeight), BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D graphics = picture.createGraphics();
			graphics.setPaint(defaultColor);
			graphics.fillRect(0, 0, picture.getWidth(), picture.getHeight());
		}
		return picture;
	}
	
	public static BufferedImage resize(BufferedImage image, int width, int height) {
		if (image != null) {
		    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
		    Graphics2D g2d = (Graphics2D) bi.createGraphics();
		    g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
		    g2d.drawImage(image, 0, 0, width, height, null);
		    g2d.dispose();
		    return bi;
		}
		return null;
	}
	
	public static Color getBlackWhiteContrastColor(Color color) {
		return (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000 >= 128 ? Color.BLACK : Color.WHITE;
	}
	
	public static Color changeIntensity(Color color, double intensity) {
		intensity = 1 - intensity;
		return new Color((int) (color.getRed() * intensity), (int) (color.getGreen() * intensity), (int) (color.getBlue() * intensity), color.getAlpha());
	}
	
	static void enableComponents(Container container, boolean enable) {
        for (Component component : container.getComponents()) {
            component.setEnabled(enable);
            if (component instanceof Container) {
                enableComponents((Container) component, enable);
            }
        }
    }
}
