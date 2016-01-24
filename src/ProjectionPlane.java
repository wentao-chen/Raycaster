import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.LinearGradientPaint;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

public class ProjectionPlane extends JDesktopPane implements Map.MapLocation3D {
	private static final long serialVersionUID = 1408388928310070980L;
	
	public static final int MAXIMUM_ABSOLUTE_SENSITIVITY = 10;
	
	private static final ProjectionPlane SINGLETON = new ProjectionPlane(Main.NAME, 1, 0, false); // TODO
	
	private final AtomicBoolean IS_RUNNING = new AtomicBoolean(false);
	private final Set<Integer> PRESSED_BUTTONS = new HashSet<Integer>();
	private final Set<Integer> PRESSED_KEYS = new HashSet<Integer>();
	private transient double mspf = 0;
	private transient Double renderTime = null;
	private int resolution = 1;
	private double sightRange = 5;
	private int sensitivity = 0;
	private int frameDelay = 10;
	private boolean seeThrough = false;

	private String name = "Player";
	private int numberOfRecentEvents = 4;
	private boolean showFPS = true;
	private boolean showTime = true;
	private boolean showShading = true;
	private boolean allowGenerateMaze = true;
	private boolean allowHorizontalDirection = true;
	private boolean showMiniMap = false;
	private double miniMapZoom = 1;
	private boolean disableKeyboardInput = false;
	private boolean disableInput = false;
	private transient Boolean previousInputState = null;
	private transient Boolean previousLockState = null;
	private String chatMessage = null;
	private Player chatMessageTarget = null;
	private float relativeVolume = -12;
	private boolean isMute = false;
	
	private Double thirdPersonDistance = null;
	private boolean freeMouse = false;
	private boolean centeringMouse = false;
	private BufferedImage screenShot;
	private final PausedMenu PAUSE_MENU;
	private final ArrayList<ProjectionPlaneEventListener> PROJECTION_PLANE_EVENT_LISTENERS = new ArrayList<ProjectionPlaneEventListener>();
	private double distanceToPlane = 0;
	
	private final Console CONSOLE;
	
	private Game game = null;
	private Player player = null;
	private final JFrame FRAME;
	private final MouseListener MOUSE_LISTENER;
	private final MouseWheelListener MOUSE_WHEEL_LISTENER;
	private final MouseMotionListener MOUSE_MOTION_LISTENER;
	private transient final AtomicBoolean SWITCHING_FULL_SCREEN = new AtomicBoolean(false);
	
	private final GraphicsDevice GRAPHICS_DEVICE = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	
	private static ModalInternalFrame previousModalFrame = null;
	
	private final SmartAdjuster.IntegerPrecision FRAME_DELAY_ADJUSTER = new SmartAdjuster.IntegerPrecision(SettingsMenu.MIN_FRAME_DELAY, SettingsMenu.MAX_FRAME_DELAY, Integer.MAX_VALUE, SettingsMenu.MIN_FRAME_DELAY, Main.TARGET_FPS) {
		@Override
		public boolean adjust() {
			int initialValue = ProjectionPlane.this.getFrameDelay();
			if (initialValue > getPreferredValue()) {
				ProjectionPlane.this.setFrameDelay(initialValue - 1);
			}
			return ProjectionPlane.this.getFrameDelay() != initialValue;
		}
		@Override
		public boolean forceAdjust() {
			int initialValue = ProjectionPlane.this.getFrameDelay();
			ProjectionPlane.this.setFrameDelay(Math.max(ProjectionPlane.this.getFrameDelay() - 1, getMinValue()));
			return ProjectionPlane.this.getFrameDelay() != initialValue;
		}
	};
	private final SmartAdjuster.DoublePrecision SIGHT_RANGE_ADJUSTER = new SmartAdjuster.DoublePrecision(SettingsMenu.MIN_SIGHT_RANGE, Double.MAX_VALUE, Integer.MAX_VALUE - 2, 20, Main.TARGET_FPS) {
		@Override
		public boolean adjust() {
			double initialValue = ProjectionPlane.this.getSightRange();
			if (initialValue > getPreferredValue()) {
				ProjectionPlane.this.setSightRange(initialValue - 1);
			}
			return ProjectionPlane.this.getSightRange() != initialValue;
		}

		@Override
		public boolean forceAdjust() {
			double initialValue = ProjectionPlane.this.getSightRange();
			ProjectionPlane.this.setSightRange(Math.max(ProjectionPlane.this.getSightRange() - 1, getMinValue()));
			return ProjectionPlane.this.getSightRange() != initialValue;
		}
	};
	private final SmartAdjuster.IntegerPrecision RESOLUTION_ADJUSTER = new SmartAdjuster.IntegerPrecision(SettingsMenu.MIN_RESOLUTION, SettingsMenu.MAX_RESOLUTION, Integer.MAX_VALUE - 1, SettingsMenu.MAX_RESOLUTION - 1, Main.TARGET_FPS) {
		@Override
		public boolean adjust() {
			int initialValue = ProjectionPlane.this.getResolution();
			if (initialValue < SettingsMenu.MAX_RESOLUTION - getPreferredValue() + 1) {
				ProjectionPlane.this.setResolution(initialValue + 1);
			}
			return ProjectionPlane.this.getResolution() != initialValue;
		}
		@Override
		public boolean forceAdjust() {
			int initialValue = ProjectionPlane.this.getResolution();
			ProjectionPlane.this.setResolution(Math.min(ProjectionPlane.this.getResolution() + 1, getMaxValue()));
			return ProjectionPlane.this.getResolution() != initialValue;
		}
	};
	private static final Comparator<SmartAdjuster<?>> SMART_ADJUSTER_COMPARATOR = new Comparator<SmartAdjuster<?>>() {
		@Override
		public int compare(SmartAdjuster<?> o1, SmartAdjuster<?> o2) {
			return Integer.compare(o2.getPriority(), o1.getPriority());
		}
	};
	
