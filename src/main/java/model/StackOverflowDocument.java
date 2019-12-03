package model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StackOverflowDocument {
    String title;
    String questionBody;
    String tags;
    List<String> answers = new ArrayList<String>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuestionBody() {
        return questionBody;
    }

    public void setQuestionBody(String questionBody) {
        this.questionBody = questionBody;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    /**
     * Create from XML file
     * @param xmlFile XML file to be parsed
     */
    public StackOverflowDocument(File xmlFile) throws DocumentParsingException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document doc = null;

        try {
            builder = factory.newDocumentBuilder();
            InputStream content = wrapWithRoot(xmlFile);
            doc = builder.parse(content);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new DocumentParsingException("Exception when parsing XML file");
        }

        // Parse question
        Node questionNode = doc.getElementsByTagName("question").item(0);
        if (questionNode.getNodeType() == Node.ELEMENT_NODE) {
            Element questionElem = (Element) questionNode;
            Node a = questionElem.getElementsByTagName("Title").item(0);
            title = questionElem.getElementsByTagName("Title").item(0).getTextContent();
            questionBody = questionElem.getElementsByTagName("Body").item(0).getTextContent();
            tags = questionElem.getElementsByTagName("Tags").item(0).getTextContent();
        } else {
            throw new DocumentParsingException("Could not parse question tag");
        }

        // Parse answers
        NodeList answers = doc.getElementsByTagName("answer");
        for (int i = 0; i < answers.getLength(); i++) {
            Node answer = answers.item(i);
            if (answer.getNodeType() == Node.ELEMENT_NODE) {
                Element answerElem = (Element) answer;
                String body = answerElem.getElementsByTagName("Body").item(0).getTextContent();
                this.answers.add(body);
            }
        }
    }

    private InputStream wrapWithRoot(File xmlFile) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(xmlFile);
        List<InputStream> streams = Arrays.asList(
                new ByteArrayInputStream("<root>".getBytes()),
                fis,
                new ByteArrayInputStream("</root>".getBytes())
        );

        return new SequenceInputStream(Collections.enumeration(streams));
    }
}
