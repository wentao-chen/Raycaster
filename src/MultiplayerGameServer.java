import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;

public class MultiplayerGameServer extends MultiplayerGameConnection {
	
	public static final int STANDARD_PORT = 80;
	public static final int STANDARD_BACKLOG = 10;
	
	private ServerSocket server = null;
	private final HashMap<Player, InetAddress> IP_ADDRESS = new HashMap<Player, InetAddress>();
	private final HashMap<InetAddress, Connection> CONNECTIONS = new HashMap<InetAddress, Connection>();
	
	private final String SERVER_NAME;
	private final String PASSWORD;
	private final DefaultMap MAP;
	private final Player PLAYER;

	public MultiplayerGameServer(String serverName, String password, DefaultMap map) {
		super(MultiplayerGameConnection.ConnectionType.SERVER);
		if (serverName == null) throw new IllegalArgumentException("server name cannot be null");
		if (map == null) throw new IllegalArgumentException("map cannot be null");
		SERVER_NAME = serverName;
		PASSWORD = password;
		MAP = map;
		Game game = new Game.Builder(new MultiplayerGameMode()).allowsGenerateMaze(false).map(getMap().generateMap(DefaultMap.DefaultTeamSet.TERRORISTS_TEAM_SET)).build();
		game.setServer(this);
		PLAYER = createPlayer(game);
	}
	
	private static Player createPlayer(Game game) {
		if (game != null) {
			String availablePlayerName = "Player";
			for (int i = 1; i <= game.getNumberOfPlayers() && game.hasName(availablePlayerName); i++) {
				availablePlayerName = "Player (" + i + ")";
			}
			return new Player.Builder(availablePlayerName, game, game.getMap().getDefaultPlayerCharacteristics().getSpeed(), game.getMap().getDefaultPlayerCharacteristics().getJumpHeight()).initialSpectatorLocation(game.getMap().getInitialSpecatatorViewLocation()).stepHeight(game.getMap().getDefaultPlayerCharacteristics().getStepHeight()).projectedImagePath(Main.TERRORIST_IMAGE_PATH).build();
		}
		return null;
	}

	/**
	 * Gets the name of the server.
	 * @return the name of the server
	 */
	public String getName() {
		return SERVER_NAME;
	}
	
	/**
	 * Gets the map used for the game.
	 * @return the map used for the game
	 */
	public DefaultMap getMap() {
		return MAP;
	}
	
	@Override
	public Player getPlayer() {
		return PLAYER;
	}

	@Override
	public String getServerIP() {
		try {
			return InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
		}
		return null;
	}
	
	@Override
	public boolean isRunning() {
		return MultiplayerGameServer.this.server != null && !MultiplayerGameServer.this.server.isClosed();
	}
	
