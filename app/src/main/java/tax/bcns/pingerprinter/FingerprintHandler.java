package tax.bcns.pingerprinter;

/**
 * Created by LeeBeomWoo on 2017-11-23.
 */
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.hardware.fingerprint.FingerprintManager;
 import android.Manifest;
 import android.os.Build;
 import android.os.CancellationSignal;
 import android.support.v4.app.ActivityCompat;
 import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    // You should use the CancellationSignal method whenever your app can no longer process user input, for example when your app goes
    // into the background. If you don’t use this method, then other apps will be unable to access the touch sensor, including the lockscreen!//
    //앱이 더 이상 사용자 입력을 처리 할 수 없을 때마다 (예 : 앱이 백그라운드로 들어갈 때) CancellationSignal 메서드를 사용해야합니다.
    // 이 방법을 사용하지 않으면 다른 앱이 lockscreen을 포함하여 터치 센서에 액세스 할 수 없습니다!

    private CancellationSignal cancellationSignal;
    private Context context;

    public FingerprintHandler(Context mContext) {
        context = mContext;
    }

    //Implement the startAuth method, which is responsible for starting the fingerprint authentication process//
    // 지문 인증 프로세스를 시작하는 startAuth 메서드를 구현합니다. //
    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    //onAuthenticationError is called when a fatal error has occurred. It provides the error code and error message as its parameters//
    // onAuthenticationError는 치명적인 오류가 발생하면 호출됩니다. 오류 코드와 오류 메시지를 매개 변수로 제공합니다 //
    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {

        //I’m going to display the results of fingerprint authentication as a series of toasts.
        //Here, I’m creating the message that’ll be displayed if an error occurs//
        // 지문 인증 결과를 일련의 토스트로 표시 할 것입니다.
        // 여기에 오류가 발생하면 표시 될 메시지가 생성됩니다. //
        Toast.makeText(context, "Authentication error\n" + errString, Toast.LENGTH_LONG).show();
    }


    //onAuthenticationFailed is called when the fingerprint doesn’t match with any of the fingerprints registered on the device//
    // onAuthenticationFailed는 지문이 장치에 등록 된 지문 중 하나와 일치하지 않을 때 호출됩니다.
    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(context, "Authentication failed", Toast.LENGTH_LONG).show();
    }


    //onAuthenticationHelp is called when a non-fatal error has occurred. This method provides additional information about the error,
    //so to provide the user with as much feedback as possible I’m incorporating this information into my toast
    // onAuthenticationHelp는 치명적이지 않은 오류가 발생하면 호출됩니다. 이 메서드는 오류에 대한 추가 정보를 제공합니다.
    // 가능한 한 많은 피드백을 사용자에게 제공합니다.이 정보를 내 토스트에 통합합니다. //
    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Toast.makeText(context, "Authentication help\n" + helpString, Toast.LENGTH_LONG).show();
    }
    //onAuthenticationSucceeded is called when a fingerprint has been successfully matched to one of the fingerprints stored on the user’s device
    //onAuthenticationSucceeded는 사용자의 기기에 저장된 지문 중 하나와 지문이 성공적으로 일치하면 호출됩니다
    @Override
    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {
        result.getCryptoObject();
        Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show();
    }

}