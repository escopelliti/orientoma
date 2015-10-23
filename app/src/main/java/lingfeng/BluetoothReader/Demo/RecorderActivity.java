package lingfeng.BluetoothReader.Demo;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class RecorderActivity extends Activity {

    private static final String TAG = "Orientoma.Recorder";

    //Layout views
    private TextView mTextLastRecord;
    private TextView mTextRecordsHistory;
    private Spinner mSpinnerNodes;
    private Button mButtonRecord;
    private TextView mTextInfo;

    //Fields
    private String node_id = "";
    private String uid = "";
    private Document mDoc;

    //Bluetooth reader fields and constants
    String partialPacketReceived;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2; //
    public static final int MESSAGE_WRITE = 3; //
    public static final int MESSAGE_DEVICE_NAME = 4; //
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;
    private final MyHandler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        ActionBar ab = getActionBar();
        ab.setTitle("Recorder");

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //Load controls
        mTextInfo = (TextView) findViewById(R.id.text_info_rec);
        mTextLastRecord = (TextView) findViewById(R.id.text_last_record);
        mTextRecordsHistory = (TextView) findViewById(R.id.textView_output_history);
        mSpinnerNodes = (Spinner) findViewById(R.id.spinner_node);
        mButtonRecord = (Button) findViewById(R.id.button_store_uid);

        //Set up listeners
        mSpinnerNodes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                node_id = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Nothing useful here
            }
        });

        mButtonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If one of the two strings is empty, we don't do anything
                if(uid == null || uid.length() == 0 || node_id == null || node_id.length() == 0) {
                    Log.d(TAG, "buttonRecord.onClick called with empty strings.");
                    mTextInfo.setText("Read a tag before trying to store a record");
                    return;
                }

                //If there is already a <uid> node with the current uid, return
                //NOTE: I could use getElementById to search directly on the whole
                Element e = mDoc.getElementById(uid);
                if(e != null) {
                    Log.d(TAG, "Tried to store a new record with uid '" + uid + "', but one node is already in the DOM");

                    mTextInfo.setText("This uid is already mapped to " + ((Element) e.getParentNode()).getAttribute("id"));
                    return;
                }

                //Append a <uid> child to the correct <node> element
                e = mDoc.getElementById(node_id);
                Element c = mDoc.createElement("uid");
                c.setAttribute("id", uid);
                e.appendChild(c);
                Log.d(TAG, "Appended UID "+uid+" to node "+node_id);

                //Write some output to the user
                mTextLastRecord.setText("Added UID "+uid+" to node "+node_id+"\n"+mTextLastRecord.getText());
                mTextInfo.setText("");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Entering onStart");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null)
                setupChat();
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
            return;
        }

        //Open the map file to load the nodes id list
        Document map;
        try {
            map = db.parse(getResources().openRawResource(R.raw.boella));
        } catch (SAXException e) {
            Log.e(TAG, "Map file has XML syntax errors");
            return;
        } catch (IOException e) {
            Log.e(TAG, "Map not found");
            return;
        }
        NodeList nodes = map.getElementsByTagName("node");
        List<String> node_ids = new LinkedList<String>();
        for(int i=0; i<nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            node_ids.add(e.getAttribute("id"));
        }

        //Open (or create if not existing) the mapping file
        File file = new File(getFilesDir(), getResources().getString(R.string.uid_node_map_fname));
        if(!file.exists()) {
            //Create a new DOM and set up the basic elements (root and nodes)
            mDoc = db.newDocument();
            Element root = mDoc.createElement("mapping");
            mDoc.appendChild(root);
            for(String n : node_ids) {
                Element el = mDoc.createElement("node");
                el.setAttribute("id", n);
            }

        } else {
            try {
                mDoc = db.parse(file);
            } catch (IOException e) {
                Log.e(TAG, "Error in opening the " + getResources().getString(R.string.uid_node_map_fname) + " file.");
                return;
            } catch (SAXException e) {
                Log.e(TAG, "Error in opening the " + getResources().getString(R.string.uid_node_map_fname) + " file.");
                return;
            }
        }

        //Populate the spinner with the node ids
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, node_ids);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerNodes.setAdapter(dataAdapter);

        //Reset texts as needed
        mTextInfo.setText("");
        mTextLastRecord.setText("");
        mTextRecordsHistory.setText("");

        Log.d(TAG, "onStart completed");
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            //Save the DOM object to an xml file
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult( new File(getFilesDir(), getResources().getString(R.string.uid_node_map_fname)));
            Source input = new DOMSource(mDoc);
            transformer.transform(input, output);
        } catch (TransformerException e) {
            Log.e(TAG, "Error in saving the DOM object to xml.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                //Intent serverIntent = new Intent(this, DeviceListActivity.class);
                //  startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");
        mChatService = new BluetoothChatService(this, mHandler);
    }

    private void ensureDiscoverable() {
        Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<RecorderActivity> mActivity;

        public MyHandler(RecorderActivity activity) {
            mActivity = new WeakReference<RecorderActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            RecorderActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                        ActionBar ab = activity.getActionBar();
                        switch (msg.arg1) {
                            case BluetoothChatService.STATE_CONNECTED:
                                ab.setTitle("Recorder - " + activity.getResources().getString(R.string.title_connected_to) + " " + activity.mConnectedDeviceName);
                                break;
                            case BluetoothChatService.STATE_CONNECTING:
                                ab.setTitle("Recorder - " + activity.getResources().getString(R.string.title_connecting));
                                break;
                            case BluetoothChatService.STATE_LISTEN:
                            case BluetoothChatService.STATE_NONE:
                                ab.setTitle("Recorder - " + activity.getResources().getString(R.string.title_not_connected));
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        // zzc is the currently built packet. It's updated with several fragments.
                        // msg.obj is the data array
                        // msg.arg1 is i, number of bytes read from the buffer
                        // msg.arg2 is -1

                        String s3 = String.valueOf(byte2HexStr(
                                (byte[]) msg.obj, msg.arg1));
                        String readMessage = (new StringBuilder(s3)).toString();

                        activity.partialPacketReceived += readMessage;
                        if (activity.partialPacketReceived.length() > 4) {
                            activity.uid = activity.partialPacketReceived.substring(8, activity.partialPacketReceived.length() - 4);
                            activity.partialPacketReceived = "";
                            activity.mTextLastRecord.setText("Read UID "+activity.uid);
                        } else return;

                        break;

                    case MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        activity.mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                        Toast.makeText(activity.getApplicationContext(),
                                "Connected to " + activity.mConnectedDeviceName,
                                Toast.LENGTH_SHORT).show();
                        break;

                    case MESSAGE_TOAST:
                        Toast.makeText(activity.getApplicationContext(),
                                msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                                .show();
                        break;
                }
            }
        }

        public static String byte2HexStr(byte abyte0[], int i) {
            StringBuilder stringbuilder = new StringBuilder("");
            int j = 0;
            do {
                if (j >= i)
                    return stringbuilder.toString().toUpperCase().trim();
                String s = Integer.toHexString(abyte0[j] & 0xff);
                String s1;
                if (s.length() == 1)
                    s1 = (new StringBuilder("0")).append(s).toString();
                else
                    s1 = s;
                stringbuilder.append(s1);
                j++;
            } while (true);
        }

        public static byte[] hexStr2Bytes(String s) {
            s = s.toString().trim().replace(" ", "");
            int i = s.length() / 2;
            System.out.println(i);
            byte abyte0[] = new byte[i];
            int j = 0;
            do {
                if (j >= i)
                    return abyte0;
                int k = j * 2 + 1;
                int l = k + 1;
                StringBuilder stringbuilder = new StringBuilder("0x");
                int i1 = j * 2;
                String s1 = s.substring(i1, k);
                StringBuilder stringbuilder1 = stringbuilder.append(s1);
                String s2 = s.substring(k, l);
                byte byte0 = (byte) (Integer.decode(stringbuilder1.append(s2).toString()).intValue() & 0xff);
                abyte0[j] = byte0;
                j++;
            } while (true);
        }
    }
}
