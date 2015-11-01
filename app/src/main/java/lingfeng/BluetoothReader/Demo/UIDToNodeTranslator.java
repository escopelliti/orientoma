package lingfeng.BluetoothReader.Demo;

import android.util.Log;
import it.orientoma.app.R;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class UIDToNodeTranslator {

    private static final String TAG = "Orientoma.Translator";
    private Document mDoc;

    public void init(BluetoothReaderDemoActivity activity) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            mDoc = db.parse(new File(activity.getFilesDir(), activity.getResources().getString(R.string.uid_node_map_fname)));
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "Failed creating the documentBuilder");
            return;
        } catch (SAXException e) {
            Log.e(TAG, "The xml file for the mapping has syntax errors");
            return;
        } catch (IOException e) {
            Log.e(TAG, "Xml file for the mapping not found");
            return;
        }
    }

    public String getNodeId(String uid) {
        if (mDoc == null)
            return null;
        Element e = (Element) mDoc.getElementById(uid);
        if (e == null)
            return null;
        Element n = (Element) e.getParentNode();
        if (n == null) //This should be useless as in theory every <uid> tag is always under a <node> tag, but you never know..
            return null;
        return n.getAttribute("id");
    }
}
