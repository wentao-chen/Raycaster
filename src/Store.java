import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;


public class Store implements Serializable {
	private static final long serialVersionUID = -2145209466178729349L;

	public static final String DEFAULT_STORE_TITLE = "Store";
	
	private static final String EQUIPMENT_CATEGORY_NAME = "Equipment";
	
	public static final Store DEFAULT_STORE = new Store()
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
			.addCategory(EQUIPMENT_CATEGORY_NAME, KeyStroke.getKeyStroke("8"))
					.addItem(Player.Kevlar.DEFAULT_KEVLAR, KeyStroke.getKeyStroke("1"))
					.addItem(Player.KevlarAndHelmet.DEFAULT_KEVLAR_AND_HELMET, KeyStroke.getKeyStroke("2"))
					.addItem(ThrowableWeapon.createDefaultFlashbang(), KeyStroke.getKeyStroke("3"))
					.addItem(ThrowableWeapon.createDefaultHEGrenade(), KeyStroke.getKeyStroke("4"))
					.addItem(ThrowableWeapon.createDefaultSmokeGrenade(), KeyStroke.getKeyStroke("5"))
					.addItem(TacticalShield.createDefaultTacticalShield(), KeyStroke.getKeyStroke("6"))
					.addItem(DefuseKit.createDefaultDefuseKit(), KeyStroke.getKeyStroke("7"))
					.addItem(Bench.DEFAULT_BENCH, KeyStroke.getKeyStroke("8")).store()
			.addCategory(RocketLauncher.RocketLauncherType.BASIC_ROCKET_LAUNCHER.getName(), KeyStroke.getKeyStroke("9"))
					.addItem(RocketLauncher.createAllRocketLaunchers()).store()
			.lock();
	
	public static final Store DEFAULT_MARKERS_STORE = new Store().addItem(new Marker(), KeyStroke.getKeyStroke("1")).lock();
	
	private final String STORE_TITLE;
	private final ArrayList<StoreOption> OPTIONS = new ArrayList<StoreOption>();
	private final AtomicBoolean IS_LOCKED = new AtomicBoolean(false);
	
	private transient ArrayList<StoreItem> boughtItems = new ArrayList<StoreItem>();
	
	public Store() {
		this(null);
	}
	
	public Store(String storeTitle) {
		STORE_TITLE = storeTitle != null ? storeTitle : DEFAULT_STORE_TITLE;
	}
	
	public String getStoreTitle() {
		return STORE_TITLE;
	}
	
	public StoreCategory addCategory(String name, KeyStroke hotKey) {
		return addCategory(OPTIONS.size(), name, hotKey);
	}
	
	public StoreCategory addCategory(int index, String name, KeyStroke hotKey) {
		if (isLocked()) throw new IllegalStateException("Categories cannot be added to a locked store");
		StoreCategory category = new StoreCategory(name, hotKey);
		synchronized (this) {
			OPTIONS.add(index, category);
		}
		return category;
	}
	
	public Store addItem(StoreItem[] items) {
		return addItem(items, createHotKeysArray(items.length));
	}
	
	public Store addItem(StoreItem[] items, KeyStroke... hotKeys) {
		if (items.length != hotKeys.length) throw new IllegalArgumentException("the number of items must equal the number of hotkeys");
		for (int i = 0; i < items.length; i++) {
			addItem(items[i], hotKeys[i]);
		}
		return this;
	}
	
	public Store addItem(StoreItem item, KeyStroke hotKey) {
		return addItem(OPTIONS.size(), item, hotKey);
	}
	
	public Store addItem(int index, StoreItem item, KeyStroke hotKey) {
		if (isLocked()) throw new IllegalStateException("Items cannot be added to a locked store");
		synchronized (this) {
			OPTIONS.add(index, new StoreProduct(item, hotKey));
		}
		return this;
	}
	
	public Store removeOption(int index) {
		if (isLocked()) throw new IllegalStateException("Items cannot be removed from a locked store");
		synchronized (this) {
			OPTIONS.remove(index);
		}
		return this;
	}
	
