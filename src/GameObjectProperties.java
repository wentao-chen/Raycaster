import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;


public class GameObjectProperties<K extends GameObjectProperties.StringIdentifier> {
	private final HashMap<K, String[]> STRING_PROPERTIES = new HashMap<K, String[]>();
	private final HashMap<K, Double[]> DOUBLE_PROPERTIES = new HashMap<K, Double[]>();
	private final HashMap<K, Integer[]> INTEGER_PROPERTIES = new HashMap<K, Integer[]>();
	private final HashMap<K, Boolean[]> BOOLEAN_PROPERTIES = new HashMap<K, Boolean[]>();
	private final HashMap<K, EnumerationProperties> ENUMERATION_PROPERTIES = new HashMap<K, EnumerationProperties>();
	private final HashMap<K, Boolean> IS_ARRAY = new HashMap<K, Boolean>();
	
	GameObjectProperties(PropertyKey<K>... propertyKeys) {
		if (propertyKeys == null) throw new IllegalArgumentException("property keys cannot be null");
		for (PropertyKey<K> k : propertyKeys) {
			if (k.getPropertyType() == PropertyType.STRING) {
				STRING_PROPERTIES.put(k.getPropertyKey(), null);
			} else if (k.getPropertyType() == PropertyType.DOUBLE) {
				DOUBLE_PROPERTIES.put(k.getPropertyKey(), null);
			} else if (k.getPropertyType() == PropertyType.INTEGER) {
				INTEGER_PROPERTIES.put(k.getPropertyKey(), null);
			} else if (k.getPropertyType() == PropertyType.BOOLEAN) {
				BOOLEAN_PROPERTIES.put(k.getPropertyKey(), null);
			} else if (k.getPropertyType() == PropertyType.ENUMERATION) {
				ENUMERATION_PROPERTIES.put(k.getPropertyKey(), new EnumerationProperties(k.getPossibleValues()));
			}
			IS_ARRAY.put(k.getPropertyKey(), k.isArray());
		}
	}
	
	public Set<K> getAllKeys() {
		return IS_ARRAY.keySet();
	}
	
	public String getStringValue(K k) {
		String[] stringProperties = getStringProperties(k);
		if (stringProperties != null) {
			String value = "";
			for (String s : stringProperties) {
				value += s + ",";
			}
			return value.lastIndexOf(",") == value.length() - 1 ? value.substring(0, value.length() - 1) : value;
		}
		Double[] doubleProperties = getDoubleProperties(k);
		if (doubleProperties != null) {
			String value = "";
			for (Double d : doubleProperties) {
				value += d.toString() + ",";
			}
			return value.lastIndexOf(",") == value.length() - 1 ? value.substring(0, value.length() - 1) : value;
		}
		Integer[] integerProperties = getIntegerProperties(k);
		if (integerProperties != null) {
			String value = "";
			for (Integer i : integerProperties) {
				value += i.toString() + ",";
			}
			return value.lastIndexOf(",") == value.length() - 1 ? value.substring(0, value.length() - 1) : value;
		}
		Boolean[] booleanProperties = getBooleanProperties(k);
		if (booleanProperties != null) {
			String value = "";
			for (Boolean b : booleanProperties) {
				value += (b ? "TRUE" : "FALSE") + ",";
			}
			return value.lastIndexOf(",") == value.length() - 1 ? value.substring(0, value.length() - 1) : value;
		}
		EnumerationProperties enumerationProperty = getEnumerationProperties(k);
		if (enumerationProperty != null) {
			String value = "";
			for (int i = 0; i < enumerationProperty.getArraySize(); i++) {
				value += enumerationProperty.getCurrentValueIndex(i) + ",";
			}
			return value.lastIndexOf(",") == value.length() - 1 ? value.substring(0, value.length() - 1) : value;
		}
		return null;
	}
	
	public Integer getEnumerationPropertyIndex(K k) {
		return ENUMERATION_PROPERTIES.get(k) != null && ENUMERATION_PROPERTIES.get(k).getArraySize() > 0 ? ENUMERATION_PROPERTIES.get(k).getCurrentValueIndex(0) : null;
	}
	
	public String stringProperty(K k) {
		return STRING_PROPERTIES.get(k) != null && STRING_PROPERTIES.get(k).length > 0 ? STRING_PROPERTIES.get(k)[0] : null;
	}
	
	public double doubleProperty(K k) {
		return DOUBLE_PROPERTIES.get(k) != null && DOUBLE_PROPERTIES.get(k).length > 0 ? DOUBLE_PROPERTIES.get(k)[0] : null;
	}
	