	private ProjectionPlane(String title, int resolution, int sensitivity, boolean isMute) {
		super();
		FRAME = new JFrame(title);
		getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getFrame().setIconImage(Main.FRAME_ICON.getImage());
		getFrame().setContentPane(this);
		getFrame().pack();
		getFrame().setSize(640, 400);
		getFrame().addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}
			@Override
			public void windowClosing(WindowEvent e) {
			}
			@Override
			public void windowClosed(WindowEvent e) {
				if (!SWITCHING_FULL_SCREEN.get()) {
					stop();
				}
				SWITCHING_FULL_SCREEN.set(true);
			}
			@Override
			public void windowIconified(WindowEvent e) {
			}
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			@Override
			public void windowActivated(WindowEvent e) {
			}
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
		});
		addProjectionPlaneStoppedListeners(new ProjectionPlaneEventListener() {
			@Override
			public void projectionPlaneStopped() {
				stop();
				getFrame().dispose();
			}
			@Override
			public void playerMoved(Player player) {
			}
			@Override
			public void postDrawFrameCheck() {
			}
		});
		
		setResolution(resolution);
		setSensitivity(sensitivity);
		setMute(isMute);
		
		MOUSE_LISTENER = new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (isCursorLocked() && isGameRunning() && getPlayer() != null && !getPlayer().isDefusingBomb() && (getPlayer().getTacticalShield() == null || !getPlayer().getTacticalShield().mouseClicked(e, getPlayer()))) {
					// Event is not consumed by tactical shield.
					if (getPlayer().getMainHoldItem() != null) {
						getPlayer().getMainHoldItem().mouseClicked(e, getPlayer());
					}
				} else if (!isCursorLocked()) {
					repaint();
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (isCursorLocked()) {
					PRESSED_BUTTONS.add(e.getButton());
					if (isGameRunning() && getPlayer() != null && !getPlayer().isDefusingBomb() && getPlayer().getMainHoldItem() != null) {
						getPlayer().getMainHoldItem().mousePressed(e, getPlayer());
					}
				} else {
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				int buttonCode = e.getButton();
				List<Integer> found = new ArrayList<Integer>();
				for(Integer button : PRESSED_BUTTONS){
				    if(button.equals(buttonCode)){
				        found.add(button);
				    }
				}
				if (InputConfiguration.USE.hasInputMouseButton(buttonCode) && getPlayer() != null) {
					getPlayer().cancelDefusingBomb();
				}
				PRESSED_BUTTONS.removeAll(found);
				if (isGameRunning() && getPlayer() != null && !getPlayer().isDefusingBomb() && getPlayer().getMainHoldItem() != null) {
					getPlayer().getMainHoldItem().mouseReleased(e, getPlayer());
				}
				if (!isCursorLocked()) {
					repaint();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
		};
		MOUSE_WHEEL_LISTENER = new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (isThirdPerson()) {
					setThirdPerson(getThirdPersonDistance() + e.getPreciseWheelRotation() / 10);
				}
			}
		};
		MOUSE_MOTION_LISTENER = new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				ProjectionPlane.this.mouseMoved(e.getX(), e.getY());
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				mouseMoved(e);
			}
		};
		
		PRESSED_BUTTONS.clear();
		PRESSED_KEYS.clear();
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
	            if (e.getID() == KeyEvent.KEY_PRESSED) {
	            	keyPressed(e.getKeyCode());
	            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
	            	keyReleased(e.getKeyCode());
	            } else if (e.getID() == KeyEvent.KEY_TYPED) {
	            	keyTyped(e.getKeyCode());
	            }
	            return false;
			}
        });
        
        PAUSE_MENU = new PausedMenu(new PausedMenuActionListener() {
    		@Override
    		public void pauseMenuItemClicked(MouseEvent event, PausedMenuAction action) {
    			if (action == PausedMenuAction.NEW_GAME) {
    				promptNewGame();
    			} else if (action == PausedMenuAction.RESUME) {
    				lockCursor(true);
    			} else if (action == PausedMenuAction.NEW_SERVER) {
    				if (getGame() == null || !getGame().isRunning()) {
    					NewServerDialog.showNewServerDialog(ProjectionPlane.this, new DialogDisposedAction<MultiplayerGameServer>() {
    						@Override
    						public void dialogDisposed(final MultiplayerGameServer SERVER) {
    		    				if (SERVER != null) {
    		    					setPlayer(SERVER.getPlayer());
    		    					setGame(SERVER.getPlayer().getGame(), true);
    		    					getMap().showTeamSelectionInternalDialog(false, new DialogDisposedAction<Team>() {
    									@Override
    									public void dialogDisposed(Team t) {
    										getPlayer().setTeam(t, true);
    										getPlayer().setHealth(0);
    										getPlayer().resetKillsAndDeaths();
    				    					getGame().setToSpawnLocation(getPlayer());
    				    					lockCursor(getGame().isRunning());
    				    					try {
    				    						SERVER.startServer();
    				    					} catch (UnknownHostException e) {
    				    					} catch (IOException e) {
    				    					}
    									}
    								});
    		    				}
    						}
    					});
    				}
    			} else if (action == PausedMenuAction.FIND_SERVERS) {
    				if (getGame() != null && getGame().isServer()) {
    					int input = JOptionPane.showInternalOptionDialog(ProjectionPlane.this, "You need to close your current server before finding other servers", "Server", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Close", "Cancel"}, "Cancel");
    					if (input == 0) {
    						getGame().stop();
    						ProjectionPlane.this.screenShot = Main.BACKGROUND_WALL_PAPER;
    					} else {
    						return;
    					}
    				}
    				FindServersDialog.showFindServersDialog(ProjectionPlane.this, new DialogDisposedAction<MultiplayerGameClient>() {
    					private boolean waitDialogDisposed = true;
						@Override
						public void dialogDisposed(final MultiplayerGameClient CLIENT) {
		    				if (CLIENT != null) {
		    					if (CLIENT.startClient()) {
		    						if (CLIENT.getPlayer() == null) {
		    							final ModalInternalFrame WAIT_DIALOG = addInternalFrame("Connecting...", true, true, true, true, true, true, true, new DialogDisposedSimpleAction() {
		    								@Override
											public void dialogDisposed() {
		    		    						if (!waitDialogDisposed && CLIENT.getPlayer() != null && CLIENT.getPlayer().getGame() != null) {
		    		    							final Player PLAYER = CLIENT.getPlayer();
		    		    							setPlaneName(PLAYER.getName());
		    		    							PLAYER.getGame().getMap().showTeamSelectionInternalDialog(false, new DialogDisposedAction<Team>() {
		    											@Override
		    											public void dialogDisposed(Team t) {
		    				    							PLAYER.setTeam(t, true);
		    				    							setPlayer(PLAYER);
		    				    							setGame(PLAYER.getGame(), true);
		    				    							getGame().setToSpawnLocation(PLAYER);
		    				    							lockCursor(getGame().isRunning());
		    											}
		    										});
		    		    						} else {
		    		    							try {
		    		    								CLIENT.closeConnection();
		    		    							} catch (IOException e) {
		    		    							}
		    		    							if (waitDialogDisposed) {
		    		    								JOptionPane.showInternalMessageDialog(ProjectionPlane.this, "Connection Cancelled.", "Connection Error", JOptionPane.ERROR_MESSAGE);
		    					    				} else {
		    		    								JOptionPane.showInternalMessageDialog(ProjectionPlane.this, "Unable to recieve from the server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
		    				    					}
		    		    				    	}
											}
										});
		    							WAIT_DIALOG.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		    							WAIT_DIALOG.getContentPane().setLayout(new BorderLayout());
		    							WAIT_DIALOG.getContentPane().add(new JLabel("Connected. Waiting for server response..."), BorderLayout.CENTER);
		    							WAIT_DIALOG.addInternalFrameListener(new InternalFrameListener() {
											@Override
											public void internalFrameOpened(InternalFrameEvent e) {
											}
											@Override
											public void internalFrameIconified(InternalFrameEvent e) {
											}
											@Override
											public void internalFrameDeiconified(InternalFrameEvent e) {
											}
											@Override
											public void internalFrameDeactivated(InternalFrameEvent e) {
											}
											@Override
											public void internalFrameClosing(InternalFrameEvent e) {
											}
											@Override
											public void internalFrameClosed(InternalFrameEvent e) {
		    									synchronized (CLIENT) {
		    										CLIENT.notifyAll();
		    									}
											}
											@Override
											public void internalFrameActivated(InternalFrameEvent e) {
											}
										});
		    							WAIT_DIALOG.pack();
		    							new Thread(new Runnable() {
		    								@Override
		    								public void run() {
		    									synchronized (CLIENT) {
		    										while (CLIENT.getPlayer() == null && WAIT_DIALOG.isVisible() && CLIENT.isRunning()) {
		    											try {
															CLIENT.wait();
														} catch (InterruptedException e) {
														}
		    										}
		    									}
		    									if (WAIT_DIALOG.isVisible()) {
			    									waitDialogDisposed = false;
			    									WAIT_DIALOG.dispose();
		    									}
		    								}
		    							}).start();
		    						}
		    					} else {
		    			    		JOptionPane.showInternalMessageDialog(ProjectionPlane.this, "Unable to establish connection with server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
		    					}
		    				}
						}
					});
    			} else if (action == PausedMenuAction.CREATE_ITEM) {
    				ObjectCreatorDialog.showObjectCreatorDialog(new DialogDisposedAction<ObjectCreatorDialog.CreatableObject>() {
						@Override
						public void dialogDisposed(ObjectCreatorDialog.CreatableObject newObject) {
		    				if (newObject != null) {
		    					newObject.objectCreated();
		    				}
						}
    				});
    			} else if (action == PausedMenuAction.SETTINGS) {
    				SettingsMenu.showDialog(ProjectionPlane.this, getPlaneName(), getFrameDelay(), FRAME_DELAY_ADJUSTER, isGameRunning() ? (int) getSightRange() : null, getGame() != null ? (int) Math.ceil(Map.findDistance(getMap().getWidth(), getMap().getHeight())) : 0, SIGHT_RANGE_ADJUSTER, getResolution(), RESOLUTION_ADJUSTER, isShowFPS(), isShowTime(), isShowShading(), getSensitivity(), getRelativeVolume(), isMute(), new ProjectionPlane.DialogDisposedAction<SettingsMenu>() {
    					@Override
						public void dialogDisposed(SettingsMenu settings) {
    						if (settings != null) {
        	    				if (settings.getName() != null) {
        	    					setPlaneName(settings.getName());
        	    				}
        	    				setFrameDelay(settings.getFrameDelay());
        	    				if (settings.getSightRange() != null) {
        	    					setSightRange(settings.getSightRange());
        	    				}
        	    				setResolution(settings.getResolution());
        	    				setShowFPS(settings.getShowFPS());
        	    				setShowTime(settings.getShowTime());
        	    				setShowShading(settings.getShowShading());
        	    				setSensitivity(settings.getSensitivity());
        	    				if (!settings.isMute()) {
        	    					setRelativeVolume(settings.getRelativeVolume());
        	    				}
        	    				setMute(settings.isMute());
    						}
						}
					});
    			} else if (action == PausedMenuAction.QUIT) {
    				if (getGame() != null && getGame().isRunning()) {
    					getGame().stop();
    					lockCursor(false);
    				} else {
    					stop();
    				}
    			}
    		}
    	});
        
        CONSOLE = new Console(this, 350, 200,
        		Gun.DEFAULT_PERFECT_ACCURACY_COMMAND,
        		Gun.DEFAULT_RECOIL_COMMAND,
        		Player.DEFAULT_PLAYER_SPEED_COMMAND,
        		Player.DEFAULT_PLAYER_JUMP_COMMAND,
        		Player.DEFAULT_FALL_DAMAGE_COMMAND,
        		Player.DEFAULT_PLAYER_MONEY_COMMAND,
        		new Console.Command("Transparency", "", "transparent") {
		        	@Override
		        	public String getDescription() {
		        		return "Sets the transparency of the walls " + (ProjectionPlane.this.seeThrough ? "off" : "on");
		        	}
		    		@Override
		    		public boolean execute(Console console, Game game, String input) {
						synchronized (ProjectionPlane.this) {
							ProjectionPlane.this.seeThrough = !ProjectionPlane.this.seeThrough;
						}
		    			console.appendText("ACTION: Transparency " + (ProjectionPlane.this.seeThrough ? "ON" : "OFF"));
		    			return true;
		    		}
				},
        		new Console.Command("Store", "Opens store", "store") {
		    		@Override
		    		public boolean execute(Console console, Game game, String input) {
						openStore(Store.DEFAULT_STORE, false);
		    			console.appendText("ACTION: Store Opened");
		    			return true;
		    		}
				},
				new Console.Command("Third Person", "", "thirdperson") {
		        	@Override
		        	public String getDescription() {
		        		return "Sets the third person view " + (isThirdPerson() ? "off" : "on");
		        	}
		    		@Override
		    		public boolean execute(Console console, Game game, String input) {
						setThirdPerson(isThirdPerson() ? null : 2d);
		    			console.appendText("ACTION: Third Person View " + (isThirdPerson() ? "ON" : "OFF"));
		    			return true;
		    		}
				},
        		Map.DEFAULT_RADAR_DETECTION_COMMAND,
        		Player.DEFAULT_PLAYER_STATS_COMMAND);
        
		setInputEnabled(true);
	}
	
	public static ProjectionPlane getSingleton() {
		return SINGLETON;
	}
	
	public boolean isRunning() {
		return IS_RUNNING.get();
	}
	
	public void start() {
		setVisible(true);
		IS_RUNNING.set(true);
		lockCursor(false);
		
		long cTime = System.nanoTime();
		while (isRunning()) {
			cTime = System.nanoTime();
			while (!ProjectionPlane.this.freeMouse && isRunning()) {
				synchronized (this) {
					this.mspf = (System.nanoTime() - cTime) / 1000000d;
				}
				cTime = System.nanoTime();
				
				updateDistanceToPlane();

				repaint();
				
				checkKeys();
				checkButtons();
				attemptToStand();
				checkGame();
		        
				for (ProjectionPlaneEventListener l : PROJECTION_PLANE_EVENT_LISTENERS) {
					if (l != null) {
						l.postDrawFrameCheck();
					}
				}
		        try {Thread.sleep(Math.max(getFrameDelay(), (int) (this.renderTime != null ? 1000 / 60 - this.renderTime : 0)));} catch (Exception e) {}
			}
			checkButtons();
		}
		stop();
		removeAllMouseListeners();
		for (ProjectionPlaneEventListener l : PROJECTION_PLANE_EVENT_LISTENERS) {
			if (l != null) {
				l.projectionPlaneStopped();
			}
		}
		ProjectionPlane.this.chatMessage = null;
		ProjectionPlane.this.chatMessageTarget = null;
	}
	
	public void stop() {
		IS_RUNNING.set(false);
		if (getGame() != null) {
			getGame().stop();
		}
	}
	
	@Override
	public void setVisible(boolean b) {
		getFrame().setVisible(b);
		super.setVisible(b);
	}
	
	private void draw(Graphics g, Double renderTime) {
		g.clearRect(0, 0, getWidth(), getHeight());

		String pointedItemString = renderMapItems(g);
		
		/* TODO ((Graphics2D) g).setPaint(new RadialGradientPaint(getWidth() / 2, getHeight() / 2, getWidth() / 4, new float[]{0, 0.2f, 1}, new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(0, 0, 0, 200)}));
		g.fillRect(0, 0, getWidth(), getHeight());*/
		
		if (!(getPlayer().getMainHoldItem() instanceof ZoomGraphicsDrawer) || !((ZoomGraphicsDrawer) getPlayer().getMainHoldItem()).clearGraphicsForZoom()) {
			drawVerticalInfo(g, Color.WHITE);
			if (!getPlayer().isSpectator()) {
				drawPlayerHoldItem(g);
				drawMoneyInfo(g, Color.WHITE);
				g.setColor(Color.WHITE);
				drawCrossHairs(g);
				if (getGame().allowsRadar()) {
					getMap().drawRadar(g, getWidth() / 14, getWidth() / 14, getWidth() / 15, Color.GREEN, 2500, 15, getPlayer(), getGame().getMapItems());
				}
				drawCompass(g, 16, Color.WHITE);
				drawHorizontalAngleAndLocation(g, Color.WHITE);
				if (this.showMiniMap) {
					getMap().drawMap(g, getPlayer(), 0, 0, (int) Math.round(getWidth() * 0.15), (int) Math.round(getHeight() * 0.2), miniMapZoom, getSightRange());
				}
				if (getMap().isStoreLocation(getLocationX(), getLocationY(), getPlayer().getBottomHeight(), getPlayer())) {
					Store.drawStoreIcon(g, Color.GREEN, (int) (getWidth() * 0.005), (int) (getHeight() * 0.45), (int) (getHeight() * 0.06), (int) (getHeight() * 0.06));
				}
				if (getPlayer().getBomb() != null) {
					Bomb.drawBombIcon(g, Color.GREEN, (int) (getWidth() * 0.0065), (int) (getHeight() * 0.52), (int) (getHeight() * 0.05), (int) (getHeight() * 0.038), getMap().isBombSite(getLocationX(), getLocationY(), getLocationZ()) ? Color.RED : null, 300, 150);
				}
				if (getPlayer().hasDefuseKit()) {
					DefuseKit.drawDefuseKitIcon(g, Color.GREEN, (int) (getWidth() * 0.005), (int) (getHeight() * 0.38), (int) (getHeight() * 0.06), (int) (getHeight() * 0.06));
				}
			}
		}
		if (!getPlayer().isSpectator() && getPlayer().getMainHoldItem() instanceof ZoomGraphicsDrawer) {
			((ZoomGraphicsDrawer) getPlayer().getMainHoldItem()).drawZoomGraphics(g, getWidth(), getHeight());
		}
		if (getPlayer() != null) {
			Double progress = getPlayer().getDefuseProgress();
			if (progress != null) {
				DefuseKit.drawProgressBar(g, getWidth(), getHeight(), DefuseKit.DEFUSE_BAR_COLOR, progress);
			}
		}
		if (!getPlayer().isSpectator()) {
			drawPlayerInfo(g, Color.WHITE, Color.RED);
			drawHitInfo(g);
		}
		drawFlash(g);
		drawSpectatorBars(g);
		drawRenderingInfo(g, Color.WHITE, renderTime);
		drawPointedItemInfo(g, Color.BLUE, pointedItemString);
		drawClock(g, Color.WHITE, Color.WHITE, Color.WHITE, 500, 250, Color.RED);
		drawRecentEvents(g);
		CylinderMapItem[] itemsCollided = getGame().getCylinderMapItemsInRange(getLocationX(), getLocationY(), getPlayer().getPhysicalHalfWidth(), 0, getPlayer(), false);
		for (CylinderMapItem i : itemsCollided) {
			i.itemColliding(getPlayer(), g, getWidth(), getHeight());
		}
		drawChat(g, Color.WHITE);
		drawGameMainMessage(g, Color.YELLOW);
		drawScoreTable(g);
		
		if (renderTime != null) {
			ArrayList<SmartAdjuster<?>> smartAdjusters = new ArrayList<SmartAdjuster<?>>();
			if (FRAME_DELAY_ADJUSTER.isActivated()) {
				smartAdjusters.add(FRAME_DELAY_ADJUSTER);
			}
			if (RESOLUTION_ADJUSTER.isActivated()) {
				smartAdjusters.add(RESOLUTION_ADJUSTER);
			}
			if (SIGHT_RANGE_ADJUSTER.isActivated()) {
				smartAdjusters.add(SIGHT_RANGE_ADJUSTER);
			}
			if (smartAdjusters.size() > 0) {
				Collections.sort(smartAdjusters, SMART_ADJUSTER_COMPARATOR);
				boolean adjusted = false;
				Integer priority = null;
				for (SmartAdjuster<?> sa : smartAdjusters) {
					if (priority != null && priority != sa.getPriority()) {
						break;
					} else if (sa.isActivated() && 1000d / renderTime < sa.getMinFPS() && sa.adjust()) {
						adjusted = true;
						priority = sa.getPriority();
					}
				}
				priority = null;
				if (!adjusted) {
					for (SmartAdjuster<?> sa : smartAdjusters) {
						if (priority != null && priority != sa.getPriority()) {
							break;
						} else if (sa.isActivated() && 1000d / renderTime < sa.getMinFPS() && sa.forceAdjust()) {
							adjusted = true;
							priority = sa.getPriority();
						}
					}
				}
			}
		}
	}
	
	private void drawFlash(Graphics g) {
		if (System.currentTimeMillis() <= getPlayer().getStartFlashTime() + getPlayer().getFlashDuration()) {
			g.setColor(new Color(255, 255, 255, 255 - (int) Math.floor(255 * Math.pow((System.currentTimeMillis() - getPlayer().getStartFlashTime()) * 1d / getPlayer().getFlashDuration(), 3))));
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}
	
	private Double getCrossHairsFocus() {
		if (getPlayer().getTacticalShield() == null || !getPlayer().getTacticalShield().isDeployed()) {
			return getPlayer().getMainHoldItem() == null ? null : getPlayer().getMainHoldItem().getCrossHairsFocus(getPlayer());
		} else {
			return getPlayer().getTacticalShield().getCrossHairsFocus();
		}
	}
	
	private void drawCrossHairs(Graphics g) {
		if (!isThirdPerson()) {
			Double crossHairsFocusRaw = getCrossHairsFocus();
			if (crossHairsFocusRaw != null) {
				int crossHairsFocus = (int) Math.max(Math.min(Math.round(getDistanceToPlane() * Math.tan(crossHairsFocusRaw)), getWidth() / 30), 3);
				Point mouse = new Point(getWidth() / 2, getHeight() / 2);
				g.drawLine(mouse.x - crossHairsFocus - 10, mouse.y, mouse.x - crossHairsFocus, mouse.y);
				g.drawLine(mouse.x + crossHairsFocus, mouse.y, mouse.x + crossHairsFocus + 10, mouse.y);
				g.drawLine(mouse.x, mouse.y - crossHairsFocus - 10, mouse.x, mouse.y - crossHairsFocus);
				g.drawLine(mouse.x, mouse.y + crossHairsFocus, mouse.x, mouse.y + crossHairsFocus + 10);
				g.setFont(new Font(Main.DEFAULT_FONT, Font.PLAIN, 10));
				FontMetrics fm = g.getFontMetrics(g.getFont());
				String viewVAngleStr = String.valueOf(Math.round(Math.toDegrees(getPlayer().getVerticalDirection()) * 100) / 100d) + "\u00B0";
				g.drawString(viewVAngleStr, mouse.x - crossHairsFocus - 15 - fm.stringWidth(viewVAngleStr), mouse.y + fm.getAscent() - fm.getHeight() / 2);
				if (allowHorizontalDirection()) {
					String viewHAngleStr = String.valueOf(Math.round(Math.toDegrees(((Math.PI / 2 - getPlayer().getHorizontalDirection()) % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2)) * 100) / 100d) + "\u00B0";
					g.drawString(viewHAngleStr, mouse.x - fm.stringWidth(viewHAngleStr) / 2, mouse.y + crossHairsFocus + 10 + fm.getAscent());
				}
			}
		}
	}
	
	private void drawCompass(Graphics g, int nOfDirections, Color color) {
		if (allowHorizontalDirection()) {
			g.setColor(color);
			for (int i = 0; i < (int) Math.ceil(getPlayer().getHorizontalFOV() / (Math.PI * 2 / nOfDirections)); i++) {
				double angle = (getPlayer().getHorizontalDirection() + getPlayer().getHorizontalFOV() / 2) % (Math.PI * 2 / nOfDirections) - getPlayer().getHorizontalFOV() / 2 + i * (Math.PI * 2 / nOfDirections) - getPlayer().getHorizontalDirection() + Math.PI / 2;
				angle = (angle % (Math.PI * 2) + (Math.PI * 2)) % (Math.PI * 2);
				int x = (int) Math.round(getWidth() / 2 + Math.tan(angle + getPlayer().getHorizontalDirection() - Math.PI / 2) * getDistanceToPlane());
				
				Compass.CompassPoints point = Compass.degressToCompassPoint(angle, nOfDirections);
				double lineHeight = (4 - point.getAbbr().length()) * 8;
				g.drawLine(x, (int) Math.floor(35 - lineHeight / 2), x, (int) Math.ceil(35 + lineHeight / 2));
				
				g.setFont(new Font(Main.DEFAULT_FONT, (3 - point.getAbbr().length()) / 2, 9 + (3 - point.getAbbr().length()) * 2));
				FontMetrics fm = g.getFontMetrics(g.getFont());
				String directionAbbr = point.getAbbr();
				String directionAngle = String.valueOf(Math.round(Math.toDegrees(angle) * 10) / 10d) + "\u00B0";
				g.drawString(" " + directionAngle, x - fm.stringWidth(directionAngle) / 2, 20 - fm.getDescent());
				g.drawString(directionAbbr, x - fm.stringWidth(directionAbbr) / 2, 50 + fm.getAscent());
			}
		}
	}
	
	private void drawVerticalInfo(Graphics g, Color color) {
		g.setColor(color);
		g.setFont(new Font(Main.DEFAULT_FONT, Font.PLAIN, 10));
		FontMetrics fm = g.getFontMetrics(g.getFont());
		String viewHeightStr = String.valueOf(Math.round(getLocationZ() * 100) / 100d) + " m";
		String viewVAngleStr = String.valueOf(Math.round(Math.toDegrees(getPlayer().getVerticalDirection()) * 100) / 100d) + "\u00B0";
		String floorHeightStr = String.valueOf(Math.round(getPlayer().getFloorHeight() * 100) / 100d) + " m";
		g.drawLine(getWidth() - 15, getHeight() / 2, getWidth(), getHeight() / 2);
		if (getPlayer().isJumping()) {
			String jumpHeightStr = String.valueOf(Math.round(getPlayer().getJumpHeight() * 100) / 100d) + " m";
			g.drawString(jumpHeightStr, getWidth() - 5 - fm.stringWidth(jumpHeightStr), getHeight() / 2 + fm.getAscent() - fm.getHeight() / 2 - 40);
		}
		g.drawString(viewHeightStr, getWidth() - 5 - fm.stringWidth(viewHeightStr), getHeight() / 2 + fm.getAscent() - fm.getHeight() / 2 - 20);
		g.drawString(viewVAngleStr, getWidth() - 20 - fm.stringWidth(viewVAngleStr), getHeight() / 2 + fm.getAscent() - fm.getHeight() / 2);
		g.drawString(floorHeightStr, getWidth() - 5 - fm.stringWidth(floorHeightStr), getHeight() / 2 + fm.getAscent() - fm.getHeight() / 2 + 20);
	}
	
	private void drawHorizontalAngleAndLocation(Graphics g, Color color) {
		if (allowHorizontalDirection()) {
			g.setColor(color);
			g.setFont(new Font(Main.DEFAULT_FONT, Font.PLAIN, 10));
			FontMetrics fm = g.getFontMetrics(g.getFont());
			String viewHAngleStr = String.valueOf(Math.round(Math.toDegrees(((Math.PI / 2 - getPlayer().getHorizontalDirection()) % (Math.PI * 2) + Math.PI * 2) % (Math.PI * 2)) * 100) / 100d) + "\u00B0";
			String locationString = "(" + (Math.round(getLocationX() * 100) / 100d) + ", " + (Math.round(getLocationY() * 100) / 100d) + ")";
			g.drawLine(getWidth() / 2, 0, getWidth() / 2, 15);
			g.drawString(viewHAngleStr, getWidth() / 2 - fm.stringWidth(viewHAngleStr) / 2, 15 + fm.getAscent());
			g.drawString(locationString, getWidth() / 2 - fm.stringWidth(locationString) / 2, 15 + fm.getAscent() + fm.getHeight());
		}
	}
	
	private void drawRenderingInfo(Graphics g, Color color, Double renderTime) {
		if (isShowFPS() && renderTime != null) {
			g.setColor(color);
			g.setFont(new Font(Main.DEFAULT_FONT, Font.PLAIN, 10));
			FontMetrics fm = g.getFontMetrics(g.getFont());
			String fpsSubString = String.valueOf(Math.round(100000d / renderTime) / 100d);
			fpsSubString = fpsSubString.substring(0, Math.min(fpsSubString.length(), 5));
			String fpsStr = "FPS: " + fpsSubString; // + " Avg: " +  Math.round(this.nOfFrames / (Math.max(getGame().getGameDuration(), 1) / 1000d) * 100) / 100d;
			String renderTimeStr = "Render Time(ms): " + Math.round(renderTime * 100) / 100d;
			g.drawString(fpsStr, 5, getHeight() + fm.getAscent() - fm.getHeight() / 2 - 20);
			g.drawString(renderTimeStr, 5, getHeight() + fm.getAscent() - fm.getHeight() / 2 - 10);
		}
	}
	
	private void drawPointedItemInfo(Graphics g, Color color, String stringAboveCrossHairs) {
		if (stringAboveCrossHairs != null) {
			if (System.currentTimeMillis() > getPlayer().getStartFlashTime() + getPlayer().getFlashDuration()) {
				g.setColor(color);
				g.setFont(new Font(Main.DEFAULT_FONT, Font.PLAIN, 11));
				FontMetrics fm = g.getFontMetrics(g.getFont());
				g.drawString(stringAboveCrossHairs, 5, getHeight() + fm.getAscent() - fm.getHeight() * 3 / 2 - 10 - (isShowFPS() ? 20 : 0));
			}
		}
	}
	
	private void drawMoneyInfo(Graphics g, Color textColor) {
		if (!getPlayer().isSpectator()) {
			g.setColor(textColor);
			g.setFont(Weapon.getWeaponsInfoFont(getWidth(), getHeight()));
			FontMetrics fm = g.getFontMetrics(g.getFont());
			fm = g.getFontMetrics(g.getFont());
			String moneyStr = "$ " + getPlayer().getMoney();
			g.drawString(moneyStr, getWidth() - 5 - fm.stringWidth(moneyStr), getHeight() - 5 - fm.getHeight() - fm.getDescent());
		}
	}
	
	private void drawPlayerInfo(Graphics g, Color textColor, Color symbolColor) {
		g.setFont(new Font(Main.DEFAULT_FONT, Font.BOLD, getHeight() / 30));
		FontMetrics fm = g.getFontMetrics(g.getFont());
		String healthStr = String.valueOf(getPlayer().getHealth());
		int healthStrCenterX = (int) Math.round(getWidth() * 0.1);
		g.setColor(textColor);
		g.drawString(healthStr, healthStrCenterX - fm.stringWidth(healthStr) / 2, getHeight() - 10 - fm.getDescent());
		g.setColor(symbolColor);
		g.fillRect(healthStrCenterX - 10 - fm.stringWidth(healthStr) / 2 - fm.getHeight() + 1, getHeight() - 10 - fm.getHeight() / 2 - fm.getHeight() / 6 + 1, fm.getHeight() - 1, fm.getHeight() / 3 - 1);
		g.fillRect(healthStrCenterX - 10 - fm.stringWidth(healthStr) / 2 - fm.getHeight() / 2 - fm.getHeight() / 6 + 1, getHeight() - 10 - fm.getHeight() + 1, fm.getHeight() / 3 - 1, fm.getHeight() - 1);

		int armorStrCenterX = (int) Math.round(getWidth() * 0.3);
		String armorStr = String.valueOf(getPlayer().getArmor());
		Player.drawKevlarAndHelmetSymbol(g, armorStrCenterX - 10 - fm.stringWidth(armorStr) / 2 - fm.getHeight() + 1, getHeight() - 10 - fm.getHeight() + 1, fm.getHeight() - 1, fm.getHeight() - 1, getPlayer().hasHelmet());
		g.setColor(textColor);
		g.drawString(armorStr, armorStrCenterX - fm.stringWidth(armorStr) / 2, getHeight() - 10 - fm.getDescent());
		
		if (getPlayer().hasMuscles()) {
			int musclesStrCenterX = (int) Math.round(getWidth() * 0.7);
			String musclesString = Math.round(Player.MUSCLE_SIZE * 100) + "%";
			g.drawImage(Main.BICEPS_IMAGE, musclesStrCenterX - 10 - fm.stringWidth(musclesString) / 2 - fm.getHeight() + 1, getHeight() - 10 - fm.getHeight() + 1, fm.getHeight() - 1, fm.getHeight() - 1, null);
			g.drawString(musclesString, musclesStrCenterX - fm.stringWidth(musclesString) / 2, getHeight() - 10 - fm.getDescent());
		}
	}
	
	private void drawClock(Graphics g, Color textColor, Color symbolColor, Color spectatorTextColor, long flashInterval, long flashDuration, Color flashTextColor) {
		if (getPlayer().isSpectator()) {
			g.setColor(spectatorTextColor);
			FontMetrics fm = g.getFontMetrics(g.getFont());
			String timeStr = "Time: " + new SimpleDateFormat("mm:ss").format(new Date(Math.abs((long) Math.floor(getGame().getRoundDuration() / 1000d) * 1000)));
			g.drawString(timeStr, getWidth() - fm.stringWidth(timeStr) - 3, fm.getHeight());
		} else {
			g.setColor(getGame().getRoundDuration() >= 0 ? textColor : (System.currentTimeMillis() % Math.abs(flashInterval) <= Math.abs(flashDuration) ? flashTextColor : textColor));
			g.setFont(new Font(Main.DEFAULT_FONT, Font.BOLD, getHeight() / 30));
			FontMetrics fm = g.getFontMetrics(g.getFont());
			String timeStr = new SimpleDateFormat("mm:ss").format(new Date(Math.abs((long) Math.floor(getGame().getRoundDuration() / 1000d) * 1000)));
			int timeStrCenterX = (int) Math.round(getWidth() * 0.5);
			g.drawString(timeStr, timeStrCenterX - fm.stringWidth(timeStr) / 2, getHeight() - 10 - fm.getDescent());
			g.setColor(symbolColor);
			g.drawOval(timeStrCenterX - 10 - fm.stringWidth(timeStr) / 2 - fm.getHeight() + 1, getHeight() - 10 - fm.getHeight(), fm.getHeight() - 1, fm.getHeight() - 1);
			g.fillOval(timeStrCenterX - 10 - fm.stringWidth(timeStr) / 2 - fm.getHeight() * 11 / 20 + 1, getHeight() - 10 - fm.getHeight() * 11 / 20 - 1, fm.getHeight() / 10, fm.getHeight() / 10);
			g.drawLine(timeStrCenterX - 10 - fm.stringWidth(timeStr) / 2 - fm.getHeight() / 2 + 1, getHeight() - 10 - fm.getHeight() / 2, timeStrCenterX - 10 - fm.stringWidth(timeStr) / 2 - fm.getHeight() / 2 + 1, getHeight() - 10 - fm.getHeight() * 9 / 10);
			g.drawLine(timeStrCenterX - 10 - fm.stringWidth(timeStr) / 2 - fm.getHeight() / 2 + 1, getHeight() - 10 - fm.getHeight() / 2, timeStrCenterX - 10 - fm.stringWidth(timeStr) / 2 - fm.getHeight() / 10 + 1, getHeight() - 10 - fm.getHeight() / 2);
		}
	}
	
	private void drawHitInfo(Graphics g) {
		if (!isThirdPerson()) {
			ArrayList<AttackEvent> attackEvents = getPlayer().getAttackEvents();
			if (attackEvents.size() > 0) {
				g.setColor(Color.RED);
				for (AttackEvent e : attackEvents) {
					g.drawArc(getWidth() / 4, getHeight() / 4, getWidth() / 2, getHeight() / 2, (int) Math.toDegrees(Math.PI / 2 - e.getIncomingDirection() - getHorizontalDirection()) - 20, 40);
				}
				g.setColor(new Color(255, 0, 0, 80));
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		}
	}
	
	private void drawPlayerHoldItem(Graphics g) {
		if (!isThirdPerson() && getPlayer().isTacticalShieldDeployed()) {
			getPlayer().getTacticalShield().drawImage(g, getWidth(), getHeight(), getDistanceToPlane(), getPlayer().getPhysicalHalfWidth(), getPlayer().getSightHeight(), Color.WHITE);
		} else {
			if (!isThirdPerson() && getPlayer().getMainHoldItem() != null) {
				getPlayer().getMainHoldItem().drawImage(g, getWidth(), getHeight(), Color.WHITE);
			}
			if (!isThirdPerson() && getPlayer().getTacticalShield() != null) {
				getPlayer().getTacticalShield().drawImage(g, getWidth(), getHeight(), getDistanceToPlane(), getPlayer().getPhysicalHalfWidth(), getPlayer().getSightHeight(), Color.WHITE);
			}
		}
	}
	
	private void drawRecentEvents(Graphics g) {
		int i = 0;
		g.setFont(Main.RECENT_EVENTS_FONT);
		FontMetrics fm = g.getFontMetrics(g.getFont());
		Color backColor = new Color(0, 0, 0, 96);
		for (Game.KillEvent e : getGame().getRecentKillEvents(getNumberOfRecentEvents())) {
			int headShotIconWidth = e.isHeadShot() && Game.KillEvent.HEADSHOT_ICON != null ? (fm.getHeight() - 1) * Game.KillEvent.HEADSHOT_ICON.getWidth() / Game.KillEvent.HEADSHOT_ICON.getHeight() : 0;
			if (i++ < getNumberOfRecentEvents()) {
				String infoString1 = (Game.KillEvent.HEADSHOT_ICON == null || !e.isHeadShot() ? ((e.isHeadShot() ? "--[HEAD]" : "-") + "-> ") : " ") + e.getVictim().getName();
				String infoString2 = "";
				if (e.getKiller() != null && e.getCauseOfDeath() != null) {
					infoString2 = e.getKiller().getName() + " ---(" + e.getCauseOfDeath().getName() + ")";
				} else if (e.getCauseOfDeath() != null) {
					infoString2 = "---(" + e.getCauseOfDeath().getName() + ")";
				} else if (e.getVictim().equals(e.getKiller())){
					infoString2 = "Suicide";
				}
				if (Game.KillEvent.HEADSHOT_ICON != null) {
					infoString2 += " ";
				}
				g.setColor(backColor);
				g.fillRoundRect(getWidth() - fm.stringWidth(infoString1 + infoString2) - headShotIconWidth - 10, i * fm.getHeight() - fm.getAscent() + fm.getLeading() + 6, fm.stringWidth(infoString1 + infoString2) + headShotIconWidth + 10, fm.getHeight() - 1, 3, 3);
				if (e.getVictim().getTeam() != null) {
					g.setColor(e.getVictim().getTeam().getColor());
				} else {
					g.setColor(Team.SPECTATOR_COLOR);
				}
				if (Game.KillEvent.HEADSHOT_ICON != null && headShotIconWidth > 0) {
					g.drawImage(Game.KillEvent.HEADSHOT_ICON, getWidth() - fm.stringWidth(infoString1) - headShotIconWidth - 5, i * fm.getHeight() - fm.getAscent() + fm.getLeading() + 6, headShotIconWidth, fm.getHeight() - 1, null);
				}
				g.drawString(infoString1, getWidth() - fm.stringWidth(infoString1) - 5, i * fm.getHeight() + 5);
				if (e.getKiller() != null && e.getKiller().getTeam() != null) {
					g.setColor(e.getKiller().getTeam().getColor());
				} else {
					g.setColor(Team.SPECTATOR_COLOR);
				}
				g.drawString(infoString2, getWidth() - fm.stringWidth(infoString1 + infoString2) - headShotIconWidth - 5, i * fm.getHeight() + 5);
			}
		}
	}
	
	private void drawSpectatorBars(Graphics g) {
		if (getPlayer().isSpectator()) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, getWidth(), getHeight() / 8);
			g.fillRect(0, getHeight() - getHeight() / 8, getWidth(), getHeight() / 8);
			g.setColor(Color.WHITE);
			g.setFont(Main.SPECTATOR_INFO_FONT);
			FontMetrics fm = g.getFontMetrics(g.getFont());
			String caption = "Free Cam";
			g.drawString(caption, (getWidth() - fm.stringWidth(caption)) / 2, getHeight() - getHeight() / 16 + fm.getHeight() / 2 - fm.getDescent());
		}
	}
	
	private void drawChat(Graphics g, Color color) {
		int inputLineY = (int) (getHeight() * 0.65);
		g.setFont(Main.CHAT_FONT);
		FontMetrics fm = g.getFontMetrics(g.getFont());
		Color backColor = new Color(70, 70, 0, 100);
		if (isChatEnabled()) {
			g.setColor(backColor);
			g.fillRect(0, inputLineY - fm.getAscent() + fm.getLeading(), getWidth(), fm.getHeight());
			g.setColor(ChatMessage.PUBLIC_CHAT_MESSAGE_COLOR);
			g.drawString("(Hold CTRL for Team Chat | Enter <Player Name> followed by CTRL for Private Chat) ", 3, inputLineY);
			ChatMessage.ChatMessageTarget target = this.chatMessageTarget != null ? this.chatMessageTarget : null;
			if (PRESSED_KEYS.contains(KeyEvent.VK_CONTROL)) {
				target = getPlayer().getTeam();
			}
			String message = "<" + (target != null ? target.getRecieverName() : ChatMessage.PUBLIC_CHAT_RECIEVER_NAME) + ">: " + this.chatMessage;
			inputLineY += fm.getHeight();
			g.setColor(backColor);
			g.fillRect(0, inputLineY - fm.getAscent() + fm.getLeading(), getWidth(), fm.getHeight());
			g.setColor(target != null ? target.getMessageColor() : ChatMessage.PUBLIC_CHAT_MESSAGE_COLOR);
			g.drawString(message, 3, inputLineY);
			if (System.currentTimeMillis() % 1000 <= 500) {
				int x = 4 + fm.stringWidth(message);
				g.drawLine(x, inputLineY + fm.getDescent(), x, inputLineY - fm.getHeight());
			}
		} else {
			inputLineY += fm.getHeight();
		}
		for (ChatMessage.MessageContent m : getPlayer().getRecentChatMessages(Main.NUMBER_OF_RECENT_CHAT_MESSAGES)) {
			inputLineY += fm.getHeight();
			g.setColor(m.isSender(getPlayer()) ? ChatMessage.SELF_CHAT_MESSAGE_COLOR : m.getMessageColor());
			String messageString = m.getSenderName() + (m.isSender(getPlayer()) ? " (ME)" : "");
			messageString +=  " <" + m.getRecieverName() + ">";
			messageString += " :" + m.getMessage();
			g.drawString(messageString, 3, inputLineY);
		}
	}
	
	private void drawGameMainMessage(Graphics g, Color color) {
		if (getGame() != null) {
			String message = getGame().getMainMessage();
			if (message != null) {
				g.setFont(Main.GAME_MAIN_MESSAGE_FONT);
				g.setColor(color);
				FontMetrics fm = g.getFontMetrics(g.getFont());
				String[] messages = message.split("\n");
				int lineY = getHeight() / 3 - fm.getHeight() / 2 + fm.getDescent();
				for (String s : messages) {
					g.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, lineY);
					lineY += fm.getHeight();
				}
			}
		}
	}
	
	private void drawScoreTable(Graphics g) {
		if (!isChatEnabled() && InputConfiguration.VIEW_SCORE.hasInput(PRESSED_KEYS, PRESSED_BUTTONS)) {
			Color mainColor = new Color(120, 120, 0);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setFont(Main.SCORE_FONT);
			FontMetrics fm = g2d.getFontMetrics(g2d.getFont());
			RoundRectangle2D backRect = new RoundRectangle2D.Double(getWidth() * 0.1, getHeight() * 0.1, getWidth() * 0.8, getHeight() * 0.8, getWidth() * 0.05, getWidth() * 0.05);
			g2d.setColor(new Color(100, 100, 0, 60));
			g2d.fill(backRect);
			g2d.setColor(mainColor);
			g2d.draw(backRect);

			g2d.setColor(Color.WHITE);
			int currentY = (int) (getHeight() * 0.1 + fm.getHeight());
			String serverClientInfo = "";
			String ipAddress = getGame().getServerIP();
			if (getGame().isClient()) {
				serverClientInfo += " - (Client" + (ipAddress != null ? ": " + ipAddress : "") + ") Server: " + ipAddress;
			} else if (getGame().isServer()) {
				serverClientInfo += " - (Host)" + (ipAddress != null ? " Server: " + ipAddress : "");
			}
			if (getGame().isClient() || getGame().isServer()) {
				serverClientInfo += " ";
			}
			g2d.drawString(getPlayer().getName() + serverClientInfo, (int) (getWidth() * 0.12), currentY);
			g2d.drawString("Score", (int) (getWidth() * 0.6), currentY);
			g2d.drawString("Deaths", (int) (getWidth() * 0.7), currentY);
			g2d.drawString("Latency", (int) (getWidth() * 0.8), currentY);
			g2d.setColor(mainColor);
			currentY += 4;
			g2d.drawLine((int) (getWidth() * 0.105), (int) (getHeight() * 0.1 + fm.getHeight()) + 4, (int) (getWidth() * 0.895), currentY);

			currentY += 5;
			for (int i = 1; i <= getMap().getNumberOfTeams(); i++) {
				int nOfTeamPlayers = 0;
				for (Player p : getPlayer().getGame().getPlayers()) {
					if (getMap().getTeam(i - 1).equals(p.getTeam())) {
						nOfTeamPlayers++;
					}
				}
				currentY += 10;
				g2d.setColor(getMap().getTeam(i - 1).getColor());
				g2d.drawString(getMap().getTeam(i - 1).getName() + " - " + nOfTeamPlayers + " Players", (int) (getWidth() * 0.2), currentY);
				g2d.drawString(String.valueOf(getGame().getTeamScore(getMap().getTeam(i - 1))), (int) (getWidth() * 0.6), currentY);
				currentY += 4;
				g2d.drawLine((int) (getWidth() * 0.105), currentY, (int) (getWidth() * 0.895), currentY);
				currentY += fm.getHeight() + 5;
				ArrayList<Player> team = new ArrayList<Player>();
				for (Player p : getGame().getPlayers()) {
					if (getMap().getTeam(i - 1).equals(p.getTeam())) {
						team.add(p);
					}
				}
				Collections.sort(team, new Comparator<Player>() {
					@Override
					public int compare(Player o1, Player o2) {
						if (o1.getKills() == o2.getKills()) {
							return o1.getDeaths() - o2.getDeaths();
						} else {
							return o1.getKills() - o2.getKills();
						}
					}
				});
				for (Player p : team) {
					if (p.equals(getPlayer())) {
						Color previousColor = g2d.getColor();
						g2d.setColor(new Color(100, 100, 0, 70));
						g2d.fillRect((int) Math.ceil(getWidth() * 0.1), currentY - fm.getHeight() + fm.getDescent(), (int) Math.floor(getWidth() * 0.8), fm.getHeight());
						g2d.setColor(previousColor);
					}
					g2d.drawString(p.getName(), (int) (getWidth() * 0.2), currentY);
					g2d.drawString(String.valueOf(p.getKills()), (int) (getWidth() * 0.6), currentY);
					g2d.drawString(String.valueOf(p.getDeaths()), (int) (getWidth() * 0.7), currentY);
					g2d.drawString(p.getLatency() != null ? String.valueOf(p.getLatency()) : "", (int) (getWidth() * 0.8), currentY);
					if (p.getHealth() <= 0) {
						g2d.drawString("DEAD", (int) (getWidth() * 0.45), currentY);
					} else if ((getPlayer().getTeam() == null || getPlayer().getTeam().equals(p.getTeam())) && p.getBomb() != null) {
						g2d.drawString("BOMB", (int) (getWidth() * 0.45), currentY);
					}
					currentY += fm.getHeight();
				}
			}
			int nOfSpectators = 0;
			for (Player p : getPlayer().getGame().getPlayers()) {
				if (p.isSpectatorTeam()) {
					nOfSpectators++;
				}
			}
			currentY += 10;
			g2d.setColor(Team.SPECTATOR_COLOR);
			g2d.drawString(Team.SPECTATOR_NAME + " - " + nOfSpectators + " Players", (int) (getWidth() * 0.2), currentY);
			currentY += 4;
			g2d.drawLine((int) (getWidth() * 0.105), currentY, (int) (getWidth() * 0.895), currentY);
			currentY += fm.getHeight() + 5;
			ArrayList<Player> team = new ArrayList<Player>();
			for (Player p : getGame().getPlayers()) {
				if (p.isSpectatorTeam()) {
					team.add(p);
				}
			}
			Collections.sort(team, new Comparator<Player>() {
				@Override
				public int compare(Player o1, Player o2) {
					if (o1.getKills() == o2.getKills()) {
						return o1.getDeaths() - o2.getDeaths();
					} else {
						return o1.getKills() - o2.getKills();
					}
				}
			});
			for (Player p : team) {
				if (p.equals(getPlayer())) {
					Color previousColor = g2d.getColor();
					g2d.setColor(new Color(100, 100, 0, 70));
					g2d.fillRect((int) Math.ceil(getWidth() * 0.1), currentY - fm.getHeight() + fm.getDescent(), (int) Math.floor(getWidth() * 0.8), fm.getHeight());
					g2d.setColor(previousColor);
				}
				g2d.drawString(p.getName(), (int) (getWidth() * 0.2), currentY);
				currentY += fm.getHeight();
			}
		}
	}
	
	private void attemptToStand() {
		if (getPlayer() != null && !isChatEnabled()) {
			if (!InputConfiguration.DUCK.hasInput(PRESSED_KEYS, PRESSED_BUTTONS) && getPlayer().getCrouchLevel() == Player.CrouchLevel.CROUCH) {
				getPlayer().stand();
			}
		}
	}
	
	private void checkKeys() {
		if (isKeyboardInputEnabled() && isGameRunning() && !isChatEnabled() && !CONSOLE.hasInputFocus()) {
			int movementX = 0;
			int movementY = 0;
			if (InputConfiguration.MOVE_FORWARD.hasInput(PRESSED_KEYS, PRESSED_BUTTONS)) {
				movementY++;
			}
			if (InputConfiguration.MOVE_BACK.hasInput(PRESSED_KEYS, PRESSED_BUTTONS)) {
				movementY--;
			}
			if (InputConfiguration.MOVE_LEFT.hasInput(PRESSED_KEYS, PRESSED_BUTTONS)) {
				movementX--;
			}
			if (InputConfiguration.MOVE_RIGHT.hasInput(PRESSED_KEYS, PRESSED_BUTTONS)) {
				movementX++;
			}
			if (InputConfiguration.FLOAT_UP.hasInput(PRESSED_KEYS, PRESSED_BUTTONS)) {
				if (getPlayer().isSpectator()) {
					getPlayer().moveSpectatorHeight(this.mspf, true);
				}
			} else if (InputConfiguration.FLOAT_DOWN.hasInput(PRESSED_KEYS, PRESSED_BUTTONS)) {
				if (getPlayer().isSpectator()) {
					getPlayer().moveSpectatorHeight(this.mspf, false);
				}
			}
			if (movementX != 0 || movementY != 0) {
				if (getPlayer().move(this.mspf, Compass.getPrincipleAngle(movementX, movementY) - Math.PI / 2, false)) {
					for (ProjectionPlaneEventListener l : PROJECTION_PLANE_EVENT_LISTENERS) {
						if (l != null) {
							l.playerMoved(getPlayer());
						}
					}
				}
			}
			if (InputConfiguration.LOOK_UP.hasInput(PRESSED_KEYS, PRESSED_BUTTONS)) {
				getPlayer().setVerticalDirection(getPlayer().getVerticalDirection() + Math.atan(3 * Math.pow(2, getSensitivity()) * Math.tan(getPlayer().getHorizontalFOV() / 2) / (getWidth() / 2)));
			} else if (InputConfiguration.LOOK_DOWN.hasInput(PRESSED_KEYS, PRESSED_BUTTONS)) {
				getPlayer().setVerticalDirection(getPlayer().getVerticalDirection() - Math.atan(3 * Math.pow(2, getSensitivity()) * Math.tan(getPlayer().getHorizontalFOV() / 2) / (getWidth() / 2)));
			} else if (InputConfiguration.TURN_LEFT.hasInput(PRESSED_KEYS, PRESSED_BUTTONS)) {
				getPlayer().setHorizontalDirection(getPlayer().getHorizontalDirection() + Math.atan(3 * Math.pow(2, getSensitivity()) * Math.tan(getPlayer().getHorizontalFOV() / 2) / (getWidth() / 2)));
			} else if (InputConfiguration.TURN_RIGHT.hasInput(PRESSED_KEYS, PRESSED_BUTTONS)) {
				getPlayer().setHorizontalDirection(getPlayer().getHorizontalDirection() - Math.atan(3 * Math.pow(2, getSensitivity()) * Math.tan(getPlayer().getHorizontalFOV() / 2) / (getWidth() / 2)));
			}
			HoldItem mainHoldItem = getPlayer().getMainHoldItem();
			if (mainHoldItem != null) {
				HashSet<Integer> keys = null;
				synchronized (PRESSED_KEYS) {
					keys = new HashSet<Integer>(PRESSED_KEYS);
				}
				mainHoldItem.checkKeys(keys, getPlayer());
			}
		}
	}
	
	private boolean checkInputEvent(int code, boolean isKeyboard) {
		if (code == KeyEvent.VK_ENTER) {
			synchronized (this) {
				this.chatMessage = "";
			}
		} else if (InputConfiguration.CHAT.hasInput(code, isKeyboard)) {
			synchronized (this) {
				this.chatMessage = "";
				this.chatMessageTarget = null;
			}
		} else if (!getPlayer().isSpectator() && InputConfiguration.JUMP.hasInput(code, isKeyboard)) {
			getPlayer().jump(this.mspf);
		} else if (!getPlayer().isSpectator() && InputConfiguration.DUCK.hasInput(code, isKeyboard)) {
			getPlayer().crouch(this.mspf); 
		} else if (InputConfiguration.BUY_MENU.hasInput(code, isKeyboard) && getMap().isStoreLocation(getLocationX(), getLocationY(), getPlayer().getBottomHeight(), getPlayer())) {
			openStore(false);
		} else if (InputConfiguration.AUTO_BUY.hasInput(code, isKeyboard) && getMap().isStoreLocation(getLocationX(), getLocationY(), getPlayer().getBottomHeight(), getPlayer())) {
			openStore(true);
		} else if (code == KeyEvent.VK_Q) {
			if (getPlayer().getLastHoldItem() != null) {
				getPlayer().setMainHoldItem(getPlayer().getLastHoldItem());
			}
		} else if (InputConfiguration.USE.hasInput(code, isKeyboard)) {
			if (!getPlayer().isDefusingBomb()) {
				getPlayer().startDefusingBomb();
			}
			getMap().activateNearbyDoors(getPlayer());
		} else {
			return false;
		}
		return true;
	}
	
	private void keyPressed(int keyCode) {
		if (isKeyboardInputEnabled()) {
			synchronized (PRESSED_KEYS) {
				PRESSED_KEYS.add(keyCode);
			}
			if (keyCode == KeyEvent.VK_ESCAPE && !CONSOLE.hasInputFocus()) {
				if (CONSOLE.isVisible()) {
					CONSOLE.setVisible(false);
				} else {
					lockCursor(this.freeMouse);
				}
			} else if (keyCode == KeyEvent.VK_X) {
				getGame().getGameMode().createDefaultPlayer(getGame());
			} else if (isChatEnabled()) {
				if (keyCode == KeyEvent.VK_BACK_SPACE) {
					synchronized (this) {
						if (this.chatMessage.length() > 0) {
							this.chatMessage = this.chatMessage.substring(0, this.chatMessage.length() - 1);
						} else if (this.chatMessageTarget != null) {
							this.chatMessageTarget = null;
						} else {
							this.chatMessage = "";
						}
					}
				} else if (keyCode == KeyEvent.VK_CONTROL) {
					synchronized (this) {
						Integer nameLength = null;
						for (Player p : getGame().getPlayers()) {
							if (this.chatMessage.equals(p.getName()) && !p.equals(this.chatMessageTarget)) {
								this.chatMessageTarget = p;
								nameLength = p.getName().length();
								break;
							}
						}
						if (nameLength != null) {
							this.chatMessage = this.chatMessage.substring(nameLength);
						}
					}
				} else if (keyCode == KeyEvent.VK_ENTER) {
					ChatMessage.ChatMessageTarget target = this.chatMessageTarget != null ? this.chatMessageTarget : null;
					if (PRESSED_KEYS.contains(KeyEvent.VK_CONTROL)) {
						target = getPlayer().getTeam();
					}
					getGame().sendChatMessages(new ChatMessage(getPlayer(), target, this.chatMessage));
					closeChat();
				} else if (Character.isLetterOrDigit((char) keyCode) || keyCode == KeyEvent.VK_SPACE) {
					char letter = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK) != PRESSED_KEYS.contains(KeyEvent.VK_SHIFT) ?  Character.toUpperCase((char) keyCode) : Character.toLowerCase((char) keyCode);
					synchronized (this) {
						this.chatMessage += letter;
					}
				}
			} else if (!this.freeMouse && isGameRunning() && !CONSOLE.hasInputFocus() && !isChatEnabled()) {
				if (!checkInputEvent(keyCode, true)) {
					for (HoldItem.HoldItemSlot s : getPlayer().getCurrentHoldItemSlots()) {
						if (s != null && s.getHotKey() != null && keyCode == s.getHotKey()) {
							getPlayer().setItemInSlotAsMainHoldItem(s, getPlayer().getMainHoldItem());
							break;
						}
					}
				}
				
				if (allowGenerateMaze() && keyCode == KeyEvent.VK_H) {
					getGame().getMap().generateMaze(Map.MazeAlgorithms.KRUSKAL, 2, false, (int) Math.floor(getPlayer().getLocationX()), (int) Math.floor(getPlayer().getLocationY()), Color.GREEN, true);
				} else if (keyCode == KeyEvent.VK_M) {
					getGame().requestTeamChange(getPlayer());
				} else if (keyCode == KeyEvent.VK_N) {
					this.showMiniMap = !this.showMiniMap;
				}
				if (getPlayer().getTacticalShield() == null || !getPlayer().getTacticalShield().keyPressed(keyCode, getPlayer())) {
					// Event is not consumed by tactical shield.
					if (getPlayer().getMainHoldItem() != null) {
						getPlayer().getMainHoldItem().keyPressed(keyCode, getPlayer());
					}
				}
			}
		}
	}

	private void keyReleased(int keyCode) {
		List<Integer> found = new ArrayList<Integer>();
		for (Integer key : new HashSet<Integer>(PRESSED_KEYS)) {
		    if (key.equals(keyCode)) {
		        found.add(key);
		    }
		}
		synchronized (PRESSED_KEYS) {
			PRESSED_KEYS.removeAll(found);
		}

		if (isGameRunning() && keyCode == KeyEvent.VK_BACK_QUOTE) {
			if (CONSOLE.isVisible()) {
				CONSOLE.requestInputFocus();
			} else {
				CONSOLE.setVisible(true);
				CONSOLE.requestInputFocus();
			}
		} else if (InputConfiguration.USE.hasInputKey(keyCode) && getPlayer() != null) {
			getPlayer().cancelDefusingBomb();
		}
		if (isGameRunning() && getPlayer() != null) {
			if (getPlayer().getMainHoldItem() != null) {
				getPlayer().getMainHoldItem().keyReleased(keyCode, getPlayer());
			}
		}
	}
	
	private void keyTyped(int keyCode) {
		if (isKeyboardInputEnabled() && isGameRunning() && getPlayer() != null && getPlayer().getMainHoldItem() != null) {
			getPlayer().getMainHoldItem().keyTyped(keyCode, getPlayer());
		}
	}
	
	private void checkButtons() {
		if (isKeyboardInputEnabled() && isGameRunning() && getPlayer() != null && getPlayer().getMainHoldItem() != null) {
			getPlayer().getMainHoldItem().checkButtons(new HashSet<Integer>(this.PRESSED_BUTTONS), getPlayer());
		}
	}
	
	private void mouseMoved(int newX, int newY) {
		if (isGameRunning() && getPlayer() != null) {
			if (this.centeringMouse) {
				this.centeringMouse = false;
			} else {
				int oldX = MouseInfo.getPointerInfo().getLocation().x;
				int oldY = MouseInfo.getPointerInfo().getLocation().y;
				if (!this.freeMouse) {
					recenterMouse();
					double horizontalAngleChange = Math.atan((MouseInfo.getPointerInfo().getLocation().x - oldX) * Math.pow(2, getSensitivity()) * Math.tan(getPlayer().getHorizontalFOV() / 2) / (getWidth() / 2));
					double verticalAngleChange = Math.atan((MouseInfo.getPointerInfo().getLocation().y - oldY) * Math.pow(2, getSensitivity()) * Math.tan(getPlayer().getHorizontalFOV() / 2) / (getWidth() / 2));
					getPlayer().setHorizontalDirection(getPlayer().getHorizontalDirection() + horizontalAngleChange);
					getPlayer().setVerticalDirection(getPlayer().getVerticalDirection() + verticalAngleChange);
				}
			}
		}
	}
	
	public boolean isFullScreen() {
		return GRAPHICS_DEVICE.getFullScreenWindow() != null;
	}
	
	public void setFullScreen() {
		SWITCHING_FULL_SCREEN.set(true);
		getFrame().dispose();
		getFrame().setUndecorated(true);
		getFrame().setResizable(false);
		GRAPHICS_DEVICE.setFullScreenWindow(getFrame());
		getFrame().setVisible(true);
		if (GRAPHICS_DEVICE.isDisplayChangeSupported()) {
			GRAPHICS_DEVICE.setDisplayMode(GRAPHICS_DEVICE.getDisplayModes()[GRAPHICS_DEVICE.getDisplayModes().length - 1]);
		}
	}
	
	public void restoreScreen() {
		SWITCHING_FULL_SCREEN.set(true);
		getFrame().dispose();
		getFrame().setUndecorated(false);
		getFrame().setResizable(true);
		GRAPHICS_DEVICE.setFullScreenWindow(null);
        getFrame().setVisible(true);
	}
	
	public boolean isChatEnabled() {
		return this.chatMessage != null;
	}
	
	private synchronized void closeChat() {
		this.chatMessage = null;
		this.chatMessageTarget = null;
	}
	
	public void freezeCurrentInputState() {
		if (this.previousInputState != null || this.previousLockState != null) {
			throw new IllegalStateException("the input state is already frozen");
		}
		synchronized (this) {
			this.previousInputState = isInputEnabled();
			this.previousLockState = isCursorLocked();
		}
		setInputEnabled(false);
		lockCursor(false);
	}
	
	public void restorePreviousInputState() {
		if (this.previousInputState == null || this.previousLockState == null) {
			throw new IllegalStateException("the input state must be frozen before it can be restored");
		}
		setInputEnabled(this.previousInputState);
		lockCursor(this.previousLockState);
		synchronized (this) {
			this.previousInputState = null;
			this.previousLockState = null;
		}
	}
	
	private boolean isKeyboardInputEnabled() {
		return !this.disableKeyboardInput;
	}
	
	private synchronized void setKeyboardInputEnabled(boolean enabled) {
		this.disableKeyboardInput = !enabled;
	}
	
	private boolean isInputEnabled() {
		return !this.disableInput;
	}
	
	void setInputEnabled(boolean enabled) {
		synchronized (this) {
			this.disableInput = !enabled;
		}
		if (enabled) {
			addMouseListener(MOUSE_LISTENER);
			addMouseListener(PAUSE_MENU);
			addMouseWheelListener(MOUSE_WHEEL_LISTENER);
			addMouseMotionListener(MOUSE_MOTION_LISTENER);
		} else {
			removeAllMouseListeners();
			removeAllMouseWheelListeners();
			removeAllMouseMotionListeners();
		}
		setKeyboardInputEnabled(enabled);
	}
	
	public ModalInternalFrame addInternalFrame(String title, boolean modal, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, boolean disposeOnIconified, boolean setVisible) {
		return addInternalFrame(title, modal, resizable, closable, maximizable, iconifiable, disposeOnIconified, setVisible, null);
	}
	
	public ModalInternalFrame addInternalFrame(String title, boolean modal, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, boolean disposeOnIconified, boolean setVisible, DialogDisposedSimpleAction action) {
		ModalInternalFrame frame = new ModalInternalFrame(ProjectionPlane.previousModalFrame, title, modal, resizable, closable, maximizable, iconifiable, disposeOnIconified, action);
		add(frame);
		frame.setVisible(setVisible);
		return frame;
	}
	
	private void openStore(boolean isAutobuy) {
		openStore(null, isAutobuy);
	}
	
	private void openStore(Store store, boolean isAutobuy) {
		setInputEnabled(false);
		lockCursor(false, true, false);
		DialogDisposedAction<StoreItem[]> ACTION = new DialogDisposedAction<StoreItem[]>() {
			@Override
			public void dialogDisposed(StoreItem[] boughtItems) {
				for (StoreItem i : boughtItems) {
					if (i != null) {
						i.itemBought(getPlayer());
					}
				}
				lockCursor(true, true, true);
				setInputEnabled(true);
			}
		};
		if (store == null) {
			getGame().getMap().openStore(getPlayer(), ACTION, isAutobuy);
		} else {
			store.openStore(getPlayer(), ACTION, isAutobuy);
		}
	}
	
	private void promptNewGame() {
		final ModalInternalFrame FRAME = addInternalFrame("New Game", true, true, true, true, true, true, true, null);
		FRAME.getContentPane().setLayout(new GridLayout(0, 1));
		JLabel label = new JLabel("Game Mode:", SwingConstants.CENTER);
		label.setFont(new Font(Main.DEFAULT_FONT, Font.BOLD, 20));
		
		GameMode[] selectionItems = new GameMode[DefaultGameModes.values().length + 1];
		selectionItems[0] = null;
		for (int i = 0; i < DefaultGameModes.values().length; i++) {
			selectionItems[i + 1] = DefaultGameModes.values()[i];
		}
		final JComboBox<GameMode> COMBO_BOX = new JComboBox<GameMode>(selectionItems);
		COMBO_BOX.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (COMBO_BOX.getSelectedItem() != null) {
					try {
						((GameMode) COMBO_BOX.getSelectedItem()).setUpGameMode(new DialogDisposedAction<Game.Builder>() {
							@Override
							public void dialogDisposed(Game.Builder builder) {
								if (builder != null) {
									FRAME.dispose();
									setGame(builder.build(), true);
									lockCursor(true);
								}
							}
						});
					} catch (ClassCastException ex) {
					}
				}
			}
		});
		
		FRAME.getContentPane().add(label);
		FRAME.getContentPane().add(COMBO_BOX);
		FRAME.setSize(300, 200);
	}
	
	private void checkGame() {
		if (!getGame().isRunning()) {
			lockCursor(false, false, true);
			/*String message = getGame().getGameCompletedMessage();
			if (message == null || JOptionPane.showInternalConfirmDialog(getFrame(), message, "Game Over", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				getGame().resetGame();
				lockCursor(true);
				new Thread(getGame()).start();
			} else {
				lockCursor(false);
			}*/
			lockCursor(false);
		}
	}
	
	private void showFadedPauseMenu(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		Graphics2D g2d = (Graphics2D) g;
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, isGameRunning() ? 0.35f : 0.5f));
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(this.screenShot, 0, 0, getWidth(), getHeight(), null);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		PAUSE_MENU.drawPausedMenu(g);
	}
	
	public boolean isCursorLocked() {
		return !this.freeMouse;
	}
	
	public void lockCursor(boolean lock) {
		lockCursor(lock, true, true);
	}
	
	private void lockCursor(boolean lock, boolean showDefaultBackground, boolean pauseBotMovement) {
		if (!lock || !isRunning()) {
			if (pauseBotMovement && getGame() != null) {
				getGame().pauseBotMovement();
			}
			this.freeMouse = true;
			setCursor(Cursor.getDefaultCursor());
			this.screenShot = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
			if (isGameRunning() || !showDefaultBackground) {
				draw(this.screenShot.getGraphics(), null);
			} else {
				this.screenShot = Main.BACKGROUND_WALL_PAPER;
			}
			closeChat();
		} else if (isGameRunning()) {
			getGame().resumeBotMovement();
			this.freeMouse = false;
			setCursor(Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank cursor"));
			recenterMouse();
		} else {
			lockCursor(false, showDefaultBackground, pauseBotMovement);
		}
		repaint();
	}
	
	private void removeAllMouseListeners() {
		for (MouseListener i : getMouseListeners()) {
			removeMouseListener(i);
		}
	}
	
	private void removeAllMouseWheelListeners() {
		for (MouseWheelListener i : getMouseWheelListeners()) {
			removeMouseWheelListener(i);
		}
	}
	
	private void removeAllMouseMotionListeners() {
		for (MouseMotionListener i : getMouseMotionListeners()) {
			removeMouseMotionListener(i);
		}
	}

	private void recenterMouse() {
		Robot robot = null;
		try {
			robot = new Robot();
		} catch (Exception e) {
		}
		if (robot != null && isShowing()) {
			Point mouseCenter = new Point(getWidth() / 2, getHeight() / 2);
			SwingUtilities.convertPointToScreen(mouseCenter, this);
			robot.mouseMove(mouseCenter.x, mouseCenter.y);
			synchronized (this) {
				this.centeringMouse = true;
			}
		}
	}
	
	private final JFrame getFrame() {
		return FRAME;
	}
	
	public Game getGame() {
		return this.game;
	}
	
	public boolean isGameRunning() {
		return isRunning() && getGame() != null && getGame().isRunning();
	}
	
	private void setGame(Game game, boolean startGame) {
		if (game == null) throw new IllegalArgumentException("game cannot be null");
		if (getGame() != null) {
			getGame().stop();
		}
		synchronized (this) {
			this.game = game;
			this.chatMessage = null;
		}
		setSightRange(getGame().getMap().getDefaultSightRange());
		setAllowGenerateMaze(getGame().allowsGenerateMaze());
		setAllowHorizontalDirection(getGame().allowsHorizontalDirection());
		if (!getGame().hasPlayer(getPlayer())) {
			/*Player player = new Player.Builder(getPlaneName(), getGame(), 2, 0.7).height(1).stepHeight(0.2).horizontalDirectionD(Math.random() * Math.PI * 2).build();
			getGame().assignTeam(player, getFrame());*/
			Player player = getGame().getGameMode().createDefaultPlayer(getGame());
			setPlayer(player);
		}
		SIGHT_RANGE_ADJUSTER.activate((double) SettingsMenu.MIN_SIGHT_RANGE, Math.ceil(Map.findDistance(getMap().getWidth(), getMap().getHeight())), Integer.MAX_VALUE - 1, Math.min(10d, Math.ceil(Map.findDistance(getMap().getWidth(), getMap().getHeight()))), Main.TARGET_FPS);
		if (startGame) {
			getGame().start();
		}
	}

	/**
	 * Gets the player who is viewing through the projection plane
	 * @return the player who is viewing through the projection plane
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * Sets the player who will view through the projection plane
	 * @param player the player who will view through the projection plane
	 */
	public void setPlayer(Player player) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		synchronized (this) {
			this.player = player;
			this.chatMessage = null;
		}
		setSightRange(getPlayer().getMap().getDefaultSightRange());
		getPlayer().setName(getPlaneName());
		updateDistanceToPlane();
	}
	
	/**
	 * Gets the map of the world that the player viewing through the projection plane is in
	 * @return the map of the world of the player viewing through the projection plane
	 */
	public Map getMap() {
		return getGame().getMap();
	}

	/**
	 * Gets the horizontal direction in radians. The direction is the principle angle with east at 0.
	 * @return the horizontal direction in radians.
	 */
	public double getHorizontalDirection() {
		return getPlayer().getHorizontalDirection();
	}
	
	/**
	 * Gets the distance between the observer and the plane in pixels. Calculated using the horizontal field of view.
	 * @return the distance between the observer and the plane in pixels
	 * @see Player#getHorizontalFOV()
	 */
	public double getDistanceToPlane() {
		return this.distanceToPlane;
	}
	
	private synchronized void updateDistanceToPlane() {
		this.distanceToPlane = getWidth() / 2d / Math.tan(getPlayer().getHorizontalFOV() / 2d);
	}

	/**
	 * Gets the x-coordinate of the location of the observer in blocks.
	 * @return the x-coordinate of the location in blocks
	 */
	public double getLocationX() {
		return getPlayer().getLocationX() - (isThirdPerson() ? Math.min(Math.max(Math.cos(getHorizontalDirection()) * getThirdPersonDistance(), getPlayer().getLocationX() - getMap().getWidth() + 0.0001), getPlayer().getLocationX() - 0.0001) : 0);
	}

	/**
	 * Gets the y-coordinate of the location of the observer in blocks.
	 * @return the y-coordinate of the location in blocks
	 */
	public double getLocationY() {
		return getPlayer().getLocationY() + (isThirdPerson() ? Math.min(Math.max(Math.sin(getHorizontalDirection()) * getThirdPersonDistance(), 0.0001 - getPlayer().getLocationY()), getMap().getHeight() - getPlayer().getLocationY() - 0.0001) : 0);
	}

	/**
	 * Gets the z-coordinate of the location of the observer in blocks.
	 * @return the z-coordinate of the location in blocks
	 */
	public double getLocationZ() {
		return getPlayer().getViewHeight() - (isThirdPerson() ? Math.min(Math.sin(getPlayer().getVerticalDirection()) * getThirdPersonDistance(), getPlayer().getViewHeight()) : 0);
	}
	
	public int getFrameDelay() {
		return this.frameDelay;
	}
	
	private synchronized void setFrameDelay(int frameDelay) {
		this.frameDelay = frameDelay;
	}
	
	public int getResolution() {
		return this.resolution;
	}
	
	public synchronized void setResolution(int resolution) {
		this.resolution = Math.max(resolution, SettingsMenu.MIN_RESOLUTION);
	}
	
	public double getSightRange() {
		return this.sightRange;
	}
	
	public synchronized void setSightRange(double sightRange) {
		this.sightRange = Math.abs(sightRange);
	}
	
	public int getSensitivity() {
		return this.sensitivity;
	}
	
	public synchronized void setSensitivity(int sensitivity) {
		this.sensitivity = sensitivity;
	}
	
	public void addProjectionPlaneStoppedListeners(ProjectionPlaneEventListener... listeners) {
		if (listeners != null) {
			for (ProjectionPlaneEventListener l : listeners) {
				if (l != null) {
					synchronized (PROJECTION_PLANE_EVENT_LISTENERS) {
						PROJECTION_PLANE_EVENT_LISTENERS.add(l);
					}
				}
			}
		}
	}
	
	public void removeProjectionPlaneStoppedListeners(ProjectionPlaneEventListener... listeners) {
		if (listeners != null) {
			for (ProjectionPlaneEventListener l : listeners) {
				if (l != null) {
					synchronized (PROJECTION_PLANE_EVENT_LISTENERS) {
						PROJECTION_PLANE_EVENT_LISTENERS.remove(l);
					}
				}
			}
		}
	}
	
	public boolean isThirdPerson() {
		return this.thirdPersonDistance != null;
	}
	
	public double getThirdPersonDistance() {
		return this.thirdPersonDistance;
	}
	
	public synchronized void setThirdPerson(Double thirdPersonDistance) {
		this.thirdPersonDistance = thirdPersonDistance != null ? Math.max(Math.min(thirdPersonDistance, 10), 0.1) : null;
	}
	
	public String getPlaneName() {
		return this.name;
	}
	
	private void setPlaneName(String name) {
		synchronized (this) {
			this.name = name;
		}
		if (getPlayer() != null) {
			getPlayer().setName(getPlaneName());
		}
	}

	public int getNumberOfRecentEvents() {
		return this.numberOfRecentEvents;
	}
	
	
	public boolean isShowFPS() {
		return this.showFPS;
	}
	
	public synchronized void setShowFPS(boolean showFPS) {
		this.showFPS = showFPS;
	}
	
	public boolean isShowTime() {
		return this.showTime;
	}
	
	public void setShowTime(boolean showTime) {
		this.showTime = showTime;
	}
	
	public boolean isShowShading() {
		return this.showShading;
	}
	
	public void setShowShading(boolean showShading) {
		this.showShading = showShading;
	}
	
	public boolean allowGenerateMaze() {
		return this.allowGenerateMaze;
	}
	
	public void setAllowGenerateMaze(boolean allow) {
		this.allowGenerateMaze = allow;
	}
	
	public boolean allowHorizontalDirection() {
		return this.allowHorizontalDirection;
	}
	
	public void setAllowHorizontalDirection(boolean allow) {
		this.allowHorizontalDirection = allow;
	}
	
	private CastRayProjectionItemInfo getWallInfo(Wall wall, Map.Ray2D ray) {
		Point2D intersectionPoint = wall.getIntersectionPoint(ray);
		if (intersectionPoint != null) {
			return new CastRayProjectionItemInfo(ray, wall.getTopSurfaceDistance(ray), getHorizontalDirection(), wall, intersectionPoint, false);
		}
		return null;
	}
	
	private Double addWall(double x, double y, double distance, Map.Ray2D ray, double minimumHeightDistanceRatio, ArrayList<CastRayProjectionItemInfo> walls, Set<Wall> wallsSet, double locationZ) {
		if (getMap().inGrid(x, y)) {
			Wall wall = getMap().getWall(x, y);
			if (!wallsSet.contains(wall) && wall.getTopHeight(Integer.MAX_VALUE) > 0) {
				double minimumHeightDistanceRatio2 = (wall.getTopHeight(Integer.MAX_VALUE) - locationZ) / distance;
				if (minimumHeightDistanceRatio2 > minimumHeightDistanceRatio) {
					if (wall.getNumberOfGaps() <= 1 && !wall.isTransparent(ray)) {
						minimumHeightDistanceRatio = minimumHeightDistanceRatio2;
					}
					CastRayProjectionItemInfo info = getWallInfo(wall, ray);
					if (info != null) {
						wallsSet.add(wall);
						walls.add(info);
					}
				}
			}
			return minimumHeightDistanceRatio;
		}
		return null;
	}
	
	private ArrayList<CastRayProjectionItemInfo> getWallsAndFences(Map.Ray2D ray, double locationZ) {
		Set<Wall> wallsSet = new HashSet<Wall>();
		ArrayList<CastRayProjectionItemInfo> wallsAndFences = new ArrayList<CastRayProjectionItemInfo>();
		Wall currentWall = getMap().getWall(ray.getLocationX(), ray.getLocationY());
		if (getMap().inGrid(ray.getLocationX(), ray.getLocationY()) && currentWall.getTopHeight(Integer.MAX_VALUE) > 0) {
			CastRayProjectionItemInfo info = getWallInfo(currentWall, ray);
			if (info != null) {
				wallsSet.add(currentWall);
				wallsAndFences.add(info);
			}
		}
		// Horizontal Intersections
		int upOrDown = ray.getDirection() < Math.PI ? 1 : -1;
		double initialPosition = ray.getLocationY();
		double aY = ray.getDirection() < Math.PI ? Math.floor(initialPosition) : Math.ceil(initialPosition);
		double aX = ray.getLocationX() + (initialPosition - aY) / Math.tan(ray.getDirection());
		double change = 1 / Math.tan(ray.getDirection());
		double minimumHeightDistanceRatio = -Double.MAX_VALUE;
		double initialDistance = Map.findDistance(getPlayer(), aX, aY);
		double distanceChange = Math.sqrt(change * change + 1);
		for (int i = 0; i < getMap().getHeight(); i++) {
			Double value = addWall(aX + i * upOrDown * change, initialPosition - i * upOrDown, initialDistance + i * distanceChange, ray, minimumHeightDistanceRatio, wallsAndFences, wallsSet, locationZ);
			if (value != null) {
				minimumHeightDistanceRatio = value;
			} else {
				break;
			}
		}
		// Vertical Intersections
		upOrDown = ray.getDirection() < Math.PI / 2 || ray.getDirection() > Math.PI * 3 / 2 ? 1 : -1;
		initialPosition = ray.getLocationX();
		aX = ray.getDirection() < Math.PI / 2 || ray.getDirection() > Math.PI * 3 / 2 ? Math.ceil(initialPosition) : Math.floor(initialPosition);
		aY = ray.getLocationY() + (initialPosition - aX) * Math.tan(ray.getDirection());
		change = Math.tan(ray.getDirection());
		minimumHeightDistanceRatio = -Double.MAX_VALUE;
		initialDistance = Map.findDistance(getPlayer(), aX, aY);
		distanceChange = Math.sqrt(change * change + 1);
		for (int i = 0; i < getMap().getWidth(); i++) {
			Double value = addWall(initialPosition + i * upOrDown, aY - i * upOrDown * change, initialDistance + i * distanceChange, ray, minimumHeightDistanceRatio, wallsAndFences, wallsSet, locationZ);
			if (value != null) {
				minimumHeightDistanceRatio = value;
			} else {
				break;
			}
		}
		for (Fence f : getMap().getFences()) {
			Point2D intersectionPoint = f.getIntersectionPoint(ray);
			if (intersectionPoint != null) {
				wallsAndFences.add(new CastRayProjectionItemInfo(ray, 0, getHorizontalDirection(), f, intersectionPoint, false));
			}
		}
		Collections.sort(wallsAndFences);
		return wallsAndFences;
	}
	
	private ArrayList<CastRayProjectionItemInfo> getFilteredRayWallAndFencesInfo(Map.Ray2D ray, double locationZ) {
		ArrayList<CastRayProjectionItemInfo> wallHit = new ArrayList<CastRayProjectionItemInfo>();
		double heightDistanceRatio = -Double.MAX_VALUE;
		for (CastRayProjectionItemInfo i : getWallsAndFences(ray, locationZ)) {
			double currentWallHeightDistanceRatio = (i.getTopHeight(Integer.MAX_VALUE) - locationZ) / i.getDistance();
			int nOfGaps = i.getProjectedItem().getNumberOfGaps();
			boolean isTransparent = i.getProjectedItem().getOpacityAlpha(ray, getPlayer().getVerticalDirection()) < 255;
			double bottomHeight = i.getBottomHeight(0);
			if (currentWallHeightDistanceRatio > heightDistanceRatio + 0.0001) {
				if (nOfGaps <= 1 && !isTransparent && (bottomHeight == 0 || (bottomHeight - locationZ) / i.getDistance() < heightDistanceRatio)) {
					heightDistanceRatio = currentWallHeightDistanceRatio;
				} else if (nOfGaps > 1 && !isTransparent) {
					currentWallHeightDistanceRatio = (i.getTopHeight(0) - locationZ) / i.getDistance();
					if (currentWallHeightDistanceRatio > heightDistanceRatio) {
						heightDistanceRatio = currentWallHeightDistanceRatio;
					}
				}
				wallHit.add(i);
			}
			if (i.getTopSurfaceDistance() > 0 && (nOfGaps > 1 || isTransparent || i.getTopHeight(Integer.MAX_VALUE) < locationZ)) {
				double currentTopSurfaceHeightDistanceRatio = (i.getTopHeight(Integer.MAX_VALUE) - locationZ) / (i.getDistance() + i.getTopSurfaceDistance());
				if (currentTopSurfaceHeightDistanceRatio > heightDistanceRatio + 0.0001) {
					if (nOfGaps <= 1 && !isTransparent) {
						heightDistanceRatio = currentTopSurfaceHeightDistanceRatio;
					} else if (nOfGaps > 1 && !isTransparent) {
						currentTopSurfaceHeightDistanceRatio = (i.getTopHeight(0) - locationZ) / (i.getDistance() + i.getTopSurfaceDistance());
						if (currentTopSurfaceHeightDistanceRatio > heightDistanceRatio) {
							heightDistanceRatio = currentTopSurfaceHeightDistanceRatio;
						}
					}
					wallHit.add(new CastRayProjectionItemInfo(i, true));
				}
			}
		}
		return wallHit;
	}
	
	private ArrayList<CastRayProjectionItemInfo> getRayMapItem3DInfo(Map.Ray2D ray, List<? extends MapItem3D> items) {
		if (items != null && items.size() > 0) {
			ArrayList<CastRayProjectionItemInfo> itemHit = new ArrayList<CastRayProjectionItemInfo>();
			for (MapItem3D i : items) {
				Point2D intersectionPoint = i.getIntersectionPoint(ray);
				if (intersectionPoint != null) {
					CastRayProjectionItemInfo info = new CastRayProjectionItemInfo(ray, i.getTopSurfaceDistance(ray), getHorizontalDirection(), i, intersectionPoint, false);
					itemHit.add(info);
					itemHit.add(new CastRayProjectionItemInfo(info, true));
				}
			}
			Collections.sort(itemHit, Collections.reverseOrder());
			return itemHit;
		}
		return null;
	}
	
	/**
	 * Calculates the height (in pixels) of the middle of the projection screen based on the vertical direction of the player and the distance to the projection plane.
	 * @return the height (in pixels) of the middle of the projection screen
	 * @see Player#getVerticalDirection()
	 * @see #getDistanceToPlane()
	 */
	public int getProjectionScreenMiddle() {
		return (int) (getHeight() / 2d + Math.tan(getPlayer().getVerticalDirection()) * getDistanceToPlane());
	}
	
	/**
	 * Gets the height (in pixels) on the projection screen of a point at a certain height and certain distance
	 * @param height the absolute height (in meters) of the point
	 * @param locationZ the current view height (in meters) of the observer
	 * @param distance the distance (in meters) of the point
	 * @param projectionPlaneMiddle the height (in pixels) of the middle of the projection screen (option argument that can be passed to reduce computational time if this method is called often)
	 * @return the height (in pixels) on the projection screen
	 * @see #getProjectionScreenMiddle()
	 */
	private int getPixelHeight(double height, double locationZ, double distance, int projectionPlaneMiddle) {
		/*if (getHeight() / 2 + getDistanceToPlane() * getDistanceToPlane() / getHeight() * Math.tan(getPlayer().getVerticalDirection() - Math.atan((height - locationZ) / distance)) > 100000)
			System.out.println(Math.atan((height - locationZ) / distance));
		double verticalFOV = Math.tan(getPlayer().getHorizontalFOV() / 2) * getHeight() / getWidth();
		double dAngle = getPlayer().getVerticalDirection() - Math.atan((height - locationZ) / distance);
		//return (int) (getHeight() / 2 + getDistanceToPlane() * getDistanceToPlane() / getHeight() * Math.tan(dAngle));
		//return (int) (getHeight() / 2 + getDistanceToPlane() * (Math.tan(dAngle) * Math.sin(verticalFOV / 2)) / getHeight());*/
		return (int) (projectionPlaneMiddle - (height - locationZ) * getDistanceToPlane() / distance);
	}

	/**
	 * Gets the audio volume in decibels relative to the original volume of the sound file.
	 * @return the audio volume of the player in decibels relative to the original volume of the sound file
	 */
	public float getRelativeVolume() {
		return this.relativeVolume;
	}
	
	/**
	 * Gets the audio volume in decibels relative to the original volume of the sound file or {@code null} if the audio is mute.
	 * @return the audio volume of the player in decibels relative to the original volume of the sound file or {@code null} if the audio is mute
	 */
	public Float getDesiredVolume() {
		return isMute() ? null : getRelativeVolume();
	}
	
	/**
	 * Sets the audio volume of the player in decibels relative to the original volume of the sound file..
	 * @param relativeVolume the audio volume of the player in decibels relative to the original volume of the sound file.
	 */
	public void setRelativeVolume(float relativeVolume) {
		this.relativeVolume = relativeVolume;
	}
	
	/**
	 * Checks if the audio playback is mute.
	 * @return true if the audio playback is mute; otherwise, false
	 */
	public boolean isMute() {
		return this.isMute;
	}
	
	/**
	 * Sets the audio volume.
	 * @param isMute true to mute audio, false to enable audio
	 */
	public void setMute(boolean isMute) {
		this.isMute = isMute;
	}
	
	private double getIntensity(double correctedDistance) {
		return (isShowShading() ? Math.min(Math.max((int) Math.floor(Math.pow(correctedDistance / getSightRange(), 2) * 255), 0), 255) : 0) / 256d;
	}
	
	static Color getNewColorWithIntensity(Color originalColor, double intensity) {
		intensity = 1 - intensity;
		return new Color((int) (originalColor.getRed() * intensity), (int) (originalColor.getGreen() * intensity), (int) (originalColor.getBlue() * intensity), originalColor.getAlpha());
	}
	
	private CastRayProjectionItemInfo castRay(Graphics g, int x, CastRayProjectionItemInfo[] items3DInfo, int projectionPlaneMiddle, double locationX, double locationY, double locationZ, double validDistanceMin, double validDistanceMax, int checkDrawOverX, int checkDrawOverY) {
		if (items3DInfo == null || items3DInfo.length <= 0) return null;
		CastRayProjectionItemInfo closestItemInfo = null;
		final Map.Ray2D RAY = new Map.Ray2D(locationX, locationY, items3DInfo[0].getDirection());
		final Ladder[] LADDERS = getMap().getLadders();
		for (CastRayProjectionItemInfo c : items3DInfo) {
			for (int i = 0; i < c.getProjectedItem().getNumberOfGaps(); i++) {
				if (c.getDistance() < validDistanceMin) {
					return closestItemInfo;
				} else if (c.getDistance() < validDistanceMax) {
					int bottomY = 0;
					int topY = 0;
					if (!c.isTopSurface()) {
						topY = getPixelHeight(c.getTopHeight(i), locationZ, c.getCorrectedDistance(), projectionPlaneMiddle);
						bottomY = getPixelHeight(c.getDrawBottomHeight(i), locationZ, c.getCorrectedDistance(), projectionPlaneMiddle);
						if (x == checkDrawOverX && topY <= checkDrawOverY && checkDrawOverY <= bottomY) {
							closestItemInfo = c;
						}
						BufferedImage bitmap = c.getProjectedItem().getBitmap(c.POINT_OF_INTERSECTION);
						if (bitmap != null) {
							g.drawImage(bitmap, x, topY, getResolution(), bottomY - topY, null);
						} else {
							Color wallColor = c.getProjectedItem().getProjectedColor(RAY);
							if (wallColor == null) {
								wallColor = getMap().getWallColor();
							}
							wallColor = getNewColorWithIntensity(wallColor, getIntensity(c.getCorrectedDistance()));
							if (isShowShading() && topY < bottomY) {
								double horizontalDistanceSquared = Math.pow(c.getCorrectedDistance(), 2);
								double distance3DTop = Math.sqrt(horizontalDistanceSquared + Math.pow(c.getTopHeight(i) - locationZ, 2));
								double distance3DBottom = Math.sqrt(horizontalDistanceSquared + Math.pow(c.getBottomHeight(i) - locationZ, 2));
								//double colorIntensityTop = 1 - Math.min(Math.max((int) Math.floor(Math.pow(distance3DTop / getSightRange(), 2) * 255), 0), 255) / 256d;
								//double colorIntensityBottom = 1 - Math.min(Math.max((int) Math.floor(Math.pow(distance3DBottom / getSightRange(), 2) * 255), 0), 255) / 256d;
								//Color topColor = new Color((int) (wallColor.getRed() * colorIntensityTop), (int) (wallColor.getGreen() * colorIntensityTop), (int) (wallColor.getBlue() * colorIntensityTop));
								//Color bottomColor = new Color((int) (wallColor.getRed() * colorIntensityBottom), (int) (wallColor.getGreen() * colorIntensityBottom), (int) (wallColor.getBlue() * colorIntensityBottom));
								Color topColor = getNewColorWithIntensity(wallColor, getIntensity(distance3DTop));
								Color bottomColor = getNewColorWithIntensity(wallColor, getIntensity(distance3DBottom));
								((Graphics2D) g).setPaint(new LinearGradientPaint(0, bottomY, 0, topY, new float[]{0, 1}, new Color[]{bottomColor, topColor}));
							} else {
								((Graphics2D) g).setPaint(null);
								g.setColor(wallColor);
							}
							g.fillRect(x, topY, getResolution(), bottomY - topY);
							g.setColor(Color.BLACK);
							if (c.getTopHeight(i) > 0) {
								g.drawLine(x, topY, x + getResolution() - 1, topY);
								if (c.getBottomHeight(i) == 0) {
									g.drawLine(x, bottomY, x + getResolution() - 1, bottomY);
								}
							}
						}
					} else if ((c.getTopHeight(i) > 0 && c.getTopHeight(i) < locationZ) || c.getBottomHeight(i) > locationZ) {
						double correctedTopSurfaceDistance = c.getCorrectedDistance() + c.getTopSurfaceDistance() * Math.cos(RAY.getDirection() - getHorizontalDirection());
						double distance = Math.min(correctedTopSurfaceDistance, getSightRange() * Math.cos(RAY.getDirection() - getHorizontalDirection()));
						if (c.getTopHeight(i) < locationZ) {
							bottomY = getPixelHeight(c.getTopHeight(i), locationZ, c.getCorrectedDistance(), projectionPlaneMiddle);
							topY = getPixelHeight(c.getTopHeight(i), locationZ, distance, projectionPlaneMiddle);
						} else {
							bottomY = getPixelHeight(c.getBottomHeight(i), locationZ, distance, projectionPlaneMiddle);
							topY = getPixelHeight(c.getBottomHeight(i), locationZ, c.getCorrectedDistance(), projectionPlaneMiddle);
						}
						Color topColor = c.getProjectedItem().getTopColor(RAY);
						if (topColor == null) {
							topColor = getMap().getFloorColor();
							topColor = new Color(topColor.getRed(), topColor.getGreen(), topColor.getBlue(), c.getProjectedItem().getOpacityAlpha(RAY, getPlayer().getVerticalDirection()));
						}
						if (isShowShading() && topY < bottomY) {
							double verticalDistanceSquared = Math.pow(locationZ - c.getTopHeight(i), 2);
							double distance3D = Math.sqrt(Math.pow(c.getCorrectedDistance(), 2) + verticalDistanceSquared);
							double farDistance3D = Math.sqrt(Math.pow(correctedTopSurfaceDistance, 2) + verticalDistanceSquared);
							Color closeColor = getNewColorWithIntensity(topColor, getIntensity(distance3D));
							Color farColor = getNewColorWithIntensity(topColor, getIntensity(farDistance3D));
							((Graphics2D) g).setPaint(new LinearGradientPaint(0, bottomY, 0, topY, new float[]{0, 1}, new Color[]{closeColor, farColor}));
						} else {
							((Graphics2D) g).setPaint(null);
							g.setColor(topColor);
						}
						g.fillRect(x, topY, getResolution(), bottomY - topY);
					}
				}
			}
			if (c.getDistance() >= validDistanceMin && c.getDistance() < validDistanceMax && !c.isTopSurface()) {
				for (Ladder l : LADDERS) {
					double distance = Map.findDistance(l, c.POINT_OF_INTERSECTION.getX(), c.POINT_OF_INTERSECTION.getY());
					if (distance >= l.getInnerRadius() && distance <= l.getRadius()) {
						int topY2 = getPixelHeight(l.getTopHeight(), locationZ, c.getCorrectedDistance(), projectionPlaneMiddle);
						int bottomY2 = getPixelHeight(l.getBottomHeight(), locationZ, c.getCorrectedDistance(), projectionPlaneMiddle);
						g.setColor(l.getColor());
						g.fillRect(x, topY2, getResolution(), bottomY2 - topY2);
					} else if (distance <= l.getRadius()) {
						for (int i = 1; i <= l.getRungs(); i++) {
							int topY2 = getPixelHeight((l.getTopHeight() - l.getBottomHeight()) * (i * 1d / (l.getRungs() + 1)) + l.getBottomHeight() + l.getRungHalfHeight(), locationZ, c.getCorrectedDistance(), projectionPlaneMiddle);
							int bottomY2 = getPixelHeight((l.getTopHeight() - l.getBottomHeight()) * (i * 1d / (l.getRungs() + 1)) + l.getBottomHeight() - l.getRungHalfHeight(), locationZ, c.getCorrectedDistance(), projectionPlaneMiddle);
							g.setColor(l.getColor());
							g.fillRect(x, topY2, getResolution(), bottomY2 - topY2);
						}
					}
				}
				Bullet.BulletMarking[] bulletMarkings = c.getBulletMarkings(RAY);
				if (bulletMarkings != null) {
					g.setColor(Bullet.BulletMarking.BULLET_MARKINGS_COLOR);
					for (Bullet.BulletMarking m : bulletMarkings) {
						int verticalLocationOnWall1 = getPixelHeight(Math.min(m.getLocationZ() + m.getMarkingRadius() * 2, c.getTopHeight(Integer.MAX_VALUE)), locationZ, c.getCorrectedDistance(), projectionPlaneMiddle);
						int verticalLocationOnWall2 = getPixelHeight(m.getLocationZ() - m.getMarkingRadius() * 2, locationZ, c.getCorrectedDistance(), projectionPlaneMiddle);
						g.fillRect(x, verticalLocationOnWall1, getResolution(), verticalLocationOnWall2 - verticalLocationOnWall1);
					}
				}
			}
		}
		return closestItemInfo;
	}
	
	private void drawTacticalShield(Graphics g, CastRayCylinderMapItemInfo item, int projectionPlaneMiddle, double locationX, double locationY, double locationZ, boolean drawInFront) {
		if (item.getMapItem() instanceof Player) {
			Player p = (Player) item.getMapItem();
			double playerCorrectedDistance = Map.findDistance(p, locationX, locationY);
			Line2D position = p.getCurrentTacticalShieldBaseLocation();
			TacticalShield tacticalShield = p.getTacticalShield();
			if (position != null && tacticalShield != null) {
				for (int x = 0; x <= getWidth(); x++) {
					Map.Ray2D ray = new Map.Ray2D(locationX, locationY, (Math.atan((getWidth() / 2d - x) / getDistanceToPlane()) + getHorizontalDirection()));
					Point2D intersection = ray.getIntersectionPoint(position);
					if (intersection != null) {
						double distance = Map.findDistance(ray, intersection.getX(), intersection.getY());
						double correctedDistance = distance * Math.cos(ray.getDirection() - getHorizontalDirection());
						if ((correctedDistance <= playerCorrectedDistance) == drawInFront) {
							double heightBottom = tacticalShield.getShieldBottomHeight(p);
							int bottom = getPixelHeight(heightBottom, locationZ, correctedDistance, projectionPlaneMiddle);
							int top = getPixelHeight(heightBottom + tacticalShield.getShieldHeight(), locationZ, correctedDistance, projectionPlaneMiddle);
							double shieldPosition = ray.getIntersectionPointRelativeToLine(position) * tacticalShield.getShieldWidth();
							boolean hasSlit = shieldPosition >= (tacticalShield.getShieldWidth() - tacticalShield.getSlitWidth()) / 2 && shieldPosition <= (tacticalShield.getShieldWidth() + tacticalShield.getSlitWidth()) / 2;
							if (hasSlit) {
								int slitBottom = (int) Math.round((bottom - top) * tacticalShield.getSlitBottomHeight()) + top;
								int slitTop = (int) Math.round((bottom - top) * tacticalShield.getSlitTopHeight()) + top;
								g.setColor(tacticalShield.getSlitColor());
								g.fillRect(x, slitTop, getResolution(), slitBottom - slitTop);
								g.setColor(tacticalShield.getColor());
								g.fillRect(x, slitBottom, getResolution(), bottom - slitBottom);
								g.fillRect(x, top, getResolution(), slitTop - top);
							} else {
								g.setColor(tacticalShield.getColor());
								g.fillRect(x, top, getResolution(), bottom - top);
							}
						}
					}
				}
			}
		}
	}

	public String renderMapItems(Graphics g) {
		int projectionPlaneMiddle = getProjectionScreenMiddle();
		
		getMap().updateDoors();
		
		final double LOCATION_X = getLocationX();
		final double LOCATION_Y = getLocationY();
		final double LOCATION_Z = getLocationZ();
		CylinderMapItem[] items2D = getGame().getCylinderMapItemsInRange(LOCATION_X, LOCATION_Y, getSightRange(), getPlayer().getPhysicalHalfWidth(), isThirdPerson() ? null : getPlayer(), false);
		ArrayList<CastRayCylinderMapItemInfo> items2DInfo = new ArrayList<CastRayCylinderMapItemInfo>();
		for (CylinderMapItem i : items2D) {
			CastRayCylinderMapItemInfo newInfo = new CastRayCylinderMapItemInfo(LOCATION_X, LOCATION_Y, LOCATION_Z, getHorizontalDirection(), getWidth(), getDistanceToPlane(), projectionPlaneMiddle, i);
			if (Player.isHorizontalAngleInView(getPlayer().getHorizontalDirection(), getPlayer().getHorizontalFOV() + newInfo.getSideAngle() * 2, Compass.getPrincipleAngle(i.getLocationX() - LOCATION_X, LOCATION_Y - i.getLocationY()))) {
				items2DInfo.add(newInfo);
			}
		}
		Collections.sort(items2DInfo, Collections.reverseOrder());

		final LinkedList<? extends MapItem3D> ITEMS_3D = getMap().get3DItemsInRange(LOCATION_X, LOCATION_Y, getSightRange());
		BufferedImage image = getMap().getBackgroundImage();
		if (!this.seeThrough && image != null) {
			g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		} else {
			g.setColor(getMap().getBackgroundColor());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		// Draw floors
		if (!this.seeThrough) {
			if (isShowShading()) {
				((Graphics2D) g).setPaint(new LinearGradientPaint(0, getHeight() / 2 + projectionPlaneMiddle, 0, (int) (LOCATION_Z * getDistanceToPlane() / getSightRange()) + projectionPlaneMiddle - getHeight() / 2, new float[]{0, 1}, new Color[]{getMap().getFloorColor(), Color.BLACK}));
			} else {
				((Graphics2D) g).setPaint(null);
				g.setColor(getMap().getFloorColor());
			}
			for (int x = 0; x < getWidth(); x += getResolution()) {
				double direction = Math.atan((getWidth() / 2d - x) / getDistanceToPlane());
				double distanceToEdge = getMap().getDistanceToEdge(new Map.Ray2D(LOCATION_X, LOCATION_Y, direction + getHorizontalDirection()));
				double distance = Math.min(distanceToEdge, getSightRange());
				int floorBottomY = getPixelHeight(0, LOCATION_Z, distance * Math.cos(direction), projectionPlaneMiddle);
				g.fillRect(x, floorBottomY - 1, getResolution(), getHeight() - floorBottomY);
			}
			Color edgeWallColor = getMap().getEdgeWallColor();
			if (edgeWallColor != null) {
				for (int x = 0; x < getWidth(); x += getResolution()) {
					double direction = Math.atan((getWidth() / 2d - x) / getDistanceToPlane());
					Map.Ray2D ray = new Map.Ray2D(LOCATION_X, LOCATION_Y, direction + getHorizontalDirection());
					double distance = getMap().getDistanceToEdge(ray);
					if (distance <= getSightRange()) {
						int floorBottomY = getPixelHeight(0, LOCATION_Z, distance * Math.cos(direction), projectionPlaneMiddle);
						int topY = getPixelHeight(getMap().getEdgeWallHeight(), LOCATION_Z, distance * Math.cos(direction), projectionPlaneMiddle);
						g.setColor(getNewColorWithIntensity(edgeWallColor, getIntensity(distance)));
						g.fillRect(x, topY, getResolution(), floorBottomY - topY);
						g.setColor(Bullet.BulletMarking.BULLET_MARKINGS_COLOR);
						for (Bullet.BulletMarking m : getMap().getBulletMarkings(ray, distance)) {
							int verticalLocationOnWall1 = getPixelHeight(Math.min(m.getLocationZ() + m.getMarkingRadius() * 2, getMap().getEdgeWallHeight()), LOCATION_Z, distance * Math.cos(direction), projectionPlaneMiddle);
							int verticalLocationOnWall2 = getPixelHeight(m.getLocationZ() - m.getMarkingRadius() * 2, LOCATION_Z, distance * Math.cos(direction), projectionPlaneMiddle);
							g.fillRect(x, verticalLocationOnWall1, getResolution(), verticalLocationOnWall2 - verticalLocationOnWall1);
						}
					}
				}
			}
		}
		
		final CastRayProjectionItemInfo[][] INFOS = new CastRayProjectionItemInfo[getWidth()][];
		final Thread[] THREADS = new Thread[2];
		for (int i = 0; i < THREADS.length; i++) {
			final int I = i;
			THREADS[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					for (int x = getResolution() * I; x < getWidth(); x += getResolution() * THREADS.length) {
						Map.Ray2D ray = new Map.Ray2D(LOCATION_X, LOCATION_Y, (Math.atan((getWidth() / 2d - x) / getDistanceToPlane()) + getHorizontalDirection()));
						ArrayList<CastRayProjectionItemInfo> aList = getFilteredRayWallAndFencesInfo(ray, LOCATION_Z);
						ArrayList<CastRayProjectionItemInfo> bList = getRayMapItem3DInfo(ray, ITEMS_3D);
						if (bList != null) {
							aList.addAll(bList);
						}
						Collections.sort(aList, Collections.reverseOrder());
						INFOS[x] = aList.toArray(new CastRayProjectionItemInfo[aList.size()]);
					}
				}
			});
			THREADS[i].start();
		}
		for (int i = 0; i < THREADS.length; i++) {
			try {THREADS[i].join();} catch (InterruptedException e) {}
		}
		
		double previousFurthestDistance = getSightRange();
		CastRayMapItemInfo closestItemAtCenter = null;
		for (CastRayCylinderMapItemInfo i : items2DInfo) {
			if (i.getDistance() > getPlayer().getPhysicalHalfWidth()) {
				for (int x = 0; x < getWidth(); x += getResolution() * (this.seeThrough ? 2 : 1)) {
					CastRayProjectionItemInfo itemInfo = castRay(g, x, INFOS[x], projectionPlaneMiddle, LOCATION_X, LOCATION_Y, LOCATION_Z, i.getDistance(), previousFurthestDistance, getWidth() / 2, getHeight() / 2);
					if (itemInfo != null) {
						closestItemAtCenter = itemInfo;
					}
				}
				drawTacticalShield(g, i, projectionPlaneMiddle, LOCATION_X, LOCATION_Y, LOCATION_Z, false);
				g.drawImage(i.getMapItem().getProjectedImage(), i.getScreenLeftX(), i.getScreenTopY(), i.getScreenRightX() - i.getScreenLeftX(), i.getScreenBottomY() - i.getScreenTopY(), null);
				i.getMapItem().drawOverImage(g, getWidth(), getHeight(), i.getScreenLeftX(), i.getScreenTopY(), i.getScreenRightX() - i.getScreenLeftX(), i.getScreenBottomY() - i.getScreenTopY());
				drawTacticalShield(g, i, projectionPlaneMiddle, LOCATION_X, LOCATION_Y, LOCATION_Z, true);
				previousFurthestDistance = i.getDistance();
				if (i.containsScreenPoint(getWidth() / 2, getHeight() / 2) && (!isThirdPerson() || !getPlayer().equals(i.getMapItem()))) {
					closestItemAtCenter = i;
				}
			}
		}
		
		for (int x = 0; x < getWidth(); x += getResolution() * (this.seeThrough ? 2 : 1)) {
			CastRayProjectionItemInfo itemInfo = castRay(g, x, INFOS[x], projectionPlaneMiddle, LOCATION_X, LOCATION_Y, LOCATION_Z, 0, previousFurthestDistance, getWidth() / 2, getHeight() / 2);
			if (itemInfo != null) {
				closestItemAtCenter = itemInfo;
			}
		}
		return closestItemAtCenter != null ? closestItemAtCenter.getDisplayName(getPlayer()) + " [" + closestItemAtCenter.getDistance() + "m]" : "";
	}
	
	@Override
	public void paintComponent(Graphics g) {
		if (isGameRunning()) {
			long startTime = System.nanoTime();
			draw(g, this.renderTime);
			this.renderTime = (System.nanoTime() - startTime) / 1000000d;
		}
		if (this.freeMouse) {
			showFadedPauseMenu(g);
		}
	}
	
	public static Float getVolumeByDistance(Float relativeVolume, double distance) {
		return relativeVolume != null ? relativeVolume - (float) distance : null;
	}
	
	public static Float getVolumeByDistance(Float relativeVolume, Map.MapLocation2D source, Map.MapLocation2D reciever) {
		return getVolumeByDistance(relativeVolume, Map.findDistance2D(source, reciever));
	}
	
	public static Float getVolumeByDistance(Float relativeVolume, Map.MapLocation3D source, Map.MapLocation3D reciever) {
		return getVolumeByDistance(relativeVolume, Map.findDistance3D(source, reciever));
	}
	
	public static Clip playSoundFile(File file, Double distance) {
		return playSoundFile(distance != null ? getVolumeByDistance(getSingleton().getDesiredVolume(), distance) : getSingleton().getDesiredVolume(), file);
	}
	
	public static Clip playSoundFile(Float relativeVolume, File file) {
		if (file != null && relativeVolume != null) {
			AudioInputStream audio = null;
			Clip clip = null;
			try {
				clip = AudioSystem.getClip();
				audio = AudioSystem.getAudioInputStream(file);
				clip.open(audio);
				((FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN)).setValue(relativeVolume);
				clip.stop();
				clip.start();
				final Clip CLIP = clip;
				clip.addLineListener(new LineListener() {
					@Override
					public void update(LineEvent myLineEvent) {
						if (myLineEvent.getType() == LineEvent.Type.STOP) {
				        	CLIP.close();
						}
				    }
				});
			} catch (LineUnavailableException e) {
			} catch (IOException e) {
			} catch (UnsupportedAudioFileException e) {
			} finally {
				if (audio != null) {
					try {
						audio.close();
					} catch (IOException e) {
					}
				}
			}
			return clip;
		}
		return null;
	}
	
	private static double reduceAngle(double angle) {
		if (angle > Math.PI) {
			return angle % (Math.PI * 2);
		} else if (angle <= -Math.PI) {
			return angle % (Math.PI * 2) + Math.PI * 2;
		}
		return angle;
	}
	
	private interface CastRayMapItemInfo {
		public String getDisplayName(Player player);
		public double getDistance();
	}
	
	private class CastRayCylinderMapItemInfo implements CastRayMapItemInfo, Comparable<CastRayCylinderMapItemInfo> {
		
		private final CylinderMapItem MAP_ITEM;
		/**
		 * The distance of the item in blocks.
		 */
		private final double ACTUAL_DISTANCE;
		private final double SIDE_ANGLE;
		private final int SCREEN_LEFT_X;
		private final int SCREEN_RIGHT_X;
		private final int SCREEN_BOTTOM_Y;
		private final int SCREEN_TOP_Y;
		
		public CastRayCylinderMapItemInfo(double currentX, double currentY, double currentZ, double currentHorizontalDirection, int planeWidth, double distanceToPlane, int projectionPlaneMiddle, CylinderMapItem mapItem) {
			ACTUAL_DISTANCE = Map.findDistance(mapItem, currentX, currentY);
			MAP_ITEM = mapItem;
			double directAngle = reduceAngle(currentHorizontalDirection - Compass.getPrincipleAngle(currentX - mapItem.getLocationX(), mapItem.getLocationY() - currentY));
			SIDE_ANGLE = Math.atan(mapItem.getPhysicalHalfWidth() / ACTUAL_DISTANCE);
			SCREEN_LEFT_X = planeWidth / 2 + (int) (distanceToPlane * Math.tan(reduceAngle(directAngle - SIDE_ANGLE)));
			SCREEN_RIGHT_X = planeWidth / 2 + (int) (distanceToPlane * Math.tan(reduceAngle(directAngle + SIDE_ANGLE)));
			//SCREEN_BOTTOM_Y = (int) Math.round(projectionPlaneMiddle - (mapItem.getBottomHeight() - currentZ) * distanceToPlane / (ACTUAL_DISTANCE * -Math.cos(directAngle)));
			//SCREEN_TOP_Y = (int) Math.round(projectionPlaneMiddle - (mapItem.getBottomHeight() + mapItem.getPhysicalHeight() - currentZ) * distanceToPlane / (ACTUAL_DISTANCE *- Math.cos(directAngle)));
			SCREEN_BOTTOM_Y = getPixelHeight(mapItem.getBottomHeight(), currentZ, ACTUAL_DISTANCE * -Math.cos(directAngle), projectionPlaneMiddle);
			SCREEN_TOP_Y = getPixelHeight(mapItem.getBottomHeight() + mapItem.getPhysicalHeight(), currentZ, ACTUAL_DISTANCE * -Math.cos(directAngle), projectionPlaneMiddle);
		}
		
		/**
		 * Gets the actual distance of the wall in blocks.
		 * @return the actual distance of the wall in blocks.
		 */
		@Override
		public double getDistance() {
			return ACTUAL_DISTANCE;
		}
		
		@Override
		public String getDisplayName(Player player) {
			return getMapItem().getDisplayName(player);
		}
		
		public CylinderMapItem getMapItem() {
			return MAP_ITEM;
		}
		
		public double getSideAngle() {
			return SIDE_ANGLE;
		}

		public int getScreenLeftX() {
			return SCREEN_LEFT_X;
		}

		public int getScreenRightX() {
			return SCREEN_RIGHT_X;
		}
		
		public int getScreenBottomY() {
			return SCREEN_BOTTOM_Y;
		}
		
		public int getScreenTopY() {
			return SCREEN_TOP_Y;
		}
		
		public boolean containsScreenPoint(int pixelX, int pixelY) {
			return getScreenLeftX() <= pixelX && pixelX <= getScreenRightX() && getScreenTopY() <= pixelY && pixelY <= getScreenBottomY();
		}
		
		@Override
		public int compareTo(CastRayCylinderMapItemInfo o) {
			return Double.compare(ACTUAL_DISTANCE, o.ACTUAL_DISTANCE);
		}
	}
	
	private static final class CastRayProjectionItemInfo implements CastRayMapItemInfo, Comparable<CastRayProjectionItemInfo> {
		private final double ACTUAL_DISTANCE;
		private final double CORRECTED_DISTANCE;
		private final double TOP_SURFACE_DISTANCE;
		private final ProjectionPlaneItem PROJECTED_ITEM;
		private final double DIRECTION;
		private final boolean IS_TOP_SURFACE;
		private final Point2D POINT_OF_INTERSECTION;
		
		private CastRayProjectionItemInfo(CastRayProjectionItemInfo info, boolean isTop) {
			CORRECTED_DISTANCE = info.getCorrectedDistance();
			ACTUAL_DISTANCE = info.getDistance() + (!info.isTopSurface() && isTop ? info.getTopSurfaceDistance() : 0) - (info.isTopSurface() && !isTop ? info.getTopSurfaceDistance() : 0);
			TOP_SURFACE_DISTANCE = info.getTopSurfaceDistance();
			PROJECTED_ITEM = info.getProjectedItem();
			DIRECTION = info.getDirection();
			IS_TOP_SURFACE = isTop;
			POINT_OF_INTERSECTION = info.POINT_OF_INTERSECTION;
		}
		
		public CastRayProjectionItemInfo(Map.Ray2D ray, double topSurfaceDistance, double horizontalDirection, ProjectionPlaneItem projectedItem, Point2D pointOfIntersection, boolean isTop) {
			double actualDistance = Map.findDistance(ray.getLocationX(), ray.getLocationY(), pointOfIntersection.getX(), pointOfIntersection.getY());
			CORRECTED_DISTANCE = actualDistance * Math.cos(ray.getDirection() - horizontalDirection);
			ACTUAL_DISTANCE = actualDistance + (isTop ? topSurfaceDistance : 0);
			TOP_SURFACE_DISTANCE = topSurfaceDistance;
			PROJECTED_ITEM = projectedItem;
			DIRECTION = ray.getDirection();
			IS_TOP_SURFACE = isTop;
			POINT_OF_INTERSECTION = new Point2D.Double(pointOfIntersection.getX(), pointOfIntersection.getY());
		}

		/**
		 * Gets the actual distance of the item in blocks. If the item is a top surface, the actual distance includes the top surface distance.
		 * @return the actual distance of the item in blocks.
		 */
		@Override
		public double getDistance() {
			return ACTUAL_DISTANCE;
		}
		
		/**
		 * Gets the corrected distance for a projection screen of the item in blocks.
		 * @return the corrected distance of the item in blocks.
		 */
		public double getCorrectedDistance() {
			return CORRECTED_DISTANCE;
		}
		
		/**
		 * Gets the distance across the top of the 3D item in blocks
		 * @return the distance across the top of the 3D item
		 */
		public double getTopSurfaceDistance() {
			return TOP_SURFACE_DISTANCE;
		}
		
		@Override
		public String getDisplayName(Player player) {
			return getProjectedItem().getDisplayName(player);
		}
		
		public ProjectionPlaneItem getProjectedItem() {
			return PROJECTED_ITEM;
		}
		
		public double getTopHeight(int index) {
			return getProjectedItem().getTopHeight(POINT_OF_INTERSECTION, index);
		}
		
		public double getBottomHeight(int index) {
			return getProjectedItem().getBottomHeight(POINT_OF_INTERSECTION, index);
		}
		
		public double getDrawBottomHeight(int index) {
			return getProjectedItem().getDrawBottomHeight(POINT_OF_INTERSECTION, index);
		}
		
		public Bullet.BulletMarking[] getBulletMarkings(Map.Ray2D ray) {
			return getProjectedItem().getBulletMarkings(ray, getDistance());
		}
		
		public double getDirection() {
			return DIRECTION;
		}
		
		public boolean isTopSurface() {
			return IS_TOP_SURFACE;
		}

		@Override
		public int compareTo(CastRayProjectionItemInfo o) {
			if (Math.abs(getDistance() - o.getDistance()) < 0.0001) {
				if (isTopSurface() && !o.isTopSurface()) {
					return -1;
				} else if (!isTopSurface() && o.isTopSurface()) {
					return 1;
				} else {
					return 0;
				}
			}
			return Double.compare(getDistance(), o.getDistance());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(getDistance());
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + ((PROJECTED_ITEM == null) ? 0 : PROJECTED_ITEM.hashCode());
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
			CastRayProjectionItemInfo other = (CastRayProjectionItemInfo) obj;
			if (Double.doubleToLongBits(getDistance()) != Double.doubleToLongBits(other.getDistance()))
				return false;
			if (PROJECTED_ITEM == null) {
				if (other.PROJECTED_ITEM != null)
					return false;
			} else if (!PROJECTED_ITEM.equals(other.PROJECTED_ITEM))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "CastRayWallInfo [distance=" + getDistance() + ", item=" + PROJECTED_ITEM + ", topSurface=" + isTopSurface() + "]";
		}
	}
	
	private class PausedMenu implements MouseListener {

		private final PausedMenuActionListener ACTION_LISTENER;
		private final PausedMenuAction[] MENU_ITEMS_ACTION = PausedMenuAction.values();
		private Rectangle[] menuItemRectangles = new Rectangle[MENU_ITEMS_ACTION.length];
		
		private transient Graphics recentGraphics = null;
		private final Color PRESSED_COLOR = new Color(255, 0, 0, 200);
		private transient int highlightItem = -1;
		
		private PausedMenu(PausedMenuActionListener actionListener) {
			ACTION_LISTENER = actionListener;
		}
		
		private void drawPausedMenu(Graphics g) {
			this.recentGraphics = g;
			g.setFont(Main.PAUSE_MENU_CLICKABLE_ITEMS_FONT);
			FontMetrics fm = g.getFontMetrics(g.getFont());
			int count = 0;
			for (int i = 0; i < MENU_ITEMS_ACTION.length; i++) {
				if (MENU_ITEMS_ACTION[MENU_ITEMS_ACTION.length - i - 1].showIffGameRunning() == null || isGameRunning() == MENU_ITEMS_ACTION[MENU_ITEMS_ACTION.length - i - 1].showIffGameRunning()) {
					if (i == this.highlightItem) {
						g.setColor(PRESSED_COLOR);
					} else {
						g.setColor(Color.BLACK);
					}
					g.drawString(MENU_ITEMS_ACTION[MENU_ITEMS_ACTION.length - i - 1].getName(), 10, getHeight() - 10 - fm.getHeight() * count);
					menuItemRectangles[i] = new Rectangle(10, getHeight() - 10 - fm.getHeight() * (count + 1) + fm.getDescent(), fm.stringWidth(MENU_ITEMS_ACTION[MENU_ITEMS_ACTION.length - i - 1].getName()), fm.getHeight());
					count++;
				} else {
					menuItemRectangles[i] = null;
				}
			}
			if (isGameRunning()) {
				g.setColor(Color.BLACK);
				g.setFont(Main.PAUSE_MENU_GAME_MODE_NAME_FONT);
				fm = g.getFontMetrics(g.getFont());
				g.drawString(getGame().getGameMode().getName(), getWidth() - 10 - fm.stringWidth(getGame().getGameMode().getName()), getHeight() - 10);
				g.setFont(Main.PAUSE_MENU_MAP_NAME_FONT);
				fm = g.getFontMetrics(g.getFont());
				g.drawString(getGame().getMap().getName(), getWidth() - 10 - fm.stringWidth(getGame().getMap().getName()), 10 + fm.getHeight());
			}
		}
		
		private boolean inRectangle(int x, int y, Rectangle r) {
			return r != null && inRectangle(x, y, r.x, r.y, r.width, r.height);
		}
		
		private boolean inRectangle(int x, int y, int rx, int ry, int rw, int rh) {
			return x >= rx && x <= rx + rw && y >= ry && y <= ry + rh;
		}

		public void mouseClicked(MouseEvent e) {
			if (ProjectionPlane.this.freeMouse) {
				for (int i = 0 ; i < MENU_ITEMS_ACTION.length; i++) {
					if (inRectangle(e.getX(), e.getY(), menuItemRectangles[i])) {
						ACTION_LISTENER.pauseMenuItemClicked(e, MENU_ITEMS_ACTION[MENU_ITEMS_ACTION.length - i - 1]);
						return;
					}
				}
				lockCursor(true);
			}
		}

		public void mousePressed(MouseEvent e) {
			if (ProjectionPlane.this.freeMouse) {
				for (int i = 0 ; i < MENU_ITEMS_ACTION.length; i++) {
					if (inRectangle(e.getX(), e.getY(), menuItemRectangles[i])) {
						synchronized (this) {
							this.highlightItem = i;
						}
						drawPausedMenu(this.recentGraphics);
						return;
					}
				}
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (ProjectionPlane.this.freeMouse) {
				synchronized (this) {
					this.highlightItem = -1;
				}
				drawPausedMenu(this.recentGraphics);
				repaint();
			}
		}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}
	}
	
	public class ModalInternalFrame extends JInternalFrame {
		private static final long serialVersionUID = -3099626673944862352L;
		
		private final ModalInternalFrame PARENT;
		private ModalInternalFrame child = null;
		private final boolean MODAL;
		
		public ModalInternalFrame(ModalInternalFrame parent, String title, boolean modal, boolean resizable, final boolean CLOSABLE, boolean maximizable, boolean iconifiable, final boolean DISPOSE_ON_ICONIFIED, final DialogDisposedSimpleAction DISPOSE_ACTION) {
			super(title, resizable, CLOSABLE, maximizable, iconifiable);
			setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
			setFrameIcon(Main.FRAME_ICON);
			PARENT = parent;
			MODAL = modal;
			if (parent != null) {
				synchronized (parent) {
					parent.child = this;
				}
			}
			addInternalFrameListener(new InternalFrameListener() {
				@Override
				public void internalFrameOpened(InternalFrameEvent e) {
				}
				@Override
				public void internalFrameIconified(InternalFrameEvent e) {
					if (DISPOSE_ON_ICONIFIED && CLOSABLE) {
						dispose();
					}
				}
				@Override
				public void internalFrameDeiconified(InternalFrameEvent e) {
				}
				@Override
				public void internalFrameDeactivated(InternalFrameEvent e) {
				}
				@Override
				public void internalFrameClosing(InternalFrameEvent e) {
				}
				@Override
				public void internalFrameClosed(InternalFrameEvent e) {
					if (isModal()) {
						if (PARENT == null) {
							restorePreviousInputState();
						} else {
							setParentEnabled(true);
						}
					}
					if (PARENT != null) {
						synchronized (PARENT) {
							PARENT.child = null;
						}
					}
					synchronized (ProjectionPlane.class) {
						ProjectionPlane.previousModalFrame = PARENT;
					}
					if (DISPOSE_ACTION != null) {
						DISPOSE_ACTION.dialogDisposed();
					}
				}
				@Override
				public void internalFrameActivated(InternalFrameEvent e) {
					if (isModal()) {
						moveLowestChildToFront();
					}
				}
			});
		}
		
		private void setParentEnabled(boolean b) {
			PARENT.setResizable(b);
			PARENT.setClosable(b);
			PARENT.setMaximizable(b);
			PARENT.setIconifiable(b);
			PARENT.superSetVisible(b);
			Main.enableComponents(PARENT, b);
		}
		
		private void moveLowestChildToFront() {
			if (this.child != null) {
				this.child.moveLowestChildToFront();
			} else {
				moveToFront();
				if (!isSelected()) {
					try {
						setSelected(true);
					} catch (PropertyVetoException e1) {
					}
				}
			}
		}
		
		public boolean isModal() {
			return MODAL;
		}
		
		public ProjectionPlane getOuter() {
			return ProjectionPlane.this;
		}
		
		private void superSetVisible(boolean aFlag) {
			super.setVisible(aFlag);
		}
		
		@Override
		public void setVisible(boolean aFlag) {
			boolean wasVisible = isVisible();
			super.setVisible(aFlag);
			if (isModal()) {
				if (aFlag && !wasVisible) {
					if (PARENT == null) {
						try {
							freezeCurrentInputState();
						} catch (IllegalStateException e) {
							throw new IllegalStateException("An existing modal internal dialog must be the parent of this dialog");
						}
					} else {
						setParentEnabled(false);
					}
					synchronized (ProjectionPlane.class) {
						ProjectionPlane.previousModalFrame = this;
					}
				}
			}
		}
	}
	
	public interface DialogDisposedSimpleAction {
		public void dialogDisposed();
	}
	
	public interface DialogDisposedAction<T> {
		public void dialogDisposed(T t);
	}
	
	public interface ProjectionPlaneItem {
		public String getDisplayName(Player player);
		public int getOpacityAlpha(Map.Ray2D ray, double verticalAngle);
		public int getNumberOfGaps();
		public double getTopHeight(Point2D point, int index);
		public double getBottomHeight(Point2D point, int index);
		public double getDrawBottomHeight(Point2D point, int index);
		public Color getTopColor(Map.Ray2D ray);
		public Color getBottomColor(Map.Ray2D ray);
		public Color getProjectedColor(Map.Ray2D ray);
		public BufferedImage getBitmap(Point2D pointOfIntersection);
		public Point2D getIntersectionPoint(Map.Ray2D ray);
		public Bullet.BulletMarking[] getBulletMarkings(Map.Ray2D ray, double distance);
	}
	
	enum InputConfigurationCategory {
		MOVEMENT("Movement"), COMMUNICATION("Communication"), MENU("Menu"), MISCELLANEOUS("Miscellaneous");
		private final String NAME;
		private InputConfigurationCategory(String name) {
			NAME = name;
		}
		public String getName() {
			return NAME;
		}
	}
	
	enum InputConfiguration {
		MOVE_FORWARD("Move Forward", InputConfigurationCategory.MOVEMENT, KeyEvent.VK_W, null),
		MOVE_BACK("Move Back", InputConfigurationCategory.MOVEMENT, KeyEvent.VK_S, null),
		MOVE_LEFT("Move Left(Strafe)", InputConfigurationCategory.MOVEMENT, KeyEvent.VK_A, null),
		MOVE_RIGHT("Move Right(Strafe)", InputConfigurationCategory.MOVEMENT, KeyEvent.VK_D, null),
		FLOAT_UP("Float Up", InputConfigurationCategory.MOVEMENT, KeyEvent.VK_SPACE, null),
		FLOAT_DOWN("Float Down", InputConfigurationCategory.MOVEMENT, KeyEvent.VK_CONTROL, null),
		JUMP("Jump", InputConfigurationCategory.MOVEMENT, KeyEvent.VK_SPACE, null),
		DUCK("Duck", InputConfigurationCategory.MOVEMENT, KeyEvent.VK_CONTROL, null),
		LOOK_UP("Look Up", InputConfigurationCategory.MOVEMENT, KeyEvent.VK_UP, null),
		LOOK_DOWN("Look Down", InputConfigurationCategory.MOVEMENT, KeyEvent.VK_DOWN, null),
		TURN_LEFT("Turn Left", InputConfigurationCategory.MOVEMENT, KeyEvent.VK_LEFT, null),
		TURN_RIGHT("Turn Right", InputConfigurationCategory.MOVEMENT, KeyEvent.VK_RIGHT, null),
		CHAT("Chat", InputConfigurationCategory.COMMUNICATION, KeyEvent.VK_Y, null),
		BUY_MENU("Buy Menu", InputConfigurationCategory.MENU, KeyEvent.VK_B, null),
		AUTO_BUY("Automatically Buy Equipment", InputConfigurationCategory.MENU, KeyEvent.VK_F1, null),
		USE("Use Items", InputConfigurationCategory.MISCELLANEOUS, KeyEvent.VK_E, null),
		VIEW_SCORE("View Score", InputConfigurationCategory.MISCELLANEOUS, KeyEvent.VK_TAB, null),
		;

		private final String NAME;
		private final InputConfigurationCategory CATEGORY;
		private final Integer DEFAULT_KEY;
		private final Integer DEFAULT_MOUSE_BUTTON;
		private Integer currentKey = null;
		private Integer currentMouseButton = null;
		
		private InputConfiguration(String name, InputConfigurationCategory category, Integer defaultKey, Integer defaultMouseButton) {
			NAME = name;
			CATEGORY = category;
			DEFAULT_KEY = defaultKey;
			DEFAULT_MOUSE_BUTTON = defaultMouseButton;
			this.currentKey = DEFAULT_KEY;
			this.currentMouseButton = DEFAULT_MOUSE_BUTTON;
		}

		public String getName() {
			return NAME;
		}
		
		public String getInputStringRepresentation() {
			if (getCurrentKey() != null) {
				return KeyEvent.getKeyText(getCurrentKey());
			} else if (getCurrentMouseButton() != null) {
				switch (getCurrentMouseButton()) {
					case MouseEvent.BUTTON1:
						return "Mouse1";
					case MouseEvent.BUTTON2:
						return "Mouse2";
					case MouseEvent.BUTTON3:
						return "Mouse3";
				}
			}
			return null;
		}
		
		public InputConfigurationCategory getInputConfigurationCategory() {
			return CATEGORY;
		}
		
		public Integer getDefaultKey() {
			return DEFAULT_KEY;
		}
		
		public Integer getDefaultMouseButton() {
			return DEFAULT_MOUSE_BUTTON;
		}
		
		public Integer getCurrentKey() {
			return this.currentKey;
		}
		
		public Integer getCurrentMouseButton() {
			return this.currentMouseButton;
		}
		
		public synchronized void setCurrentKey(Integer key) {
			this.currentKey = key;
		}
		
		public synchronized void setCurrentMouseButton(Integer button) {
			this.currentMouseButton = button;
		}
		
		public void setToDefault() {
			setCurrentKey(getDefaultKey());
			setCurrentMouseButton(getDefaultMouseButton());
		}
		
		public boolean hasInput() {
			return getCurrentKey() != null || getCurrentMouseButton() != null;
		}
		
		private boolean hasInputKey(int keyCode) {
			return getCurrentKey() != null && getCurrentKey() == keyCode;
		}
		
		private boolean hasInputKey(Set<Integer> pressedKeys) {
			return pressedKeys.contains(getCurrentKey());
		}
		
		private boolean hasInputMouseButton(int button) {
			return getCurrentMouseButton() != null && getCurrentMouseButton() == button;
		}
		
		private boolean hasInputMouseButton(Set<Integer> pressedButtons) {
			return pressedButtons.contains(getCurrentMouseButton());
		}
		
		private boolean hasInput(int code, boolean isKey) {
			return isKey ? hasInputKey(code) : hasInputMouseButton(code);
		}
		
		private boolean hasInput(Set<Integer> pressedKeys, Set<Integer> pressedButtons) {
			return hasInputKey(pressedKeys) || hasInputMouseButton(pressedButtons);
		}
	}
}