package com.dreamcloud.esa.annoatation.handler;

import com.dreamcloud.esa.debug.ArticleFoundException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract public class XmlReadingHandler extends DefaultHandler implements AutoCloseable {
    private String documentTag = "doc";
    private ArrayList<String> allowedTags = new ArrayList<>();

    private StringBuilder content;
    private boolean inDoc = false;
    private int docsRead = 0;
    private Map<String, String> xmlFields;
    private String currentTag;

    public XmlReadingHandler() {
        this.allowDefaultTags();
    }

    public void allowDefaultTags() {
        this.allowedTags.add("title");
        this.allowedTags.add("text");
        this.allowedTags.add("incomingLinks");
        this.allowedTags.add("outgoingLinks");
        this.allowedTags.add("terms");
    }

    public void allowTag(String tag) {
        this.allowedTags.add(tag);
    }

    public void clearAllowedTags() {
        this.allowedTags.clear();
    }

    public void reset() {
        this.content = null;
        this.docsRead = 0;
        this.inDoc = false;
        this.xmlFields = null;
        this.currentTag = null;
    }

    public void setDocumentTag(String documentTag) {
        this.documentTag = documentTag;
    }

    public int getDocsRead() {
        return this.docsRead;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (documentTag.equals(localName)) {
            inDoc = true;
            xmlFields = new ConcurrentHashMap<>();
        } else if (inDoc && this.allowedTags.contains(localName)) {
            content = new StringBuilder();
            currentTag = localName;
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (inDoc) {
            if (documentTag.equals(localName)) {
                inDoc = false;
                docsRead++;
                this.handleDocument(xmlFields);
            } else {
                xmlFields.put(currentTag, content.toString());
            }
        }
    }

    public void characters(char[] ch, int start, int length) {
        if (content != null) {
            content.append(ch, start, length);
            //Probably not needed, but if you had <td>foo</td><td>bar</td> you'd end up with "foobar" as a token
            //since we don't create a new string builder unless we see a tag we are looking for (title/text/etc)
            content.append(' ');
        }
    }

    public void logMessage(String message) {
        if (docsRead % 1000 == 0) {
            System.out.println(message);
        }
    }

    public void close() throws Exception {

    }

    abstract public void handleDocument(Map<String, String> xmlFields) throws SAXException;
}