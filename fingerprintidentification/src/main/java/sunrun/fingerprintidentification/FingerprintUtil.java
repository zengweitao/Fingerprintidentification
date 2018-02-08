package sunrun.fingerprintidentification;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

/**
 * Created by zeng on 2018/2/6.
 */

public class FingerprintUtil {
    private FingerprintManagerCompat mFingerprintManager;
    private KeyguardManager mKeyManager;
    private CancellationSignal mCancellationSignal;
    private Activity mActivity;

    public FingerprintUtil(Context ctx) {
        mActivity = (Activity) ctx;
        mFingerprintManager = FingerprintManagerCompat.from(mActivity);
        mKeyManager = (KeyguardManager) mActivity.getSystemService(Context.KEYGUARD_SERVICE);

    }

    public void callFingerPrintVerify(final IFingerprintResultListener listener) {
        if (!isHardwareDetected()) {
            return;
        }
        if (!isHasEnrolledFingerprints()) {
            if (listener != null) {
                listener.onNoEnroll();
            }
            return;
        }
        if (!isKeyguardSecure()) {
            if (listener != null) {
                listener.onInSecurity();
            }
            return;
        }
        if (listener != null) {
            listener.onSupport();
        }

        if (listener != null) {
            listener.onAuthenticateStart();
        }
        if (mCancellationSignal == null) {
            mCancellationSignal = new CancellationSignal();
        }
        try {
            mFingerprintManager.authenticate(null, 0, mCancellationSignal, new FingerprintManagerCompat.AuthenticationCallback() {
                //多次尝试都失败会走onAuthenticationError。会停止响应一段时间。提示尝试次数过多。请稍后再试。
                @Override
                public void onAuthenticationError(int errMsgId, CharSequence errString) {
                    if (listener != null)
                        listener.onAuthenticateError(errMsgId, errString);
                }

                //指纹验证失败走此方法，比如小米前4次验证失败走onAuthenticationFailed,第5次走onAuthenticationError
                @Override
                public void onAuthenticationFailed() {
                    if (listener != null)
                        listener.onAuthenticateFailed();
                }

                @Override
                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                    if (listener != null)
                        listener.onAuthenticateHelp(helpMsgId, helpString);

                }

                //当验证的指纹成功时会回调此函数。然后不再监听指纹sensor
                @Override
                public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                    if (listener != null)
                        listener.onAuthenticateSucceeded(result);
                }

            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否录入指纹，有些设备上即使录入了指纹，可是没有开启锁屏password的话此方法还是返回false
     *
     * @return
     */
    private boolean isHasEnrolledFingerprints() {
        try {
            return mFingerprintManager.hasEnrolledFingerprints();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否有指纹识别硬件支持
     *
     * @return
     */
    public boolean isHardwareDetected() {
        try {
            return mFingerprintManager.isHardwareDetected();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 推断是否开启锁屏password
     *
     * @return
     */
    private boolean isKeyguardSecure() {
        try {
            return mKeyManager.isKeyguardSecure();
        } catch (Exception e) {
            return false;
        }

    }

    /**
     * 指纹识别回调接口
     */
    public interface IFingerprintResultListener {
        void onInSecurity();

        void onNoEnroll();

        void onSupport();

        void onAuthenticateStart();

        void onAuthenticateError(int errMsgId, CharSequence errString);

        void onAuthenticateFailed();

        void onAuthenticateHelp(int helpMsgId, CharSequence helpString);

        void onAuthenticateSucceeded(FingerprintManagerCompat.AuthenticationResult result);

    }

    public void cancelAuthenticate() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }


    public void onDestroy() {
        cancelAuthenticate();
        mKeyManager = null;
        mFingerprintManager = null;

    }
}
