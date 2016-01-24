import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;


public class Console {
	
	private final JTextArea TEXT_AREA = new JTextArea();
	private final JTextField TEXT_FIELD = new JTextField();
	
	private final ArrayList<Command> COMMANDS = new ArrayList<Command>();
	private Command currentCommand = null;
	
	private final ProjectionPlane.ModalInternalFrame CONSOLE_FRAME;
	
	public static final Command LIST_COMMANDS_COMMAND = new Command("List Commands", "Lists all available commands", "commands") {
		@Override
		public boolean execute(Console console, Game game, String input) {
			console.listCommands();
			return true;
		}
	};
	public static final Command CLEAR_COMMAND = new Command("Clear Console", "Clears the console", "clear") {
		@Override
		public boolean execute(Console console, Game game, String input) {
			console.clearText();
			return true;
		}
	};

	public Console(final ProjectionPlane P_PLANE, int width, int height, Command... commands) {
		CONSOLE_FRAME = P_PLANE.addInternalFrame("Console", false, true, true, false, true, true, false);
		CONSOLE_FRAME.getContentPane().setLayout(new BorderLayout());
		TEXT_AREA.setEditable(false);
		CONSOLE_FRAME.getContentPane().add(new JScrollPane(TEXT_AREA, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
		CONSOLE_FRAME.getContentPane().add(TEXT_FIELD, BorderLayout.SOUTH);
		TEXT_FIELD.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				inputReceived(TEXT_FIELD.getText(), P_PLANE.getGame());
				TEXT_FIELD.setText("");
			}
		});
		TEXT_FIELD.getInputMap().put(KeyStroke.getKeyStroke("UP"), "SCROLL UP");
		TEXT_FIELD.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "SCROLL DOWN");
		TEXT_FIELD.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "HIDE");
		TEXT_FIELD.getInputMap().put(KeyStroke.getKeyStroke("BACK_QUOTE"), "LOSE FOCUS");
		TEXT_FIELD.getActionMap().put("SCROLL UP", new AbstractAction() {
			private static final long serialVersionUID = -812244313749521362L;
			@Override
			public void actionPerformed(ActionEvent e) {
				Rectangle currentView = TEXT_AREA.getVisibleRect();
				Rectangle newView = new Rectangle(currentView);
				newView.setLocation(currentView.getLocation().x, currentView.getLocation().y - 3);
				TEXT_AREA.scrollRectToVisible(newView);
			}
		});
		TEXT_FIELD.getActionMap().put("SCROLL DOWN", new AbstractAction() {
			private static final long serialVersionUID = 7409750905327385720L;
			@Override
			public void actionPerformed(ActionEvent e) {
				Rectangle currentView = TEXT_AREA.getVisibleRect();
				Rectangle newView = new Rectangle(currentView);
				newView.setLocation(currentView.getLocation().x, currentView.getLocation().y + 3);
				TEXT_AREA.scrollRectToVisible(newView);
			}
		});
		TEXT_FIELD.getActionMap().put("HIDE", new AbstractAction() {
			private static final long serialVersionUID = 3861373589620329665L;
			@Override
			public void actionPerformed(ActionEvent e) {
				CONSOLE_FRAME.setVisible(false);
			}
		});
		TEXT_FIELD.getActionMap().put("LOSE FOCUS", new AbstractAction() {
			private static final long serialVersionUID = 7373206421671074711L;
			@Override
			public void actionPerformed(ActionEvent e) {
				P_PLANE.requestFocus();
				TEXT_FIELD.setText("");
			}
		});
		CONSOLE_FRAME.setBounds(0, 0, width, height);
		addCommands(commands);
	}
	
	public boolean isVisible() {
		return CONSOLE_FRAME.isVisible();
	}
	
	public void setVisible(boolean aFlag) {
		CONSOLE_FRAME.setVisible(aFlag);
		TEXT_FIELD.setText("");
	}
	
	/**
	 * Adds an array of commands to the console. Added commands can be called in the console. Commands with an existing {@link Command#getExecutionString()} cannot be added.<br><br>
	 * Note: The execution string is not case-sensitive.
	 * @param commands the commands to be added to the console
	 */
	public void addCommands(Command... commands) {
		if (commands != null) {
			synchronized (this) {
				for (Command c : commands) {
					if (c != null && !doesExecutionStringExists(c.getExecutionString().toLowerCase())) {
						COMMANDS.add(c);
					}
				}
			}
		}
	}
	
	public void removeCommands(Command... commands) {
		if (commands != null) {
			synchronized (this) {
				for (Command c : commands) {
					COMMANDS.remove(c);
				}
			}
		}
	}
	
	public boolean hasCommand(Command command) {
		return COMMANDS.contains(command);
	}
	
	public boolean doesExecutionStringExists(String executionString) {
		for (Command c2 : new LinkedList<Command>(COMMANDS)) {
			if (executionString.equals(c2.getExecutionString().toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public synchronized void cancelCommand() {
		this.currentCommand = null;
	}
	
	public boolean hasInputFocus() {
		return TEXT_FIELD.isFocusOwner();
	}

	public void requestInputFocus() {
		TEXT_FIELD.requestFocus();
		TEXT_FIELD.setText("");
	}

	public void appendText(String text) {
		TEXT_AREA.append(text + "\n");
	}
	
	/**
	 * Clears the console text area.
	 */
	public void clearText() {
		TEXT_AREA.setText("");
	}
	
	/**
	 * Appends the available commands to the console.
	 */
	public void listCommands() {
		int nOfCommands = COMMANDS.size() + 2;
		appendText("Commands List (" + nOfCommands + " commands):");
		nOfCommands = 1;
		appendText(nOfCommands++ + ") " + LIST_COMMANDS_COMMAND.getName() + ": " + LIST_COMMANDS_COMMAND.getDescription());
		appendText("Enter: \"" + LIST_COMMANDS_COMMAND.getExecutionString() + "\"");
		appendText(nOfCommands++ + ") " + CLEAR_COMMAND.getName() + ": " + CLEAR_COMMAND.getDescription());
		appendText("Enter: \"" + CLEAR_COMMAND.getExecutionString() + "\"");
		for (Command c : new LinkedList<Command>(COMMANDS)) {
			appendText(nOfCommands++ + ") " + c.getName() + ": " + c.getDescription());
			appendText("Enter: \"" + c.getExecutionString() + "\"");
		}
	}
	
	private void inputReceived(String input, Game game) {
		if (this.currentCommand != null) {
			if (this.currentCommand.execute(this, game, input)) {
				synchronized (this) {
					this.currentCommand = null;
				}
			}
		} else {
			try {
				int commandNumber = Integer.parseInt(input);
				if (commandNumber == 1) {
					input = LIST_COMMANDS_COMMAND.getExecutionString();
				} else if (commandNumber == 2) {
					input = CLEAR_COMMAND.getExecutionString();
				} else if (commandNumber >= 3 && commandNumber < COMMANDS.size() + 3) {
					input = COMMANDS.get(commandNumber - 3).getExecutionString();
				}
			} catch (NumberFormatException e) {
			}
			input = input.toLowerCase();
			boolean commandExecuted = false;
			
			if (input.equals(LIST_COMMANDS_COMMAND.getExecutionString().toLowerCase())) {
				commandExecuted = true;
				LIST_COMMANDS_COMMAND.execute(this, game, input);
			} else if (input.equals(CLEAR_COMMAND.getExecutionString().toLowerCase())) {
				commandExecuted = true;
				CLEAR_COMMAND.execute(this, game, input);
			}
			for (Command c : new LinkedList<Command>(COMMANDS)) {
				if (input.equals(c.getExecutionString().toLowerCase())) {
					commandExecuted = true;
					synchronized (this) {
						this.currentCommand = c;
					}
					if (c.execute(this, game, input)) {
						synchronized (this) {
							this.currentCommand = null;
						}
					}
					break;
				}
			}
			if (!commandExecuted) {
				appendText("ERROR: COMMAND \"" + input + "\" NOT FOUND");
				appendText("Enter \"" + LIST_COMMANDS_COMMAND.getExecutionString().toLowerCase() + "\" for a list of commands");
			}
		}
	}
	
	public static abstract class Command {
		private final String NAME;
		private final String DESCRIPTION;
		private final String EXECUTION_STRING;
		
		public Command(String name, String description, String executionString) {
			if (name == null) throw new IllegalArgumentException("name cannot be null");
			if (description == null) throw new IllegalArgumentException("description cannot be null");
			if (executionString == null) throw new IllegalArgumentException("execution string cannot be null");
			try {
				Integer.parseInt(executionString);
				throw new IllegalArgumentException("execution string cannot be in number format");
			} catch (NumberFormatException e) {
			}
			executionString = executionString.toLowerCase();
			if (LIST_COMMANDS_COMMAND != null && executionString.equals(LIST_COMMANDS_COMMAND.getExecutionString().toLowerCase())) throw new IllegalArgumentException("execution string \"" + LIST_COMMANDS_COMMAND.getExecutionString().toLowerCase() + "\" already exists");
			if (CLEAR_COMMAND != null && executionString.equals(CLEAR_COMMAND.getExecutionString().toLowerCase())) throw new IllegalArgumentException("execution string \"" + CLEAR_COMMAND.getExecutionString().toLowerCase() + "\" already exists");
			NAME = name;
			DESCRIPTION = description;
			EXECUTION_STRING = executionString;
		}
		
		public String getName() {
			return NAME;
		}
		
		public String getDescription() {
			return DESCRIPTION;
		}
		
		/**
		 * Gets the string that is matched to check if the command is called. This string should be constant and always have the same value any time during execution.<br><br>
		 * Note: The returned string is all lower-case.
		 * @return a lower-case string representation used to execute the command
		 */
		public final String getExecutionString() {
			return EXECUTION_STRING;
		}
		
		/**
		 * Executes the command. The console becomes locked on the command. When the command is locked, no other command can be called and all input is directed at the locked command.
		 * The method should return {@code true} to unlock the command after executing. Returning {@code false} keeps the command locked.
		 * {@link Console#cancelCommand()} can be used to unlock the command.
		 * @param console the console receiving the command
		 * @param game the game to be modified
		 * @param input the lower-case input by the user
		 * @return {@code true} to unlock command; otherwise {@code false}
		 */
		public abstract boolean execute(Console console, Game game, String input);
	}
	
	public static abstract class PlayerSelectionCommand extends Command {
		private AtomicBoolean INITIAL_PROMPT = new AtomicBoolean(true);
    	
		public PlayerSelectionCommand(String name, String description, String executionString) {
			super(name, description, executionString);
		}

		@Override
		public boolean execute(Console console, Game game, String input) {
			if (INITIAL_PROMPT.get()) {
				console.appendText("Enter player name");
				INITIAL_PROMPT.set(false);
				return false;
			} else {
				if (input.equals("")) {
					console.appendText("All players selected. Enter new value:");
				} else {
					Player foundPlayer = null;
					for (Player p : game.getPlayers()) {
						if (p.getName().equals(input)) {
							String displayMessage = getPlayerFoundMessage(p);
							if (displayMessage != null) {
								console.appendText(displayMessage);
							}
							foundPlayer = p;
							break;
						}
					}
					if (foundPlayer == null) {
						console.appendText("ERROR: Player (" + input + ") not found. Command canceled");
						INITIAL_PROMPT.set(true);
						return true;
					}
				}
				INITIAL_PROMPT.set(true);
				return true;
			}
		}
		
		/**
		 * Gets the message displayed onto the console after the player searched for is found.
		 * @param foundPlayer the player found
		 * @return the message to be displayed or {@code null} if no message is to be displayed
		 */
		public abstract String getPlayerFoundMessage(Player foundPlayer);
	}
	
	public static abstract class PlayerModificationCommand extends Command {
		private final boolean ALLOW_ALL_PLAYERS_SELECTION;
    	private Player targetedPlayer = null;
		private AtomicBoolean INITIAL_PROMPT = new AtomicBoolean(true);
    	private final AtomicBoolean ALL_PLAYERS_SELECTED = new AtomicBoolean(false);
    	
		public PlayerModificationCommand(String name, String description, String executionString, boolean allowAllPlayersSelection) {
			super(name, description, executionString);
			ALLOW_ALL_PLAYERS_SELECTION = allowAllPlayersSelection;
		}

		@Override
		public boolean execute(Console console, Game game, String input) {
			if (INITIAL_PROMPT.get()) {
				console.appendText("Enter player name" + (ALLOW_ALL_PLAYERS_SELECTION ? " or leave blank to select all players" : ""));
				INITIAL_PROMPT.set(false);
				ALL_PLAYERS_SELECTED.set(false);
				return false;
			} else if (this.targetedPlayer == null && !ALL_PLAYERS_SELECTED.get()) {
				if (input.equals("")) {
					ALL_PLAYERS_SELECTED.set(true);
					console.appendText("All players selected. Enter new value:");
				} else {
					boolean foundPlayer = false;
					for (Player p : game.getPlayers()) {
						if (p.getName().equals(input)) {
							this.targetedPlayer = p;
							String displayMessage = getPlayerFoundMessage(p);
							if (displayMessage != null) {
								console.appendText(displayMessage);
							}
							foundPlayer = true;
							break;
						}
					}
					if (!foundPlayer) {
						console.appendText("ERROR: Player (" + input + ") not found. Command canceled");
						INITIAL_PROMPT.set(true);
						return true;
					}
				}
				return false;
			} else {
				if (ALL_PLAYERS_SELECTED.get()) {
					for (Player p : game.getPlayers()) {
						if (p != null) {
							modify(console, p, input);
						}
					}
				} else {
					modify(console, this.targetedPlayer, input);
				}
				this.targetedPlayer = null;
				INITIAL_PROMPT.set(true);
				ALL_PLAYERS_SELECTED.set(false);
				return true;
			}
		}
		
		/**
		 * Gets the message displayed onto the console after the player searched for is found.
		 * @param foundPlayer the player found
		 * @return the message to be displayed or {@code null} if no message is to be displayed
		 */
		public abstract String getPlayerFoundMessage(Player foundPlayer);
		
		/**
		 * Modifies a player with by a certain input. The command is completed after this method.
		 * @param console the console receiving the command
		 * @param targetPlayer the player to be modified
		 * @param input the input specifying the modification
		 */
		public abstract void modify(Console console, Player targetPlayer, String input);
	}
}
