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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String model=android.os.Build.MODEL; // 手机型号
        System.out.println("手机型号"+model);
        System.out.println("手机型号2"+android.os.Build.MANUFACTURER);

        if ("sys_emui".equals(GetSystem.getSystem())){//华为手机
            Toast.makeText(MainActivity.this, "华为手机", Toast.LENGTH_LONG).show();
            //初始化华为、小米的指纹识别
            initializeFinger();
        }else if ("sys_miui".equals(GetSystem.getSystem())){//小米手机
            Toast.makeText(MainActivity.this, "小米手机", Toast.LENGTH_LONG).show();
            //初始化华为、小米的指纹识别
            initializeFinger();
        }else if ("sys_flyme".equals(GetSystem.getSystem())){//魅族手机
            Toast.makeText(MainActivity.this, "魅族手机", Toast.LENGTH_LONG).show();
             //初始化华为、小米的指纹识别
            initializeFinger();
        }else {//可能是三星手机

        }


        //开始指纹识别
        Button btn_finger = (Button) findViewById(R.id.btn_activity_main_finger);
        btn_finger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFinger()) {//指纹识别条件满足
                    Toast.makeText(MainActivity.this, "请进行指纹识别", Toast.LENGTH_LONG).show();
                    startListening(null);
                }else {//指纹识别不满足

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


    private void Log(String tag, String msg) {
        Log.d(tag, msg);
    }

}
