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
 * by key.  It is set up according to the singleton design pattern so the keys
 * and values can be loaded once and accessed by different classes as needed.
 * The actual reading in of the values from the file is done with the 
 * PrismXMLReader, which then loads the keys and values into the ConfigReader.
 * @author jmjones
 */
public class ConfigReader {
    private Map<String, String> contents;
    public final static ConfigReader INSTANCE = new ConfigReader();
/**
 * Constructor for the configuration file reader is private as part of the
 * singleton implementation.  Access to the reader is through the INSTANCE 
 * variable:  ConfigReader config = ConfigReader.INSTANCE.
 */
    private ConfigReader() {
        contents = new HashMap<>();
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
