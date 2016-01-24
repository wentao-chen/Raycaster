import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;


public abstract class SmartAdjuster<T extends Number & Comparable<T>> {
	
	private boolean activated = false;
	private T minValue = null;
	private T maxValue = null;
	private Integer priority = null;
	private T preferredValue = null;
	private Double minFPS = null;
	
	private SmartAdjuster(T minValue, T maxValue) {
		this(minValue, maxValue, null, null, null);
	}
	
	private SmartAdjuster(T minValue, T maxValue, Integer priority, T preferredValue, Double minFPS) {
		if (minValue == null) throw new IllegalArgumentException("min value cannot be null");
		if (maxValue == null) throw new IllegalArgumentException("max value cannot be null");
		if (minFPS != null && minFPS <= 0) throw new IllegalArgumentException("min FPS must be greater than 0");
		if (preferredValue != null && minValue.compareTo(preferredValue) > 0) throw new IllegalArgumentException("min value cannot be greater than preferred value");
		if (preferredValue != null && preferredValue.compareTo(maxValue) > 0) throw new IllegalArgumentException("preferred value cannot be greater than max value");
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.priority = priority;
		this.preferredValue = preferredValue;
		this.minFPS = minFPS;
		activate();
	}
	
	public boolean activate(T minValue, T maxValue, int priority, T preferredValue, double minFPS) {
		if (preferredValue == null) throw new IllegalArgumentException("preferred min value cannot be null");
		if (minFPS <= 0) throw new IllegalArgumentException("min FPS must be greater than 0");
		if (minValue.compareTo(preferredValue) > 0) throw new IllegalArgumentException("min value cannot be greater than preferred value");
		if (preferredValue.compareTo(maxValue) > 0) throw new IllegalArgumentException("preferred value cannot be greater than max value");
		setMinMax(minValue, maxValue);
		this.priority = priority;
		this.preferredValue = preferredValue;
		this.minFPS = minFPS;
		return activate();
	}
	
