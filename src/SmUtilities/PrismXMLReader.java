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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is a customized XML parser for the PRISM Strong Motion project.
 * It only looks at the document object part of the xml file, and only checks for
 * element and text nodes.  It uses recursion to walk each unique path to each
 * text element and makes a tag trail of the element tags along the way.  It
 * builds the tag path by concatenating tag names separated by "/" (forward slash).
 * The actual text value is appended at the end of this string using the 
 * separator "///".  When all text entries have been discovered and their paths
 * saved, each tag trail is separated into a key (all the element tag names separated
 * by "/") and a value (the text value), and put into the config reader object
 * as a key-value pair.
 * Example: PRISM/DataUnitsForCountConversion/DataUnitsCode/DataUnitCode///04
 * would get stored as 
 * key: PRISM/DataUnitsForCountConversion/DataUnitsCode/DataUnitCode
 * value: 04
 * @author jmjones
 */
public class PrismXMLReader {
    private final boolean ignoreWhitespace = true;
    private final boolean ignoreComments = true;
    private final boolean putCDATAIntoText = true;
    private final boolean createEntityRefs = true;
    private final DocumentBuilderFactory dbFactory;
    private final DocumentBuilder dBuilder;
/**
 * This constructor for PrismXMLReader creates a document builder factory and a new
 * document builder using configuration parameters to ignore white space and comments.
 * @throws ParserConfigurationException if unable to create the builder
 */    
    public PrismXMLReader() throws ParserConfigurationException {
        //Set up and configure a document builder
        dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setIgnoringComments(ignoreComments);
        dbFactory.setIgnoringElementContentWhitespace(ignoreWhitespace);
        dbFactory.setExpandEntityReferences(createEntityRefs);
        dbFactory.setCoalescing(!putCDATAIntoText);
        dBuilder = dbFactory.newDocumentBuilder();
    }
/**
 * This method reads in the xml file given in the filename, pulls out the unique
 * paths to each text node, and enters each key-value pair in the config reader
 * object.
 * @param filename The xml file to read and parse
 * @param config The configuration object to hold each key-value pair found in
 * the xml file
 * @throws IOException if unable to read in the file
 * @throws ParserConfigurationException if parser configuration is incorrect
 * @throws SAXException if unable to correctly parse the xml
 */
    public ConfigReader readFile( String filename ) throws IOException, 
                                    ParserConfigurationException,SAXException {
        ArrayList<String> tagtrail = new ArrayList<>();
        String[] keyvalue;
        
        //Parse the xml document directly from the file
        Document doc = dBuilder.parse(new File(filename));
        Element top = doc.getDocumentElement();
        String home = top.getTagName();
        
        //Walk the paths until each text node is reached
        findSubNode(home, top, tagtrail);
        
        //Separate each tag trail into a key, value pair and enter into the 
        //config reader
        ConfigReader config = new ConfigReader(tagtrail.size());
        for (String each : tagtrail) {
            keyvalue = each.split("///");
            config.setConfigValue(keyvalue[0], keyvalue[1]);
        }
        return config;
    }
/**
 * Recursive method to find all the child nodes of each node until a text node
 * is found.  At each level the tag name is appended to the tag path to keep track
 * of the order that nodes are traversed.
 * @param name The current tag path, starting at the document node
 * @param inNode The current node being traversed
 * @param trail The array list to hold each tag trail as it is found
 */
    private void findSubNode( String name, Node inNode, ArrayList<String> trail) {
        StringBuilder result = new StringBuilder();
        String value;
        if ( inNode.hasChildNodes()) {
            NodeList list = inNode.getChildNodes();
            for (int i=0; i<list.getLength(); i++) {
                Node subnode = list.item(i);
                result.setLength(0);
                if (subnode.getNodeType() == Node.ELEMENT_NODE) {
                    result.append(name).append("/").append(subnode.getNodeName());
                    findSubNode(result.toString(), subnode, trail);
                } 
                else if (subnode.getNodeType() == Node.TEXT_NODE) {
                    value = subnode.getTextContent().trim();
                    if (!value.isEmpty()) {
                        trail.add(result.append(name).append("///")
                                                    .append(value).toString());
                    }
                }
            }
        }
    }
}