	private static StoreItem getMostExpensiveItem(StoreOption option, int money, StoreItem mostExpensiveItem, HoldItem.HoldItemSlot findItemInSlot) {
		ArrayList<StoreOption> options = option.getOptions();
		if (options != null) {
			for (StoreOption o : options) {
				mostExpensiveItem = getMostExpensiveItem(o, money, mostExpensiveItem, findItemInSlot);
			}
		} else if (option.getStoreItem() != null) {
			if (mostExpensiveItem == null && !option.getStoreItem().equals(Bench.DEFAULT_BENCH)) {
				return option.getStoreItem();
			} else {
				if (!option.getStoreItem().equals(Bench.DEFAULT_BENCH) && option.getStoreItem().getCost() > mostExpensiveItem.getCost() && option.getStoreItem().getCost() <= money && (findItemInSlot == null || (option.getStoreItem().getHoldItem() != null && findItemInSlot.equals(option.getStoreItem().getHoldItem().getHoldSlot())))) {
					return option.getStoreItem();
				}
			}
		}
		return mostExpensiveItem;
	}
	
	private StoreItem getMostExpensiveItem(int money, HoldItem.HoldItemSlot findItemInSlot) {
		StoreItem mostExpensiveItem = null;
		for (StoreOption o : OPTIONS) {
			mostExpensiveItem = getMostExpensiveItem(o, money, mostExpensiveItem, findItemInSlot);
		}
		return mostExpensiveItem;
	}
	
	private ArrayList<StoreItem> getAutobuyItems(Player player) {
		int money = player.getMoney();
		ArrayList<StoreItem> items = new ArrayList<StoreItem>();
		if (Weapon.DEFAULT_PRIMARY_WEAPON_SLOT.getCapacity() == null || player.getNumberOfItemsInSlot(Weapon.DEFAULT_PRIMARY_WEAPON_SLOT) < Weapon.DEFAULT_PRIMARY_WEAPON_SLOT.getCapacity()) {
			StoreItem newItem = getMostExpensiveItem(money, Weapon.DEFAULT_PRIMARY_WEAPON_SLOT);
			if (newItem != null) {
				items.add(newItem.getItemCopy());
				money -= newItem.getCost();
			}
		}
		if (Weapon.DEFAULT_SECONDARY_WEAPON_SLOT.getCapacity() == null || player.getNumberOfItemsInSlot(Weapon.DEFAULT_SECONDARY_WEAPON_SLOT) < Weapon.DEFAULT_SECONDARY_WEAPON_SLOT.getCapacity()) {
			StoreItem newItem = getMostExpensiveItem(money, Weapon.DEFAULT_SECONDARY_WEAPON_SLOT);
			if (newItem != null) {
				items.add(newItem.getItemCopy());
				money -= newItem.getCost();
			}
		}
		if (Weapon.DEFAULT_THROWABLE_WEAPON_SLOT.getCapacity() == null || player.getNumberOfItemsInSlot(Weapon.DEFAULT_THROWABLE_WEAPON_SLOT) < Weapon.DEFAULT_THROWABLE_WEAPON_SLOT.getCapacity()) {
			StoreItem newItem = getMostExpensiveItem(money, Weapon.DEFAULT_THROWABLE_WEAPON_SLOT);
			if (newItem != null) {
				items.add(newItem.getItemCopy());
				money -= newItem.getCost();
			}
		}
		if (Weapon.DEFAULT_MELEE_WEAPON_SLOT.getCapacity() == null || player.getNumberOfItemsInSlot(Weapon.DEFAULT_MELEE_WEAPON_SLOT) < Weapon.DEFAULT_MELEE_WEAPON_SLOT.getCapacity()) {
			StoreItem newItem = getMostExpensiveItem(money, Weapon.DEFAULT_MELEE_WEAPON_SLOT);
			if (newItem != null) {
				items.add(newItem.getItemCopy());
				money -= newItem.getCost();
			}
		}
		if (player.getMap().isBombMap() && player.getTeam() != null && player.getTeam().isDefuseTeam()) {
			StoreItem newItem = DefuseKit.createDefaultDefuseKit();
			items.add(newItem.getItemCopy());
			money -= newItem.getCost();
		}
		if (money >= Player.KevlarAndHelmet.DEFAULT_KEVLAR_AND_HELMET.getCost()) {
			items.add(Player.KevlarAndHelmet.DEFAULT_KEVLAR_AND_HELMET);
			money -= Player.KevlarAndHelmet.DEFAULT_KEVLAR_AND_HELMET.getCost();
		} else if (money >= Player.Kevlar.DEFAULT_KEVLAR.getCost()) {
			items.add(Player.Kevlar.DEFAULT_KEVLAR);
			money -= Player.Kevlar.DEFAULT_KEVLAR.getCost();
		}
		items.add(Gun.AmmoCategory.PRIMARY);
		items.add(Gun.AmmoCategory.SECONDARY);
		return items;
	}
	
