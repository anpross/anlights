package my.anlights;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Arrays;
import java.util.Stack;

import javax.xml.namespace.QName;

import my.anlights.util.BufferedContentHandler;

/**
 * SAX handler to read SSDP xml
 */
public class DescriptionHandler extends BufferedContentHandler {

	private Stack<QName> currElement = new Stack<QName>();

	private boolean isHue = false;
	private String udn;
	private String urlBase;

	@Override
	public void bCharacters(char[] ch, int start, int length)
			throws SAXException {
		String currChars = new String(ch).trim();
		if (currElement.peek().getLocalPart().equals(Constants.DESC_MODEL_NAME)) {

			if (Arrays.binarySearch(Constants.COMPATIBLE_MODELS, currChars) >= 0) {
				isHue = true;
			}
		} else if (currElement.peek().getLocalPart().equals(Constants.DESC_UDN)) {
			udn = currChars;
		} else if (currElement.peek().getLocalPart().equals(Constants.DESC_URL_BASE)) {
			urlBase = currChars;
		}
	}

	@Override
	public void bStartDocument() throws SAXException {
	}

	@Override
	public void bEndDocument() throws SAXException {
	}

	@Override
	public void bStartElement(String uri, String localName, String qName,
	                          Attributes attributes) throws SAXException {
		QName currQName = new QName(uri, localName);
		currElement.push(currQName);


	}

	@Override
	public void bEndElement(String uri, String localName, String qName)
			throws SAXException {
		@SuppressWarnings("unused")
		QName currQName = currElement.pop();
	}

	public boolean isHue() {
		return isHue;
	}

	/**
	 * use UDN to identify different bridges
	 *
	 * @return the UDN
	 */
	public String getUdn() {
		return udn;
	}

	public String getUrlBase() {
		return urlBase;
	}
}
