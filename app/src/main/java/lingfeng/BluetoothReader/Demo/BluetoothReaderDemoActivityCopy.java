package lingfeng.BluetoothReader.Demo;

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
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BluetoothReaderDemoActivityCopy extends Activity {

    // Constants
	private static final String TAG = "Demo";
	private static final boolean D = true;

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
	private TextView mTitle;

    // Fields
	private String mConnectedDeviceName = null;					// Name of the connected device
	private ArrayAdapter<String> mConversationArrayAdapter;		// Array adapter for the conversation thread
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothChatService mChatService = null;
    private String zzc="";
    private String sendMsg = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		requestWindowFeature((Window.FEATURE_NO_TITLE));
        setContentView(R.layout.main);
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
//				R.layout.custom_title);
        
        mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		Button mClearButton = (Button) findViewById(R.id.button_clear);
		mClearButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mConversationArrayAdapter.clear();
			}
		});
		
		Button mSendButton = (Button) findViewById(R.id.button_send);
		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				TextView view = (TextView) findViewById(R.id.edit_text_out);
				sendMsg = view.getText().toString();
				try {
					sendMessage(sendMsg);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		});
		
		
		
		CheckBox mCheckld=(CheckBox)findViewById(R.id.checkBox2);
		mCheckld.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
        	   if(isChecked){
        		   sendMsg = "0200020F010C03";
	   				Log.i(TAG, "����-------------->" + sendMsg);
	   				try {
	   					sendMessage(sendMsg);
	   				} catch (UnsupportedEncodingException e) {
	   					e.printStackTrace();
	   				}
        	   }
        	   else{ 
        		   sendMsg = "0200020F020F03";
	   				Log.i(TAG, "����-------------->" + sendMsg);
	   				try {
	   					sendMessage(sendMsg);
	   				} catch (UnsupportedEncodingException e) {
	   					e.printStackTrace();
	   				}
        	   } 
           	} 
		}); 
		
		CheckBox mCheckbeep=(CheckBox)findViewById(R.id.checkBox1);
		mCheckbeep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { 
        	   if(isChecked){
        		   sendMsg = "0200020E010D03";
	   				Log.i(TAG, "����-------------->" + sendMsg);
	   				try {
	   					sendMessage(sendMsg);
	   				} catch (UnsupportedEncodingException e) {
	   					e.printStackTrace();
	   				}
        	   }
        	   else{ 
        		   sendMsg = "0200020E020E03";
	   				Log.i(TAG, "����-------------->" + sendMsg);
	   				try {
	   					sendMessage(sendMsg);
	   				} catch (UnsupportedEncodingException e) {
	   					e.printStackTrace();
	   				}
        	   } 
           	} 
		}); 		
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
		ListView mConversationView = (ListView) findViewById(R.id.in);
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
        private final WeakReference<BluetoothReaderDemoActivityCopy> mActivity;

        public MyHandler(BluetoothReaderDemoActivityCopy activity) {
            mActivity = new WeakReference<BluetoothReaderDemoActivityCopy>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BluetoothReaderDemoActivityCopy activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:
                        if (D)
                            Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                        switch (msg.arg1) {
                            case BluetoothChatService.STATE_CONNECTED:
                                activity.mTitle.setText(R.string.title_connected_to);
                                activity.mTitle.append(activity.mConnectedDeviceName);
                                activity.mConversationArrayAdapter.clear();
                                break;
                            case BluetoothChatService.STATE_CONNECTING:
                                activity.mTitle.setText(R.string.title_connecting);
                                break;
                            case BluetoothChatService.STATE_LISTEN:
                            case BluetoothChatService.STATE_NONE:
                                activity.mTitle.setText(R.string.title_not_connected);
                                break;
                        }
                        break;
                    case MESSAGE_WRITE:
                        //byte[] writeBuf = (byte[]) msg.obj;
                        // construct a string from the buffer
                        //String writeMessage = new String(writeBuf);

                        SimpleDateFormat   formatter2   =   new   SimpleDateFormat   ("yyyy-MM-dd HH:mm:ss");
                        Date   curDate2   =   new   Date(System.currentTimeMillis());
                        activity.mConversationArrayAdapter.add("Me:  "+activity.sendMsg + "  "+ formatter2.format(curDate2) );
                        break;
                    case MESSAGE_READ:
                        String s3 = String.valueOf(byte2HexStr(
                                (byte[]) msg.obj, msg.arg1));
                        String readMessage = (new StringBuilder(s3)).toString();

                        activity.zzc += readMessage;
                        System.out.println(activity.zzc);
                        if(activity.zzc.length()>4){
                            SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyy-MM-dd HH:mm:ss");
                            Date   curDate   =   new   Date(System.currentTimeMillis());
                            String   str   =   formatter.format(curDate);
                            activity.mConversationArrayAdapter.add(activity.mConnectedDeviceName + ":  "+ activity.zzc + "  "+ str );
                            activity.zzc = "";
                        }
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
			Intent serverIntent = new Intent(this,DeviceListActivity.class);
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
	
	public static String byte2HexStr(byte abyte0[], int i)
	{
		StringBuilder stringbuilder = new StringBuilder("");
		int j = 0;
		do
		{
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
	
	public static byte[] hexStr2Bytes(String s)
	{
		s = s.trim().replace(" ", "");
		int i = s.length() / 2;
		System.out.println(i);
		byte abyte0[] = new byte[i];
		int j = 0;
		do
		{
			if (j >= i)
				return abyte0;
			int k = j * 2 + 1;
			int l = k + 1;
			StringBuilder stringbuilder = new StringBuilder("0x");
			int i1 = j * 2;
			String s1 = s.substring(i1, k);
			StringBuilder stringbuilder1 = stringbuilder.append(s1);
			String s2 = s.substring(k, l);
			byte byte0 = (byte)(Integer.decode(stringbuilder1.append(s2).toString()) & 0xff);
			abyte0[j] = byte0;
			j++;
		} while (true);
	}
}