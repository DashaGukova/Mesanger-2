package saxPars;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXPars extends DefaultHandler {
    private String thisElement = "";
    private String sender = "";
    private String receiver = "";
    private String time = "";
    private String text = "";

    @Override
    public void startDocument() throws SAXException {
        //System.out.println("startDocument");
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        thisElement = qName;
        //System.out.println("startElement");
        //System.out.println(" " + qName);
        time = atts.getValue(1);
        sender = atts.getValue(2);
        receiver = atts.getValue(3);
        text = atts.getValue(4);
        //System.out.println(time+sender+receiver+text);
    }

    public String getTime() {
        return this.time;
    }

    public String getSender() {
        return this.sender;
    }

    public String getReceiver() {
        return this.receiver;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        thisElement = "";
        //System.out.println("endElement");
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        //if (thisElement.equals("id")) {
        //   doc.setId(new Integer(new String(ch, start, length)));
        //}
    }

    @Override
    public void endDocument() {
       // System.out.println("endDocument");
    }
}
