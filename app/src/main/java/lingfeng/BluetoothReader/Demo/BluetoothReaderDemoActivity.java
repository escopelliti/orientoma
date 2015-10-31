package lingfeng.BluetoothReader.Demo;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

import lingfeng.BluetoothReader.Demo.navigation.Direction;
import lingfeng.BluetoothReader.Demo.navigation.Navigator;

public class BluetoothReaderDemoActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    private static final String TAG = "Orientoma.Navigator";
    private static final boolean D = true;

    String zzc = "";
    String sendMsg = "";
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2; //
    public static final int MESSAGE_WRITE = 3; //
    public static final int MESSAGE_DEVICE_NAME = 4; //
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private TextView mTextInfo;
    private ListView mConversationView;
    private Button mClearButton;
    private Button mSendFakeReadButton;
    private Button speechToTextButton;
    private Switch mCheckld = null;
    private Switch mCheckbeep = null;
    private Spinner mDestinationPicker = null;
    private Spinner mNextNodePicker = null;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    public void setmDestination(String mDestination) {
        this.mDestination = mDestination;
    }

    //Navigation data
    private String mDestination = null;
    private Navigator mNav;
    private OutputMgr outputMgr;
    private String fakeUID = "";
    private boolean initialized = false;
    private UIDToNodeTranslator translator;

    public static Handler mUiHandler = null;

    private static void navigate(String uid, BluetoothReaderDemoActivity activity) {
        Log.i(TAG, "Navigating from node " + uid);
        activity.mConversationArrayAdapter.add("Tag UID: " + uid);

        String node_id = null;

        node_id = activity.translator.getNodeId(uid);
        //nextNode = uid; //TODO: Remove this when translator will be implemented
        Log.d(TAG, "Read tag with UID " + uid + " that was translated to Node ID " + node_id);
        if (node_id == null) {
            Log.e(TAG, "Translator could not find a match for tag with UID " + uid);
            activity.mTextInfo.setText("Tag not recognized");
            return;
        } else {
            activity.mTextInfo.setText("");
        }

        if (activity.mDestination == null) {
            activity.mTextInfo.setText("Choose a destination!");
            return;
        } else if (!activity.mNav.isInitialized() || !activity.initialized) { //Init the navigator only if it wasn't initialized yet
            activity.mNav.initNavigation(node_id, activity.mDestination);
            activity.initialized = true;
        }

        //3) Ottieni dal navigatore la direzione in cui andare, passandogli la posizione corrente
        Direction nextDirection = activity.mNav.getNextDirection(node_id);

        //4) Manda in output la posizione al cieco.
        activity.outputMgr.giveFeedbackToUser(nextDirection);
        if (activity.mNav.isInitialized())
            activity.mConversationArrayAdapter.add("Found node " + node_id + "\nNext direction: " + nextDirection + "\nNext node in path: " +
                    activity.mNav.getNextNodeInPath_debug(node_id));
        else
            Log.e(TAG, "Navigation was called with an uninitialized navigator!");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ActionBar ab = getActionBar();
        ab.setTitle("Navigator");

        mTextInfo = (TextView) findViewById(R.id.text_info);
        mTextInfo.setText("");

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mClearButton = (Button) findViewById(R.id.button_clear);
        mClearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConversationArrayAdapter.clear();
            }
        });

        mSendFakeReadButton = (Button) findViewById(R.id.button_read_node);
        mSendFakeReadButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (fakeUID.length() > 0) {
                    navigate(fakeUID, BluetoothReaderDemoActivity.this);
                } else {
                    mTextInfo.setText("Seleziona un nodo");
                }
            }
        });

        speechToTextButton = (Button) findViewById(R.id.button_speech_to_text);
        speechToTextButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(getBaseContext(), SpeechToTextService.class));
            }
        });

        mCheckld = (Switch) findViewById(R.id.switch_auto);
        mCheckld.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendMsg = "0200020F010C03";
                    Log.i(TAG, "Enabling automatic reading of tags");
                    try {
                        sendMessage(sendMsg);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    sendMsg = "0200020F020F03";
                    Log.i(TAG, "Disabling automatic reading of tags");
                    try {
                        sendMessage(sendMsg);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mCheckbeep = (Switch) findViewById(R.id.switch_beep);
        mCheckbeep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendMsg = "0200020E010D03";
                    Log.i(TAG, "Enabling beeping on read");
                    try {
                        sendMessage(sendMsg);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    sendMsg = "0200020E020E03";
                    Log.i(TAG, "Disabling beeping on read");
                    try {
                        sendMessage(sendMsg);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //Carica il grafo per la mappa
        try {
            mNav = new Navigator(this.getResources().openRawResource(R.raw.boella));
            outputMgr = new OutputMgr(getApplicationContext());
        } catch (Exception e) {
            System.out.println("Mappa non trovata.");
        }

        //Popola gli spinner con la lista dei nodi
        mDestinationPicker = (Spinner) findViewById(R.id.spinner_destination);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mNav.getNodeNames());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDestinationPicker.setAdapter(dataAdapter);

        mNextNodePicker = (Spinner) findViewById(R.id.spinner_next_node);
        //In teoria non servirebbe fare un altro adapter ma non si sa mai quindi ne faccio 2
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, mNav.getNodeNames());
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNextNodePicker.setAdapter(dataAdapter2);

        //Gestisci la selezione degli items dagli spinner
        mDestinationPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDestination = parent.getItemAtPosition(position).toString();
                initialized = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Non faccio nulla se non scelgo nessun valore.
            }
        });

        mNextNodePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fakeUID = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fakeUID = "";
            }
        });

        //Inizializza il translator con il mapping fra UID e Node id
        translator = new UIDToNodeTranslator();
        translator.init(this);

        mUiHandler = new Handler() // Receive messages from service class
        {
            public void handleMessage(Message msg)
            {
                switch(msg.what)
                {
                    case 0:
                        // add the status which came from service and show on GUI
                        String read_dest = msg.obj.toString();
                        if( ((ArrayAdapter)mDestinationPicker.getAdapter()).getPosition(mDestination) >=0 && ((ArrayAdapter)mDestinationPicker.getAdapter()).getPosition(mDestination) < ((ArrayAdapter)mDestinationPicker.getAdapter()).getCount()) {
                            mDestination = read_dest;
                            initialized = false;
                            mDestinationPicker.setSelection(((ArrayAdapter)mDestinationPicker.getAdapter()).getPosition(mDestination));
                        } else {
                            Log.e(TAG, "Destinazione "+read_dest+" inesistente");
                        }
                        break;

                    default:
                        stopService(new Intent(getBaseContext(), SpeechToTextService.class));
                        break;
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D)
            Log.e(TAG, "++ ON START ++");

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
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D)
            Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity
        // returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, we know that we haven't
            // started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        mConversationArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);
        mChatService = new BluetoothChatService(this, mHandler);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (D)
            Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (D)
            Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null)
            mChatService.stop();
        if (D)
            Log.e(TAG, "--- ON DESTROY ---");
        outputMgr.shutdown();
        stopService(new Intent(getBaseContext(), SpeechToTextService.class));
    }

    private void ensureDiscoverable() {
        if (D)
            Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendMessage(String message)
            throws UnsupportedEncodingException {
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = hexStr2Bytes(message.toUpperCase());
            mChatService.write(send);
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<BluetoothReaderDemoActivity> mActivity;

        public MyHandler(BluetoothReaderDemoActivity activity) {
            mActivity = new WeakReference<BluetoothReaderDemoActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BluetoothReaderDemoActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:
                        if (D)
                            Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                        ActionBar ab = activity.getActionBar();
                        switch (msg.arg1) {
                            case BluetoothChatService.STATE_CONNECTED:
                                ab.setTitle("Navigator - " + activity.getResources().getString(R.string.title_connected_to) + " " + activity.mConnectedDeviceName);
                                activity.mConversationArrayAdapter.clear();
                                break;
                            case BluetoothChatService.STATE_CONNECTING:
                                ab.setTitle("Navigator - " + activity.getResources().getString(R.string.title_connecting));
                                break;
                            case BluetoothChatService.STATE_LISTEN:
                            case BluetoothChatService.STATE_NONE:
                                ab.setTitle("Navigator - " + activity.getResources().getString(R.string.title_not_connected));
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

                        if (readMessage.contains("200808203")) //TODO: Add the other possible answer codes!(Find a way to spot this..)
                            break; //If we read an answer from the reader and not a UID from a tag, ignore the rest.

                        activity.zzc += readMessage;
                        System.out.println(activity.zzc);
                        String readUID = null;
                        if (activity.zzc.length() > 4) {
                            //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            //Date curDate = new Date(System.currentTimeMillis());
                            //String str = formatter.format(curDate);
                            //StringBuilder formattedData = new StringBuilder();
                            //formattedData.append(activity.mConnectedDeviceName).append(":  Received a new packet\n");
                            //formattedData.append(str);
                            //formattedData.append("\n> Station ID: 0x").append(activity.zzc.substring(2, 4));
                            //formattedData.append("\n> Data Length: 0x").append(activity.zzc.substring(4, 6));
                            //formattedData.append("\n> Status code: 0x").append(activity.zzc.substring(6, 8));
                            //1) Leggi lo UID del tag letto
                            if (activity.zzc.length() < 9)
                                break; //Fix per stringhe troppo corte (che non contengono UID, sono solo comandi ritornati dal reader)
                            readUID = activity.zzc.substring(8, activity.zzc.length() - 4);
                            //formattedData.append("\n> Data: 0x").append(readUID);
                            //formattedData.append("\n> BCC: 0x").append(activity.zzc.substring(activity.zzc.length() - 4, activity.zzc.length() - 2)).append("\n");
                            activity.zzc = "";
                        } else return;

                        //2) fai la navigazione
                        navigate(readUID, activity);
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
    }

    private final MyHandler mHandler = new MyHandler(this);

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setupChat();
                } else {
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
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