import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class SettingsMenu {
	
	public static final float MIN_DECIBEL_VOLUME = -30;
	public static final int MIN_FRAME_DELAY = 0;
	public static final int MAX_FRAME_DELAY = 200;
	public static final int MIN_SIGHT_RANGE = 1;
	public static final int MIN_RESOLUTION = 1;
	public static final int MAX_RESOLUTION = 20;
	
	private final String NAME;
	private final int FRAME_DELAY;
	private final int RESOLUTION;
	private final Integer SIGHT_RANGE;
	private final boolean SHOW_FPS;
	private final boolean SHOW_TIME;
	private final boolean SHOW_SHADING;
	private final int SENSITIVITY;
	private final float RELATIVE_VOLUME;
	private final boolean IS_MUTE;
	
	private static SettingsMenu settings = null;
	
	private SettingsMenu(String name, int frameDelay, int resolution, Integer sightRange, boolean showFPS, boolean showTime, boolean showShading, int sensitivity, float relativeVolume, boolean isMute) {
		NAME = name;
		FRAME_DELAY = frameDelay;
		RESOLUTION = resolution;
		SIGHT_RANGE = sightRange;
		SHOW_FPS = showFPS;
		SHOW_TIME = showTime;
		SHOW_SHADING = showShading;
		SENSITIVITY = sensitivity;
		RELATIVE_VOLUME = relativeVolume;
		IS_MUTE = isMute;
	}
	
	public static void showDialog(final ProjectionPlane P_PLANE, String defaultName, int defaultFrameDelay, final SmartAdjuster.IntegerPrecision FRAME_DELAY_SA, Integer defaultSightRange, int maxSightRange, final SmartAdjuster.DoublePrecision SIGHT_RANGE_SA, int defaultResolution, final SmartAdjuster.IntegerPrecision RESOLUTION_SA, boolean defaultShowFPS, boolean defaultShowTime, boolean defaultShowShading, int defaultSensitivity, float defaultRelativeVolume, boolean defaultIsMute, final ProjectionPlane.DialogDisposedAction<SettingsMenu> ACTION) {
		final ProjectionPlane.ModalInternalFrame DIALOG = P_PLANE.addInternalFrame("Settings", true, true, true, true, true, true, true, new ProjectionPlane.DialogDisposedSimpleAction() {
			@Override
			public void dialogDisposed() {
				ACTION.dialogDisposed(SettingsMenu.settings);
			}
		});
		DIALOG.setSize(Math.min(700, P_PLANE.getWidth()), Math.min(570 + (defaultSightRange != null ? 50 : 0), P_PLANE.getHeight()));
		DIALOG.setMinimumSize(new Dimension(Math.min(300, P_PLANE.getWidth()), Math.min(150, P_PLANE.getHeight())));
		DIALOG.getContentPane().setLayout(new BorderLayout());
		
		JPanel mainPanel = new JPanel(new GridLayout(0, 1));
		final JTextField NAME_TEXT_FIELD = new JTextField(defaultName);
		if (defaultName != null) {
			NAME_TEXT_FIELD.setBorder(BorderFactory.createTitledBorder("Name:"));
			NAME_TEXT_FIELD.setOpaque(false);
			mainPanel.add(NAME_TEXT_FIELD);
		}
		JPanel frameDelayPanel = new JPanel(new BorderLayout());
		frameDelayPanel.setBorder(BorderFactory.createTitledBorder("Frame delay (milliseconds):"));
		final JSlider FRAME_DELAY_SLIDER = new JSlider(JSlider.HORIZONTAL, MIN_FRAME_DELAY, MAX_FRAME_DELAY, Math.min(Math.max(defaultFrameDelay, MIN_FRAME_DELAY), MAX_FRAME_DELAY));
		FRAME_DELAY_SLIDER.setEnabled(!FRAME_DELAY_SA.isActivated());
		FRAME_DELAY_SLIDER.setMajorTickSpacing((FRAME_DELAY_SLIDER.getMaximum() - FRAME_DELAY_SLIDER.getMinimum()) / 10);
		FRAME_DELAY_SLIDER.setPaintTicks(true);
		FRAME_DELAY_SLIDER.setPaintLabels(true);
		frameDelayPanel.add(FRAME_DELAY_SLIDER, BorderLayout.CENTER);
		if (FRAME_DELAY_SA != null) {
			frameDelayPanel.add(FRAME_DELAY_SA.createEmbeddedAdjusterPanel(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					FRAME_DELAY_SLIDER.setEnabled(!FRAME_DELAY_SA.isActivated());
				}
			}), BorderLayout.EAST);
		}
		mainPanel.add(frameDelayPanel);
		JPanel resolutionPanel = new JPanel(new BorderLayout());
		resolutionPanel.setBorder(BorderFactory.createTitledBorder("Quality:"));
		final JSlider RESOLUTION_SLIDER = new JSlider(JSlider.HORIZONTAL, MIN_RESOLUTION, MAX_RESOLUTION, Math.min(Math.max(MAX_RESOLUTION - defaultResolution + 1, MIN_RESOLUTION), MAX_RESOLUTION));
		RESOLUTION_SLIDER.setEnabled(!RESOLUTION_SA.isActivated());
		RESOLUTION_SLIDER.setMajorTickSpacing((RESOLUTION_SLIDER.getMaximum() - RESOLUTION_SLIDER.getMinimum()) / 10);
		RESOLUTION_SLIDER.setPaintTicks(true);
		RESOLUTION_SLIDER.setPaintLabels(true);
		resolutionPanel.add(RESOLUTION_SLIDER, BorderLayout.CENTER);
		if (RESOLUTION_SA != null) {
			resolutionPanel.add(RESOLUTION_SA.createEmbeddedAdjusterPanel(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					RESOLUTION_SLIDER.setEnabled(!RESOLUTION_SA.isActivated());
				}
			}), BorderLayout.EAST);
		}
		mainPanel.add(resolutionPanel);
		final JSlider SIGHT_RANGE_SLIDER;
		if (defaultSightRange != null) {
			JPanel sightRangePanel = new JPanel(new BorderLayout());
			sightRangePanel.setBorder(BorderFactory.createTitledBorder("View Range (m):"));
			SIGHT_RANGE_SLIDER = new JSlider(JSlider.HORIZONTAL, MIN_SIGHT_RANGE, Math.max(Math.max(defaultSightRange, maxSightRange), 1), Math.max(Math.min(defaultSightRange, maxSightRange), 1));
			SIGHT_RANGE_SLIDER.setEnabled(!SIGHT_RANGE_SA.isActivated());
			SIGHT_RANGE_SLIDER.setMajorTickSpacing((SIGHT_RANGE_SLIDER.getMaximum() - SIGHT_RANGE_SLIDER.getMinimum()) / 10);
			SIGHT_RANGE_SLIDER.setPaintTicks(true);
			SIGHT_RANGE_SLIDER.setPaintLabels(true);
			sightRangePanel.add(SIGHT_RANGE_SLIDER, BorderLayout.CENTER);
			if (SIGHT_RANGE_SA != null) {
				sightRangePanel.add(SIGHT_RANGE_SA.createEmbeddedAdjusterPanel(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						SIGHT_RANGE_SLIDER.setEnabled(!SIGHT_RANGE_SA.isActivated());
					}
				}), BorderLayout.EAST);
			}
			mainPanel.add(sightRangePanel);
		} else {
			SIGHT_RANGE_SLIDER = null;
		}
		final JSlider SENSITIVITY_SLIDER = new JSlider(JSlider.HORIZONTAL, -ProjectionPlane.MAXIMUM_ABSOLUTE_SENSITIVITY, ProjectionPlane.MAXIMUM_ABSOLUTE_SENSITIVITY, Math.min(Math.max(defaultSensitivity, -ProjectionPlane.MAXIMUM_ABSOLUTE_SENSITIVITY), ProjectionPlane.MAXIMUM_ABSOLUTE_SENSITIVITY));
		SENSITIVITY_SLIDER.setBorder(BorderFactory.createTitledBorder("Sensitivity:"));
		SENSITIVITY_SLIDER.setMajorTickSpacing((SENSITIVITY_SLIDER.getMaximum() - SENSITIVITY_SLIDER.getMinimum()) / (ProjectionPlane.MAXIMUM_ABSOLUTE_SENSITIVITY * 2));
		SENSITIVITY_SLIDER.setPaintTicks(true);
		SENSITIVITY_SLIDER.setPaintLabels(true);
		mainPanel.add(SENSITIVITY_SLIDER);
		final JSlider RELATIVE_VOLUME_SLIDER = new JSlider(JSlider.HORIZONTAL, (int) MIN_DECIBEL_VOLUME, 6, Math.min(Math.max(defaultIsMute ? (int) MIN_DECIBEL_VOLUME : (int) Math.round(defaultRelativeVolume), (int) MIN_DECIBEL_VOLUME), 6));
		final JCheckBox MUTE_CHECKBOX = new JCheckBox("Mute", defaultIsMute);
		JPanel relativeVolumePanel = new JPanel(new BorderLayout());
		relativeVolumePanel.setBorder(BorderFactory.createTitledBorder("Volume:"));
		RELATIVE_VOLUME_SLIDER.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				MUTE_CHECKBOX.setSelected(false);
			}
		});
		Hashtable<Integer, JComponent> RELATIVE_VOLUME_SLIDER_LABELS = new Hashtable<Integer, JComponent>();
		RELATIVE_VOLUME_SLIDER_LABELS.put(RELATIVE_VOLUME_SLIDER.getMinimum(), new JLabel("Quiet"));
		RELATIVE_VOLUME_SLIDER_LABELS.put(RELATIVE_VOLUME_SLIDER.getMaximum(), new JLabel("Loud"));
		relativeVolumePanel.add(RELATIVE_VOLUME_SLIDER, BorderLayout.CENTER);
		RELATIVE_VOLUME_SLIDER.setLabelTable(RELATIVE_VOLUME_SLIDER_LABELS);
		RELATIVE_VOLUME_SLIDER.setPaintLabels(true);
		MUTE_CHECKBOX.addActionListener(new ActionListener() {
			private Integer volume = null;
			@Override
			public void actionPerformed(ActionEvent e) {
				if (MUTE_CHECKBOX.isSelected()) {
					if ((float) RELATIVE_VOLUME_SLIDER.getValue() != RELATIVE_VOLUME_SLIDER.getMinimum()) {
						this.volume = RELATIVE_VOLUME_SLIDER.getValue();
					}
					RELATIVE_VOLUME_SLIDER.setValue(RELATIVE_VOLUME_SLIDER.getMinimum());
					MUTE_CHECKBOX.setSelected(true);
				} else {
					if (this.volume != null) {
						RELATIVE_VOLUME_SLIDER.setValue(this.volume);
					}
					MUTE_CHECKBOX.setSelected(false);
				}
			}
		});
		relativeVolumePanel.add(MUTE_CHECKBOX, BorderLayout.WEST);
		mainPanel.add(relativeVolumePanel);
		
		JPanel booleanPanel = new JPanel(new GridLayout(0, 1));
		booleanPanel.setBorder(BorderFactory.createTitledBorder("Display Settings"));
		final JCheckBox FPS_CHECKBOX = new JCheckBox("Show FPS", defaultShowFPS);
		booleanPanel.add(FPS_CHECKBOX);
		final JCheckBox TIME_CHECKBOX = new JCheckBox("Show Time", defaultShowTime);
		booleanPanel.add(TIME_CHECKBOX);
		mainPanel.add(booleanPanel);
		final JCheckBox SHOW_SHADING_CHECKBOX = new JCheckBox("Show Shading", defaultShowShading);
		booleanPanel.add(SHOW_SHADING_CHECKBOX);
		final JCheckBox FULLSCREEN_CHECKBOX = new JCheckBox("Full Screen", P_PLANE.isFullScreen());
		FULLSCREEN_CHECKBOX.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (FULLSCREEN_CHECKBOX.isSelected()) {
					P_PLANE.setFullScreen();
				} else {
					P_PLANE.restoreScreen();
				}
			}
		});
		booleanPanel.add(FULLSCREEN_CHECKBOX);
		mainPanel.add(booleanPanel);
		
		synchronized (SettingsMenu.class) {
			SettingsMenu.settings = null;
		}
		
		JPanel mainKeyboardSettingsPanel = new JPanel(new BorderLayout());
		JButton editKeyButton = new JButton("Edit Key");
		JButton clearKeyButton = new JButton("Clear Key");
		final KeyboardConfigurationPanel KEYBOARD_CONFIGURATION_PANEL = new KeyboardConfigurationPanel(editKeyButton, clearKeyButton);
		
		JPanel keyboardConfigurePanel = new JPanel(new BorderLayout());
		JButton useDefaultButton = new JButton("Use Defaults");
		useDefaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				KEYBOARD_CONFIGURATION_PANEL.setToDefault();
			}
		});
		keyboardConfigurePanel.add(useDefaultButton, BorderLayout.WEST);
		editKeyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				KEYBOARD_CONFIGURATION_PANEL.editCurrentItem();
			}
		});
		clearKeyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				KEYBOARD_CONFIGURATION_PANEL.clearCurrentItem();
			}
		});
		JPanel editKeyAndClearKeyPanel = new JPanel(new GridLayout(1, 0));
		editKeyAndClearKeyPanel.add(editKeyButton);
		editKeyAndClearKeyPanel.add(clearKeyButton);
		keyboardConfigurePanel.add(editKeyAndClearKeyPanel, BorderLayout.EAST);
		
		mainKeyboardSettingsPanel.add(new JScrollPane(KEYBOARD_CONFIGURATION_PANEL.getPanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		mainKeyboardSettingsPanel.add(keyboardConfigurePanel, BorderLayout.SOUTH);
		
		JButton saveButton = new JButton("Save Settings");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (SettingsMenu.class) {
					SettingsMenu.settings = new SettingsMenu(NAME_TEXT_FIELD.getText(),
							FRAME_DELAY_SLIDER.getValue(),
							MAX_RESOLUTION - RESOLUTION_SLIDER.getValue() + 1,
							SIGHT_RANGE_SLIDER != null ? SIGHT_RANGE_SLIDER.getValue() : null,
							FPS_CHECKBOX.isSelected(),
							TIME_CHECKBOX.isSelected(),
							SHOW_SHADING_CHECKBOX.isSelected(),
							SENSITIVITY_SLIDER.getValue(),
							RELATIVE_VOLUME_SLIDER.getValue(),
							MUTE_CHECKBOX.isSelected());
				}
				DIALOG.dispose();
			}
		});
		
		JPanel mainPanelOuter = new JPanel(new BorderLayout());
		mainPanelOuter.add(new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		mainPanelOuter.add(saveButton, BorderLayout.SOUTH);
		
		JPanel aboutPanel = new JPanel();
		aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.PAGE_AXIS));
		aboutPanel.add(new JLabel(Main.NAME, SwingConstants.CENTER));
		aboutPanel.add(new JLabel("Version: " + Main.getVersion(), SwingConstants.CENTER));
		aboutPanel.add(new JLabel("Wentao Chen", SwingConstants.CENTER));
		
		JTabbedPane mainTabbedPane = new JTabbedPane();
		mainTabbedPane.addTab("Main", null, mainPanelOuter, "Main Settings");
		mainTabbedPane.addTab("Keyboard", null, mainKeyboardSettingsPanel, "Keyboard Settings");
		mainTabbedPane.addTab("About", null, aboutPanel, "About");

		DIALOG.getContentPane().add(mainTabbedPane, BorderLayout.CENTER);
	}

	public String getName() {
		return NAME;
	}

	public int getFrameDelay() {
		return FRAME_DELAY;
	}

	public int getResolution() {
		return RESOLUTION;
	}

	public Integer getSightRange() {
		return SIGHT_RANGE;
	}
	
	public boolean getShowFPS() {
		return SHOW_FPS;
	}
	
	public boolean getShowTime() {
		return SHOW_TIME;
	}
	
	public boolean getShowShading() {
		return SHOW_SHADING;
	}
	
	public int getSensitivity() {
		return SENSITIVITY;
	}
	
	public float getRelativeVolume() {
		return RELATIVE_VOLUME;
	}
	
	public boolean isMute() {
		return IS_MUTE;
	}
	
	private static class KeyboardConfigurationPanel {
		private final JPanel PANEL = new JPanel(new GridLayout(0, 2));
		private final ArrayList<KeyboardConfigurationLabel> KEY_LABELS = new ArrayList<KeyboardConfigurationLabel>();
		private final JButton EDIT_KEY_BUTTON;
		private final JButton CLEAR_KEY_BUTTON;
		
		private transient boolean isEditing = false;
		private transient KeyboardConfigurationLabel selectedItem = null;
		
		public KeyboardConfigurationPanel(JButton editKeyButton, JButton clearKeyButton) {
			EDIT_KEY_BUTTON = editKeyButton;
			CLEAR_KEY_BUTTON = clearKeyButton;
			CLEAR_KEY_BUTTON.setEnabled(false);
			PANEL.setBorder(BorderFactory.createLoweredBevelBorder());
			PANEL.setFocusable(true);
			PANEL.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
				}
				@Override
				public void focusLost(FocusEvent e) {
					synchronized (KeyboardConfigurationPanel.this) {
						KeyboardConfigurationPanel.this.isEditing = false;
					}
					EDIT_KEY_BUTTON.setEnabled(true);
				}
			});
			PANEL.addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent e) {
				}
				@Override
				public void keyPressed(KeyEvent e) {
				}
				@Override
				public void keyReleased(KeyEvent e) {
					if (KeyboardConfigurationPanel.this.isEditing) {
						if (KeyboardConfigurationPanel.this.selectedItem != null) {
							KeyboardConfigurationPanel.this.selectedItem.getItem().setCurrentKey(e.getKeyCode());
							KeyboardConfigurationPanel.this.selectedItem.getItem().setCurrentMouseButton(null);
						}
						unSelectCurrentItem();
					}
				}
			});
			boolean first = true;
			for (ProjectionPlane.InputConfigurationCategory c : ProjectionPlane.InputConfigurationCategory.values()) {
				if (!first) {
					PANEL.add(new JLabel());
					PANEL.add(new JLabel());
				} else {
					first = false;
				}
				JLabel titleNameLabel = new JLabel(c.getName());
				JLabel titleKeyButtonLabel = new JLabel("KEY/BUTTON");
				titleNameLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
				titleKeyButtonLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
				PANEL.add(titleNameLabel);
				PANEL.add(titleKeyButtonLabel);
				for (ProjectionPlane.InputConfiguration i : ProjectionPlane.InputConfiguration.values()) {
					if (i.getInputConfigurationCategory() == c) {
						final KeyboardConfigurationLabel KEY_LABEL = new KeyboardConfigurationLabel(i, Color.GRAY);
						MouseListener mouseListener = new MouseListener() {
							@Override
							public void mouseClicked(MouseEvent e) {
								if (KeyboardConfigurationPanel.this.selectedItem != KEY_LABEL) {
									unSelectCurrentItem();
								}
								KEY_LABEL.setHighlighted(!KEY_LABEL.isHighlighted());
								if (KEY_LABEL.isHighlighted()) {
									synchronized (KeyboardConfigurationPanel.this) {
										KeyboardConfigurationPanel.this.selectedItem = KEY_LABEL;
									}
									CLEAR_KEY_BUTTON.setEnabled(KeyboardConfigurationPanel.this.selectedItem.getItem().hasInput());
								} else {
									unSelectCurrentItem();
								}
							}
							@Override
							public void mousePressed(MouseEvent e) {
							}
							@Override
							public void mouseReleased(MouseEvent e) {
								if (KeyboardConfigurationPanel.this.isEditing) {
									if (KeyboardConfigurationPanel.this.selectedItem != null) {
										KeyboardConfigurationPanel.this.selectedItem.getItem().setCurrentKey(null);
										KeyboardConfigurationPanel.this.selectedItem.getItem().setCurrentMouseButton(e.getButton());
										unSelectCurrentItem();
									}
								}
							}
							@Override
							public void mouseEntered(MouseEvent e) {
							}
							@Override
							public void mouseExited(MouseEvent e) {
							}
						};
						KEY_LABEL.getNameLabel().addMouseListener(mouseListener);
						KEY_LABEL.getLabel().addMouseListener(mouseListener);
						PANEL.add(KEY_LABEL.getNameLabel());
						PANEL.add(KEY_LABEL.getLabel());
						KEY_LABELS.add(KEY_LABEL);
					}
				}
			}
		}
		
		public JPanel getPanel() {
			return PANEL;
		}
		
		private void unSelectCurrentItem() {
			if (this.selectedItem != null) {
				EDIT_KEY_BUTTON.setEnabled(true);
				CLEAR_KEY_BUTTON.setEnabled(false);
				this.selectedItem.updateText();
				this.selectedItem.setHighlighted(false);
				synchronized (this) {
					this.isEditing = false;
					this.selectedItem = null;
				}
			}
		}
		
		public void editCurrentItem() {
			if (this.selectedItem != null) {
				synchronized (this) {
					this.isEditing = true;
				}
				getPanel().requestFocusInWindow();
				EDIT_KEY_BUTTON.setEnabled(false);
				CLEAR_KEY_BUTTON.setEnabled(this.selectedItem.getItem().hasInput());
				this.selectedItem.setPromptMode();
			}
		}
		
		public void clearCurrentItem() {
			if (this.selectedItem != null) {
				KeyboardConfigurationPanel.this.selectedItem.getItem().setCurrentKey(null);
				KeyboardConfigurationPanel.this.selectedItem.getItem().setCurrentMouseButton(null);
			}
			unSelectCurrentItem();
		}
		
		public void setToDefault() {
			if (this.selectedItem != null) {
				this.selectedItem.setToDefault();
			} else {
				for (KeyboardConfigurationLabel l : KEY_LABELS) {
					l.setToDefault();
				}
			}
			unSelectCurrentItem();
		}
		
		private static class KeyboardConfigurationLabel {
			private final JLabel NAME_LABEL = new JLabel();
			private final JLabel LABEL = new JLabel();
			private final ProjectionPlane.InputConfiguration ITEM;
			public KeyboardConfigurationLabel(ProjectionPlane.InputConfiguration item, Color backgroundColor) {
				ITEM = item;
				NAME_LABEL.setText(item.getName());
				LABEL.setText(item.getInputStringRepresentation());
				NAME_LABEL.setBackground(backgroundColor);
				LABEL.setBackground(backgroundColor);
			}
			
			public JLabel getNameLabel() {
				return NAME_LABEL;
			}
			
			public JLabel getLabel() {
				return LABEL;
			}
			
			public ProjectionPlane.InputConfiguration getItem() {
				return ITEM;
			}
			
			public boolean isHighlighted() {
				return getLabel().isOpaque();
			}
			
			public void setHighlighted(boolean highlighted) {
				getNameLabel().setOpaque(highlighted);
				getLabel().setOpaque(highlighted);
				getNameLabel().repaint();
				getLabel().repaint();
			}
			
			public void setPromptMode() {
				getLabel().setText("???");
			}
			
			public void updateText() {
				getLabel().setText(getItem().getInputStringRepresentation());
			}
			
			public void setToDefault() {
				getItem().setToDefault();
				updateText();
			}
		}
	}
}
