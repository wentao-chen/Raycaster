import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class RandomNames {
	
	private final Set<String> NAMES = new HashSet<String>();
	private final Set<String> USED_NAMES = new HashSet<String>();
	
	public RandomNames(String path) {
		try {
			List<String> names = Files.readAllLines(Paths.get(path), Charset.forName("UTF-8"));
			if (names != null) {
				for (String s : names) {
					NAMES.add(s);
				}
			}
		} catch (IOException e) {
		}
	}
	
	public void addNames(String... names) {
		if (names != null) {
			for (String s : names) {
				NAMES.add(s);
			}
		}
	}
	
	public void removeNames(String... names) {
		if (names != null) {
			for (String s : names) {
				NAMES.remove(s);
			}
		}
	}

	public int getNumberOfUnusedNames() {
		return NAMES.size();
	}

	public int getNumberOfUsedNames() {
		return USED_NAMES.size();
	}
	
	public boolean isUnusedName(String name) {
		return NAMES.contains(name);
	}
	
	public boolean isUsedName(String name) {
		return USED_NAMES.contains(name);
	}
	
	public void unUseAllNames() {
		for (String s : USED_NAMES) {
			NAMES.add(s);
		}
		USED_NAMES.clear();
	}
	
	public String[] getUnusedNames() {
		return NAMES.toArray(new String[NAMES.size()]);
	}
	
	public String[] getUsedNames() {
		return USED_NAMES.toArray(new String[USED_NAMES.size()]);
	}
	
	public String getRandomName() {
		int i = (int) Math.floor(Math.random() * (getNumberOfUnusedNames() + getNumberOfUsedNames()));
		Iterator<String> iterator = null;
		if (i < getNumberOfUnusedNames()) {
			iterator = NAMES.iterator();
		} else {
			iterator = USED_NAMES.iterator();
			i -= getNumberOfUnusedNames();
		}
		while (iterator.hasNext()) {
			if (i-- <= 0) {
				return iterator.next();
			}
			iterator.next();
		}
		return null;
	}
	
	public String getRandomUnusedName() {
		int i = (int) Math.floor(Math.random() * getNumberOfUnusedNames());
		Iterator<String> iterator = NAMES.iterator();
		while (iterator.hasNext()) {
			if (i-- <= 0) {
				return iterator.next();
			}
			iterator.next();
		}
		return null;
	}
	
	public String getRandomUsedName() {
		int i = (int) Math.floor(Math.random() * getNumberOfUsedNames());
		Iterator<String> iterator = USED_NAMES.iterator();
		while (iterator.hasNext()) {
			if (i-- <= 0) {
				return iterator.next();
			}
			iterator.next();
		}
		return null;
	}
	
	public String getRandomUniqueName() {
		int i = (int) Math.floor(Math.random() * getNumberOfUnusedNames());
		Iterator<String> iterator = NAMES.iterator();
		while (iterator.hasNext()) {
			if (i-- <= 0) {
				String name = iterator.next();
				iterator.remove();
				USED_NAMES.add(name);
				return name;
			}
			iterator.next();
		}
		return null;
	}
}
