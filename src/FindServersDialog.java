import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

class FindServersDialog {
	
	private static MultiplayerGameClient client = null;

	public static void showFindServersDialog(final ProjectionPlane P_PLANE, final ProjectionPlane.DialogDisposedAction<MultiplayerGameClient> ACTION) {
		final ProjectionPlane.ModalInternalFrame DIALOG = P_PLANE.addInternalFrame("Find Servers", true, true, true, true, true, true, true, new ProjectionPlane.DialogDisposedSimpleAction() {
			@Override
			public void dialogDisposed() {
				ACTION.dialogDisposed(FindServersDialog.client);
			}
		});
		DIALOG.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		final JTabbedPane TABBED_PANE = new JTabbedPane();
		final BorderLayout MAIN_LAN_PANEL_LAYOUT = new BorderLayout();
		final JPanel MAIN_LAN_PANEL = new JPanel(MAIN_LAN_PANEL_LAYOUT);
		createLANTable(500, new ProjectionPlane.DialogDisposedAction<JComponent>() {
			@Override
			public void dialogDisposed(JComponent component) {
				MAIN_LAN_PANEL.add(component, BorderLayout.CENTER);
				TABBED_PANE.addTab("LAN", null, MAIN_LAN_PANEL, "Find Servers on LAN");
				JPanel bottomLANPanel = new JPanel(new BorderLayout());
				JButton findSpecificServerButton = new JButton("Find Server");
				findSpecificServerButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final String IP_ADDRESS = JOptionPane.showInternalInputDialog(DIALOG, "Enter the IP Address of the server", "Find Server", JOptionPane.PLAIN_MESSAGE);
						if (IP_ADDRESS == null) {
							return;
						}
					    new WaitDialog<LANServerTableItem>("Connecting...", "Connecting to Server. Please wait...", true, "Connecting...", new ProjectionPlane.DialogDisposedAction<LANServerTableItem>() {
					    	@Override
							public void dialogDisposed(LANServerTableItem item) {
						    	if (item == null) {
						    		JOptionPane.showInternalMessageDialog(DIALOG, "Cannot find server", "Server Not Found", JOptionPane.ERROR_MESSAGE);
						    	} else if (checkPassword(P_PLANE, item)) {
									if (connect(item)) {
										DIALOG.dispose();
									} else {
										JOptionPane.showInternalMessageDialog(DIALOG, "Server at " + IP_ADDRESS + " could not be found.", "Could not find server", JOptionPane.ERROR_MESSAGE);
									}
						    	} else {
						    		JOptionPane.showInternalMessageDialog(DIALOG, "Incorrect Password", "Incorrect Password", JOptionPane.ERROR_MESSAGE);
								}
							}
						}) {
					    	@Override
							public LANServerTableItem run() {
								LANServerTableItem lanServerTableItem = null;
								ObjectOutputStream outStream = null;
								ObjectInputStream inStream = null;
								Socket socket = null;
							    try {
							    	socket = new Socket(IP_ADDRESS, MultiplayerGameServer.STANDARD_PORT);
								    outStream = new ObjectOutputStream(socket.getOutputStream());
								    outStream.flush();
								    outStream.writeObject(new NetworkMessage(NetworkMessage.MessageType.REQUEST_GAME_INFO));
								    inStream = new ObjectInputStream(socket.getInputStream());
								    Object message = inStream.readObject();
								    if (message instanceof NetworkMessage) {
								    	NetworkMessage networkMessage = (NetworkMessage) message;
								    	if (networkMessage.getMessageType() == NetworkMessage.MessageType.RETURN_GAME_INFO) {
								    		MultiplayerGameServer.GameInfo gameInfo = (MultiplayerGameServer.GameInfo) networkMessage.getMessage();
								    		lanServerTableItem = new LANServerTableItem(gameInfo.getServerName(), IP_ADDRESS, gameInfo.getPlayers(), gameInfo.getMaxPlayers(), gameInfo.getPassword(), gameInfo.getMapName());
									    }
								    }
							    } catch (IOException ex) {
							    } catch (ClassNotFoundException e) {
								} finally {
							    	if (outStream != null) {
									    try {
											outStream.writeObject(new NetworkMessage(NetworkMessage.MessageType.TERMINATE_CONNECTION));
											outStream.close();
										} catch (UnknownHostException e1) {
										} catch (IOException e1) {
										}
							    	}
							    	if (inStream != null) {
							    		try {
											inStream.close();
										} catch (IOException e) {
										}
							    	}
							    	if (socket != null) {
							    		try {
											socket.close();
										} catch (IOException e) {
										}
							    	}
							    }
								return lanServerTableItem;
							}
					    };
					}
				});
				bottomLANPanel.add(findSpecificServerButton, BorderLayout.WEST);
				JButton refreshButton = new JButton("Refresh");
				refreshButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int timeOut = 1000;
						try {
							String input = JOptionPane.showInternalInputDialog(DIALOG, "Enter the maximum wait time (ms)", "Wait Time", JOptionPane.PLAIN_MESSAGE);
							if (input == null) {
								return;
							}
							timeOut = Integer.parseInt(input);
						} catch (NumberFormatException ex) {
						}
						Component centerComponent = MAIN_LAN_PANEL_LAYOUT.getLayoutComponent(BorderLayout.CENTER);
						if (centerComponent != null) {
							MAIN_LAN_PANEL.remove(centerComponent);
						}
						createLANTable(timeOut, new ProjectionPlane.DialogDisposedAction<JComponent>() {
							@Override
							public void dialogDisposed(JComponent component) {
								MAIN_LAN_PANEL.add(component, BorderLayout.CENTER);
								MAIN_LAN_PANEL.revalidate();
								MAIN_LAN_PANEL.repaint();
							}
						}, DIALOG, getPotentialHosts("192.168.0", 1, 30, true));
					}
				});
				bottomLANPanel.add(refreshButton, BorderLayout.EAST);
				MAIN_LAN_PANEL.add(bottomLANPanel, BorderLayout.SOUTH);
				TABBED_PANE.setMnemonicAt(0, KeyEvent.VK_L);
				DIALOG.getContentPane().add(TABBED_PANE);
				DIALOG.pack();
				DIALOG.setSize(500, 250);
				DIALOG.setVisible(true);
			}
		}, DIALOG, "localhost");
	}
	
	private static String[] getPotentialHosts(String subnet, int startIndex, int endIndex, boolean includeLocalHost) {
		startIndex = Math.max(startIndex, 1);
		endIndex = Math.min(endIndex, 255);
		ArrayList<String> hosts = new ArrayList<String>();
		if (includeLocalHost) {
			hosts.add("localhost");
		}
		for (int i = startIndex; i < endIndex; i++) {
			hosts.add(subnet + "." + i);
		}
		return hosts.toArray(new String[hosts.size()]);
	}
	
	private static void getLANServers(final int TIME_OUT, final ProjectionPlane.DialogDisposedAction<LANServerTableItem[]> ACTION, final String... CHECK_HOSTS) {
		if (CHECK_HOSTS == null) {
			ACTION.dialogDisposed(new LANServerTableItem[0]);
		} else {
			final ArrayList<LANServerTableItem> AVALIABLE_SERVERS = new ArrayList<LANServerTableItem>();
			new WaitDialog<Void>("Searching...", "Searching for servers. Please wait...", true, 0, CHECK_HOSTS.length, new ProjectionPlane.DialogDisposedAction<Void>() {
				@Override
				public void dialogDisposed(Void t) {
					ACTION.dialogDisposed(AVALIABLE_SERVERS.toArray(new LANServerTableItem[AVALIABLE_SERVERS.size()]));
				}
			}) {
		    	@Override
				public Void run() {
					int i = 0;
					for (String host : CHECK_HOSTS) {
				    	ObjectOutputStream outStream = null;
				    	ObjectInputStream inStream = null;
						Socket socket = null;
						try {
							setProgress(i);
							socket = new Socket(host, MultiplayerGameServer.STANDARD_PORT);
						    socket.setSoTimeout(TIME_OUT / CHECK_HOSTS.length);
						    outStream = new ObjectOutputStream(socket.getOutputStream());
						    outStream.flush();
						    outStream.writeObject(new NetworkMessage(NetworkMessage.MessageType.REQUEST_GAME_INFO));
						    inStream = new ObjectInputStream(socket.getInputStream());
						    Object message = inStream.readObject();
						    if (message instanceof NetworkMessage) {
						    	NetworkMessage networkMessage = (NetworkMessage) message;
						    	if (!isCancelled() && networkMessage.getMessageType() == NetworkMessage.MessageType.RETURN_GAME_INFO) {
						    		MultiplayerGameServer.GameInfo gameInfo = (MultiplayerGameServer.GameInfo) networkMessage.getMessage();
						    		AVALIABLE_SERVERS.add(new LANServerTableItem(gameInfo.getServerName(), host, gameInfo.getPlayers(), gameInfo.getMaxPlayers(), gameInfo.getPassword(), gameInfo.getMapName()));
						    	}
						    }
						} catch (UnknownHostException e) {
						} catch (IOException e) {
						} catch (ClassNotFoundException e) {
						} finally {
					    	if (outStream != null) {
							    try {
									outStream.writeObject(new NetworkMessage(NetworkMessage.MessageType.TERMINATE_CONNECTION));
									outStream.close();
								} catch (UnknownHostException e1) {
								} catch (IOException e) {
								}
					    	}
					    	if (inStream != null) {
					    		try {
									inStream.close();
								} catch (IOException e) {
								}
					    	}
						    if (socket != null) {
							    try {
							    	socket.close();
							    } catch(IOException e) {
							    }
						    }
						}
						if (isCancelled()) {
							break;
						}
					}
					return null;
				}
			};
		}
	}
	
	private static void createLANTable(int timeout, final ProjectionPlane.DialogDisposedAction<JComponent> ACTION, final ProjectionPlane.ModalInternalFrame DIALOG, String... checkHosts) {
		getLANServers(timeout, new ProjectionPlane.DialogDisposedAction<FindServersDialog.LANServerTableItem[]>() {
			@Override
			public void dialogDisposed(LANServerTableItem[] tableItems) {
				JPanel panel = new JPanel(new BorderLayout());
				String[] columnNames = new String[6];
				columnNames[0] = "Server";
				columnNames[1] = "IP Address";
				columnNames[2] = "Player";
				columnNames[3] = "Map";
				columnNames[4] = "Locked";
				columnNames[5] = "Connect";
				Object[][] data = new Object[tableItems.length][columnNames.length];
				int count = 0;
				for (LANServerTableItem i : tableItems) {
					if (i != null) {
						data[count][0] = i.SERVER_NAME;
						data[count][1] = i.IP_ADDRESS;
						data[count][2] = i.PLAYERS + "/" + i.MAX_PLAYERS;
						data[count][3] = i.MAP_NAME;
						data[count][4] = i.PASSWORD != null ? "Yes" : "No";
						data[count][5] = i;
					}
					count++;
				}
				JPanel tablePanel = new JPanel(new BorderLayout());
				DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
				JTable table = new JTable(tableModel) {
					private static final long serialVersionUID = 1508784803306832299L;
					@Override
					public boolean isCellEditable(int row, int column){  
						if (column >= getColumnCount() - 1) return true;
						return false;
					} 
				};
				table.setAutoCreateRowSorter(true);
				TableButton buttonEditor = new TableButton(table, "Connect");
				buttonEditor.addTableButtonListener(new TableButtonListener() {
					@Override
					public void tableButtonClicked(JTable table, int row, int col) {
						Object clickedObject = table.getModel().getValueAt(table.convertRowIndexToModel(row), col);
						if (clickedObject instanceof LANServerTableItem) {
							if (checkPassword(DIALOG.getOuter(), (LANServerTableItem) clickedObject)) {
								connect((LANServerTableItem) clickedObject);
								DIALOG.dispose();
							} else {
					    		JOptionPane.showInternalMessageDialog(DIALOG, "Incorrect Password", "Incorrect Password", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				});
		        table.getColumn("Connect").setCellRenderer(buttonEditor);
		        table.getColumn("Connect").setCellEditor(buttonEditor);
		        table.setPreferredScrollableViewportSize(table.getPreferredSize());
		        table.setFillsViewportHeight(true);
		        panel.add(table.getTableHeader(), BorderLayout.PAGE_START);
				tablePanel.add(table, BorderLayout.CENTER);
				panel.add(tablePanel, BorderLayout.CENTER);
				ACTION.dialogDisposed(new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
			}
		}, checkHosts);
	}
	
	private static boolean checkPassword(ProjectionPlane pPlane, LANServerTableItem lanServerTableItem) {
		if (lanServerTableItem != null) {
			if (lanServerTableItem.PASSWORD != null) {
				String password = JOptionPane.showInternalInputDialog(pPlane, "Enter the password for the server", "Password", JOptionPane.INFORMATION_MESSAGE);
				if (!lanServerTableItem.PASSWORD.equals(password)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	private static boolean connect(LANServerTableItem lanServerTableItem) {
		if (lanServerTableItem != null) {
			synchronized (FindServersDialog.class) {
				FindServersDialog.client = new MultiplayerGameClient(lanServerTableItem.IP_ADDRESS);
			}
			return true;
		}
		return false;
	}
	
	private static class LANServerTableItem {
		private final String SERVER_NAME;
		private final String IP_ADDRESS;
		private final int PLAYERS;
		private final int MAX_PLAYERS;
		private final String PASSWORD;
		private final String MAP_NAME;
		private LANServerTableItem(String serverName, String ipAddress, int players, int maxPlayers, String password, String mapName) {
			SERVER_NAME = serverName;
			IP_ADDRESS = ipAddress;
			PLAYERS = players;
			MAX_PLAYERS = maxPlayers;
			PASSWORD = password;
			MAP_NAME = mapName;
		}
	}
	
	private static class TableButton extends JButton implements TableCellRenderer, TableCellEditor {
		private static final long serialVersionUID = -6365958713866918115L;
		
		private final JTable TABLE;
		private int selectedRow;
		private int selectedColumn;
		private final Vector<TableButtonListener> LISTENER = new Vector<TableButtonListener>();

		private TableButton(JTable table, String text) {
			super(text);
			TABLE = table;
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					for (TableButtonListener l : LISTENER) {
						l.tableButtonClicked(TABLE, selectedRow, selectedColumn);
					}
				}
			});
		}

		public void addTableButtonListener(TableButtonListener l) {
			LISTENER.add(l);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			if (value == Boolean.FALSE) return null;
			return this;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
			if (value == Boolean.FALSE) return null;
			selectedRow = row;
			selectedColumn = col;
			return this;
		}

		@Override
		public void addCellEditorListener(CellEditorListener arg0) {
		}

		@Override
		public void cancelCellEditing() {
		}

		@Override
		public Object getCellEditorValue() {
			return "";
		}

		@Override
		public boolean isCellEditable(EventObject arg0) {
			return true;
		}

		@Override
		public void removeCellEditorListener(CellEditorListener arg0) {
		}

		@Override
		public boolean shouldSelectCell(EventObject arg0) {
			return false;
		}

		@Override
		public boolean stopCellEditing() {
			return true;
		}
	}
	
	private static interface TableButtonListener extends EventListener {
		  public void tableButtonClicked(JTable table, int row, int col);
	}
	
	private static abstract class WaitDialog<T> {
		
		private T value = null;
		private boolean cancelled = false;
		private final JProgressBar PROGRESS_BAR;
		
		private WaitDialog(String title, String message, boolean cancelable, String progressPanelMessage, ProjectionPlane.DialogDisposedAction<T> action) {
			this(title, message, cancelable, progressPanelMessage, null, 0, action);
		}
		
		private WaitDialog(String title, String message, boolean cancelable, int progressMin, int progressMax, ProjectionPlane.DialogDisposedAction<T> action) {
			this(title, message, cancelable, null, progressMin, progressMax, action);
		}
		
		private WaitDialog(String title, String message, boolean cancelable, String progressPanelMessage, Integer progressMin, int progressMax, final ProjectionPlane.DialogDisposedAction<T> ACTION) {
			if (progressMin != null) {
				PROGRESS_BAR = new JProgressBar(progressMin, progressMax);
				PROGRESS_BAR.setValue(progressMin);
			} else {
				PROGRESS_BAR = new JProgressBar(0, 0);
				PROGRESS_BAR.setString(progressPanelMessage);
			}
			PROGRESS_BAR.setStringPainted(true);
			final ProjectionPlane.ModalInternalFrame WAIT_DIALOG = ProjectionPlane.getSingleton().addInternalFrame(title, true, true, cancelable, true, false, false, true, new ProjectionPlane.DialogDisposedSimpleAction() {
				@Override
				public void dialogDisposed() {
					ACTION.dialogDisposed(WaitDialog.this.value);
				}
			});
			WAIT_DIALOG.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
			WAIT_DIALOG.getContentPane().setLayout(new BorderLayout());
			WAIT_DIALOG.getContentPane().add(new JLabel(message, SwingConstants.CENTER), BorderLayout.NORTH);
			WAIT_DIALOG.getContentPane().add(PROGRESS_BAR, BorderLayout.CENTER);
			
			final Thread THREAD = new Thread(new Runnable() {
				@Override
				public void run() {
					synchronized (WaitDialog.this) {
						WaitDialog.this.value = WaitDialog.this.run();
					}
					WAIT_DIALOG.dispose();
				}
				
			});
			
			if (cancelable) {
				final JButton CANCEL_BUTTON = new JButton("Cancel");
				CANCEL_BUTTON.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						PROGRESS_BAR.setString("Cancelling...");
						synchronized (WaitDialog.this) {
							WaitDialog.this.cancelled = true;
						}
						WAIT_DIALOG.setIconifiable(false);
						WAIT_DIALOG.setClosable(false);
						CANCEL_BUTTON.setEnabled(false);
					}
				});
				WAIT_DIALOG.addInternalFrameListener(new InternalFrameListener() {
					@Override
					public void internalFrameOpened(InternalFrameEvent e) {
					}
					@Override
					public void internalFrameIconified(InternalFrameEvent e) {
						PROGRESS_BAR.setString("Cancelling...");
						synchronized (WaitDialog.this) {
							WaitDialog.this.cancelled = true;
						}
						WAIT_DIALOG.setIconifiable(false);
						WAIT_DIALOG.setClosable(false);
						CANCEL_BUTTON.setEnabled(false);
					}
					@Override
					public void internalFrameDeiconified(InternalFrameEvent e) {
					}
					@Override
					public void internalFrameDeactivated(InternalFrameEvent e) {
					}
					@Override
					public void internalFrameClosing(InternalFrameEvent e) {
						PROGRESS_BAR.setString("Cancelling...");
						synchronized (WaitDialog.this) {
							WaitDialog.this.cancelled = true;
						}
						WAIT_DIALOG.setIconifiable(false);
						WAIT_DIALOG.setClosable(false);
						CANCEL_BUTTON.setEnabled(false);
					}
					@Override
					public void internalFrameClosed(InternalFrameEvent e) {
					}
					@Override
					public void internalFrameActivated(InternalFrameEvent e) {
					}
				});
				WAIT_DIALOG.getContentPane().add(CANCEL_BUTTON, BorderLayout.SOUTH);
			}
			WAIT_DIALOG.pack();
			WAIT_DIALOG.setMinimumSize(WAIT_DIALOG.getSize());
			this.cancelled = false;
			THREAD.start();
		}
		
		protected final boolean isCancelled() {
			return this.cancelled;
		}
		
		public void setProgress(int progress) {
			PROGRESS_BAR.setValue(progress);
		}
		
		public abstract T run();
	}
}
