import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class MapCreatorDialog {
	
	private static final Color SELECTED_COLOR = new Color(100, 0, 0, 20);
	
	private static final double MAX_HEIGHT = 1000;

	private static boolean cancelSetUp = true;
	private static DefaultMap defaultMap = null;

	public static void showMapCreatorDialog(Map initialMap, final ProjectionPlane.DialogDisposedAction<Map> ACTION) {
		synchronized (MapCreatorDialog.class) {
			MapCreatorDialog.defaultMap = null;
			MapCreatorDialog.cancelSetUp = true;
		}
		final MapModifierPanel MAP = initialMap != null ? new MapModifierPanel(initialMap) : new MapModifierPanel(5, 5);
		final ProjectionPlane.ModalInternalFrame FRAME = ProjectionPlane.getSingleton().addInternalFrame("Map Creator", true, true, true, true, true, true, true, new ProjectionPlane.DialogDisposedSimpleAction() {
			@Override
			public void dialogDisposed() {
				if (MapCreatorDialog.cancelSetUp) {
					ACTION.dialogDisposed(null);
				} else if (MapCreatorDialog.defaultMap != null) {
					ACTION.dialogDisposed(MapCreatorDialog.defaultMap.generateMap(DefaultMap.DefaultTeamSet.ZOMBIES_TEAM_SET));
				} else {
					Map newMap = new Map(MAP.width, MAP.height, "Custom Map", Team.DefaultTeams.COUNTER_ZOMBIES.getTeam(), Team.DefaultTeams.ZOMBIES.getTeam());
					for (int y = 0; y < MAP.height; y++) {
						for (int x = 0; x < MAP.width; x++) {
							newMap.setWall(new Wall.Builder(x, y).surfaceHeights(MAP.wallHeight[y][x]).build());
						}
					}
					ACTION.dialogDisposed(newMap);
				}
			}
		});
		FRAME.getContentPane().setLayout(new BorderLayout());
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		DefaultMap[] defaultMaps = new DefaultMap[DefaultMap.values().length + 1];
		int count = 0;
		defaultMaps[count++] = null;
		for (DefaultMap m : DefaultMap.values()) {
			defaultMaps[count++] = m;
		}
		final JComboBox<DefaultMap> DEFAULT_MAPS_COMBO_BOX = new JComboBox<DefaultMap>(defaultMaps);
		DEFAULT_MAPS_COMBO_BOX.setBorder(BorderFactory.createTitledBorder("Default maps"));
		DEFAULT_MAPS_COMBO_BOX.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (DEFAULT_MAPS_COMBO_BOX.getSelectedItem() != null) {
					try {
						synchronized (MapCreatorDialog.class) {
							MapCreatorDialog.defaultMap = (DefaultMap) DEFAULT_MAPS_COMBO_BOX.getSelectedItem();
						}
						if (MapCreatorDialog.defaultMap != null) {
							synchronized (MapCreatorDialog.class) {
								MapCreatorDialog.cancelSetUp = false;
							}
							FRAME.dispose();
						}
					} catch (ClassCastException ex) {
					}
				}
			}
		});
		final JCheckBox CUSTOM_MAP_CHECKBOX = new JCheckBox("Custom Map");
		final JPanel SIZES_PANEL = new JPanel(new GridLayout(1, 0));
		SIZES_PANEL.setBorder(BorderFactory.createTitledBorder("Map Size:"));
		final SpinnerNumberModel[] SPINNERS_MODELS = new SpinnerNumberModel[2];
		final JSpinner[] SPINNERS = new JSpinner[2];
		SPINNERS_MODELS[0] = new SpinnerNumberModel(MAP.width, Math.min(MAP.width, 3), Math.max(MAP.width, 30), 2);
		SPINNERS[0] = new JSpinner(SPINNERS_MODELS[0]);
		SPINNERS_MODELS[1] = new SpinnerNumberModel(MAP.height, Math.min(MAP.height, 3), Math.max(MAP.height, 30), 2);
		SPINNERS[1] = new JSpinner(SPINNERS_MODELS[1]);
		SIZES_PANEL.add(SPINNERS[0]);
		SIZES_PANEL.add(SPINNERS[1]);
		for (int i = 0; i < 2; i++) {
			SPINNERS[i].addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					MAP.setGridSize(SPINNERS_MODELS[0].getNumber().intValue(), SPINNERS_MODELS[1].getNumber().intValue());
				}
			});
		}
		final JPanel CENTER_PANEL = new JPanel(new BorderLayout());
		JPanel textFieldPanel = new JPanel(new GridLayout(0, 1));
		JTextField textField1 = new JTextField();
		textField1.setOpaque(false);
		textField1.setEditable(false);
		textField1.setBorder(BorderFactory.createEmptyBorder());
		textField1.setText("Drag or hold shift to select multiple cells.");
		JTextField textField2 = new JTextField();
		textField2.setOpaque(false);
		textField2.setEditable(false);
		textField2.setBorder(BorderFactory.createEmptyBorder());
		textField2.setText("Left click to specify height, middle click to decrement by 1, right click to increment by 1.");
		textFieldPanel.add(textField1);
		textFieldPanel.add(textField2);
		CENTER_PANEL.add(textFieldPanel, BorderLayout.NORTH);
		CENTER_PANEL.add(MAP, BorderLayout.CENTER);
		final JButton CREATE_BUTTON = new JButton("Create Map");
		CREATE_BUTTON.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (MapCreatorDialog.class) {
					MapCreatorDialog.cancelSetUp = false;
				}
				FRAME.dispose();
			}
		});
		mainPanel.add(SIZES_PANEL, BorderLayout.NORTH);
		mainPanel.add(CENTER_PANEL, BorderLayout.CENTER);
		JPanel customMapPanel = new JPanel(new BorderLayout());
		CUSTOM_MAP_CHECKBOX.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CREATE_BUTTON.setEnabled(CUSTOM_MAP_CHECKBOX.isSelected());
				DEFAULT_MAPS_COMBO_BOX.setEnabled(!CUSTOM_MAP_CHECKBOX.isSelected());
				Main.enableComponents(SIZES_PANEL, CUSTOM_MAP_CHECKBOX.isSelected());
				Main.enableComponents(CENTER_PANEL, CUSTOM_MAP_CHECKBOX.isSelected());
			}
		});
		CREATE_BUTTON.setEnabled(CUSTOM_MAP_CHECKBOX.isSelected());
		DEFAULT_MAPS_COMBO_BOX.setEnabled(!CUSTOM_MAP_CHECKBOX.isSelected());
		Main.enableComponents(SIZES_PANEL, CUSTOM_MAP_CHECKBOX.isSelected());
		Main.enableComponents(CENTER_PANEL, CUSTOM_MAP_CHECKBOX.isSelected());
		customMapPanel.add(CUSTOM_MAP_CHECKBOX, BorderLayout.NORTH);
		customMapPanel.add(mainPanel, BorderLayout.CENTER);
		
		FRAME.getContentPane().add(DEFAULT_MAPS_COMBO_BOX, BorderLayout.NORTH);
		FRAME.getContentPane().add(customMapPanel, BorderLayout.CENTER);
		FRAME.getContentPane().add(CREATE_BUTTON, BorderLayout.SOUTH);
		FRAME.setSize(600, 400);
	}

	private static class MapModifierPanel extends JPanel {
		private static final long serialVersionUID = -1110632794398992746L;
		
		private int width = 3;
		private int height = 3;
		private double[][] wallHeight = new double[this.height][this.width];
		private JPanel[][] wallHeightPanels = new JPanel[this.height][this.width];
		private JLabel[][] wallHeightLabels = new JLabel[this.height][this.width];

		private transient boolean cancelSetUp = true;
		private transient boolean dragged = false;
		private transient boolean drawDragBox = false;
		private transient Point lastDragged = null;
		private transient Point lastClicked = null;
		private boolean[][] selectedBoxes = new boolean[this.height][this.width];
		
		private MapModifierPanel(int width, int height) {
			super(new GridLayout(width, height));
			addListeners();
			setGridSize(width, height);
		}
		
		private MapModifierPanel(Map defaultMap) {
			super(new GridLayout(defaultMap.getWidth(), defaultMap.getHeight()));
			addListeners();
			setGridSize(defaultMap.getWidth(), defaultMap.getHeight());
			for (int y = 0; y < defaultMap.getHeight(); y++) {
				for (int x = 0; x < defaultMap.getWidth(); x++) {
					this.wallHeight[y][x] = defaultMap.getWall(x, y).getTopHeight(Integer.MAX_VALUE);
					this.wallHeight[y][x] = Math.max(Math.min(MapModifierPanel.this.wallHeight[y][x], MapCreatorDialog.MAX_HEIGHT), 0);
					this.wallHeightLabels[y][x].setText(String.valueOf(this.wallHeight[y][x]));
				}
			}
		}
		
		private void addListeners() {
			addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {
					if (isEnabled()) {
						if (dragged) {
							drawDragBox = false;
							if (lastClicked != null && lastDragged != null) {
								int dragBoxWidth = Math.abs(lastDragged.x - lastClicked.x);
								int dragBoxHeight = Math.abs(lastDragged.y - lastClicked.y);
								lastClicked.x = Math.min(lastClicked.x, lastDragged.x);
								lastClicked.y = Math.min(lastClicked.y, lastDragged.y);
								dragBoxWidth = Math.min(dragBoxWidth, getWidth() - lastClicked.x);
								dragBoxHeight = Math.min(dragBoxHeight, getHeight() - lastClicked.y);
								Rectangle2D dragBox = new Rectangle2D.Float(lastClicked.x, lastClicked.y, dragBoxWidth, dragBoxHeight);
								for (int y = 0; y < height; y++) {
									for (int x = 0; x < width; x++) {
										int leftX = MapModifierPanel.this.wallHeightPanels[y][x].getLocation().x;
										int topY = MapModifierPanel.this.wallHeightPanels[y][x].getLocation().y;
										int width = MapModifierPanel.this.wallHeightPanels[y][x].getWidth();
										int height = MapModifierPanel.this.wallHeightPanels[y][x].getHeight();
										if (dragBox.intersects(new Rectangle2D.Float(leftX, topY, width, height))) {
											MapModifierPanel.this.selectedBoxes[y][x] = true;
										} else if (!e.isShiftDown()) {
											MapModifierPanel.this.selectedBoxes[y][x] = false;
										}
									}
								}
							}
							repaintSelectedBoxes();
						}
						dragged = false;
						lastClicked = null;
					}
				}
				@Override
				public void mousePressed(MouseEvent e) {
					if (isEnabled()) {
						lastClicked = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), MapModifierPanel.this);
						drawDragBox = true;
						dragged = false;
					}
				}
				@Override
				public void mouseExited(MouseEvent e) {
				}
				@Override
				public void mouseEntered(MouseEvent e) {
				}
				@Override
				public void mouseClicked(MouseEvent e) {
				}
			});
			addMouseMotionListener(new MouseMotionListener() {
				@Override
				public void mouseMoved(MouseEvent e) {
				}
				@Override
				public void mouseDragged(MouseEvent e) {
					if (isEnabled()) {
						lastDragged = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), MapModifierPanel.this);
						dragged = true;
						repaint();
					}
				}
			});
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					this.selectedBoxes[y][x] = false;
				}
			}
			repaintSelectedBoxes();
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (drawDragBox && lastClicked != null && lastDragged != null) {
				g.setColor(new Color(0, 0, 255, 50));
				g.fillRect(Math.min(lastClicked.x, lastDragged.x), Math.min(lastClicked.y, lastDragged.y), Math.abs(lastDragged.x - lastClicked.x), Math.abs(lastDragged.y - lastClicked.y));
			}
		}
		
		private void repaintSelectedBoxes() {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (this.selectedBoxes[y][x]) {
						this.wallHeightPanels[y][x].setOpaque(true);
						this.wallHeightPanels[y][x].setBackground(SELECTED_COLOR);
					} else {
						this.wallHeightPanels[y][x].setOpaque(false);
					}
				}
			}
			repaint();
		}
		
		private void addAllSelectedBoxesHeight(Double addHeight) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (MapModifierPanel.this.selectedBoxes[y][x]) {
						if (addHeight != null) {
							MapModifierPanel.this.wallHeight[y][x] += addHeight;
							MapModifierPanel.this.wallHeight[y][x] = Math.max(Math.min(MapModifierPanel.this.wallHeight[y][x], MapCreatorDialog.MAX_HEIGHT), 0);
							MapModifierPanel.this.wallHeightLabels[y][x].setText(String.valueOf(MapModifierPanel.this.wallHeight[y][x]));
						}
					}
				}
			}
			repaint();
			repaintSelectedBoxes();
		}
		
		private void setAllSelectedBoxesHeight(Double newHeight) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (MapModifierPanel.this.selectedBoxes[y][x]) {
						if (newHeight != null) {
							MapModifierPanel.this.wallHeight[y][x] = newHeight;
							MapModifierPanel.this.wallHeight[y][x] = Math.max(Math.min(MapModifierPanel.this.wallHeight[y][x], MapCreatorDialog.MAX_HEIGHT), 0);
							MapModifierPanel.this.wallHeightLabels[y][x].setText(String.valueOf(MapModifierPanel.this.wallHeight[y][x]));
						}
						MapModifierPanel.this.selectedBoxes[y][x] = false;
					}
				}
			}
			repaint();
			repaintSelectedBoxes();
		}
		
		private Double promptNewHeight(double defaultValue) {
			final JDialog DIALOG = new JDialog((Frame) null, "Wall", true);
			DIALOG.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			DIALOG.getContentPane().setLayout(new BorderLayout());
			SpinnerNumberModel model = new SpinnerNumberModel(defaultValue, 0, MapCreatorDialog.MAX_HEIGHT, 0.5);
			JSpinner spinner = new JSpinner(model);
			spinner.setBorder(BorderFactory.createTitledBorder("Wall Height:"));
			JButton setButton = new JButton("Set");
			setButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cancelSetUp = false;
					DIALOG.dispose();
				}
			});
			DIALOG.getContentPane().add(spinner, BorderLayout.CENTER);
			DIALOG.getContentPane().add(setButton, BorderLayout.SOUTH);
			DIALOG.pack();
			DIALOG.setVisible(true);
			if (cancelSetUp) {
				return null;
			} else {
				return model.getNumber().doubleValue();
			}
		}
		
		private void setGridSize(int width, int height) {
			width = Math.max(width, 3);
			height = Math.max(height, 3);
			((GridLayout) getLayout()).setRows(height);
			((GridLayout) getLayout()).setColumns(width);
			removeAll();

			double[][] newHeights = new double[height][width];
			for (int y = 0; y < height; y++) {
				if (y < this.wallHeight.length) {
					for (int x = 0; x < width; x++) {
						if (x < this.wallHeight[y].length) {
							newHeights[y][x] = this.wallHeight[y][x];
						} else {
							break;
						}
					}
				} else {
					break;
				}
			}
			
			this.width = width;
			this.height = height;
			this.wallHeight = newHeights;
			this.wallHeightPanels = new JPanel[this.height][this.width];
			this.wallHeightLabels = new JLabel[this.height][this.width];
			this.selectedBoxes = new boolean[this.height][this.width];

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					this.wallHeightLabels[y][x] = new JLabel(String.valueOf(this.wallHeight[y][x]), SwingConstants.CENTER);
					this.wallHeightPanels[y][x] = new JPanel(new BorderLayout());
					this.wallHeightPanels[y][x].setOpaque(false);
					this.wallHeightPanels[y][x].addMouseMotionListener(new MouseMotionListener() {
						@Override
						public void mouseMoved(MouseEvent e) {
							MapModifierPanel.this.dispatchEvent(e);
						}
						@Override
						public void mouseDragged(MouseEvent e) {
							MapModifierPanel.this.dispatchEvent(e);
						}
					});
					this.wallHeightPanels[y][x].setBorder(BorderFactory.createLineBorder(Color.BLACK));
					this.wallHeightPanels[y][x].add(this.wallHeightLabels[y][x], BorderLayout.CENTER);
					final int X = x;
					final int Y = y;
					this.wallHeightPanels[y][x].addMouseListener(new MouseListener() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if (isEnabled()) {
								if (e.getButton() == MouseEvent.BUTTON1) {
									if (!e.isShiftDown()) {
										MapModifierPanel.this.selectedBoxes[Y][X] = true;
										repaintSelectedBoxes();
										Double newHeight = promptNewHeight(0);
										setAllSelectedBoxesHeight(newHeight);
										repaint();
										repaintSelectedBoxes();
										return;
									} else {
										MapModifierPanel.this.selectedBoxes[Y][X] = !MapModifierPanel.this.selectedBoxes[Y][X];
										repaintSelectedBoxes();
									}
								} else {
									if (e.getButton() == MouseEvent.BUTTON2) {
										MapModifierPanel.this.selectedBoxes[Y][X] = true;
										addAllSelectedBoxesHeight(-1d);
									} else if (e.getButton() == MouseEvent.BUTTON3) {
										MapModifierPanel.this.selectedBoxes[Y][X] = true;
										addAllSelectedBoxesHeight(1d);
									}
								}
							}
						}
						@Override
						public void mousePressed(MouseEvent e) {
							if (isEnabled()) {
								MapModifierPanel.this.dispatchEvent(e);
							}
						}
						@Override
						public void mouseReleased(MouseEvent e) {
							if (isEnabled()) {
								MapModifierPanel.this.dispatchEvent(e);
							}
						}
						@Override
						public void mouseEntered(MouseEvent e) {
						}
						@Override
						public void mouseExited(MouseEvent e) {
						}
					});
					add(this.wallHeightPanels[y][x]);
				}
			}
			
			revalidate();
			repaint();
		}
	}
}
