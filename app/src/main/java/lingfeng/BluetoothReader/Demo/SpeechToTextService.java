package lingfeng.BluetoothReader.Demo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.*;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Locale;


public class SpeechToTextService extends Service {
    protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

    protected IBinder mBinder;

    protected boolean mIsListening;
    protected volatile boolean mIsCountDownOn;
    private boolean mIsStreamSolo;

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("my service", "here we are");
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
    }

    protected class IncomingHandler extends Handler {
        private WeakReference<SpeechToTextService> mtarget;

        IncomingHandler(SpeechToTextService target) {
            mtarget = new WeakReference<SpeechToTextService>(target);
        }


        @Override
        public void handleMessage(Message msg) {
            final SpeechToTextService target = mtarget.get();

            switch (msg.what) {
                case MSG_RECOGNIZER_START_LISTENING:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        // turn off beep sound
                        if (!mIsStreamSolo) {
                            mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
                            mIsStreamSolo = true;
                        }
                    }
                    if (!target.mIsListening) {
                        target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                        target.mIsListening = true;
                        Log.d("my service", "message start listening"); //$NON-NLS-1$
                    }
                    break;

                case MSG_RECOGNIZER_CANCEL:
                    if (mIsStreamSolo) {
                        mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
                        mIsStreamSolo = false;
                    }
                    target.mSpeechRecognizer.cancel();
                    target.mIsListening = false;
                    Log.d("my service", "message canceled recognizer"); //$NON-NLS-1$

                    break;
            }
        }
    }

    // Count down timer for Jelly Bean work around
    protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000) {

        @Override
        public void onTick(long millisUntilFinished) {
            // TODO Auto-generated method stub
            Log.d("my service", "tick");
        }

        @Override
        public void onFinish() {
            Log.d("my service", "onFinish");
            mIsCountDownOn = false;
            Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
            try {
                mServerMessenger.send(message);
                stopService(new Intent(getBaseContext(), SpeechToTextService.class));
//                message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
//                mServerMessenger.send(message);
            } catch (RemoteException e) {

            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mIsCountDownOn) {
            mNoSpeechCountDown.cancel();
        }
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected class SpeechRecognitionListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
            // speech input will be processed, so there is no need for count down anymore
            if (mIsCountDownOn) {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }
            Log.d("my service", "onBeginingOfSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d("my service", "buffer");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d("my service", "onEndOfSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onError(int error) {
            if (mIsCountDownOn) {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }
            mIsListening = false;
            stopService(new Intent(getBaseContext(), SpeechToTextService.class));
//            Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
//            try {
//                mServerMessenger.send(message);
//            } catch (RemoteException e) {
//                Log.d("my service", "error = " + error);
//            }
            Log.d("my service", "error = " + error); //$NON-NLS-1$
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.d("my service", "event = " + eventType);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.d("my service", "partial results");
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mIsCountDownOn = true;
                mNoSpeechCountDown.start();

            }
            Log.d("my service", "onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(Bundle results) {
            String listenedResult = results.getStringArrayList("results_recognition").get(0);
            Log.d("my service", listenedResult);
            //Toast.makeText(getApplicationContext(), listenedResult, Toast.LENGTH_SHORT).show();
            stopService(new Intent(getBaseContext(), SpeechToTextService.class));
            if (null != BluetoothReaderDemoActivity.mUiHandler) {

                Message msgToActivity = new Message();
                msgToActivity.what = 0;

                msgToActivity.obj = listenedResult;

                BluetoothReaderDemoActivity.mUiHandler.sendMessage(msgToActivity);

            }
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            //Log.d("my service", "rmsdB");
        }

    }
}

