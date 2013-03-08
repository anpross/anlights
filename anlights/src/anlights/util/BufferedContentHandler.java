/*
 * Created on 07.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package anlights.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Buffers the calls to characters method so that the method get only called once
 * if characters appear in the SAX Stream.  
 * 
 * @author Andreas Pross
 */
public abstract class BufferedContentHandler extends DefaultHandler {

    StringBuffer sbChar = null;

    public BufferedContentHandler() {
        sbChar = new StringBuffer();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        sbChar.append(ch, start, length);
    }

    private void processChars() throws SAXException {
        if (sbChar.length() > 0) {
            bCharacters(sbChar.toString().toCharArray(), 0, sbChar.toString().length());
            sbChar = new StringBuffer();
        }
    }

    public void startDocument() throws SAXException {
        bStartDocument();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        bStartElement(uri, localName, qName, attributes);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        processChars();
        bEndElement(uri, localName, qName);
    }

    public void endDocument() throws SAXException {
        bEndDocument();
    }

    public abstract void bCharacters(char[] ch, int start, int length) throws SAXException;

    public abstract void bStartDocument() throws SAXException;

    public abstract void bEndDocument() throws SAXException;

    public abstract void bStartElement(String uri, String localName, String qName, Attributes attributes) throws SAXException;

    public abstract void bEndElement(String uri, String localName, String qName) throws SAXException;
}