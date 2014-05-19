/*
 * Copyright (C) 2014 jmjones
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package SmUtilities;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a collection to hold the configuration file parameters.
 * It wraps a Map collection that uses keys and values to store data and retrieve
 * by key.
 * @author jmjones
 */
public class ConfigReader {
    private Map<String, String> contents;
/**
 * Constructor for the Configuration file reader uses a hash map set with
 * defaults of expected upper limit of 16 entries.
 */    
    public ConfigReader() {
        contents = new HashMap<>(); //default of 16 entries
    }
/**
 * Constructor for the configuration file reader that allows setting the
 * number of entries expected from the file.
 * @param length number of entries to be put into the config file reader
 */
    public ConfigReader(int length) {
        contents = new HashMap<>(length);
    }
/**
 * Getter for the value stored for the given key.
 * @param key The key associated with the key-value pair
 * @return The value for the given key
 */
    public String getConfigValue(String key) {
        return contents.get(key);
    }
/**
 * Setter for a key-value pair
 * @param key The key for storage and retrieval
 * @param value The value to store
 */
    public void setConfigValue(String key, String value) {
        String line = contents.put(key, value);
    }
}
