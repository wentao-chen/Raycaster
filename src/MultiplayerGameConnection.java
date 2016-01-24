import java.io.IOException;
import java.util.ArrayList;


public abstract class MultiplayerGameConnection implements PlayerListener {
	
	private final ConnectionType CONNECTION_TYPE;
	
	public MultiplayerGameConnection(ConnectionType connectionType) {
		CONNECTION_TYPE = connectionType;
	}
	
	public boolean isServer() {
		return CONNECTION_TYPE == ConnectionType.SERVER;
	}
	
	public boolean isClient() {
		return CONNECTION_TYPE == ConnectionType.CLIENT;
	}

	public Game getGame() {
		return getPlayer() != null ? getPlayer().getGame() : null;
	}
	
	public abstract Player getPlayer();
	
	public abstract String getServerIP();
	
	public abstract boolean isRunning();
	
	@Override
	public void playerTeamChanged(Player player) {
		sendMessage(new NetworkMessage(NetworkMessage.MessageType.TEAM_CHANGED, player.getTeam(), player.getID()), true);
	}
	
	@Override
	public void playerMoved(Player player) {
		if (!player.isSpectator() && (isServer() || player.equals(getPlayer()))) {
			sendMessage(new NetworkMessage(NetworkMessage.MessageType.PLAYER_MOVED, new Map.Point3D(player.getLocationX(), player.getLocationY(), player.getBottomHeight()), player.getID()), true);
		}
	}

	public void sendChatMessage(Player player, ChatMessage message) {
		if (getGame() != null) {
			ArrayList<Integer> willReceive = new ArrayList<Integer>();
			for (Player p : getGame().getPlayers()) {
				if (message.willRecieveMessage(p)) {
					willReceive.add(p.getID());
				}
			}
			int[] willRecieve2 = new int[willReceive.size()];
			for (int i = 0; i < willRecieve2.length; i++) {
				willRecieve2[i] = willReceive.get(i);
			}
			sendMessage(new NetworkMessage(NetworkMessage.MessageType.SEND_CHAT_MESSAGE, message.getMessageContent(), player.getID(), willRecieve2), true);
		}
	}
	
	public void shootBullet(Player player, Bullet bullet) {
		sendMessage(new NetworkMessage(NetworkMessage.MessageType.BULLET_SHOT, bullet, player.getID()), true);
	}
	
	public abstract void sendMessage(NetworkMessage networkMessage, boolean requireFullyEstablishedConnection);

	public void closeConnection() throws IOException {
		closeConnection(false);
	}

	/**
	 * Closes the connection.
	 * @param errorTermination {@code true} if the termination is due to an error, otherwise {@code false}
	 * @throws IOException if an I/O error occurs when closing the socket
	 */
	protected abstract void closeConnection(boolean errorTermination) throws IOException;

	public enum ConnectionType {
		SERVER, CLIENT;
	}
}
