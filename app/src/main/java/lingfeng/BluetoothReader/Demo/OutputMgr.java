package lingfeng.BluetoothReader.Demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

import lingfeng.BluetoothReader.Demo.navigation.Direction;

/**
 * Created by ilrosso on 09/10/15.
 */
public class OutputMgr {

    private Context appContext;
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("51b2383e-51e0-49b3-ba61-6aa74fead4e3");
    private final static String TAG = "Orientoma.OutputMgr";

    private final static int SX = 1;
    private final static int DX = 2;
    private final static int CBACK = 3;
    private final static int FORWARD = 4;
    private final static int TARGET = 5;

    public OutputMgr(Context appContext) {
        this.appContext = appContext;

        PebbleKit.registerPebbleConnectedReceiver(appContext, new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("OutputMgr", "Pebble connected!");
            }

        });

        PebbleKit.registerPebbleDisconnectedReceiver(appContext, new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("OutputMgr", "Pebble disconnected!");
            }

        });

        PebbleKit.startAppOnPebble(appContext, PEBBLE_APP_UUID);
    }

    public void giveFeedbackToUser(Direction direction) {
        if(direction == null) {
            Log.d(TAG, "giveFeedbackToUser called with null direction");
            return;
        }

        boolean connected = PebbleKit.isWatchConnected(appContext);
        if(!connected){
            Log.w("Orientoma", "Pebble is not connected!");
            return;
        }

        PebbleDictionary data = new PebbleDictionary();
        Log.d("Orientoma", "Received from navigator the command "+direction.toString());

        switch (direction) {
            case BACKWARD:
                data.addString(CBACK, "back");
                break;
            case RIGHT:
                data.addString(DX, "dx");
                break;
            case LEFT:
                data.addString(SX, "sx");
                break;
            case FORWARD:
                data.addString(FORWARD, "forward");
                break;
            case TARGET:
                data.addString(TARGET, "target reached");
                break;
        }
        PebbleKit.sendDataToPebble(appContext, PEBBLE_APP_UUID, data);
    }

    public void shutdown() {

        PebbleKit.closeAppOnPebble(appContext, PEBBLE_APP_UUID);
    }
}
