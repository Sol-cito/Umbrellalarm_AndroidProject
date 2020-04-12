package com.example.umbrellaapplicationproject;

import android.os.AsyncTask;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class BackgroundThreadForXML extends AsyncTask<Integer, Integer, Document> {
    /* return document which is originally RSS data from the weather cast website below */
    @Override
    protected Document doInBackground(Integer... integers) {
        Document doc = null;
        try {
            URL url = new URL("https://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=1120059000");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            doc = documentBuilder.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

}