	/**
	 * Starts the server. The server will begin listening for connections.
	 * @throws UnknownHostException if no IP address for the host could be found, or if a scope_id was specified for a global IPv6 address
	 * @throws IOException if an I/O error occurs when opening the socket.
	 */
	public void startServer() throws UnknownHostException, IOException {
		if (this.server == null || this.server.isClosed()) {
			synchronized (this) {
				//this.server = new ServerSocket(STANDARD_PORT, STANDARD_BACKLOG, InetAddress.getByName("0.0.0.0"));
				this.server = new ServerSocket(STANDARD_PORT);
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (isRunning()) {
						Connection connection = null;
						try {
							connection = new Connection(MultiplayerGameServer.this.server.accept());
							synchronized (MultiplayerGameServer.this) {
								CONNECTIONS.put(connection.IP_ADDRESS, connection);
							}
							connection.start();
						} catch (IOException ioException) {
						}
					}
				}
			}, "Main Server Dispatch Thread").start();
		}
	}
	
	@Override
	public synchronized void closeConnection(boolean errorTermination) throws IOException {
		if (this.server != null) {
			while (CONNECTIONS.size() > 0) {
				InetAddress connectionAddress = CONNECTIONS.keySet().iterator().next();
				closeConnection(connectionAddress, true);
			}
			if (!this.server.isClosed()) {
				this.server.close();
			}
			this.server = null;
		}
		if (getGame() != null) {
			getGame().stop();
			if (errorTermination) {
	    		JOptionPane.showInternalMessageDialog(ProjectionPlane.getSingleton(), "Connection Terminated", "Connection Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Closes a connection to an IP address.
	 * @param ipAddress the IP address of the connection to be terminated
	 */
	public void closeConnection(InetAddress ipAddress) {
		closeConnection(ipAddress, true);
	}
	
	private void closeConnection(InetAddress ipAddress, boolean sendTerminationMessage) {
		Connection c = CONNECTIONS.get(ipAddress);
		if (c != null) {
			if (sendTerminationMessage) {
				sendMessage(ipAddress, false, new NetworkMessage(NetworkMessage.MessageType.TERMINATE_CONNECTION));
			}
			c.stop();
			synchronized (this) {
				CONNECTIONS.remove(ipAddress);
				for (Player p : IP_ADDRESS.keySet()) {
					if (ipAddress.equals(IP_ADDRESS.get(p))) {
						IP_ADDRESS.remove(p);
					}
				}
			}
		}
	}
	
	private void messageReceived(InetAddress ipAddress, NetworkMessage message) {
		if (message != null) {
			if (message.getSenderPlayerID() != null) {
				Player p = getGame().getPlayerByID(message.getSenderPlayerID());
				if (p != null) {
					p.setLatency(System.currentTimeMillis() - message.getSendTime());
				}
			}
			if (message.getMessageType() == NetworkMessage.MessageType.TERMINATE_CONNECTION) {
				closeConnection(ipAddress, false);
				if (message.getSenderPlayerID() != null) {
					getGame().removePlayers(getGame().getPlayerByID(message.getSenderPlayerID()));
				}
			} else if (message.getMessageType() == NetworkMessage.MessageType.FATAL_ERROR) {
				closeConnection(ipAddress, false);
			} else if (message.getMessageType() == NetworkMessage.MessageType.REQUEST_GAME_INFO) {
				GameInfo gameInfo = new GameInfo(getName(), getGame().getNumberOfPlayers(), getGame().getMap().getMaxPlayers(), PASSWORD, getMap().getName());
				sendMessage(ipAddress, false, new NetworkMessage(NetworkMessage.MessageType.RETURN_GAME_INFO, gameInfo, null));
			} else if (message.getMessageType() == NetworkMessage.MessageType.REQUEST_PLAYER) {
				Player returnPlayer = createPlayer(getGame());
				synchronized (this) {
					IP_ADDRESS.put(returnPlayer, ipAddress);
				}
				sendMessage(ipAddress, false, new NetworkMessage(NetworkMessage.MessageType.RETURN_PLAYER, returnPlayer, null));
				Connection c = CONNECTIONS.get(ipAddress);
				if (c != null) {
					synchronized (c) {
						c.isConnectionFullyEstablished = true;
					}
				}
			} else if (message.getMessageType() == NetworkMessage.MessageType.TEAM_CHANGED) {
				if (message.getSenderPlayerID() != null && (message.getMessage() == null || message.getMessage() instanceof Team)) {
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
						if (team != null) {
							String newImagePath = null;
							newImagePath = Team.DefaultTeams.TERRORISTS.getTeam().equals(team) ? (Math.random() < 0.5 ? Main.TERRORIST_IMAGE_PATH : Main.TERRORIST2_IMAGE_PATH) : Main.COUNTER_TERRORIST_IMAGE_PATH;
							if (newImagePath != null) {
								player.setProjectedImage(newImagePath);
							}
						}
					}
					sendMessage(message, true, ipAddress);
				}
			} else if (message.getMessageType() == NetworkMessage.MessageType.PLAYER_MOVED) {
				if (message.getSenderPlayerID() != null && message.getMessage() != null) {
					Map.Point3D newLocation = (Map.Point3D) message.getMessage();
					Player player = getGame().getPlayerByID(message.getSenderPlayerID());
					if (player != null) {
						player.setPlayerListeners(false);
						player.setLocation(newLocation.x, newLocation.y, newLocation.z);
						player.setPlayerListeners(true);
					}
					sendMessage(message, true, ipAddress);
				}
			} else if (message.getMessageType() == NetworkMessage.MessageType.SEND_CHAT_MESSAGE) {
				if (message.getSenderPlayerID() != null && message.getMessage() != null) {
					if (message.willPlayerRecieve(getPlayer())) {
						getPlayer().addChatMessage((ChatMessage.MessageContent) message.getMessage());
					}
					sendMessage(message, true, ipAddress);
				}
			} else if (message.getMessageType() == NetworkMessage.MessageType.BULLET_SHOT) {
				if (message.getSenderPlayerID() != null && message.getMessage() != null) {
					getGame().shootBullet((Bullet) message.getMessage(), true);
					sendMessage(message, true, ipAddress);
				}
			}
		}
	}
	
	@Override
	public void sendMessage(NetworkMessage message, boolean requireFullyEstablishedConnection) {
		for (InetAddress a : new LinkedList<InetAddress>(CONNECTIONS.keySet())) {
			sendMessage(a, requireFullyEstablishedConnection, message);
		}
	}

	/**
	 * Sends a message to all connections with exceptions. The message will only be sent if {@link NetworkMessage#willPlayerRecieve(Player)} returns {@code true} with the reciever as an argument.
	 * @param message the message to be sent
	 * @param requireFullyEstablishedConnection {@code true} if the message should be sent after the game message has been sent; otherwise {@code false}
	 * @param ipAddress the IP addresses of the connections that will not receive the message
	 */
	public void sendMessage(NetworkMessage message, boolean requireFullyEstablishedConnection, InetAddress... ipAddress) {
		Set<InetAddress> set = new HashSet<InetAddress>(Arrays.asList(ipAddress));
		for (Player p : new LinkedList<Player>(IP_ADDRESS.keySet())) {
			if (!set.contains(IP_ADDRESS.get(p)) && message.willPlayerRecieve(p)) {
				sendMessage(IP_ADDRESS.get(p), requireFullyEstablishedConnection, message);
			}
		}
	}
	
	/**
	 * Sends a message to a specific connection.
	 * @param ipAddress the IP address of the connection
	 * @param requireFullyEstablishedConnection {@code true} if the message should be sent after the game message has been sent; otherwise {@code false}
	 * @param message the message to be sent
	 */
	public void sendMessage(InetAddress ipAddress, boolean requireFullyEstablishedConnection, NetworkMessage message) {
		Connection c = CONNECTIONS.get(ipAddress);
		if (c != null) {
			c.sendMessage(message, requireFullyEstablishedConnection);
		}
	}
	
	public class Connection {
		private final Socket CONNECTION;
		private final InetAddress IP_ADDRESS;
		private final ObjectOutputStream OUT_STREAM;
		private final ObjectInputStream IN_STREAM;
		private final AtomicBoolean IS_RUNNING = new AtomicBoolean(false);
		private boolean isConnectionFullyEstablished = false;
		
		private Connection(Socket connection) throws IOException {
			CONNECTION = connection;
			IP_ADDRESS = CONNECTION.getInetAddress();
			OUT_STREAM = new ObjectOutputStream(connection.getOutputStream());
			IN_STREAM = new ObjectInputStream(connection.getInputStream());
			OUT_STREAM.flush();
		}
		
		public void sendMessage(NetworkMessage message) {
			sendMessage(message, true);
		}
		
		private void sendMessage(NetworkMessage message, boolean requireFullyEstablishedConnection) {
			if (!requireFullyEstablishedConnection || (this.isConnectionFullyEstablished && requireFullyEstablishedConnection)) {
				try {
					OUT_STREAM.writeObject(message);
					System.out.println("SERVER SENTS MESSAGE TO CLIENT: " + message);
					OUT_STREAM.flush();
				} catch (IOException ioException) {
					System.out.println("SERVER WRITE ERROR (" + System.currentTimeMillis() + "):");
					ioException.printStackTrace();
					try {
						if (OUT_STREAM != null) {
							OUT_STREAM.writeObject(new NetworkMessage(NetworkMessage.MessageType.FATAL_ERROR, null));
							OUT_STREAM.flush();
						}
						close();
					} catch (IOException ioException2) {
					}
				}
			}
		}
		
		private void close() throws IOException {
			OUT_STREAM.close();
			IN_STREAM.close();
			CONNECTION.close();
			synchronized (MultiplayerGameServer.this) {
				MultiplayerGameServer.this.CONNECTIONS.remove(this);
			}
		}
		
		public void start() {
			new Thread(new Runnable() {
				@Override
				public void run() {
					IS_RUNNING.set(true);
					while (IS_RUNNING.get()) {
						try {
							Object message = IN_STREAM.readObject();
							System.out.println("SERVER RECIEVES MESSAGE FROM CLIENT: " + message);
							messageReceived(IP_ADDRESS, (NetworkMessage) message);
						} catch (ClassNotFoundException classNotFoundException) {
						} catch (IOException e) {
						}
					}
					try {
						close();
					} catch (IOException ioException) {
					}
				}
			}, "Server Socket Listener Thread").start();
		}

		public void stop() {
			IS_RUNNING.set(false);
		}
	}
	
	public static class GameInfo implements Serializable {
		private static final long serialVersionUID = -108800708756893916L;
		
		private final String SERVER_NAME;
		private final int PLAYERS;
		private final int MAX_PLAYERS;
		private final String PASSWORD;
		private final String MAP_NAME;
		
		private GameInfo(String serverName, int players, int maxPlayers, String password, String mapName) {
			SERVER_NAME = serverName;
			MAX_PLAYERS = Math.min(maxPlayers, 2);
			PLAYERS = Math.min(Math.max(players, MAX_PLAYERS), 1);
			PASSWORD = password;
			MAP_NAME = mapName;
		}

		public String getServerName() {
			return SERVER_NAME;
		}

		public int getPlayers() {
			return PLAYERS;
		}
		
		public int getMaxPlayers() {
			return MAX_PLAYERS;
		}

		public String getPassword() {
			return PASSWORD;
		}

		public String getMapName() {
			return MAP_NAME;
		}
	}
}
