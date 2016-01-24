import java.awt.Color;
import java.io.Serializable;


public class ChatMessage {
	public static final long DISPLAY_DURATION = 4500;
	
	public static final String PUBLIC_CHAT_RECIEVER_NAME = "Public Chat";
	public static final Color PUBLIC_CHAT_MESSAGE_COLOR = Color.WHITE;
	public static final Color SELF_CHAT_MESSAGE_COLOR = Color.WHITE;
	
	private final Player SENDER;
	private final MessageContent CONTENT;
	private final ChatMessageTarget TARGET;
	
	public ChatMessage(Player sender, ChatMessageTarget target, String message) {
		if (sender == null) throw new IllegalArgumentException("sender cannot be null");
		if (message == null) throw new IllegalArgumentException("message cannot be null");
		SENDER = sender;
		CONTENT = new MessageContent(sender, message, target != null ? target.getRecieverName() : PUBLIC_CHAT_RECIEVER_NAME, target != null ? target.getMessageColor() : PUBLIC_CHAT_MESSAGE_COLOR);
		TARGET = target;
	}
	
	public Player getSender() {
		return SENDER;
	}
	
	public MessageContent getMessageContent() {
		return CONTENT;
	}
	
	public boolean willRecieveMessage(Player p) {
		return TARGET == null || TARGET.willRecieveMessage(p);
	}

	public interface ChatMessageTarget {
		public String getRecieverName();
		public Color getMessageColor();
		public boolean willRecieveMessage(Player p);
	}
	
	public static class MessageContent implements Serializable {
		private static final long serialVersionUID = -2432009420024990515L;

		private final int SENDER_ID;
		private final String SENDER_NAME;
		private final String MESSAGE;
		private final String RECIEVER_NAME;
		private final Color MESSAGE_COLOR;
		private Long expireTime = null;
		
		private MessageContent(Player sender, String message, String recieverName, Color messageColor) {
			SENDER_ID = sender.getID();
			SENDER_NAME = sender.getName();
			MESSAGE = message;
			RECIEVER_NAME = recieverName;
			MESSAGE_COLOR = messageColor;
		}
		
		public boolean isSender(Player p) {
			return p != null && p.getID() == SENDER_ID;
		}
		
		public String getSenderName() {
			return SENDER_NAME;
		}
		
		public String getMessage() {
			return MESSAGE;
		}
		
		public String getRecieverName() {
			return RECIEVER_NAME;
		}
		
		public Color getMessageColor() {
			return MESSAGE_COLOR;
		}
		
		public void startExpireTime() {
			if (this.expireTime == null) {
				synchronized (this) {
					this.expireTime = System.currentTimeMillis() + DISPLAY_DURATION;
				}
			}
		}
		
		public boolean isExpired() {
			return this.expireTime != null && System.currentTimeMillis() >= this.expireTime;
		}
	}
}