	public void setMinMax(T minValue, T maxValue) {
		if (minValue == null) throw new IllegalArgumentException("min value cannot be null");
		if (maxValue == null) throw new IllegalArgumentException("max value cannot be null");
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	public boolean isActivated() {
		return this.activated;
	}
	
	public boolean activate() {
		this.activated = this.minValue != null && this.maxValue != null && this.priority != null && this.preferredValue != null && this.minFPS != null;
		return isActivated();
	}
	
	public void deActivate() {
		this.activated = false;
	}
	
	public T getMinValue() {
		return this.minValue;
	}
	
	public T getMaxValue() {
		return this.maxValue;
	}
	
	public Integer getPriority() {
		return this.priority;
	}
	
	public T getPreferredValue() {
		return this.preferredValue;
	}
	
	public Double getMinFPS() {
		return this.minFPS;
	}
	
	protected abstract JSlider createGetPreferredValueSlider(T preferredMinValue, T min, T max);
	
	protected abstract T getSliderValue(int sliderValue);
	
	public void showEditorDialog() {
		JInternalFrame dialog = ProjectionPlane.getSingleton().addInternalFrame("Smart Adjuster", true, true, true, true, true, true, true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getContentPane().setLayout(new GridLayout(0, 1));
		
		JPanel priorityPanel = new JPanel(new BorderLayout());
		priorityPanel.setBorder(BorderFactory.createTitledBorder("Priority"));
		final JSpinner PRIORITY_SPINNER = new JSpinner(new SpinnerNumberModel(this.priority != null ? this.priority : 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
		priorityPanel.add(PRIORITY_SPINNER, BorderLayout.CENTER);
		dialog.getContentPane().add(priorityPanel);

		JPanel preferredValuePanel = new JPanel(new BorderLayout());
		preferredValuePanel.setBorder(BorderFactory.createTitledBorder("Preferred Value"));
		final JSlider PREFERRED_VALUE_SPINNER = createGetPreferredValueSlider(this.preferredValue, this.minValue, this.maxValue);
		preferredValuePanel.add(PREFERRED_VALUE_SPINNER, BorderLayout.CENTER);
		dialog.getContentPane().add(preferredValuePanel);
		
		JPanel targetFPSPanel = new JPanel(new BorderLayout());
		targetFPSPanel.setBorder(BorderFactory.createTitledBorder("Target Minimum Frames per Second"));
		final JSpinner TARGET_FPS_SPINNER = new JSpinner();
		Dimension targetFPSSpinnerSize = TARGET_FPS_SPINNER.getPreferredSize();
		TARGET_FPS_SPINNER.setModel(new SpinnerNumberModel(this.minFPS != null ? this.minFPS : Main.TARGET_FPS, 0, Double.MAX_VALUE, 1));
		TARGET_FPS_SPINNER.setPreferredSize(targetFPSSpinnerSize);
		targetFPSPanel.add(TARGET_FPS_SPINNER, BorderLayout.CENTER);
		dialog.getContentPane().add(targetFPSPanel);
		
		dialog.addInternalFrameListener(new InternalFrameListener() {
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
				SmartAdjuster.this.priority = (Integer) PRIORITY_SPINNER.getValue();
				SmartAdjuster.this.preferredValue = getSliderValue(PREFERRED_VALUE_SPINNER.getValue());
				SmartAdjuster.this.minFPS = (Double) TARGET_FPS_SPINNER.getValue();
			}
			@Override
			public void internalFrameActivated(InternalFrameEvent e) {
			}
		});
		dialog.pack();
	}

	public JPanel createEmbeddedAdjusterPanel(final ActionListener ACTIVATED_LISTENER) {
		JPanel panel = new JPanel(new BorderLayout());
		final JCheckBox CHECK_BOX = new JCheckBox();
		CHECK_BOX.setSelected(isActivated());
		final JButton BUTTON = new JButton("Smart Adjust");
		BUTTON.setEnabled(isActivated());
		CHECK_BOX.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (CHECK_BOX.isSelected()) {
					if (!activate()) {
						showEditorDialog();
						activate();
					}
				} else {
					deActivate();
				}
				BUTTON.setEnabled(CHECK_BOX.isSelected());
				if (ACTIVATED_LISTENER != null) {
					ACTIVATED_LISTENER.actionPerformed(e);
				}
			}
		});
		panel.add(CHECK_BOX, BorderLayout.WEST);
		BUTTON.setOpaque(false);
		BUTTON.setContentAreaFilled(false);
		BUTTON.setBorderPainted(false);
		BUTTON.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showEditorDialog();
			}
		});
		panel.add(BUTTON, BorderLayout.CENTER);
		return panel;
	}
	
	public abstract boolean adjust();
	
	public abstract boolean forceAdjust();
	
	public static abstract class IntegerPrecision extends SmartAdjuster<Integer> {
		
		public IntegerPrecision(int minValue, int maxValue) {
			super(minValue, maxValue);
		}
		
		public IntegerPrecision(int minValue, int maxValue, int priority, int preferredMinValue, double minFPS) {
			super(minValue, maxValue, priority, preferredMinValue, minFPS);
		}

		@Override
		protected JSlider createGetPreferredValueSlider(Integer preferredMinValue, Integer min, Integer max) {
			JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, preferredMinValue != null ? preferredMinValue : 0);
			slider.setMajorTickSpacing((max - min) / 10);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			return slider;
		}

		@Override
		protected Integer getSliderValue(int sliderValue) {
			return sliderValue;
		}
	}

	
	public static abstract class DoublePrecision extends SmartAdjuster<Double> {
		
		public DoublePrecision(double minValue, double maxValue) {
			super(minValue, maxValue);
		}
		
		public DoublePrecision(double minValue, double maxValue, int priority, double preferredMinValue, double minFPS) {
			super(minValue, maxValue, priority, preferredMinValue, minFPS);
		}

		@Override
		protected JSlider createGetPreferredValueSlider(Double preferredMinValue, Double min, Double max) {
			JSlider slider = new JSlider(JSlider.HORIZONTAL, (int) Math.round(min), (int) Math.round(max), preferredMinValue != null ? (int) Math.round(preferredMinValue) : 0);
			slider.setMajorTickSpacing((int) Math.round((max - min) / 10));
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			return slider;
		}

		@Override
		protected Double getSliderValue(int sliderValue) {
			return (double) sliderValue;
		}
	}
}
