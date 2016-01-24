import java.io.Serializable;

public final class NetworkMessage implements Serializable {
	private static final long serialVersionUID = 8699380198986849795L;
	
	private final Serializable MESSAGE;
	private final MessageType MESSAGE_TYPE;
	private final Integer SENDER_PLAYER_ID;
	private final long SEND_TIME;
	private final int[] RECIEVING_IDS;

	public NetworkMessage(MessageType messageType) {
		this(messageType, null);
	}

	public NetworkMessage(MessageType messageType, Integer senderPlayerID) {
		this(messageType, null, senderPlayerID);
	}

	public NetworkMessage(MessageType messageType, Serializable message, Integer senderPlayerID) {
		this(messageType, message, senderPlayerID, null);
	}

	public NetworkMessage(MessageType messageType, Serializable message, Integer senderPlayerID, int... recievingIDs) {
		if (messageType == null) throw new IllegalArgumentException("message type cannot be null");
		if (message != null && messageType != null && messageType.getMessageClass() != null && !messageType.getMessageClass().equals(message.getClass())) throw new IllegalArgumentException("Message class must match message type class");
		if (recievingIDs != null && recievingIDs.length == 0) throw new IllegalArgumentException("recieving IDs cannot have 0 elements");
		MESSAGE = messageType != null ? (messageType.getMessageClass() != null ? message : null) : null;
		MESSAGE_TYPE = messageType;
		SENDER_PLAYER_ID = senderPlayerID;
		SEND_TIME = System.currentTimeMillis();
		RECIEVING_IDS = recievingIDs;
	}

	public Object getMessage() {
		return MESSAGE;
	}
	
	public MessageType getMessageType() {
		return MESSAGE_TYPE;
	}
	
	public long getSendTime() {
		return SEND_TIME;
	}
	
	public Integer getSenderPlayerID() {
		return SENDER_PLAYER_ID;
	}
	
	public boolean willPlayerRecieve(Player player) {
		if (RECIEVING_IDS == null) {
			return true;
		} else if (player != null) {
			for (int i : RECIEVING_IDS) {
				if (player.getID() == i) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "NetworkMessage [" + (getMessage() != null ? "MESSAGE=" + getMessage().toString() + ", " : "") + "MESSAGE_TYPE=" + getMessageType() + ", SEND_TIME=" + getSendTime() + "]";
	}

	public enum MessageType {
		TERMINATE_CONNECTION(null),
		FATAL_ERROR(null),
		RETURN_GAME_INFO(MultiplayerGameServer.GameInfo.class), REQUEST_GAME_INFO(null),
		REQUEST_PLAYER(null), RETURN_PLAYER(Player.class),
		TEAM_CHANGED(Team.class),
		PLAYER_MOVED(Map.Point3D.class),
		SEND_CHAT_MESSAGE(ChatMessage.MessageContent.class),
		BULLET_SHOT(Bullet.class);
		
		private final Class<?> MESSAGE_CLASS;
		
		MessageType(Class<?> messageClass) {
			MESSAGE_CLASS = messageClass;
		}
		
		public Class<?> getMessageClass() {
			return MESSAGE_CLASS;
		}
	}
}
