import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


class NewServerDialog {
	
	private static MultiplayerGameServer server = null;
	
	public static void showNewServerDialog(ProjectionPlane pPlane, final ProjectionPlane.DialogDisposedAction<MultiplayerGameServer> ACTION) {
		final ProjectionPlane.ModalInternalFrame DIALOG = pPlane.addInternalFrame("New Server", true, true, true, true, true, true, true, new ProjectionPlane.DialogDisposedSimpleAction() {
			@Override
			public void dialogDisposed() {
				ACTION.dialogDisposed(NewServerDialog.server);
			}
		});
		DIALOG.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		DIALOG.getContentPane().setLayout(new BorderLayout());

		final JButton START_BUTTON = new JButton("Start");
		START_BUTTON.setEnabled(false);
		JPanel mainPanel = new JPanel(new BorderLayout());
		GridLayout layout = new GridLayout(0, 1);
		layout.setVgap(10);
		JPanel labelsPanel = new JPanel(layout);
		JPanel inputsPanel = new JPanel(layout);
		labelsPanel.add(new JLabel("Server Name:", SwingConstants.RIGHT));
		labelsPanel.add(new JLabel("Map:", SwingConstants.RIGHT));
		labelsPanel.add(new JLabel("Max. Players:", SwingConstants.RIGHT));
		labelsPanel.add(new JLabel("Password:", SwingConstants.RIGHT));
		final JTextField SERVER_NAME_TEXT_FIELD = new JTextField();
		SERVER_NAME_TEXT_FIELD.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				START_BUTTON.setEnabled(SERVER_NAME_TEXT_FIELD.getText().length() > 0);
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				insertUpdate(e);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				insertUpdate(e);
			}
		});
		inputsPanel.add(SERVER_NAME_TEXT_FIELD);
		final JComboBox<DefaultMap> MAP_COMBO_BOX = new JComboBox<DefaultMap>(new DefaultMap[]{DefaultMap.ICE_WORLD, DefaultMap.DE_DUST2});
		inputsPanel.add(MAP_COMBO_BOX);
		JComboBox<Integer> playersComboBox = new JComboBox<Integer>(new Integer[]{2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26});
		inputsPanel.add(playersComboBox);
		final JTextField PASSWORD_TEXT_FIELD = new JTextField();
		inputsPanel.add(PASSWORD_TEXT_FIELD);
		
		JPanel startPanel = new JPanel(new BorderLayout());
		StartAction startAction = new StartAction(DIALOG, SERVER_NAME_TEXT_FIELD, PASSWORD_TEXT_FIELD, MAP_COMBO_BOX);
		START_BUTTON.addActionListener(startAction);
		startPanel.add(START_BUTTON, BorderLayout.EAST);
		
		mainPanel.add(labelsPanel, BorderLayout.WEST);
		mainPanel.add(inputsPanel, BorderLayout.CENTER);
		DIALOG.getContentPane().add(mainPanel, BorderLayout.CENTER);
		DIALOG.getContentPane().add(startPanel, BorderLayout.SOUTH);
		
		DIALOG.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESC"), "EXIT");
		DIALOG.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "START");
		DIALOG.getRootPane().getActionMap().put("EXIT", new ExitAction(DIALOG));
		DIALOG.getRootPane().getActionMap().put("START", startAction);
		
		DIALOG.pack();
		DIALOG.setSize(400, 200);
		SERVER_NAME_TEXT_FIELD.requestFocusInWindow();
	}
	
	private static class StartAction extends AbstractAction implements ActionListener {
		private static final long serialVersionUID = -8804788701425025117L;
		private final ProjectionPlane.ModalInternalFrame DIALOG;
		private final JTextField SERVER_NAME_TEXT_FIELD;
		private final JTextField PASSWORD_TEXT_FIELD;
		private final JComboBox<DefaultMap> MAP_COMBO_BOX;
		private StartAction(ProjectionPlane.ModalInternalFrame dialog, JTextField serverNameTextField, JTextField passwordTextField, JComboBox<DefaultMap> mapComboBox) {
			DIALOG = dialog;
			SERVER_NAME_TEXT_FIELD = serverNameTextField;
			PASSWORD_TEXT_FIELD = passwordTextField;
			MAP_COMBO_BOX = mapComboBox;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			if (SERVER_NAME_TEXT_FIELD.getText().length() > 0) {
				String password = PASSWORD_TEXT_FIELD.getText();
				synchronized (NewServerDialog.class) {
					NewServerDialog.server = new MultiplayerGameServer(SERVER_NAME_TEXT_FIELD.getText(), password.length() > 0 ? password : null, MAP_COMBO_BOX.getItemAt(MAP_COMBO_BOX.getSelectedIndex()));
				}
				DIALOG.dispose();
			}
		}
		
	}
	
	private static class ExitAction extends AbstractAction {
		private static final long serialVersionUID = 1352798495269681893L;
		private final ProjectionPlane.ModalInternalFrame DIALOG;
		private ExitAction(ProjectionPlane.ModalInternalFrame dialog) {
			DIALOG = dialog;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			DIALOG.dispose();
		}
	}
}
