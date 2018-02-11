package sunrun.fingerprintidentification;

import android.app.Activity;
import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.os.Bundle;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;
import com.samsung.android.sdk.pass.SpassInvalidStateException;

import java.util.ArrayList;

import sunrun.fingerprintidentification.view.DialogView;

/**
 * 指纹识别
 */

public class MainActivity extends Activity {
    FingerprintManager manager;
    KeyguardManager mKeyManager;
    private final static int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 0;
    private final static String TAG = "finger_log";
    private CancellationSignal mCancellationSignal;
    //private FingerprintManagerCompat manager;

    /************************三新手机************************/
    private SpassFingerprint mSpassFingerprint;
    private Spass mSpass;
    private boolean isFeatureEnabled_index = false;
    private boolean onReadyIdentify = false;
    private boolean isFeatureEnabled_fingerprint = false;
    private boolean needRetryIdentify = false;
    private ArrayList<Integer> designatedFingers = null;
    private boolean isSM=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String model=android.os.Build.MODEL; // 手机型号
        System.out.println("手机型号"+model);
        System.out.println("手机型号2"+android.os.Build.MANUFACTURER);

        if ("sys_emui".equals(GetSystem.getSystem())){//华为手机
            isSM=false;
            Toast.makeText(MainActivity.this, "华为手机", Toast.LENGTH_LONG).show();
            //初始化华为、小米的指纹识别
            initializeFinger();
        }else if ("sys_miui".equals(GetSystem.getSystem())){//小米手机
            isSM=false;
            Toast.makeText(MainActivity.this, "小米手机", Toast.LENGTH_LONG).show();
            //初始化华为、小米的指纹识别
            initializeFinger();
        }else if ("sys_flyme".equals(GetSystem.getSystem())){//魅族手机
            isSM=false;
            Toast.makeText(MainActivity.this, "魅族手机", Toast.LENGTH_LONG).show();
             //初始化华为、小米的指纹识别
            initializeFinger();
        }else {//可能是三星手机
            isSM=true;
            Toast.makeText(MainActivity.this, "可能是三星手机", Toast.LENGTH_LONG).show();
            initializeFingerToSM();
        }


