package lingfeng.BluetoothReader.Demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import it.orientoma.app.R;

public class FunctionSelectionActivity extends Activity {

    private static final String TAG = "Orientoma";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_selection);
    }

    public void openNavigator(View view) {
        Intent intent = new Intent(this, BluetoothReaderDemoActivity.class);
        startActivity(intent);
    }

    public void openRecorder(View view) {
        Intent intent = new Intent(this, RecorderActivity.class);
        startActivity(intent);
    }
}
