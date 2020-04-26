package com.example.umbrellaapplicationproject;

import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class BackgroundThreadForXML extends AsyncTask<Long, Integer, Document> {
    /*
    * Long : parameter of doInBackground
    * Integer : parameter of onProgressUpdate (not used here)
    * Document : return value of doInBackground and also parameter of onPostExecute
     */
    /* return document which is originally RSS data from the weather cast website below */
    @Override
    protected Document doInBackground(Long... Longs) {
        Long zoneCode = Longs[0];
        Document doc = null;
        try {
            URL url = new URL("https://www.kma.go.kr/wid/queryDFSRSS.jsp?zone="+zoneCode);
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
