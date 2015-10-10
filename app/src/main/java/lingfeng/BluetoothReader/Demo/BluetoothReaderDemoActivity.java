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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import lingfeng.BluetoothReader.Demo.navigation.Direction;
import lingfeng.BluetoothReader.Demo.navigation.Navigator;

public class BluetoothReaderDemoActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    private static final String TAG = "Demo";
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
//	private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    private Button mClearButton;
    private CheckBox mCheckld = null;
    private CheckBox mCheckbeep = null;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    //Navigation data
    private String mDestination = "14"; //TODO: Rendilo selezionabile dall'utente
    private Navigator mNav;
    private OutputMgr outputMgr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
//		requestWindowFeature((Window.FEATURE_NO_TITLE));
        setContentView(R.layout.main);
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
//				R.layout.custom_title);

//      mTitle = (TextView) findViewById(R.id.title_left_text);
//		mTitle.setText(R.string.app_name);
//		mTitle = (TextView) findViewById(R.id.title_right_text);
        ActionBar ab = getActionBar();
        ab.setTitle(R.string.app_name);


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

        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                sendMsg = view.getText().toString();
                try {
                    sendMessage(sendMsg);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });


        mCheckld = (CheckBox) findViewById(R.id.checkBox2);
        mCheckld.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    sendMsg = "0200020F010C03";
                    Log.i(TAG, "����-------------->" + sendMsg);
                    try {
                        sendMessage(sendMsg);
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    sendMsg = "0200020F020F03";
                    Log.i(TAG, "����-------------->" + sendMsg);
                    try {
                        sendMessage(sendMsg);
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        mCheckbeep = (CheckBox) findViewById(R.id.checkBox1);
        mCheckbeep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    sendMsg = "0200020E010D03";
                    Log.i(TAG, "����-------------->" + sendMsg);
                    try {
                        sendMessage(sendMsg);
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    sendMsg = "0200020E020E03";
                    Log.i(TAG, "����-------------->" + sendMsg);
                    try {
                        sendMessage(sendMsg);
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        //Carica il grafo per la mappa
        try {
            mNav = new Navigator(this.getResources().getResourceName(R.xml.boella)); //TODO: Check che la stringa ritornata sia effettivamente il path al file
            outputMgr = new OutputMgr(getApplicationContext());
        } catch (Exception e) {
            System.out.println("Mappa non trovata.");
        }
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
            // Only if the state is STATE_NONE, do we know that we haven't
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

        mOutStringBuffer = new StringBuffer("");
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
//					mTitle.setText(R.string.title_connected_to);
//					mTitle.append(mConnectedDeviceName);
                                ab.setTitle(R.string.title_connected_to + " " + activity.mConnectedDeviceName);
                                activity.mConversationArrayAdapter.clear();
                                break;
                            case BluetoothChatService.STATE_CONNECTING:
//					mTitle.setText(R.string.title_connecting);
                                ab.setTitle(R.string.title_connecting);
                                break;
                            case BluetoothChatService.STATE_LISTEN:
                            case BluetoothChatService.STATE_NONE:
//					mTitle.setText(R.string.title_not_connected);
                                ab.setTitle(R.string.title_not_connected);
                                break;
                        }
                        break;
                    case MESSAGE_WRITE:
                        //byte[] writeBuf = (byte[]) msg.obj;
                        // construct a string from the buffer
                        //String writeMessage = new String(writeBuf);

                        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date curDate2 = new Date(System.currentTimeMillis());
                        activity.mConversationArrayAdapter.add("Me:  " + activity.sendMsg + "  " + formatter2.format(curDate2));
                        break;
                    case MESSAGE_READ:
                        // zzc is the currently built packet. It's updated with several fragments.
                        // msg.obj is the data array
                        // msg.arg1 is i, number of bytes read from the buffer
                        // msg.arg2 is -1

                        String s3 = String.valueOf(byte2HexStr(
                                (byte[]) msg.obj, msg.arg1));
                        String readMessage = (new StringBuilder(s3)).toString();

                        activity.zzc += readMessage;
                        System.out.println(activity.zzc);
                        String readUID = null;
                        if (activity.zzc.length() > 4) {
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date curDate = new Date(System.currentTimeMillis());
                            String str = formatter.format(curDate);
                            StringBuilder formattedData = new StringBuilder();
                            formattedData.append(activity.mConnectedDeviceName).append(":  Received a new packet\n");
                            formattedData.append(str);
                            formattedData.append("\n> Station ID: 0x").append(activity.zzc.substring(2, 4));
                            formattedData.append("\n> Data Length: 0x").append(activity.zzc.substring(4, 6));
                            formattedData.append("\n> Status code: 0x").append(activity.zzc.substring(6, 8));
                            readUID = activity.zzc.substring(8, activity.zzc.length() - 4);
                            formattedData.append("\n> Data: 0x").append(readUID);
                            formattedData.append("\n> BCC: 0x").append(activity.zzc.substring(activity.zzc.length() - 4, activity.zzc.length() - 2)).append("\n");
                            activity.mConversationArrayAdapter.add(formattedData.toString());
                            activity.zzc = "";
                        } else return;

                        //TODO: Verifica che questo sia il codice che viene eseguito quando leggo un tag

				/*TODO: 1) Leggi lo UID del tag letto

						2) Prendi tramite il tabellozzo il codice del nodo associato allo UID
						*/
                        String nextNode = null;

                        nextNode = UIDToNodeTranslator.translator.get(readUID);


//                    String cur_pos_id = "1"; //TODO: Ottieni questo da tabella
                        /*
                        IF no destination defined
						    Chiedi all'utente di scegliere una destinazione
						    Inizializza il navigatore
                            */
                        if (activity.mDestination == null) {
                            //TODO: informa l'utente che deve scegliere una destinazione
                            return;
                        } else {
                            activity.mNav.initNavigation(nextNode, activity.mDestination);
                        }

                            /*
                            Return.
						3) Ottieni dal navigatore la direzione in cui andare, passandogli la posizione corrente
					*/
                        Direction nextDirection = activity.mNav.getNextDirection(nextNode);
                        activity.outputMgr.giveFeedbackToUser(nextDirection);
//						5) Manda in output la posizione al cieco.


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
            //	stringbuilder.append(" ");
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