	public int intProperty(K k) {
		return INTEGER_PROPERTIES.get(k) != null && INTEGER_PROPERTIES.get(k).length > 0 ? INTEGER_PROPERTIES.get(k)[0] : null;
	}
	
	public boolean booleanProperty(K k) {
		return BOOLEAN_PROPERTIES.get(k) != null && BOOLEAN_PROPERTIES.get(k).length > 0 ? BOOLEAN_PROPERTIES.get(k)[0] : null;
	}
	
	public String[] getStringProperties(K k) {
		return STRING_PROPERTIES.get(k);
	}
	
	public Double[] getDoubleProperties(K k) {
		return DOUBLE_PROPERTIES.get(k);
	}
	
	public Integer[] getIntegerProperties(K k) {
		return INTEGER_PROPERTIES.get(k);
	}
	
	public Boolean[] getBooleanProperties(K k) {
		return BOOLEAN_PROPERTIES.get(k);
	}
	
	public EnumerationProperties getEnumerationProperties(K k) {
		return ENUMERATION_PROPERTIES.get(k);
	}
	
	public K getKeyFromIdentifier(String identifier) {
		for (K k : getAllKeys()) {
			if ((k.getIdentifier() == null && identifier == null) || (k.getIdentifier() != null && k.getIdentifier().toLowerCase().equals(identifier))) {
				return k;
			}
		}
		return null;
	}
	
	public boolean set(K key, String value) {
		if (key == null) {
			return false;
		}
		String[] values = value.split(",");
		if (IS_ARRAY.get(key) != null && !IS_ARRAY.get(key)) {
			values = new String[]{value};
		}
		if (STRING_PROPERTIES.containsKey(key)) {
			STRING_PROPERTIES.put(key, values);
			return true;
		}
		if (DOUBLE_PROPERTIES.containsKey(key)) {
			try {
				Double[] doubleValues = new Double[values.length];
				for (int i = 0; i < doubleValues.length; i++) {
					doubleValues[i] = Double.valueOf(values[i]);
				}
				DOUBLE_PROPERTIES.put(key, doubleValues);
				return true;
			} catch (NumberFormatException e) {
			}
		}
		if (INTEGER_PROPERTIES.containsKey(key)) {
			try {
				Integer[] integerValues = new Integer[values.length];
				for (int i = 0; i < integerValues.length; i++) {
					integerValues[i] = Integer.valueOf(values[i]);
				}
				INTEGER_PROPERTIES.put(key, integerValues);
				return true;
			} catch (NumberFormatException e) {
			}
		}
		if (BOOLEAN_PROPERTIES.containsKey(key)) {
			Boolean[] booleanValues = new Boolean[values.length];
			for (int i = 0; i < booleanValues.length; i++) {
				booleanValues[i] = values[i].toLowerCase().startsWith("t") || (values[i].contains("1") && !values[i].contains("0"));
			}
			BOOLEAN_PROPERTIES.put(key, booleanValues);
			return true;
		}
		if (ENUMERATION_PROPERTIES.containsKey(key)) {
			for (int i = 0; i < values.length; i++) {
				ENUMERATION_PROPERTIES.get(key).setToCurrentValue(i, values[i]);
			}
			return true;
		}
		return false;
	}
	
