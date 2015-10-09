package lingfeng.BluetoothReader.Demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import lingfeng.BluetoothReader.Demo.navigation.Direction;

import java.util.UUID;

/**
 * Created by ilrosso on 09/10/15.
 */
public class OutputMgr {

    private Context appContext;
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("EC7EE5C6-8DDF-4089-AA84-C3396A11CC95");

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

    public PebbleDictionary prepareDirection(String cod) {
        PebbleDictionary data = new PebbleDictionary();
        data.addUint8(0, (byte) 42);
        data.addString(1, "A string");
        return data;
    }

    public void giveFeedbackToUser(Direction direction) {

        //TODO: to handle errors
        boolean connected = PebbleKit.isWatchConnected(appContext);

        String cod = "";
        //TODO: to be defined with Sebastiano
        switch (direction) {
            case BACKWARD:
                break;
            case RIGHT:
                break;
            case LEFT:
                break;
            case FORWARD:
                break;
            case TARGET:
                break;
        }
        PebbleKit.sendDataToPebble(appContext, PEBBLE_APP_UUID, prepareDirection(cod));
    }

    public void shutdown() {

        PebbleKit.closeAppOnPebble(appContext, PEBBLE_APP_UUID);
    }
}
