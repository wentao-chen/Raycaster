import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;


public class MultiplayerGameClient extends MultiplayerGameConnection {
	
	private final String SERVER_IP;
	private final AtomicBoolean IS_RUNNING = new AtomicBoolean(false);
	
	private Socket connection = null;
	private ObjectOutputStream outStream = null;
	private ObjectInputStream inStream = null;
	private Player player = null;
	
	public MultiplayerGameClient(String serverIP) {
		super(MultiplayerGameConnection.ConnectionType.CLIENT);
		SERVER_IP = serverIP;
	}
	
	@Override
	public boolean isRunning() {
		return IS_RUNNING.get();
	}

	@Override
	public String getServerIP() {
		return SERVER_IP;
	}
	
	@Override
	public Player getPlayer() {
		return this.player;
	}
	
	public Integer getPlayerID() {
		return getPlayer() != null ? getPlayer().getID() : null;
	}
	
	public boolean startClient() {
		if (this.connection == null || this.connection.isClosed()) {
			IS_RUNNING.set(true);
			try {
				InetAddress[] allAddresses = InetAddress.getAllByName(getServerIP());
				for (InetAddress a : allAddresses) {
					try {
						this.connection = new Socket(a, MultiplayerGameServer.STANDARD_PORT);
						break;
					} catch (EOFException eofException) {
					} catch (IOException ioException) {
					}
				}
				if (this.connection == null) {
					return false;
				}
				this.outStream = new ObjectOutputStream(this.connection.getOutputStream());
				this.outStream.flush();
				this.inStream = new ObjectInputStream(this.connection.getInputStream());
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (isRunning()) {
							try {
								Object message = MultiplayerGameClient.this.inStream.readObject();
								System.out.println("CLIENT RECIEVES MESSAGE FROM SERVER: " + message);
								if (message instanceof NetworkMessage) {
									messageReceived((NetworkMessage) message);
								}
							} catch (ClassNotFoundException classNotFoundException) {
							} catch (IOException e) {
							}
						}
						try {
							if (MultiplayerGameClient.this.outStream != null) {
								MultiplayerGameClient.this.outStream.close();
							}
							if (MultiplayerGameClient.this.inStream != null) {
								MultiplayerGameClient.this.inStream.close();
							}
							if (MultiplayerGameClient.this.connection != null) {
								MultiplayerGameClient.this.connection.close();
							}
						} catch (IOException ioException) {
						}
					}
				}, "Client Socket Listener Thread").start();
				sendMessage(new NetworkMessage(NetworkMessage.MessageType.REQUEST_PLAYER), false);
				return true;
			} catch (UnknownHostException e) {
			} catch (IOException e) {
			}
		}
		return false;
	}
	
	private void messageReceived(NetworkMessage message) {
		if (getGame() != null && message.getSenderPlayerID() != null) {
			Player p = getGame().getPlayerByID(message.getSenderPlayerID());
			if (p != null) {
				p.setLatency(System.currentTimeMillis() - message.getSendTime());
			}
		}
		if (message.getMessageType() == NetworkMessage.MessageType.TERMINATE_CONNECTION) {
			closeConnection(false, false);
    		JOptionPane.showInternalMessageDialog(ProjectionPlane.getSingleton(), "The server has been shut down.", "Connection Error", JOptionPane.ERROR_MESSAGE);
		} else if (message.getMessageType() == NetworkMessage.MessageType.FATAL_ERROR) {
			closeConnection(true, false);
		} else if (message.getMessageType() == NetworkMessage.MessageType.RETURN_PLAYER) {
			if (message.getMessage() instanceof Player) {
				synchronized (this) {
					this.player = (Player) message.getMessage();
					this.player.getGame().setClient(this);
					notifyAll();
				}
			}
		} else if (message.getMessageType() == NetworkMessage.MessageType.TEAM_CHANGED) {
			if (message.getSenderPlayerID() != null && (message.getMessage() == null || message.getMessage() instanceof Team) && getGame() != null) {
				Team sentTeam = (Team) message.getMessage();
				Team team = null;
				if (sentTeam != null) {
					for (Team t : getGame().getMap().getTeams()) {
						if (t.equals(sentTeam)) {
							team = t;
							break;
						}
					}
				}
				Player player = getGame().getPlayerByID(message.getSenderPlayerID());
				if (player != null) {
					player.setPlayerListeners(false);
					player.setTeam(team, true);
					player.setPlayerListeners(true);
				}
			}
		} else if (message.getMessageType() == NetworkMessage.MessageType.PLAYER_MOVED) {
			if (message.getSenderPlayerID() != null && message.getMessage() != null && getGame() != null) {
				Map.Point3D newLocation = (Map.Point3D) message.getMessage();
				Player player = getGame().getPlayerByID(message.getSenderPlayerID());
				if (player != null) {
					player.setPlayerListeners(false);
					player.setLocation(newLocation.x, newLocation.y, newLocation.z);
					player.setPlayerListeners(true);
				}
			}
		} else if (message.getMessageType() == NetworkMessage.MessageType.SEND_CHAT_MESSAGE) {
			if (message.willPlayerRecieve(getPlayer()) && message.getSenderPlayerID() != null && message.getMessage() != null && getGame() != null) {
				getPlayer().addChatMessage((ChatMessage.MessageContent) message.getMessage());
			}
		} else if (message.getMessageType() == NetworkMessage.MessageType.BULLET_SHOT) {
			if (message.getSenderPlayerID() != null && message.getMessage() != null && getGame() != null) {
				getGame().shootBullet((Bullet) message.getMessage(), true);
			}
		}
	}
	
	@Override
	public void sendMessage(NetworkMessage message, boolean requireFullyEstablishedConnection) {
		try {
			if (this.outStream != null) {
				this.outStream.writeObject(message);
				System.out.println("CLIENT SENTS MESSAGE TO SERVER: " + message);
				this.outStream.flush();
			}
		} catch (IOException ioException) {
			System.out.println("CLIENT WRITE ERROR (" + System.currentTimeMillis() + "):");
			ioException.printStackTrace();
			try {
				if (this.outStream != null) {
					this.outStream.writeObject(new NetworkMessage(NetworkMessage.MessageType.FATAL_ERROR, null, getPlayerID()));
					this.outStream.flush();
				}
				closeConnection(true);
			} catch (IOException ioException2) {
			}
		}
	}

	@Override
	public void closeConnection(boolean errorTermination) {
		closeConnection(errorTermination, true);
	}
	
	private void closeConnection(boolean errorTermination, boolean sendTerminationMessage) {
		if (IS_RUNNING.get()) {
			if (sendTerminationMessage) {
				sendMessage(new NetworkMessage(NetworkMessage.MessageType.TERMINATE_CONNECTION, null, getPlayerID()), false);
			}
			IS_RUNNING.set(false);
			if (getGame() != null) {
				getGame().stop();
				if (errorTermination) {
		    		JOptionPane.showInternalMessageDialog(ProjectionPlane.getSingleton(), "Connection Terminated", "Connection Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
}