        //开始指纹识别
        Button btn_finger = (Button) findViewById(R.id.btn_activity_main_finger);
        btn_finger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSM) {//非三星机
                    if (isFinger()) {//指纹识别条件满足
                        Toast.makeText(MainActivity.this, "请进行指纹识别", Toast.LENGTH_LONG).show();
                        startListening(null);
                    }
                }else {//三星机
                    startIdentify();
                }
            }
        });
        //取消指纹识别
        Button btn_activity_main_finger_stop = (Button) findViewById(R.id.btn_activity_main_finger_stop);
        btn_activity_main_finger_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCancellationSignal!=null) {
                    mCancellationSignal.cancel();
                }
            }
        });
        //跳转到三星指纹识别
        Button btn_activity_main_finger_jump = (Button) findViewById(R.id.btn_activity_main_finger_jump);
        btn_activity_main_finger_jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,Main2Activity.class);
                startActivity(intent);
            }
        });

    }


    private void initializeFinger() {
        mCancellationSignal = new CancellationSignal();
        manager = (FingerprintManager) this.getSystemService(Context.FINGERPRINT_SERVICE);
        //向6.0以下兼容
        //manager = FingerprintManagerCompat.from(this);
        mKeyManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
    }

    public boolean isFinger() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.USE_FINGERPRINT, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return false;
        }
        if (!manager.isHardwareDetected()) {
            Toast.makeText(this, "没有指纹识别模块", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!mKeyManager.isKeyguardSecure()) {
            Toast.makeText(this, "没有开启锁屏密码", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!manager.hasEnrolledFingerprints()) {
            Toast.makeText(this, "没有录入指纹", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    //回调方法
    FingerprintManager.AuthenticationCallback mSelfCancelled = new FingerprintManager.AuthenticationCallback() {
        //多次指纹密码验证错误并且连续错误5次后，进入此方法；跳到密码输入界面并且，不能短时间内调用指纹验证
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            Toast.makeText(MainActivity.this, errString, Toast.LENGTH_SHORT).show();
            showAuthenticationScreen();
        }

        //指纹匹配提示
        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            Toast.makeText(MainActivity.this, helpString, Toast.LENGTH_SHORT).show();
        }

        //指纹匹配成功
        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            Toast.makeText(MainActivity.this, "指纹识别成功", Toast.LENGTH_SHORT).show();
        }

        //指纹匹配失败（没超过5次）
        @Override
        public void onAuthenticationFailed() {
            Toast.makeText(MainActivity.this, "指纹识别失败", Toast.LENGTH_SHORT).show();
        }
    };

    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "没有指纹识别权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mCancellationSignal.isCanceled()) {
            mCancellationSignal = new CancellationSignal();
        }
        manager.authenticate(cryptoObject, mCancellationSignal, 0, mSelfCancelled, null);
    }

    private void showAuthenticationScreen() {
        Intent intent = mKeyManager.createConfirmDeviceCredentialIntent("finger", "验证指纹识别");
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) { // Challenge completed, proceed with using cipher
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "识别成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "识别失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
/******************************三星手机的指纹识别***************************/
    private void initializeFingerToSM() {
        mSpass = new Spass();
        try {
            mSpass.initialize(MainActivity.this);
            isFeatureEnabled_fingerprint = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
        } catch (SsdkUnsupportedException e) {
            Log.e("Yankee", "Exception: " + e);
        } catch (UnsupportedOperationException e) {
            Toast.makeText(getApplicationContext(), "当前机型不支持指纹....", Toast.LENGTH_SHORT).show();
            Log.e("Yankee", "Fingerprint Service is not supported in the device");
        }
        if (isFeatureEnabled_fingerprint) {
            mSpassFingerprint = new SpassFingerprint(MainActivity.this);
            Log.e("Yankee", "Fingerprint Service is supported in the device.");
            Log.e("Yankee", "SDK version : " + mSpass.getVersionName());
        } else {
            Toast.makeText(getApplicationContext(), "当前机型不支持指纹....", Toast.LENGTH_SHORT).show();
            Log.e("Yankee", "Fingerprint Service is not supported in the device.");
            return;
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


    private void setIdentifyIndex() {
        if (isFeatureEnabled_index) {
            if (mSpassFingerprint != null && designatedFingers != null) {
                mSpassFingerprint.setIntendedFingerprintIndex(designatedFingers);
            }
        }
    }
    private void resetIdentifyIndex() {
        designatedFingers = null;
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
                //重新进行指纹验证
                againTest();
                Toast.makeText(getApplicationContext(), "Fail....", Toast.LENGTH_SHORT).show();
            } else if (eventStatus == SpassFingerprint.STATUS_QUALITY_FAILED) {
                Log.e("Yankee", "onFinished() : Authentification Fail for identify.");
                Toast.makeText(getApplicationContext(), "Fail....", Toast.LENGTH_SHORT).show();
                needRetryIdentify = true;
                FingerprintGuideText = mSpassFingerprint.getGuideForPoorQuality();
                //重新进行指纹验证
                againTest();
                Toast.makeText(getApplicationContext(), FingerprintGuideText, Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Yankee", "onFinished() : Authentification Fail for identify");
                Toast.makeText(getApplicationContext(), "Fail....", Toast.LENGTH_SHORT).show();
                needRetryIdentify = true;
                //重新进行指纹验证
                againTest();
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
    //重新进行指纹验证
    private void againTest() {
        DialogView dialogView = new DialogView(MainActivity.this) {
            @Override
            public void isdismiss(int tag) {
                if (tag == DialogView.OKTAG) {
                    startIdentify();
                }
            }
        };
        //   KeyBoardUtils.closeKeybord(gridPasswordView, mContext);
        dialogView.showdialog2("提示", "指纹验证失败", "算了", "再试一次");
    }
}
