package com.unionpay.marcus.bankdemo;

import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

public class KeepLiveService extends Service {
    private static final String TAG = "KeepLiveService";
    private Timer timer;
    private TimerTask task ;

    public KeepLiveService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(null == task){
            task = new TimerTask() {
                @Override
                public void run() {
                    Log.e(TAG,"*************** KEEP LIVE *********************");
                    sendRequest("https://creditcardapp.bankcomm.com/member/member/service/billing/detail.html","GET",null);
                }
            };
        }
        if(null == timer){
            timer = new Timer();
            timer.schedule(task,1000,10000);
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private static TrustManager myX509TrustManager = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            Log.d("X509TrustManager" , "checkClientTrusted()");
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            Log.d("X509TrustManager" , "checkServerTrusted()");
            X509Certificate certificate = chain[0];
            X500Principal issuerPrincipal = certificate.getIssuerX500Principal();
            Log.d("X509TrustManager" , "issuer name :" + issuerPrincipal.getName());
            X500Principal subjectPrincipal = certificate.getSubjectX500Principal();
            Log.d("X509TrustManager" , "subject name :" + subjectPrincipal.getName());

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            Log.d("X509TrustManager" , "getAcceptedIssuers()");
            return null;
        }
    };

    public String sendRequest(String url, String method, JSONObject params){
        try {
            Log.e(TAG, "url: " + url);
            Log.e(TAG, "method: " + method );
            if (null != params){
                Iterator<String> it = params.keys();
                while(it.hasNext()){
                    String key = it.next();
                    Log.e(TAG, "params: key ->" + key + " value ->"+params.getString(key));
                }
            }

            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[]{myX509TrustManager}, null);
            URL mReqUrl = new URL(url);
            URLConnection urlConnection = mReqUrl.openConnection();
            // HttpURLConnection conn = (HttpURLConnection) urlConnection;
            HttpsURLConnection conn = (HttpsURLConnection)  urlConnection;

            //设置套接工厂
            conn.setSSLSocketFactory(sslcontext.getSocketFactory());
            // set request Method
            conn.setRequestMethod(method);

            // set request CooKie
            String cookies = CookieManager.getInstance().getCookie(url);
            Log.d(TAG,"Request Cookie :" + cookies);
            conn.setRequestProperty("Cookie",cookies);

            if(method.equalsIgnoreCase("post")){
                // set request Body
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(params.toString());
                dos.flush();
                dos.close();
            }

            Log.d(TAG,">>>>> Response >>>>>");

            // according "Set-Cookie" field to add cookies into CookieManager
            Map<String, List<String>> headFields = conn.getHeaderFields();
            List<String> cookieList = headFields.get("Set-Cookie");
            if( null != cookieList){
                CookieManager cookieManager = CookieManager.getInstance();
                for(String cookie: cookieList) {
                    Log.e(TAG,"Response > Set-Cookie : "+ cookie);
                    cookieManager.setCookie(url, cookie);
                }
                // CookieSyncManager.getInstance().sync();
            }

            // set WebResourceResponse to return
            String charset = conn.getContentEncoding() != null ? conn.getContentEncoding() : Charset.defaultCharset().displayName();
            String mime = conn.getContentType();
            InputStream inputStream = conn.getInputStream();

            byte[] pageContents = IOUtils.readFully(inputStream);

            // convert the contents and return
            Map<String,String> tmpMap = new HashMap<>();
            for(Map.Entry<String, List<String>> entry: headFields.entrySet()){
                String str = "";
                for( String tampering : entry.getValue()){
                    str = str + tampering;
                }
                tmpMap.put(entry.getKey(),str);
            }

            // convert the contents and return
            String strContents = new String(pageContents, "UTF-8");

            Log.d(TAG," WebResourceResponse >");
            Log.d(TAG, " Mime : " + mime +" Charset : " + charset +
                    " Response Code : " + conn.getResponseCode() +
                    " Phase Reason : " + "OK" +
                    " Header : " + tmpMap.toString() +
                    " Content : " + strContents);
            return strContents;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public void onDestroy() {
        Log.e(TAG,"********************** onDestroy ******************");
        if(null != timer){
            timer.cancel();
            timer = null;
        }
        Intent sevice = new Intent(this, KeepLiveService.class);
        this.startService(sevice);
        super.onDestroy();
    }
}
