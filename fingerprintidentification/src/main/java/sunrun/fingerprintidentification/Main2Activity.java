package sunrun.fingerprintidentification;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;
import com.samsung.android.sdk.pass.SpassInvalidStateException;

import java.util.ArrayList;

public class Main2Activity extends Activity {

    private SpassFingerprint mSpassFingerprint;
    private Spass mSpass;
    private boolean isFeatureEnabled_index = false;
    private boolean onReadyIdentify = false;
    private boolean isFeatureEnabled_fingerprint = false;
    private boolean needRetryIdentify = false;
    private ArrayList<Integer> designatedFingers = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mSpass = new Spass();
        try {
            mSpass.initialize(Main2Activity.this);
            isFeatureEnabled_fingerprint = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
        } catch (SsdkUnsupportedException e) {
            Log.e("Yankee", "Exception: " + e);
        } catch (UnsupportedOperationException e) {
            Toast.makeText(getApplicationContext(), "当前机型不支持指纹....", Toast.LENGTH_SHORT).show();
            Log.e("Yankee", "Fingerprint Service is not supported in the device");
        }
        if (isFeatureEnabled_fingerprint) {
            mSpassFingerprint = new SpassFingerprint(Main2Activity.this);
            Log.e("Yankee", "Fingerprint Service is supported in the device.");
            Log.e("Yankee", "SDK version : " + mSpass.getVersionName());
        } else {
            Toast.makeText(getApplicationContext(), "当前机型不支持指纹....", Toast.LENGTH_SHORT).show();
            Log.e("Yankee", "Fingerprint Service is not supported in the device.");
            return;
        }
        findViewById(R.id.identify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startIdentify();
            }
        });
    }

    private void setIdentifyIndex() {
        if (isFeatureEnabled_index) {
            if (mSpassFingerprint != null && designatedFingers != null) {
                mSpassFingerprint.setIntendedFingerprintIndex(designatedFingers);
            }
        }
    }

    private void startIdentify() {
        if (onReadyIdentify == false) {
            try {
                onReadyIdentify = true;
                if (mSpassFingerprint != null) {
                    setIdentifyIndex();
                    mSpassFingerprint.startIdentify(mIdentifyListener);
                }
                if (designatedFingers != null) {
                    Log.e("Yankee", "Please identify finger to verify you with " + designatedFingers.toString() + " finger");
                } else {
                    Log.e("Yankee", "Please identify finger to verify you");
                }
            } catch (SpassInvalidStateException ise) {
                onReadyIdentify = false;
                resetIdentifyIndex();
                if (ise.getType() == SpassInvalidStateException.STATUS_OPERATION_DENIED) {
                    Log.e("Yankee", "Exception: " + ise.getMessage());
                }
            } catch (IllegalStateException e) {
                onReadyIdentify = false;
                resetIdentifyIndex();
                Log.e("Yankee", "Exception: " + e);
            }
        } else {
            Log.e("Yankee", "The previous request is remained. Please finished or cancel first");
        }
    }

    private SpassFingerprint.IdentifyListener mIdentifyListener = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) { //Log.e("Yankee","identify finished : reason =" + getEventStatusName(eventStatus));
            int FingerprintIndex = 0;
            String FingerprintGuideText = null;
            try {
                FingerprintIndex = mSpassFingerprint.getIdentifiedFingerprintIndex();
            } catch (IllegalStateException ise) {
                Log.e("Yankee", ise.getMessage());
            }
            if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                Toast.makeText(getApplicationContext(), "Success....", Toast.LENGTH_SHORT).show();
                Log.e("Yankee", "onFinished() : Identify authentification Success with FingerprintIndex : " + FingerprintIndex);
            } else if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
                Log.e("Yankee", "onFinished() : Password authentification Success");
            } else if (eventStatus == SpassFingerprint.STATUS_OPERATION_DENIED) {
                Log.e("Yankee", "onFinished() : Authentification is blocked because of fingerprint service internally.");
            } else if (eventStatus == SpassFingerprint.STATUS_USER_CANCELLED) {
                Log.e("Yankee", "onFinished() : User cancel this identify.");
            } else if (eventStatus == SpassFingerprint.STATUS_TIMEOUT_FAILED) {
                Log.e("Yankee", "onFinished() : The time for identify is finished.");
                Toast.makeText(getApplicationContext(), "Fail....", Toast.LENGTH_SHORT).show();
            } else if (eventStatus == SpassFingerprint.STATUS_QUALITY_FAILED) {
                Log.e("Yankee", "onFinished() : Authentification Fail for identify.");
                Toast.makeText(getApplicationContext(), "Fail....", Toast.LENGTH_SHORT).show();
                needRetryIdentify = true;
                FingerprintGuideText = mSpassFingerprint.getGuideForPoorQuality();
                Toast.makeText(getApplicationContext(), FingerprintGuideText, Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Yankee", "onFinished() : Authentification Fail for identify");
                Toast.makeText(getApplicationContext(), "Fail....", Toast.LENGTH_SHORT).show();
                needRetryIdentify = true;
            }
            if (!needRetryIdentify) {
                resetIdentifyIndex();
            }
        }

        @Override
        public void onReady() {
            Log.e("Yankee", "identify state is ready");
        }

        @Override
        public void onStarted() {
            Log.e("Yankee", "User touched fingerprint sensor");
        }

        @Override
        public void onCompleted() {
            Log.e("Yankee", "the identify is completed");
            onReadyIdentify = false;
            if (needRetryIdentify) {
                needRetryIdentify = false; //mHandler.sendEmptyMessageDelayed(MSG_AUTH, 100);
            }
        }
    };

    private void resetIdentifyIndex() {
        designatedFingers = null;
    }


}
