package tax.bcns.pingerprinter;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * Created by LeeBeomWoo on 2017-11-23.
 */

public class MainActivity extends AppCompatActivity {

    // Declare a string variable for the key we’re going to use in our fingerprint authentication
    // 지문 인증에 사용할 키의 문자열 변수를 선언합니다.
    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private TextView textView;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If you’ve set your app’s minSdkVersion to anything lower than 23, then you’ll need to verify that the device is running Marshmallow
        // or higher before executing any fingerprint-related code
        // 앱의 minSdkVersion을 23 미만으로 설정 한 경우 지문 관련 코드를 실행하기 전에 기기가 마시맬로 이상을 실행하는지 확인해야합니다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Get an instance of KeyguardManager and FingerprintManager//
            keyguardManager =
                    (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager =
                    (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            textView = (TextView) findViewById(R.id.textview);

            //Check whether the device has a fingerprint sensor//
            // 장치에 지문 센서가 있는지 확인합니다 //
            if (!fingerprintManager.isHardwareDetected()) {
                // If a fingerprint sensor isn’t available, then inform the user that they’ll be unable to use your app’s fingerprint functionality//
                // 지문 센서를 사용할 수없는 경우 사용자에게 앱의 지문 인식 기능을 사용할 수 없음을 알립니다. //
                textView.setText("Your device doesn't support fingerprint authentication");
            }
            //Check whether the user has granted your app the USE_FINGERPRINT permission//
            //사용자가 앱에 USE_FINGERPRINT 권한을 부여했는지 확인합니다. //
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // If your app doesn't have this permission, then display the following text//
                textView.setText("Please enable the fingerprint permission");
            }

            //Check that the user has registered at least one fingerprint//
            // 사용자가 적어도 하나의 지문을 등록했는지 확인합니다 //
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                // If the user hasn’t configured any fingerprints, then display the following message//
                // 사용자가 지문을 구성하지 않은 경우 다음 메시지를 표시합니다. //
                textView.setText("No fingerprint configured. Please register at least one fingerprint in your device's Settings");
            }

            //Check that the lockscreen is secured//
            // 잠금 화면이 안전한지 확인합니다 //
            if (!keyguardManager.isKeyguardSecure()) {
                // If the user hasn’t secured their lockscreen with a PIN password or pattern, then display the following text//
                // 사용자가 PIN 비밀번호 나 패턴으로 잠금 화면을 보호하지 않은 경우 다음 텍스트를 표시합니다. //
                textView.setText("Please enable lockscreen security in your device's Settings");
            } else {
                try {        generateKey();
                } catch (FingerprintException e) {
                    e.printStackTrace();
                }

                if (initCipher()) {
                    //If the cipher is initialized successfully, then create a CryptoObject instance//
                    // 암호가 성공적으로 초기화 된 경우 CryptoObject 인스턴스를 만듭니다. //
                    cryptoObject = new FingerprintManager.CryptoObject(cipher);

                    // Here, I’m referencing the FingerprintHandler class that we’ll create in the next section. This class will be responsible
                    // for starting the authentication process (via the startAuth method) and processing the authentication process events//
                    // 여기에서는 다음 섹션에서 만들 FingerprintHandler 클래스를 참조합니다.
                    // 이 클래스는 (startAuth 메소드를 통해) 인증 프로세스를 시작하고 인증 프로세스 이벤트를 처리합니다.
                    FingerprintHandler helper = new FingerprintHandler(this);
                    helper.startAuth(fingerprintManager, cryptoObject);
                }
            }
        }
    }

//Create the generateKey method that we’ll use to gain access to the Android keystore and generate the encryption key//
// Android 키 저장소에 액세스하고 암호화 키를 생성하는 데 사용할 generateKey 메서드를 만듭니다. //
    private void generateKey() throws FingerprintException {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            // 표준 Android 키 저장소 컨테이너 식별자 ( "AndroidKeystore")를 사용하여 키 저장소에 대한 참조 얻기 //
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Initialize an empty KeyStore//
            keyStore.load(null);

            //Initialize the KeyGenerator//
            keyGenerator.init(new

                    //Specify the operation(s) this key can be used for//
                    // 이 키를 사용할 수있는 작업 지정 //
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                    //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                    //이 키를 구성하여 사용자가 사용할 때마다 지문을 사용하여 ID를 확인해야합니다.
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            //Generate the key//
            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }

    //Create a new method that we’ll use to initialize our cipher//
    // 암호를 초기화하는 데 사용할 새 메서드를 만듭니다.
    public boolean initCipher() {
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            // 암호 인스턴스를 얻어 지문 인증에 필요한 속성으로 구성합니다. //
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the cipher has been initialized successfully//
            // 암호가 성공적으로 초기화 된 경우 true를 반환합니다.
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {

            //Return false if cipher initialization failed//
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }
}