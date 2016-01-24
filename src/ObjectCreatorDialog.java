import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;


class ObjectCreatorDialog {
	
	private static CreatableObject createdObject = null;

	public static void showObjectCreatorDialog(final ProjectionPlane.DialogDisposedAction<CreatableObject> ACTION) {
		final ProjectionPlane.ModalInternalFrame FRAME = ProjectionPlane.getSingleton().addInternalFrame("Create Item", true, true, true, true, true, true, true, new ProjectionPlane.DialogDisposedSimpleAction() {
			@Override
			public void dialogDisposed() {
				ACTION.dialogDisposed(ObjectCreatorDialog.createdObject);
			}
		});
		FRAME.getContentPane().setLayout(new GridLayout(0, 1));
		JLabel label = new JLabel("Item Type:", SwingConstants.CENTER);
		label.setFont(new Font(Main.DEFAULT_FONT, Font.BOLD, 20));
		
		final String[] SELECTION_ITEMS = {null, "Submachine Guns", "Rifle", "Machine Guns", "Basic Primary Gun"};
		final JComboBox<String> COMBO_BOX = new JComboBox<String>(SELECTION_ITEMS);
		COMBO_BOX.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (COMBO_BOX.getSelectedItem() != null) {
					if (COMBO_BOX.getSelectedItem().equals(SELECTION_ITEMS[1])) {
						/*createSubmachineGuns(new ProjectionPlane.DialogDisposedAction<SubmachineGun>() {
							@Override
							public void dialogDisposed(SubmachineGun gun) {
								synchronized (ObjectCreatorDialog.class) {
									ObjectCreatorDialog.createdObject = gun;
								}
								if (ObjectCreatorDialog.createdObject != null) {
									FRAME.dispose();
								}
							}
						});*/
					} else if (COMBO_BOX.getSelectedItem().equals(SELECTION_ITEMS[2])) {
						/*createRifle(new ProjectionPlane.DialogDisposedAction<Rifle>() {
							@Override
							public void dialogDisposed(Rifle gun) {
								synchronized (ObjectCreatorDialog.class) {
									ObjectCreatorDialog.createdObject = gun;
								}
								if (ObjectCreatorDialog.createdObject != null) {
									FRAME.dispose();
								}
							}
						});*/
					} else if (COMBO_BOX.getSelectedItem().equals(SELECTION_ITEMS[3])) {
						/*createMachineGuns(new ProjectionPlane.DialogDisposedAction<MachineGun>() {
							@Override
							public void dialogDisposed(MachineGun gun) {
								synchronized (ObjectCreatorDialog.class) {
									ObjectCreatorDialog.createdObject = gun;
								}
								if (ObjectCreatorDialog.createdObject != null) {
									FRAME.dispose();
								}
							}
						});*/
					} else if (COMBO_BOX.getSelectedItem().equals(SELECTION_ITEMS[4])) {
						createBasicPrimaryGuns(BasicPrimaryGun.BasicPrimaryGunType.RIFLE, new ProjectionPlane.DialogDisposedAction<BasicPrimaryGun>() {
							@Override
							public void dialogDisposed(BasicPrimaryGun gun) {
								synchronized (ObjectCreatorDialog.class) {
									ObjectCreatorDialog.createdObject = gun;
								}
								if (ObjectCreatorDialog.createdObject != null) {
									FRAME.dispose();
								}
							}
						});
					}
				}
			}
		});
		
		FRAME.getContentPane().add(label);
		FRAME.getContentPane().add(COMBO_BOX);
		FRAME.setSize(300, 200);
	}
	
	/*public static void createSubmachineGuns(final ProjectionPlane.DialogDisposedAction<SubmachineGun> ACTION) {
		HashMap<GameObjectProperties.PropertyKey<SubmachineGun.SubmachineGunProperties>, String> titles = new HashMap<GameObjectProperties.PropertyKey<SubmachineGun.SubmachineGunProperties>, String>();
		for (SubmachineGun.SubmachineGunProperties p : SubmachineGun.SubmachineGunProperties.values()) {
			titles.put(p, p.getTitle());
		}
		HashMap<GameObjectProperties.PropertyKey<SubmachineGun.SubmachineGunProperties>, String> descriptions = new HashMap<GameObjectProperties.PropertyKey<SubmachineGun.SubmachineGunProperties>, String>();
		for (SubmachineGun.SubmachineGunProperties p : SubmachineGun.SubmachineGunProperties.values()) {
			descriptions.put(p, p.getDescription());
		}
		HashMap<GameObjectProperties.PropertyKey<SubmachineGun.SubmachineGunProperties>, Boolean> isFilePath = new HashMap<GameObjectProperties.PropertyKey<SubmachineGun.SubmachineGunProperties>, Boolean>();
		for (SubmachineGun.SubmachineGunProperties p : SubmachineGun.SubmachineGunProperties.values()) {
			isFilePath.put(p, p.isFilePath());
		}
		new GameObjectCreatorDialog<SubmachineGun.SubmachineGunProperties>(new ProjectionPlane.DialogDisposedAction<GameObjectProperties<SubmachineGun.SubmachineGunProperties>>() {
			@Override
			public void dialogDisposed(GameObjectProperties<SubmachineGun.SubmachineGunProperties> p) {
				SubmachineGun gun = null;
				try {
					gun = SubmachineGun.createSubmachineGun(p);
				} catch (GameObjectProperties.InvalidPropertiesException e) {
					gun = null;
				}
				ACTION.dialogDisposed(gun);
			}
		}, "Create Submachine Gun", titles, descriptions, isFilePath, new File(SubmachineGun.SUBMACHINE_GUNS_DIRECTORY), SubmachineGun.SubmachineGunProperties.values());
	}
	
	public static void createRifle(final ProjectionPlane.DialogDisposedAction<Rifle> ACTION) {
		HashMap<GameObjectProperties.PropertyKey<Rifle.RifleProperties>, String> titles = new HashMap<GameObjectProperties.PropertyKey<Rifle.RifleProperties>, String>();
		for (Rifle.RifleProperties p : Rifle.RifleProperties.values()) {
			titles.put(p, p.getTitle());
		}
		HashMap<GameObjectProperties.PropertyKey<Rifle.RifleProperties>, String> descriptions = new HashMap<GameObjectProperties.PropertyKey<Rifle.RifleProperties>, String>();
		for (Rifle.RifleProperties p : Rifle.RifleProperties.values()) {
			descriptions.put(p, p.getDescription());
		}
		HashMap<GameObjectProperties.PropertyKey<Rifle.RifleProperties>, Boolean> isFilePath = new HashMap<GameObjectProperties.PropertyKey<Rifle.RifleProperties>, Boolean>();
		for (Rifle.RifleProperties p : Rifle.RifleProperties.values()) {
			isFilePath.put(p, p.isFilePath());
		}
		new GameObjectCreatorDialog<Rifle.RifleProperties>(new ProjectionPlane.DialogDisposedAction<GameObjectProperties<Rifle.RifleProperties>>() {
			@Override
			public void dialogDisposed(GameObjectProperties<Rifle.RifleProperties> p) {
				Rifle gun = null;
				try {
					gun = Rifle.createRifle(p);
				} catch (GameObjectProperties.InvalidPropertiesException e) {
					gun = null;
				}
				ACTION.dialogDisposed(gun);
			}
		}, "Create Rifle", titles, descriptions, isFilePath, new File(Rifle.RIFLES_DIRECTORY), Rifle.RifleProperties.values());
	}
	
	public static void createMachineGuns(final ProjectionPlane.DialogDisposedAction<MachineGun> ACTION) {
		HashMap<GameObjectProperties.PropertyKey<MachineGun.MachineGunProperties>, String> titles = new HashMap<GameObjectProperties.PropertyKey<MachineGun.MachineGunProperties>, String>();
		for (MachineGun.MachineGunProperties p : MachineGun.MachineGunProperties.values()) {
			titles.put(p, p.getTitle());
		}
		HashMap<GameObjectProperties.PropertyKey<MachineGun.MachineGunProperties>, String> descriptions = new HashMap<GameObjectProperties.PropertyKey<MachineGun.MachineGunProperties>, String>();
		for (MachineGun.MachineGunProperties p : MachineGun.MachineGunProperties.values()) {
			descriptions.put(p, p.getDescription());
		}
		HashMap<GameObjectProperties.PropertyKey<MachineGun.MachineGunProperties>, Boolean> isFilePath = new HashMap<GameObjectProperties.PropertyKey<MachineGun.MachineGunProperties>, Boolean>();
		for (MachineGun.MachineGunProperties p : MachineGun.MachineGunProperties.values()) {
			isFilePath.put(p, p.isFilePath());
		}
		new GameObjectCreatorDialog<MachineGun.MachineGunProperties>(new ProjectionPlane.DialogDisposedAction<GameObjectProperties<MachineGun.MachineGunProperties>>() {
			@Override
			public void dialogDisposed(GameObjectProperties<MachineGun.MachineGunProperties> p) {
				MachineGun gun = null;
				try {
					gun = MachineGun.createMachineGun(p);
				} catch (GameObjectProperties.InvalidPropertiesException e) {
					gun = null;
				}
				ACTION.dialogDisposed(gun);
			}
		}, "Create Machine Gun", titles, descriptions, isFilePath, new File(MachineGun.MACHINE_GUNS_DIRECTORY), MachineGun.MachineGunProperties.values());
	}*/
	
	public static void createBasicPrimaryGuns(BasicPrimaryGun.BasicPrimaryGunType gunType, final ProjectionPlane.DialogDisposedAction<BasicPrimaryGun> ACTION) {
		HashMap<GameObjectProperties.PropertyKey<BasicPrimaryGun.BasicPrimaryGunProperties>, String> titles = new HashMap<GameObjectProperties.PropertyKey<BasicPrimaryGun.BasicPrimaryGunProperties>, String>();
		for (BasicPrimaryGun.BasicPrimaryGunProperties p : BasicPrimaryGun.BasicPrimaryGunProperties.values()) {
			titles.put(p, p.getTitle());
		}
		HashMap<GameObjectProperties.PropertyKey<BasicPrimaryGun.BasicPrimaryGunProperties>, GameObjectCreatorDialog.PropertyValueChangeListener<BasicPrimaryGun.BasicPrimaryGunProperties>> propertyValueChangeListeners = new HashMap<GameObjectProperties.PropertyKey<BasicPrimaryGun.BasicPrimaryGunProperties>, GameObjectCreatorDialog.PropertyValueChangeListener<BasicPrimaryGun.BasicPrimaryGunProperties>>();
		for (BasicPrimaryGun.BasicPrimaryGunProperties p : BasicPrimaryGun.BasicPrimaryGunProperties.values()) {
			propertyValueChangeListeners.put(p, p.getPropertyValueChangeListener());
		}
		new GameObjectCreatorDialog<BasicPrimaryGun.BasicPrimaryGunProperties>(new ProjectionPlane.DialogDisposedAction<GameObjectProperties<BasicPrimaryGun.BasicPrimaryGunProperties>>() {
			@Override
			public void dialogDisposed(GameObjectProperties<BasicPrimaryGun.BasicPrimaryGunProperties> p) {
				BasicPrimaryGun gun = null;
				try {
					gun = BasicPrimaryGun.createBasicPrimaryGun(p);
				} catch (GameObjectProperties.InvalidPropertiesException e) {
					gun = null;
				}
				ACTION.dialogDisposed(gun);
			}
		}, "Create Basic Primary Gun", titles, BasicPrimaryGun.BasicPrimaryGunProperties.ACCURACY, propertyValueChangeListeners, BasicPrimaryGun.BasicPrimaryGunProperties.values());
	}
	
	public interface CreatableObject {
		public void objectCreated();
	}

	static class GameObjectCreatorDialog<K extends GameObjectProperties.StringIdentifier> {
		
		private GameObjectProperties<K> properties = null;
		private final ArrayList<GameObjectPanel<K>> PROPERTY_PANELS = new ArrayList<GameObjectPanel<K>>();

		public GameObjectCreatorDialog(ProjectionPlane.DialogDisposedAction<GameObjectProperties<K>> action, String title, SaveFileDirectoryGetter saveFileDirectoryGetter, File saveFileDirectory, GameObjectProperties.PropertyKey<K>... keys) {
			this(action, title, null, saveFileDirectoryGetter, null, keys);
		}
		
		public GameObjectCreatorDialog(final ProjectionPlane.DialogDisposedAction<GameObjectProperties<K>> ACTION, String title, HashMap<GameObjectProperties.PropertyKey<K>, String> titles, final SaveFileDirectoryGetter SAVE_FILE_DIRECTORY_GETTER, HashMap<GameObjectProperties.PropertyKey<K>, PropertyValueChangeListener<K>> propertyValueChangeListeners, final GameObjectProperties.PropertyKey<K>... KEYS) {
			final ProjectionPlane.ModalInternalFrame DIALOG = ProjectionPlane.getSingleton().addInternalFrame(title, true, true, true, true, true, true, true, new ProjectionPlane.DialogDisposedSimpleAction() {
				@Override
				public void dialogDisposed() {
					ACTION.dialogDisposed(GameObjectCreatorDialog.this.properties);
				}
			});
			if (KEYS == null || KEYS.length == 0) throw new IllegalArgumentException("keys must have atleast one element");
			JPanel mainPanel = new JPanel();
			JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
			for (GameObjectProperties.PropertyKey<K> k : KEYS) {
				if (k.isArray()) {
					GameArrayObjectCreatorPanel panel = new GameArrayObjectCreatorPanel(k, titles != null ? titles.get(k) : null, propertyValueChangeListeners != null ? propertyValueChangeListeners.get(k) : null);
					PROPERTY_PANELS.add(panel);
					mainPanel.add(panel);
				} else {
					GameObjectCreatorPanel panel = new GameObjectCreatorPanel(k, titles != null ? titles.get(k) : k.getPropertyKey().getIdentifier(), propertyValueChangeListeners != null ? propertyValueChangeListeners.get(k) : null);
					panel.showDeleteButton(false);
					panel.setBorder(BorderFactory.createTitledBorder(titles != null ? titles.get(k) : k.getPropertyKey().getIdentifier()));
					PROPERTY_PANELS.add(panel);
					mainPanel.add(panel);
				}
			}
			JPanel saveCreatePanel = new JPanel(new GridLayout(1, 2));
			JButton createButton = new JButton("Create");
			JButton saveButton = new JButton("Save");
			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final File SAVE_FILE_DIRECTORY = new File(SAVE_FILE_DIRECTORY_GETTER.getSaveFileDirectory());
					if (!SAVE_FILE_DIRECTORY.exists()) {
						SAVE_FILE_DIRECTORY.mkdir();
					}
					final ProjectionPlane.ModalInternalFrame FRAME = ProjectionPlane.getSingleton().addInternalFrame("Save", true, true, true, true, true, true, true);
					final JFileChooser FILE_CHOOSER = new JFileChooser(SAVE_FILE_DIRECTORY, new FileSystemView() {
						private final File SAVE_FILE_DIRECTORY_ABSOLUTE = SAVE_FILE_DIRECTORY.getAbsoluteFile();
						private final File[] ROOT_DIRECTORIES = {SAVE_FILE_DIRECTORY};
						@Override
						public File createNewFolder(File containingDir) throws IOException {
							return FileSystemView.getFileSystemView().createNewFolder(containingDir);
						}
						@Override
					    public File[] getRoots() {
							return ROOT_DIRECTORIES;
						}
						@Override
						public File getDefaultDirectory() {
							return SAVE_FILE_DIRECTORY;
						}
						@Override
						public File getHomeDirectory() {
							return SAVE_FILE_DIRECTORY;
						}
						@Override
						public boolean isRoot(File file) {
							if (SAVE_FILE_DIRECTORY.equals(file) || SAVE_FILE_DIRECTORY_ABSOLUTE.equals(file)) {
								return true;
							}
							return false;
						}
					});
					FILE_CHOOSER.updateUI();
					FILE_CHOOSER.addPropertyChangeListener(new PropertyChangeListener() {
						private final File SAVE_FILE_DIRECTORY_PARENT = SAVE_FILE_DIRECTORY.getParentFile().getAbsoluteFile();
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
								if (FILE_CHOOSER.getCurrentDirectory().getAbsoluteFile().equals(SAVE_FILE_DIRECTORY_PARENT)) {
									FILE_CHOOSER.setCurrentDirectory(SAVE_FILE_DIRECTORY);
								}
							}
						}
					});
					FILE_CHOOSER.setApproveButtonText("Save");
					FILE_CHOOSER.setAcceptAllFileFilterUsed(false);
					FILE_CHOOSER.setFileFilter(new FileFilter() {
						@Override
						public boolean accept(File pathname) {
							if (pathname.isDirectory()) {
								return true;
							}
							if (pathname.getName().indexOf(".") >= 0) {
								return pathname.getName().substring(pathname.getName().indexOf(".")).equals("txt");
							}
							return false;
						}

						@Override
						public String getDescription() {
							return "Plain Text (*.txt)";
						}
					});
					FILE_CHOOSER.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
								GameObjectProperties<K> properties = new GameObjectProperties<K>(KEYS);
								for (GameObjectPanel<K> p : PROPERTY_PANELS) {
					            	if (p != null) {
					                	properties.set(p.getKey().getPropertyKey(), p.getStringValue());
					                }
					            }
					            synchronized (GameObjectCreatorDialog.this) {
						            GameObjectCreatorDialog.this.properties = properties;
					            }
					            GameObjectCreatorDialog.this.properties.saveProperties(new File(FILE_CHOOSER.getSelectedFile() + ".txt"));
					            FRAME.dispose();
					            DIALOG.dispose();
						    } else if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
								FRAME.dispose();
						    }
						}
					});
					FRAME.add(FILE_CHOOSER);
					FRAME.pack();
					FRAME.setBounds(0, 0, Math.min(FRAME.getWidth(), ProjectionPlane.getSingleton().getWidth()), Math.min(FRAME.getHeight(), ProjectionPlane.getSingleton().getHeight()));
				}
			});
			createButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					GameObjectProperties<K> properties = new GameObjectProperties<K>(KEYS);
		            for (GameObjectPanel<K> p : PROPERTY_PANELS) {
		                if (p != null) {
		                    properties.set(p.getKey().getPropertyKey(), p.getStringValue());
		                }
		            }
		            synchronized (GameObjectCreatorDialog.this) {
			            GameObjectCreatorDialog.this.properties = properties;
		            }
		            DIALOG.dispose();
				}
			});
			saveCreatePanel.add(saveButton);
			saveCreatePanel.add(createButton);
			DIALOG.getContentPane().setLayout(new BorderLayout());
			DIALOG.getContentPane().add(scrollPane, BorderLayout.CENTER);
			DIALOG.getContentPane().add(saveCreatePanel, BorderLayout.SOUTH);
			DIALOG.pack();
			DIALOG.setSize(400, 300);
		}
		
		public void updateAllDescriptions() {
			for (GameObjectPanel<K> p : PROPERTY_PANELS) {
				p.updateDescription();
			}
		}
		
		public void updateDescription(K key) {
			for (GameObjectPanel<K> p : PROPERTY_PANELS) {
				if (key.equals(p.getKey().getPropertyKey())) {
					p.updateDescription();
					break;
				}
			}
		}
		
		public String getValue(K key) {
			for (GameObjectPanel<K> p : PROPERTY_PANELS) {
				if (key.equals(p.getKey().getPropertyKey())) {
					return p.getStringValue();
				}
			}
			return null;
		}
		
		public interface SaveFileDirectoryGetter {
			public String getSaveFileDirectory();
		}
		
		private interface GameObjectPanel<K extends GameObjectProperties.StringIdentifier> {
			public GameObjectProperties.PropertyKey<K> getKey();
			public void updateDescription();
			public String getStringValue();
		}
		
		private class GameArrayObjectCreatorPanel extends JPanel implements GameObjectPanel<K> {
			private static final long serialVersionUID = -5092696733941434528L;
			
			private final JLabel DESCRIPTION_LABEL;
			private final ArrayList<GameObjectCreatorPanel> ARRAY_PANELS = new ArrayList<GameObjectCreatorPanel>();
			
			private final GameObjectProperties.PropertyKey<K> KEY;

			private GameArrayObjectCreatorPanel(GameObjectProperties.PropertyKey<K> key, PropertyValueChangeListener<K> propertyValueChangeListener) {
				this(key, null, propertyValueChangeListener);
			}

			private GameArrayObjectCreatorPanel(GameObjectProperties.PropertyKey<K> key, String title, final PropertyValueChangeListener<K> PROPERTY_VALUE_CHANGE_LISTENER) {
				super(new BorderLayout());
				if (key == null) throw new IllegalArgumentException("key cannot be null");
				KEY = key;
				JPanel panel = new JPanel();
				JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				
				setBorder(BorderFactory.createTitledBorder(title != null ? title : KEY.getPropertyKey().getIdentifier()));
				
				panel.setLayout(new BorderLayout());
				final JPanel MAIN_PANEL = new JPanel(new GridLayout(-1, 1));
				final GameObjectCreatorPanel NEW_PANEL = new GameObjectCreatorPanel(KEY, KEY.getPropertyKey().getIdentifier() + " " + (ARRAY_PANELS.size() + 1), PROPERTY_VALUE_CHANGE_LISTENER);
				NEW_PANEL.setDeleteActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						MAIN_PANEL.remove(NEW_PANEL);
						ARRAY_PANELS.remove(NEW_PANEL);
						MAIN_PANEL.revalidate();
						MAIN_PANEL.repaint();
						if (ARRAY_PANELS.size() == 1) {
							ARRAY_PANELS.get(0).showDeleteButton(false);
						} else if (ARRAY_PANELS.size() > 1) {
							for (GameObjectCreatorPanel p : ARRAY_PANELS) {
								p.showDeleteButton(true);
							}
						}
					}
				});
				NEW_PANEL.showDeleteButton(false);
				MAIN_PANEL.add(NEW_PANEL);
				ARRAY_PANELS.add(NEW_PANEL);
				panel.add(MAIN_PANEL, BorderLayout.CENTER);
				JButton addButton = new JButton("+");
				addButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final GameObjectCreatorPanel NEW_PANEL = new GameObjectCreatorPanel(KEY, KEY.getPropertyKey().getIdentifier() + " " + (ARRAY_PANELS.size() + 1), PROPERTY_VALUE_CHANGE_LISTENER);
						NEW_PANEL.setDeleteActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								MAIN_PANEL.remove(NEW_PANEL);
								ARRAY_PANELS.remove(NEW_PANEL);
								MAIN_PANEL.revalidate();
								MAIN_PANEL.repaint();
								if (ARRAY_PANELS.size() == 1) {
									ARRAY_PANELS.get(0).showDeleteButton(false);
								} else if (ARRAY_PANELS.size() > 1) {
									for (GameObjectCreatorPanel p : ARRAY_PANELS) {
										p.showDeleteButton(true);
									}
								}
							}
						});
						MAIN_PANEL.add(NEW_PANEL);
						ARRAY_PANELS.add(NEW_PANEL);
						for (GameObjectCreatorPanel p : ARRAY_PANELS) {
							p.showDeleteButton(true);
						}
						MAIN_PANEL.revalidate();
						MAIN_PANEL.repaint();
					}
				});
				String description = KEY.getDescription();
				if (description != null) {
					DESCRIPTION_LABEL = new JLabel(description);
					add(DESCRIPTION_LABEL, BorderLayout.NORTH);
				} else {
					DESCRIPTION_LABEL = null;
				}
				add(scrollPane, BorderLayout.CENTER);
				add(addButton, BorderLayout.SOUTH);
			}
			
			@Override
			public void updateDescription() {
				if (DESCRIPTION_LABEL != null) {
					DESCRIPTION_LABEL.setText(KEY.getDescription());
				}
				for (GameObjectCreatorPanel p : ARRAY_PANELS) {
					p.updateDescription();
				}
			}
			
			@Override
			public GameObjectProperties.PropertyKey<K> getKey() {
				return KEY;
			}

			@Override
			public String getStringValue() {
				String value = "";
				for (GameObjectCreatorPanel p : ARRAY_PANELS) {
					value += p.getStringValue() + ",";
				}
				if (value.substring(value.length() - 1).equals(",")) {
					value = value.substring(0, value.length() - 1);
				}
				return value;
			}
		}
		
		private class GameObjectCreatorPanel extends JScrollPane implements GameObjectPanel<K> {
			private static final long serialVersionUID = -8178252275530553060L;
			
			private final GameObjectProperties.PropertyKey<K> KEY;
			private ActionListener deleteActionListener = null;
			private final JButton DELETE_BUTTON = new JButton("X");
			
			private final JLabel DESCRIPTION_LABEL;
			private final JTextField TEXT_FIELD;
			private final JSpinner DOUBLE_SPINNER;
			private final JSpinner INT_SPINNER;
			private final JCheckBox CHECK_BOX;
			private final JComboBox<Object> COMBO_BOX;
			private final PropertyValueChangeListener<K> PROPERTY_VALUE_CHANGE_LISTENER;

			private GameObjectCreatorPanel(GameObjectProperties.PropertyKey<K> key, String title, PropertyValueChangeListener<K> propertyValueChangeListener) {
				this(key, title, null, propertyValueChangeListener);
			}

			private GameObjectCreatorPanel(GameObjectProperties.PropertyKey<K> key, String title, ActionListener deleteActionListener, PropertyValueChangeListener<K> propertyValueChangeListener) {
				super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				if (key == null) throw new IllegalArgumentException("key cannot be null");
				KEY = key;
				PROPERTY_VALUE_CHANGE_LISTENER = propertyValueChangeListener;
				JPanel panel = new JPanel(new BorderLayout());
				setViewportView(panel);
				
				setDeleteActionListener(deleteActionListener);
				
				panel.setLayout(new BorderLayout());
				String description = key.getDescription();
				if (description != null) {
					DESCRIPTION_LABEL = new JLabel(description);
					panel.add(DESCRIPTION_LABEL, BorderLayout.NORTH);
				} else {
					DESCRIPTION_LABEL = null;
				}
				GameObjectProperties.PropertyType propertyType = KEY.getPropertyType();
				if (propertyType == GameObjectProperties.PropertyType.STRING) {
					TEXT_FIELD = new JTextField();
					TEXT_FIELD.getDocument().addDocumentListener(new DocumentListener() {
						@Override
						public void removeUpdate(DocumentEvent e) {
							changedUpdate(e);
						}
						@Override
						public void insertUpdate(DocumentEvent e) {
							changedUpdate(e);
						}
						@Override
						public void changedUpdate(DocumentEvent e) {
							if (PROPERTY_VALUE_CHANGE_LISTENER != null) {
								PROPERTY_VALUE_CHANGE_LISTENER.propertyValueChanged(GameObjectCreatorDialog.this, KEY);
							}
						}
					});
					if (KEY.getSaveFileDirectoryPath() != null) {
						JPanel textFieldPanel = new JPanel(new BorderLayout());
						textFieldPanel.add(TEXT_FIELD, BorderLayout.CENTER);
						JButton browseButton = new JButton("Browse");
						browseButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								final File SAVE_FILE_DIRECTORY = new File(KEY.getSaveFileDirectoryPath());
								if (!SAVE_FILE_DIRECTORY.exists()) {
									SAVE_FILE_DIRECTORY.mkdir();
								}
								final ProjectionPlane.ModalInternalFrame FRAME = ProjectionPlane.getSingleton().addInternalFrame("Open", true, true, true, true, true, true, true);
								final JFileChooser FILE_CHOOSER = new JFileChooser(SAVE_FILE_DIRECTORY, new FileSystemView() {
									private final File SAVE_FILE_DIRECTORY_ABSOLUTE = SAVE_FILE_DIRECTORY.getAbsoluteFile();
									private final File[] ROOT_DIRECTORIES = {SAVE_FILE_DIRECTORY};
									@Override
									public File createNewFolder(File containingDir) throws IOException {
										return FileSystemView.getFileSystemView().createNewFolder(containingDir);
									}
									@Override
								    public File[] getRoots() {
										return ROOT_DIRECTORIES;
									}
									@Override
									public File getDefaultDirectory() {
										return SAVE_FILE_DIRECTORY;
									}
									@Override
									public File getHomeDirectory() {
										return SAVE_FILE_DIRECTORY;
									}
									@Override
									public boolean isRoot(File file) {
										if (SAVE_FILE_DIRECTORY.equals(file) || SAVE_FILE_DIRECTORY_ABSOLUTE.equals(file)) {
											return true;
										}
										return false;
									}
								});
								FILE_CHOOSER.addPropertyChangeListener(new PropertyChangeListener() {
									private final File SAVE_FILE_DIRECTORY_PARENT = SAVE_FILE_DIRECTORY.getParentFile().getAbsoluteFile();
									@Override
									public void propertyChange(PropertyChangeEvent evt) {
										if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
											if (FILE_CHOOSER.getCurrentDirectory().getAbsoluteFile().equals(SAVE_FILE_DIRECTORY_PARENT)) {
												FILE_CHOOSER.setCurrentDirectory(SAVE_FILE_DIRECTORY);
											}
										}
									}
								});
								FILE_CHOOSER.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
											TEXT_FIELD.setText(SAVE_FILE_DIRECTORY.toURI().relativize(FILE_CHOOSER.getSelectedFile().toURI()).getPath());
											FRAME.dispose();
									    } else if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
											FRAME.dispose();
									    }
									}
								});
								FRAME.add(FILE_CHOOSER);
								FRAME.pack();
								FRAME.setBounds(0, 0, Math.min(FRAME.getWidth(), ProjectionPlane.getSingleton().getWidth()), Math.min(FRAME.getHeight(), ProjectionPlane.getSingleton().getHeight()));
							}
						});
						textFieldPanel.add(browseButton, BorderLayout.EAST);
						panel.add(textFieldPanel, BorderLayout.CENTER);
					} else {
						panel.add(TEXT_FIELD, BorderLayout.CENTER);
					}
				} else {
					TEXT_FIELD = null;
				}
				if (propertyType == GameObjectProperties.PropertyType.DOUBLE) {
					DOUBLE_SPINNER = new JSpinner();
					DOUBLE_SPINNER.addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							if (PROPERTY_VALUE_CHANGE_LISTENER != null) {
								PROPERTY_VALUE_CHANGE_LISTENER.propertyValueChanged(GameObjectCreatorDialog.this, KEY);
							}
						}
					});
					Dimension doubleSpinnerSize = DOUBLE_SPINNER.getPreferredSize();
					DOUBLE_SPINNER.setModel(new SpinnerNumberModel(0, -Double.MAX_VALUE, Double.MAX_VALUE, 1));
					DOUBLE_SPINNER.setPreferredSize(doubleSpinnerSize);
					panel.add(DOUBLE_SPINNER, BorderLayout.CENTER);
				} else {
					DOUBLE_SPINNER = null;
				}
				if (propertyType == GameObjectProperties.PropertyType.INTEGER) {
					INT_SPINNER = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
					INT_SPINNER.addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							if (PROPERTY_VALUE_CHANGE_LISTENER != null) {
								PROPERTY_VALUE_CHANGE_LISTENER.propertyValueChanged(GameObjectCreatorDialog.this, KEY);
							}
						}
					});
					panel.add(INT_SPINNER, BorderLayout.CENTER);
				} else {
					INT_SPINNER = null;
				}
				if (propertyType == GameObjectProperties.PropertyType.BOOLEAN) {
					CHECK_BOX = new JCheckBox();
					CHECK_BOX.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							if (PROPERTY_VALUE_CHANGE_LISTENER != null) {
								PROPERTY_VALUE_CHANGE_LISTENER.propertyValueChanged(GameObjectCreatorDialog.this, KEY);
							}
						}
					});
					CHECK_BOX.setText(title != null ? title : KEY.getPropertyKey().getIdentifier());
					panel.add(CHECK_BOX, BorderLayout.CENTER);
				} else {
					CHECK_BOX = null;
				}
				if (propertyType == GameObjectProperties.PropertyType.ENUMERATION) {
					COMBO_BOX = new JComboBox<Object>(KEY.getPossibleValues());
					COMBO_BOX.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (PROPERTY_VALUE_CHANGE_LISTENER != null) {
								PROPERTY_VALUE_CHANGE_LISTENER.propertyValueChanged(GameObjectCreatorDialog.this, KEY);
							}
						}
					});
					COMBO_BOX.setSelectedIndex(0);
					panel.add(COMBO_BOX, BorderLayout.CENTER);
				} else {
					COMBO_BOX = null;
				}
				panel.add(DELETE_BUTTON, BorderLayout.EAST);
			}
			
			@Override
			public void updateDescription() {
				if (DESCRIPTION_LABEL != null) {
					DESCRIPTION_LABEL.setText(KEY.getDescription());
				}
			}
			
			private void setDeleteActionListener(ActionListener listener) {
				this.deleteActionListener = listener;
				for (ActionListener l : DELETE_BUTTON.getActionListeners()) {
					DELETE_BUTTON.removeActionListener(l);
				}
				if (this.deleteActionListener != null) {
					DELETE_BUTTON.addActionListener(this.deleteActionListener);
				}
			}
			
			private void showDeleteButton(boolean show) {
				DELETE_BUTTON.setVisible(show);
			}
			
			public GameObjectProperties.PropertyKey<K> getKey() {
				return KEY;
			}

			@Override
			public String getStringValue() {
				if (KEY.getPropertyType() == GameObjectProperties.PropertyType.STRING) {
					return TEXT_FIELD.getText();
				} else if (KEY.getPropertyType() == GameObjectProperties.PropertyType.DOUBLE) {
					return DOUBLE_SPINNER.getValue().toString();
				} else if (KEY.getPropertyType() == GameObjectProperties.PropertyType.INTEGER) {
					return INT_SPINNER.getValue().toString();
				} else if (KEY.getPropertyType() == GameObjectProperties.PropertyType.BOOLEAN) {
					return CHECK_BOX.isSelected() ? "TRUE" : "FALSE";
				} else if (KEY.getPropertyType() == GameObjectProperties.PropertyType.ENUMERATION) {
					return String.valueOf(COMBO_BOX.getSelectedIndex());
				}
				return null;
			}
		}
		
		public static interface PropertyValueChangeListener<K extends GameObjectProperties.StringIdentifier> {
			public void propertyValueChanged(GameObjectCreatorDialog<K> panel, GameObjectProperties.PropertyKey<K> key);
		}
	}
}
