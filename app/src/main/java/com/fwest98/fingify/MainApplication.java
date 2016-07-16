package com.fwest98.fingify;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fwest98.fingify.Helpers.ExceptionHandler;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@ReportsCrashes(
    httpMethod = HttpSender.Method.PUT,
    reportType = HttpSender.Type.JSON,
    formUri = "http://logging.fingify.nl:5984/acra-fingify/_design/acra-storage/_update/report",
    formUriBasicAuthLogin = "fingify_reporter",
    formUriBasicAuthPassword = "c#%&%A#kE3#9"
)
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if(!BuildConfig.GRADLE_DEBUG) {
            ACRA.init(this);
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        Log.d("GRADLE DEBUG", String.valueOf(BuildConfig.GRADLE_DEBUG));
        Log.d("DEBUG", String.valueOf(BuildConfig.DEBUG));

        if(BuildConfig.GRADLE_DEBUG) {
            // Should NOT be used on production!
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                };
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier((s, sslSession) -> s.contains("fingify.testserver.test.home"));
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                ExceptionHandler.handleException(new Exception("Certs not accepted!", e), this, true);
            }
        }
    }
}