	public boolean saveProperties(File saveFile) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(saveFile));
			for (K k : getAllKeys()) {
				out.write(k.getIdentifier() + ":" + getStringValue(k));
				out.newLine();
			}
			return true;
        } catch (IOException e) {
        } finally {
        	if (out != null) {
        		try {
					out.close();
				} catch (IOException e) {
				}
        	}
        }
		return false;
	}
	
	public static <K extends GameObjectProperties.StringIdentifier> GameObjectProperties<K> loadGameObject(String filePath, GameObjectProperties.PropertyKey<K>... propertyKeys) {
		GameObjectProperties<K> properties = new GameObjectProperties<K>(propertyKeys);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();
            while (line != null) {
                if (line.indexOf(":") < 0 || line.length() <= line.indexOf(":") + 1) {
                	line = br.readLine();
                	continue;
                }
                String identifier = line.substring(0, line.indexOf(":")).toLowerCase();
                String value = line.substring(line.indexOf(":") + 1);
                K key = properties.getKeyFromIdentifier(identifier);
                if (key != null) {
                    properties.set(key, value);
                }
                line = br.readLine();
            }
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			if (br != null) {
				try {
					br.close();
		            return properties;
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
	
	public interface StringIdentifier {
		public String getIdentifier();
	}
	
	public interface PropertyKey<K extends StringIdentifier> {
		public String getDescription();
		public PropertyType getPropertyType();
		public K getPropertyKey();
		public boolean isArray();
		public String getSaveFileDirectoryPath();
		/**
		 * Used iff {@link #getPropertyType()} {@code == } {@link PropertyType#ENUMERATION}. Returns the values yThe result should be consistent.
		 * @return the only possible values associated with this key
		 */
		public Object[] getPossibleValues();
	}
	
	public static class DefaultPropertyKey2<K extends StringIdentifier> implements PropertyKey<K> {
		private final String DESCRIPTION;
		private final PropertyType PROPERTY_TYPE;
		private final K PROPERTY_KEY;
		private final boolean IS_ARRAY;
		private final String SAVE_FILE_DIRECTORY_PATH;
		private final Object[] POSSIBLE_VALUES;
		
		public DefaultPropertyKey2(PropertyType propertyType, K key, String description, boolean isArray, String saveFileDirectoryPath, Object... possibleValues) {
			if (propertyType != PropertyType.ENUMERATION && possibleValues != null && possibleValues.length > 0) throw new IllegalArgumentException("Non-enumeration property types cannot specify possible values");
			if (propertyType == PropertyType.ENUMERATION && (possibleValues == null || possibleValues.length < 2)) throw new IllegalArgumentException("Enumeration property types must specify at least 2 possible values");
			PROPERTY_TYPE = propertyType;
			PROPERTY_KEY = key;
			DESCRIPTION = description;
			IS_ARRAY = isArray;
			SAVE_FILE_DIRECTORY_PATH = PropertyType.STRING.equals(getPropertyType()) ? saveFileDirectoryPath : null;
			POSSIBLE_VALUES = possibleValues;
		}
		
		@Override
		public String getDescription() {
			return DESCRIPTION;
		}

		@Override
		public PropertyType getPropertyType() {
			return PROPERTY_TYPE;
		}

		@Override
		public K getPropertyKey() {
			return PROPERTY_KEY;
		}

		@Override
		public boolean isArray() {
			return IS_ARRAY;
		}

		@Override
		public String getSaveFileDirectoryPath() {
			return SAVE_FILE_DIRECTORY_PATH;
		}

		@Override
		public Object[] getPossibleValues() {
			return POSSIBLE_VALUES;
		}
	}
	
	public enum PropertyType {
		STRING, DOUBLE, INTEGER, BOOLEAN, ENUMERATION;
	}
	
	public static class EnumerationProperties {
		
		private final Object[] POSSIBLE_VALUES;
		private Integer[] currentValueIndices = new Integer[1];
		
		public EnumerationProperties(Object... possibleValues) {
			if (possibleValues == null || possibleValues.length < 2) throw new IllegalArgumentException("Enumeration Property must specify at least 2 possible values");
			POSSIBLE_VALUES = possibleValues;
		}
		
		public Integer getCurrentValueIndex(int i) {
			return this.currentValueIndices[i];
		}
		
		public Object getCurrentValue(int i) {
			return this.currentValueIndices[i] != null ? POSSIBLE_VALUES[this.currentValueIndices[i]] : null;
		}
		
		public int getArraySize() {
			return this.currentValueIndices != null ? this.currentValueIndices.length : 0;
		}
		
		public synchronized void setArraySize(int size) {
			this.currentValueIndices = new Integer[size];
		}
		
		public void setToCurrentValue(int i, String value) {
			if (value != null) {
				try {
					synchronized (this) {
						this.currentValueIndices[i] = Integer.parseInt(value);
					}
					return;
				} catch (NumberFormatException e) {
				}
				for (int i2 = 0; i2 < POSSIBLE_VALUES.length; i2++) {
					if (value.equals(POSSIBLE_VALUES[i2].toString())) {
						synchronized (this) {
							this.currentValueIndices[i] = i2;
						}
						return;
					}
				}
			}
			synchronized (this) {
				this.currentValueIndices[i] = null;
			}
		}
	}
	
	public static class InvalidPropertiesException extends Exception {
		private static final long serialVersionUID = 8759201103752548278L;

		public InvalidPropertiesException() {
			super();
		}

		public InvalidPropertiesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

		public InvalidPropertiesException(String message, Throwable cause) {
			super(message, cause);
		}

		public InvalidPropertiesException(String message) {
			super(message);
		}

		public InvalidPropertiesException(Throwable cause) {
			super(cause);
		}
	}
}