	public void openStore(final Player PLAYER, final ProjectionPlane.DialogDisposedAction<StoreItem[]> ACTION, boolean isAutoBuy, final StoreItem... DISABLE_ITEMS_LIST) {
		final int MONEY = PLAYER.getMoney();
		synchronized (this) {
			this.boughtItems = new ArrayList<StoreItem>();
		}
		if (isAutoBuy) {
			synchronized (this.boughtItems) {
				this.boughtItems.addAll(getAutobuyItems(PLAYER));
			}
			ACTION.dialogDisposed(Store.this.boughtItems.toArray(new StoreItem[Store.this.boughtItems.size()]));
			return;
		}
		final String FIRST_CONTAINER_NAME = "first";
		final ProjectionPlane.ModalInternalFrame DIALOG = ProjectionPlane.getSingleton().addInternalFrame(getStoreTitle(), true, true, true, true, true, true, true, new ProjectionPlane.DialogDisposedSimpleAction() {
			@Override
			public void dialogDisposed() {
				ACTION.dialogDisposed(Store.this.boughtItems.toArray(new StoreItem[Store.this.boughtItems.size()]));
			}
		});
		DIALOG.setSize(Math.min(800, ProjectionPlane.getSingleton().getWidth()), Math.min(500, ProjectionPlane.getSingleton().getHeight()));
		final AutobuyAction AUTOBUY_ACTION = new AutobuyAction(DIALOG, this, PLAYER);
		final CardLayout LAYOUT = new CardLayout();
		DIALOG.getContentPane().setLayout(new BorderLayout());
		
		final JPanel MAIN_PANEL = new JPanel(LAYOUT);
		final JPanel FIRST_PANEL = new JPanel(new BorderLayout());
		final StoreItemDisplayPanel DISPLAY_PANEL = new StoreItemDisplayPanel(DIALOG);
		
		JPanel moneyPanel = new JPanel(new BorderLayout());
		moneyPanel.add(new JLabel("Money: " + MONEY), BorderLayout.EAST);
		DIALOG.getContentPane().add(moneyPanel, BorderLayout.NORTH);
		
		JPanel innerSelectionPanel = new JPanel(new GridLayout(0, 1));
		ArrayList<SimpleAction> actions = new ArrayList<SimpleAction>();
		for (int i = 0; i < OPTIONS.size(); i++) {
			final int I = i;
			final JButton OPTION_BUTTON = new JButton((OPTIONS.get(i).getHotKeys() != null && OPTIONS.get(i).getHotKeys().length > 0 ? "(" + (char) OPTIONS.get(i).getHotKeys()[0].getKeyCode() + ") " : "") + OPTIONS.get(i).getName());
			OPTION_BUTTON.setEnabled(OPTIONS.get(i).isEnabled(MONEY, DISABLE_ITEMS_LIST));
			OPTION_BUTTON.setContentAreaFilled(false);
			OPTION_BUTTON.setBackground(Color.LIGHT_GRAY);
			OPTION_BUTTON.addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {
				}
				@Override
				public void mousePressed(MouseEvent e) {
				}
				@Override
				public void mouseExited(MouseEvent e) {
					DISPLAY_PANEL.showItem(null);
					OPTION_BUTTON.setOpaque(false);
				}
				@Override
				public void mouseEntered(MouseEvent e) {
					DISPLAY_PANEL.showItem(OPTIONS.get(I).getStoreItem());
					OPTION_BUTTON.setOpaque(true);
				}
				@Override
				public void mouseClicked(MouseEvent e) {
				}
			});
			ActionListener actionListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					StoreItem boughtItem = OPTIONS.get(I).optionClicked(MAIN_PANEL, LAYOUT, FIRST_CONTAINER_NAME + "|" + String.valueOf(I));
					if (boughtItem != null) {
						synchronized (Store.this.boughtItems) {
							Store.this.boughtItems.add(boughtItem);
						}
						DIALOG.dispose();
					}
				}
			};
			OPTION_BUTTON.addActionListener(actionListener);
			actions.add(new SimpleAction(actionListener));
			innerSelectionPanel.add(OPTION_BUTTON);
			Container selectedPanel = OPTIONS.get(i).getSelectedPanel(DIALOG, MAIN_PANEL, LAYOUT, FIRST_CONTAINER_NAME, FIRST_CONTAINER_NAME + "|" + String.valueOf(I), MONEY, AUTOBUY_ACTION, DISABLE_ITEMS_LIST);
			if (selectedPanel != null) {
				MAIN_PANEL.add(selectedPanel, FIRST_CONTAINER_NAME + "|" + String.valueOf(I));
			}
			if (OPTIONS.get(i).getStoreItem() != null) {
				DISPLAY_PANEL.addItem(DIALOG, OPTIONS.get(i).getStoreItem());
			}
		}
		
		JPanel mainSelectionPanel = new JPanel(new BorderLayout());
		mainSelectionPanel.add(new JScrollPane(innerSelectionPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel(new GridLayout(0, 1));
		JButton autoBuyButton = new JButton("(A) Autobuy");
		autoBuyButton.setContentAreaFilled(false);
		autoBuyButton.addActionListener(AUTOBUY_ACTION);
		bottomPanel.add(autoBuyButton);
		JButton exitButton = new JButton("(0) Exit");
		exitButton.setContentAreaFilled(false);
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DIALOG.dispose();
			}
		});
		bottomPanel.add(exitButton);
		mainSelectionPanel.add(bottomPanel, BorderLayout.SOUTH);
		FIRST_PANEL.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
				FIRST_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("0"), CloseStoreAction.CLOSE_STORE_ACTION_STRING);
				FIRST_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("NUMPAD0"), CloseStoreAction.CLOSE_STORE_ACTION_STRING);
				FIRST_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), CloseStoreAction.CLOSE_STORE_ACTION_STRING);
				FIRST_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('a'), AutobuyAction.AUTOBUY_ACTION_STRING);
				for (int i = 0; i < OPTIONS.size(); i++) {
					if (OPTIONS.get(i).isEnabled(MONEY, DISABLE_ITEMS_LIST) && OPTIONS.get(i).getHotKeys() != null) {
						for (KeyStroke k : OPTIONS.get(i).getHotKeys()) {
							FIRST_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, String.valueOf(i));
						}
					}
				}
			}
			@Override
			public void componentResized(ComponentEvent e) {
			}
			@Override
			public void componentMoved(ComponentEvent e) {
			}
			@Override
			public void componentHidden(ComponentEvent e) {
				FIRST_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("0"), "none");
				FIRST_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("NUMPAD0"), "none");
				FIRST_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "none");
				FIRST_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('a'), "none");
				for (int i = 0; i < OPTIONS.size(); i++) {
					if (OPTIONS.get(i).getHotKeys() != null) {
						for (KeyStroke k : OPTIONS.get(i).getHotKeys()) {
							FIRST_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "none");
						}
					}
				}
			}
		});
		FIRST_PANEL.getActionMap().put(CloseStoreAction.CLOSE_STORE_ACTION_STRING, new CloseStoreAction(DIALOG));
		FIRST_PANEL.getActionMap().put(AutobuyAction.AUTOBUY_ACTION_STRING, AUTOBUY_ACTION);
		for (int i = 0; i < OPTIONS.size(); i++) {
			FIRST_PANEL.getActionMap().put(String.valueOf(i), actions.get(i));
		}
		
		JSplitPane mainCenterPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainSelectionPanel, DISPLAY_PANEL.getPanel());
		mainCenterPanel.setContinuousLayout(true);
		FIRST_PANEL.add(mainCenterPanel, BorderLayout.CENTER);
		
		MAIN_PANEL.add(FIRST_PANEL, FIRST_CONTAINER_NAME);
		LAYOUT.show(MAIN_PANEL, FIRST_CONTAINER_NAME);
		DIALOG.getContentPane().add(MAIN_PANEL);
		DIALOG.setVisible(true);
	}
	
	public boolean isLocked() {
		return IS_LOCKED.get();
	}
	
	public Store lock() {
		IS_LOCKED.set(true);
		return this;
	}

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.boughtItems = new ArrayList<StoreItem>();
    }
	
	/**
	 * Creates an array with a certain number of unique key strokes.
	 * @param length the number of key strokes
	 * @return an array of unique key strokes
	 */
	public static KeyStroke[] createHotKeysArray(int length) {
		length = Math.abs(length);
		KeyStroke[] array = new KeyStroke[length];
		for (int i = 0; i < length; i++) {
			array[i] = KeyStroke.getKeyStroke(String.valueOf((char) (((int) '1') + i)));
		}
		return array;
	}
	
	public static void drawStoreIcon(Graphics g, Color color, int x, int y, int width, int height) {
		g.setColor(color);
		g.drawLine(x + (int) (width * 0.15), y + (int) (height * 0.3), x + (int) (width * 0.85), y + (int) (height * 0.3));
		g.drawLine(x + (int) (width * 0.2), y + (int) (height * 0.4), x + (int) (width * 0.833), y + (int) (height * 0.4));
		g.drawLine(x + (int) (width * 0.25), y + (int) (height * 0.5), x + (int) (width * 0.816), y + (int) (height * 0.5));
		g.drawLine(x + (int) (width * 0.3), y + (int) (height * 0.6), x + (int) (width * 0.8), y + (int) (height * 0.6));
		g.drawLine(x + (int) (width * 0.8), y + (int) (height * 0.6), x + (int) (width * 0.866), y + (int) (height * 0.2));
		g.fillOval(x + (int) (width * 0.25), y + (int) (height * 0.65), (int) (width * 0.15), (int) (height * 0.15));
		g.fillOval(x + (int) (width * 0.65), y + (int) (height * 0.65), (int) (width * 0.15), (int) (height * 0.15));
	}
	
	private static Integer getKeyStrokeDigit(KeyStroke keystroke) {
		if (keystroke != null) {
			int value = Character.getNumericValue(keystroke.getKeyCode());
			return value >= 0 ? value : null;
		}
		return null;
	}
	
	private abstract class StoreOption implements Serializable {
		private static final long serialVersionUID = 7141299937013867323L;
		protected abstract String getName();
		protected abstract KeyStroke[] getHotKeys();
		protected abstract boolean isEnabled(int money, StoreItem... disableItemsList);
		protected abstract Container getSelectedPanel(ProjectionPlane.ModalInternalFrame dialog, Container mainPanel, CardLayout layout, String previousContainerName, String currentContainerName, int money, AutobuyAction AUTO_BUY_ACTION, StoreItem... disableItemsList);
		protected abstract StoreItem optionClicked(Container parent, CardLayout layout, String currentContainerName);
		protected abstract StoreItem getStoreItem();
		protected abstract ArrayList<StoreOption> getOptions();
	}
	
	public class StoreCategory extends StoreOption {
		private static final long serialVersionUID = -7917693671457178602L;
		
		private final String NAME;
		private final KeyStroke[] HOT_KEYS;
		private final ArrayList<StoreOption> OPTIONS = new ArrayList<StoreOption>();
		
		private StoreCategory(String name, KeyStroke... hotKeys) {
			if (name == null) throw new IllegalArgumentException("name cannot be null");
			NAME = name;
			ArrayList<KeyStroke> keys = new ArrayList<KeyStroke>();
			for (KeyStroke k : hotKeys) {
				keys.add(k);
				Integer digit = Store.getKeyStrokeDigit(k);
				if (digit != null) {
					keys.add(KeyStroke.getKeyStroke("NUMPAD" + digit));
				}
			}
			HOT_KEYS = keys.toArray(new KeyStroke[keys.size()]);
		}

		@Override
		protected String getName() {
			return NAME;
		}

		@Override
		protected KeyStroke[] getHotKeys() {
			return HOT_KEYS;
		}
		
		@Override
		protected boolean isEnabled(int money, StoreItem... disableItemsList) {
			return true;
		}
		
		protected StoreCategory addCategory(String name, KeyStroke hotKey) {
			return addCategory(OPTIONS.size(), name, hotKey);
		}
		
		protected StoreCategory addCategory(int index, String name, KeyStroke hotKey) {
			if (isLocked()) throw new IllegalStateException("Categories cannot be added to a locked store");
			StoreCategory category = new StoreCategory(name, hotKey);
			synchronized (this) {
				OPTIONS.add(index, category);
			}
			return category;
		}
		
		public StoreCategory addItem(StoreItem[] items) {
			return addItem(items, createHotKeysArray(items.length));
		}
		
		protected StoreCategory addItem(StoreItem[] items, KeyStroke... hotKeys) {
			if (items.length != hotKeys.length) throw new IllegalArgumentException("the number of items must equal the number of hotkeys");
			for (int i = 0; i < items.length; i++) {
				addItem(items[i], hotKeys[i]);
			}
			return this;
		}
		
		protected StoreCategory addItem(StoreItem item, KeyStroke hotKey) {
			return addItem(OPTIONS.size(), item, hotKey);
		}
		
		protected StoreCategory addItem(int index, StoreItem item, KeyStroke hotKey) {
			if (isLocked()) throw new IllegalStateException("Items cannot be added to a locked store");
			synchronized (this) {
				OPTIONS.add(index, new StoreProduct(item, hotKey));
			}
			return this;
		}
		
		protected StoreCategory removeOption(StoreOption option) {
			if (isLocked()) throw new IllegalStateException("Items cannot be removed from a locked store");
			synchronized (this) {
				OPTIONS.remove(option);
			}
			return this;
		}

		@Override
		protected Container getSelectedPanel(final ProjectionPlane.ModalInternalFrame DIALOG, final Container MAIN_PANEL, final CardLayout LAYOUT, final String PARENT_CONTAINER_NAME, final String CURRENT_CONTAINER_NAME, final int MONEY, AutobuyAction AUTO_BUY_ACTION, final StoreItem... DISABLE_ITEMS_LIST) {
			final StoreItemDisplayPanel DISPLAY_PANEL = new StoreItemDisplayPanel(DIALOG);
			
			JPanel innerSelectionPanel = new JPanel(new GridLayout(0, 1));
			ArrayList<SimpleAction> actions = new ArrayList<SimpleAction>();
			for (int i = 0; i < OPTIONS.size(); i++) {
				final int I = i;
				final JButton OPTION_BUTTON = new JButton((OPTIONS.get(i).getHotKeys() != null && OPTIONS.get(i).getHotKeys().length > 0 && OPTIONS.get(i).getHotKeys()[0] != null ? "(" + (char) OPTIONS.get(i).getHotKeys()[0].getKeyCode() + ") " : "") + OPTIONS.get(i).getName());
				OPTION_BUTTON.setEnabled(OPTIONS.get(i).isEnabled(MONEY, DISABLE_ITEMS_LIST));
				OPTION_BUTTON.setContentAreaFilled(false);
				OPTION_BUTTON.setBackground(Color.LIGHT_GRAY);
				OPTION_BUTTON.addMouseListener(new MouseListener() {
					@Override
					public void mouseReleased(MouseEvent e) {
					}
					@Override
					public void mousePressed(MouseEvent e) {
					}
					@Override
					public void mouseExited(MouseEvent e) {
						DISPLAY_PANEL.showItem(null);
						OPTION_BUTTON.setOpaque(false);
					}
					@Override
					public void mouseEntered(MouseEvent e) {
						DISPLAY_PANEL.showItem(OPTIONS.get(I).getStoreItem());
						OPTION_BUTTON.setOpaque(true);
					}
					@Override
					public void mouseClicked(MouseEvent e) {
					}
				});
				ActionListener actionListener = new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						StoreItem boughtItem = OPTIONS.get(I).optionClicked(MAIN_PANEL, LAYOUT, CURRENT_CONTAINER_NAME + "|" + String.valueOf(I));
						if (boughtItem != null) {
							synchronized (Store.this.boughtItems) {
								Store.this.boughtItems.add(boughtItem);
							}
							DIALOG.dispose();
						}
					}
				};
				OPTION_BUTTON.addActionListener(actionListener);
				actions.add(new SimpleAction(actionListener));
				innerSelectionPanel.add(OPTION_BUTTON);
				Container selectedPanel = OPTIONS.get(i).getSelectedPanel(DIALOG, MAIN_PANEL, LAYOUT, CURRENT_CONTAINER_NAME, CURRENT_CONTAINER_NAME + "|" + String.valueOf(I), MONEY, AUTO_BUY_ACTION, DISABLE_ITEMS_LIST);
				if (selectedPanel != null) {
					MAIN_PANEL.add(selectedPanel, CURRENT_CONTAINER_NAME + "|" + String.valueOf(I));
				}
				if (OPTIONS.get(i).getStoreItem() != null) {
					DISPLAY_PANEL.addItem(DIALOG, OPTIONS.get(i).getStoreItem());
				}
			}
			
			JPanel mainSelectionPanel = new JPanel(new BorderLayout());
			mainSelectionPanel.add(new JScrollPane(innerSelectionPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
			
			final JSplitPane MAIN_CENTER_PANEL = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainSelectionPanel, DISPLAY_PANEL.getPanel());
			MAIN_CENTER_PANEL.setContinuousLayout(true);
			MAIN_CENTER_PANEL.addComponentListener(new ComponentListener() {
				@Override
				public void componentShown(ComponentEvent e) {
					MAIN_CENTER_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("0"), CloseStoreAction.CLOSE_STORE_ACTION_STRING);
					MAIN_CENTER_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("NUMPAD0"), CloseStoreAction.CLOSE_STORE_ACTION_STRING);
					MAIN_CENTER_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), CloseStoreAction.CLOSE_STORE_ACTION_STRING);
					MAIN_CENTER_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('a'), AutobuyAction.AUTOBUY_ACTION_STRING);
					for (int i = 0; i < OPTIONS.size(); i++) {
						if (OPTIONS.get(i).isEnabled(MONEY, DISABLE_ITEMS_LIST) && OPTIONS.get(i).getHotKeys() != null) {
							for (KeyStroke k : OPTIONS.get(i).getHotKeys()) {
								MAIN_CENTER_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, String.valueOf(i));
							}
						}
					}
				}
				@Override
				public void componentResized(ComponentEvent e) {
				}
				@Override
				public void componentMoved(ComponentEvent e) {
				}
				@Override
				public void componentHidden(ComponentEvent e) {
					MAIN_CENTER_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("0"), "none");
					MAIN_CENTER_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("NUMPAD0"), "none");
					MAIN_CENTER_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "none");
					MAIN_CENTER_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('a'), "none");
					for (int i = 0; i < OPTIONS.size(); i++) {
						if (OPTIONS.get(i).getHotKeys() != null) {
							for (KeyStroke k : OPTIONS.get(i).getHotKeys()) {
								MAIN_CENTER_PANEL.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "none");
							}
						}
					}
				}
			});
			MAIN_CENTER_PANEL.getActionMap().put(CloseStoreAction.CLOSE_STORE_ACTION_STRING, new BackAction(MAIN_PANEL, LAYOUT, PARENT_CONTAINER_NAME));
			MAIN_CENTER_PANEL.getActionMap().put(AutobuyAction.AUTOBUY_ACTION_STRING, AUTO_BUY_ACTION);
			for (int i = 0; i < OPTIONS.size(); i++) {
				MAIN_CENTER_PANEL.getActionMap().put(String.valueOf(i), actions.get(i));
			}
			
			JButton exitButton = new JButton("(0) Back");
			exitButton.setContentAreaFilled(false);
			exitButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					LAYOUT.show(MAIN_PANEL, PARENT_CONTAINER_NAME);
				}
			});
			mainSelectionPanel.add(exitButton, BorderLayout.SOUTH);
			
			return MAIN_CENTER_PANEL;
		}

		@Override
		protected StoreItem optionClicked(Container parent, CardLayout layout, String currentContainerName) {
			layout.show(parent, currentContainerName);
			return null;
		}

		@Override
		protected StoreItem getStoreItem() {
			return null;
		}

		@Override
		protected ArrayList<StoreOption> getOptions() {
			return new ArrayList<StoreOption>(OPTIONS);
		}
		
		public final Store store() {
			return Store.this;
		}
	}
	
	public class StoreProduct extends StoreOption {
		private static final long serialVersionUID = 2008046784445394725L;
		
		private final String NAME;
		private final KeyStroke[] HOT_KEYS;
		private final StoreItem STORE_ITEM;
		
		private StoreProduct(StoreItem item, KeyStroke... hotKeys) {
			if (item == null) throw new IllegalArgumentException("item cannot be null");
			NAME = item.getName();
			ArrayList<KeyStroke> keys = new ArrayList<KeyStroke>();
			for (KeyStroke k : hotKeys) {
				keys.add(k);
				Integer digit = Store.getKeyStrokeDigit(k);
				if (digit != null) {
					keys.add(KeyStroke.getKeyStroke("NUMPAD" + digit));
				}
			}
			HOT_KEYS = keys.toArray(new KeyStroke[keys.size()]);
			STORE_ITEM = item;
		}

		@Override
		protected String getName() {
			return NAME;
		}

		@Override
		protected KeyStroke[] getHotKeys() {
			return HOT_KEYS;
		}
		
		@Override
		protected boolean isEnabled(int money, StoreItem... disableItemsList) {
			return money >= getStoreItem().getCost() && (disableItemsList == null || !Arrays.asList(disableItemsList).contains(getStoreItem()));
		}

		@Override
		protected StoreItem getStoreItem() {
			return STORE_ITEM;
		}

		@Override
		protected Container getSelectedPanel(ProjectionPlane.ModalInternalFrame dialog, Container mainPanel, CardLayout layout, String previousContainerName, String currentContainerName, int money, AutobuyAction AUTO_BUY_ACTION, StoreItem... disableItemsList) {
			return null;
		}

		@Override
		protected StoreItem optionClicked(Container parent, CardLayout layout, String currentContainerName) {
			return getStoreItem().getItemCopy();
		}

		@Override
		protected ArrayList<StoreOption> getOptions() {
			return null;
		}
	}
	
	private static class StoreItemDisplayPanel {
		private static final AtomicInteger ID_ASSIGNER = new AtomicInteger();
		private final CardLayout LAYOUT = new CardLayout();
		private final JPanel PANEL = new JPanel(LAYOUT);
		private final HashMap<StoreItem, String> PANELS = new HashMap<StoreItem, String>();
		
		private StoreItemDisplayPanel(ProjectionPlane.ModalInternalFrame dialog, StoreItem... items) {
			PANEL.add(new JPanel(), "empty");
			if (items != null) {
				for (StoreItem i : items) {
					addItem(dialog, i);
				}
			}
			showItem(null);
		}
		
		private void addItem(ProjectionPlane.ModalInternalFrame dialog, StoreItem item) {
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(new JLabel(item.getName(), SwingConstants.CENTER), BorderLayout.NORTH);
			panel.add(new JLabel(new ImageIcon(Main.resize(item.getImage(), Math.min(180 * item.getImage().getWidth() / item.getImage().getHeight(), dialog.getWidth() - 150), 180))), BorderLayout.CENTER);
			JPanel detailsPanel = new JPanel(new GridLayout(0, 1));
			for (String s : item.getStoreInformation()) {
				detailsPanel.add(new JLabel(s));
			}
			panel.add(new JScrollPane(detailsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.SOUTH);
			String name = String.valueOf(ID_ASSIGNER.getAndIncrement());
			synchronized (this) {
				PANEL.add(panel, name);
				PANELS.put(item, name);
			}
		}
		
		private JPanel getPanel() {
			return PANEL;
		}
		
		private void showItem(StoreItem item) {
			if (item != null) {
				LAYOUT.show(PANEL, PANELS.get(item));
			} else {
				LAYOUT.show(PANEL, "empty");
			}
		}
	}
	
	private static class SimpleAction extends AbstractAction implements ActionListener {
		private static final long serialVersionUID = 1296398174501027109L;
		
		private final ActionListener ACTION;
		
		private SimpleAction(ActionListener action) {
			ACTION = action;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			ACTION.actionPerformed(e);
		}
		
	}
	
	private static class BackAction extends AbstractAction {
		private static final long serialVersionUID = 5948963705850402588L;
		
		private final Container PARENT;
		private final CardLayout LAYOUT;
		private final String BACK_STRING;
		
		private BackAction(Container parent, CardLayout layout, String backString) {
			PARENT = parent;
			LAYOUT = layout;
			BACK_STRING = backString;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LAYOUT.show(PARENT, BACK_STRING);
		}
		
	}
	
	private static class AutobuyAction extends AbstractAction {
		private static final long serialVersionUID = -340466807434456704L;
		
		private static final String AUTOBUY_ACTION_STRING = "CLOSE_STORE_ACTION_STRING";
		
		private final ProjectionPlane.ModalInternalFrame DIALOG;
		private final Store STORE;
		private final Player PLAYER;
		
		private AutobuyAction(ProjectionPlane.ModalInternalFrame dialog, Store store, Player player) {
			DIALOG = dialog;
			STORE = store;
			PLAYER = player;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			ArrayList<StoreItem> boughtItems = STORE.getAutobuyItems(PLAYER);
			synchronized (STORE.boughtItems) {
				STORE.boughtItems.addAll(boughtItems);
			}
			if (DIALOG != null) {
				DIALOG.dispose();
			}
		}
		
	}
	
	private static class CloseStoreAction extends AbstractAction {
		private static final long serialVersionUID = -340466807434456704L;
		
		private static final String CLOSE_STORE_ACTION_STRING = "CLOSE_STORE_ACTION_STRING";
		
		private final ProjectionPlane.ModalInternalFrame DIALOG;
		
		private CloseStoreAction(ProjectionPlane.ModalInternalFrame dialog) {
			DIALOG = dialog;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			DIALOG.dispose();
		}
		
	}
	
	public abstract static class BasicStoreItem implements StoreItem {
		private static final long serialVersionUID = -1406434267640601798L;
		
		private final String NAME;
		private final int COST;
		private transient BufferedImage image = null;
		
		public BasicStoreItem(String name, int cost, BufferedImage image) {
			NAME = name;
			COST = cost;
			this.image = image;
		}

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public int getCost() {
			return COST;
		}

		@Override
		public BufferedImage getImage() {
			return this.image;
		}
		
		private void writeObject(ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();
	        ImageIO.write(this.image, "png", out);
	    }

	    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	        in.defaultReadObject();
	        this.image = ImageIO.read(in);
	    }
	}
}
