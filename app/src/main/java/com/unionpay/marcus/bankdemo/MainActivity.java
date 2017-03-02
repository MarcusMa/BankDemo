package com.unionpay.marcus.bankdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

import static android.R.layout.simple_spinner_item;

public class MainActivity extends AppCompatActivity {
    private WebView mWebView;
    private TextView mBill, mLimit, mBonus;
    private EditText mUserId,mPassword,mCode;
    private ImageView mImage;
    private Button mLogin, mClearCookies;
    private Spinner mCardNoSpinner;
    private ArrayAdapter mCardNoListAdapter;
    private Context mContext;
    private final String TAG = "test";

    private String cmbchina_url = "https://xyk.cmbchina.com/login";
    private String cmbchina_basic_url = "https://xyk.cmbchina.com";
    private final String CMBCHINA_GET_TOTAL_BILL = "getTotalBill.do";
    private final String CMBCHINA_QUERY_LIMIT_BY_SESSION = "queryLimitBySession.do";
    private final String CMBCHINA_QUERY_BONUS_BY_SESSION = "queryBonusBySession.do";
    private final String CMBCHINA_SESSION_SESS = "session.sess";
    private final String CMBCHINA_LOGIN = "login.do";
    private final String CMBCHINA_VALID_CODE = "captcha.code";

    private String bankcomm_url = "https://creditcardapp.bankcomm.com/idm/sso/login.html";
    private String bankcomm_basic_url = "https://creditcardapp.bankcomm.com/";

    private final int MSG_UPDATE_BILL_TEXT = 1;
    private final int MSG_UPDATE_LIMIT_TEXT = 2;
    private final int MSG_UPDATE_BONUS_TEXT = 3;
    private final int MSG_HIDE_WEB_VIEW = 4;
    private final int MSG_SHOW_WEB_VIEW = 5;
    private final int MSG_UPDATE_VALID_CODE = 6;

    private final int MSG_CMBCHINA_DO_ALL_REQUEST = 31;
    private final int MSG_CMBCHINA_DO_LOGIN = 32;
    private final int MSG_CMBCHINA_SESSION_SESS = 33;
    private final int MSG_CMBCHINA_REQ_VALID_CODE = 34;

    private final int MSG_BANKCOMM_DO_ALL_REQUEST = 51;
    private final int MSG_BANKCOMM_DO_BILLING_DETIAL_REQUEST = 52;
    private final int MSG_BANKCOMM_UPDATE_CARD_SELECT_LIST = 53;

    private final String BANKCOMM_GET_CARD_LIST = "sac/user/cardList.html";
    private final String BANKCOMM_INTEGRATION_INFO = "member/member/financial/billing/integrationInfo.json";
    private final String BANKCOMM_BALANCE_QRY = "member/member/service/billing/balanceQry.html"; // 额度
    private final String BANKCOMM_LIMIT_QRY = "member/member/limit/info.json";
    private final String BANKCOMM_BILLING_INFO_QRY = "member/member/service/billing/billingInfoQry.html"; // 账单
    private final String BANKCOMM_POINT_INFO_QRY = "member/member/service/billing/pointInfoQry.html"; // 积分
    private final String BANKCOMM_FINISHED = "member/member/service/billing/finished.html";
    private final String BANKCOMM_BILLING_DETIAL = "member/member/service/billing/detail.html"; //https://creditcardapp.bankcomm.com/
    private final String BANKCOMM_I_JIAME_SERVLET = "https://creditcardapp.bankcomm.com/idm/IjiamiServlet";
    private final String BANKCOMM_LOGIN = "idm/sso/login.html";

    // for cmbchina
    private String userSessionId="";
    private String uuId = "";
    private String enUserId = "";
    private String enPassword = "";
    private Bitmap codePic;

    // for bankcomm
    private List<String> bankCommCardList = new ArrayList<>();

//    private final String CMBCHINA_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
//            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDd844tvJK4okLS0w3YUlgplte6\n" +
//            "cGFK7+6hDWfyxu99iLJEFnTW5AikqLpvn+E+oioZ5DiGjGhLxqPI45iGzDdJBWx8\n" +
//            "bNWkvmT/gAfTC/k0/6ZbgbycrLtxHKToldVS5e4UX+GcqFd+79la/pWLttdG9T/3\n" +
//            "wRE1KVmh36RuN32vWwIDAQAB\n" +
//            "-----END PUBLIC KEY-----";

    private final String CMBCHINA_PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDd844tvJK4okLS0w3YUlgplte6cGFK7+6hDWfyxu99iLJEFnTW5AikqLpvn+E+oioZ5DiGjGhLxqPI45iGzDdJBWx8bNWkvmT/gAfTC/k0/6ZbgbycrLtxHKToldVS5e4UX+GcqFd+79la/pWLttdG9T/3wRE1KVmh36RuN32vWwIDAQAB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mWebView = (WebView) findViewById(R.id.my_webview);
        mBill = (TextView) findViewById(R.id.tx_bill);
        mBonus = (TextView) findViewById(R.id.tx_bonus);
        mLimit = (TextView) findViewById(R.id.tx_limit);
        mUserId = (EditText) findViewById(R.id.userId);
        mPassword = (EditText) findViewById(R.id.password);

        mCardNoSpinner = (Spinner) findViewById(R.id.cardNo_Spinner);

        mCardNoSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(mContext, bankCommCardList.get(position),Toast.LENGTH_SHORT).show();
                String selectedCardNo = bankCommCardList.get(position);
                if( null != selectedCardNo && position!=0);
                {
                    Message msg  = Message.obtain();
                    msg.what = MSG_BANKCOMM_DO_ALL_REQUEST;
                    msg.obj = bankCommCardList.get(position);
                    handler.sendMessage(msg);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mCode = (EditText) findViewById(R.id.code);
        mImage = (ImageView) findViewById(R.id.imageCode);
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.imageCode){
                    handler.sendEmptyMessage(MSG_CMBCHINA_REQ_VALID_CODE);
                }
            }
        });

        mLogin = (Button) findViewById(R.id.btn_login);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.btn_login){
                    try {
                        RSAUtils.loadPublicKey(CMBCHINA_PUBLIC_KEY);
                        enUserId = RSAUtils.encryptWithRSA(mUserId.getText().toString());
                        enPassword = RSAUtils.encryptWithRSA(mPassword.getText().toString());
                        handler.sendEmptyMessage(MSG_CMBCHINA_SESSION_SESS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mClearCookies = (Button) findViewById(R.id.btn_logout);
        mClearCookies.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // handler.sendEmptyMessage(MSG_BANKCOMM_DO_ALL_REQUEST);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                CookieSyncManager.getInstance().sync();

            }
        });
        /* for cmbchina */
        mUserId.setText("530326199112283936");
        mPassword.setText("159357");

        // handler.sendEmptyMessage(MSG_CMBCHINA_REQ_VALID_CODE); // cmbchina
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e("test", "shouldOverrideUrlLoading: " + url);
                // handler.sendEmptyMessage(MSG_HIDE_WEB_VIEW);
                // handler.sendEmptyMessage(MSG_CMBCHINA_DO_ALL_REQUEST); // for cmbchina
                //return false;
                // return super.shouldOverrideUrlLoading(view, cmbchina_url);
                return super.shouldOverrideUrlLoading(view,url);
            }


            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (url.contains("index.html"))
                {
                    Log.e("test", "shouldInterceptRequest: " + url);
                    handler.sendEmptyMessage(MSG_HIDE_WEB_VIEW);
                    // handler.sendEmptyMessage(MSG_CMBCHINA_DO_ALL_REQUEST); for cmbchina
                    handler.sendEmptyMessage(MSG_BANKCOMM_DO_ALL_REQUEST);
                }

                return super.shouldInterceptRequest(view, url);
            }
        });
        // CookieManager.getInstance().getCookie(cmbchina_url); // cmbchina
        // String cookieString = CookieManager.getInstance().getCookie(cmbchina_url);
        CookieManager.getInstance().getCookie(bankcomm_url); // bankcomm
        String cookieString = CookieManager.getInstance().getCookie(bankcomm_url);
        Log.d("test","cookie:" + cookieString);
        if (cookieString!= null && !cookieString.isEmpty()){
            handler.sendEmptyMessage(MSG_HIDE_WEB_VIEW);
            String[] cookies = cookieString.split(";");

            for(String tmpCookie : cookies){
                String[] array = tmpCookie.trim().split("=");
                if(array.length == 2){
                    String key = array[0].trim();
                    String value = array[1].trim();
                    Log.d("test", "coookie key: " + key + " value: " + value);
                    if("userSessionId".equalsIgnoreCase(key)){
                        userSessionId = value;
                        // handler.sendEmptyMessage(MSG_CMBCHINA_DO_ALL_REQUEST); // cmbchina
                        // handler.sendEmptyMessage(MSG_BANKCOMM_DO_ALL_REQUEST); // bankcomm
                        // sendRequest(CMBCHINA_GET_TOTAL_BILL);
                    }
                }
            }
            // handler.sendEmptyMessage(MSG_BANKCOMM_DO_ALL_REQUEST);
            handler.sendEmptyMessage(MSG_BANKCOMM_DO_BILLING_DETIAL_REQUEST);
            // mWebView.loadUrl("https://creditcardapp.bankcomm.com/member/member/home/index.html");
        }
        else {
            handler.sendEmptyMessage(MSG_SHOW_WEB_VIEW);
        }

        /** start keep live service **/
        Intent intent = new Intent(this,KeepLiveService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);

    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String str = (String) msg.obj;
            switch (msg.what){
                case MSG_CMBCHINA_DO_ALL_REQUEST:
                    CmbChinaTask task = new CmbChinaTask();
                    task.execute(CMBCHINA_GET_TOTAL_BILL);
                    CmbChinaTask task2 = new CmbChinaTask();
                    task2.execute(CMBCHINA_QUERY_BONUS_BY_SESSION);
                    CmbChinaTask task3 = new CmbChinaTask();
                    task3.execute(CMBCHINA_QUERY_LIMIT_BY_SESSION);
                    break;
                case MSG_CMBCHINA_REQ_VALID_CODE:
                    CmbChinaTask task6 = new CmbChinaTask();
                    task6.execute(CMBCHINA_VALID_CODE);
                    break;
                case MSG_UPDATE_VALID_CODE:
                    mImage.setImageBitmap(codePic);
                    break;
                case MSG_CMBCHINA_SESSION_SESS:
                    CmbChinaTask task4 = new CmbChinaTask();
                    task4.execute(CMBCHINA_SESSION_SESS,enUserId);
                    break;
                case MSG_CMBCHINA_DO_LOGIN:
                    CmbChinaTask task5 = new CmbChinaTask();
                    task5.execute(CMBCHINA_LOGIN,enPassword,enUserId,mCode.getText().toString(),uuId);
                    break;
                case MSG_UPDATE_BILL_TEXT:
                    mBill.setText(str);
                    break;
                case MSG_UPDATE_BONUS_TEXT:
                    mBonus.setText(str);
                    break;
                case MSG_UPDATE_LIMIT_TEXT:
                    mLimit.setText(str);
                    break;
                case MSG_HIDE_WEB_VIEW:
                    mWebView.setVisibility(View.GONE);
                    break;
                case MSG_SHOW_WEB_VIEW:
                    mWebView.setVisibility(View.VISIBLE);
                    // mWebView.loadUrl(cmbchina_url); // for cmbchina
                    mWebView.loadUrl(bankcomm_url);
                    break;
                case MSG_BANKCOMM_DO_ALL_REQUEST:
                    BankCommTask task21 = new BankCommTask();
                    BankCommTask task23 = new BankCommTask();
                    BankCommTask task24 = new BankCommTask();
                    BankCommTask task25 = new BankCommTask();
                    BankCommTask task26 = new BankCommTask();
                    String cardNo =(String) msg.obj;
                    try {
                        cardNo = URLEncoder.encode(cardNo,"utf-8");
                        // task21.execute(BANKCOMM_BALANCE_QRY,cardNo);
                        task21.execute(BANKCOMM_LIMIT_QRY,cardNo);
                        task23.execute(BANKCOMM_BILLING_INFO_QRY,cardNo);
                        task24.execute(BANKCOMM_POINT_INFO_QRY,cardNo);
                        // task25.execute(BANKCOMM_FINISHED,cardNo,"20170117");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_BANKCOMM_DO_BILLING_DETIAL_REQUEST:
                    BankCommTask task27 = new BankCommTask();
                    task27.execute(BANKCOMM_BILLING_DETIAL);
                    break;
                case MSG_BANKCOMM_UPDATE_CARD_SELECT_LIST:
                    mCardNoListAdapter = new ArrayAdapter<String>(mContext, simple_spinner_item, bankCommCardList);
                    mCardNoListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mCardNoSpinner.setAdapter(mCardNoListAdapter);
                    break;
                default:break;
            }
            super.handleMessage(msg);
        }
    };

    private class CmbChinaTask extends AsyncTask<String, Integer, String> {

        private String currentActionName;

        //onPreExecute方法用于在执行后台任务前做一些UI操作
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute() called");

        }

        //doInBackground方法内部执行后台任务,不可在此方法内修改UI
        @Override
        protected String doInBackground(String... params) {
            Log.i(TAG, "doInBackground(Params... params) called");
            JSONObject obj = new JSONObject();
            currentActionName = params[0];
            String requestUrl = cmbchina_basic_url + "/" + currentActionName;
            String method = "POST";
            try {
                obj.put("actionName",currentActionName);
                if(CMBCHINA_SESSION_SESS.equalsIgnoreCase(currentActionName)){
                    obj.put("idNo",params[1]);
                    obj.put("idType","0");
                    obj.put("x_caller",".0");
                    obj.put("x_mchannel","webapp");
                    obj.put("x_traceid","webapp");
                }else if(CMBCHINA_LOGIN.equalsIgnoreCase(currentActionName)){
                    obj.put("encodedStr","");
                    obj.put("loginType","0");
                    obj.put("pin",params[1]);
                    obj.put("realId",params[2]);
                    obj.put("type","0");
                    obj.put("valicode",params[3]);
                    obj.put("x_caller","webapp");
                    obj.put("x_mchannel","webapp");
                    obj.put("x_traceid",params[4]);
                }
                else if(CMBCHINA_VALID_CODE.equalsIgnoreCase(currentActionName)){
                    method = "GET";
                }
                else{
                    obj.put("actionName",currentActionName);
                    obj.put("userSessionId", userSessionId);
                    obj.put("x_caller", ".0");
                    obj.put("x_mchannel", "webapp");
                    obj.put("x_traceid", "webapp");
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            return sendRequest(requestUrl,method,obj);
        }

        //onPostExecute方法用于在执行完后台任务后更新UI,显示结果
        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "onPostExecute(Result result) called");
            if (currentActionName == CMBCHINA_VALID_CODE) {
                handler.sendEmptyMessage(MSG_UPDATE_VALID_CODE);
            }
            else {
                try {
                    JSONObject json = new JSONObject(result);
                    String respCode = json.optString("respCode");
                    if ("1000".equalsIgnoreCase(respCode)) {
                        Message msg = Message.obtain();
                        switch (currentActionName) {
                            case CMBCHINA_GET_TOTAL_BILL:
                                msg.what = MSG_UPDATE_BILL_TEXT;
                                msg.obj = json.optJSONArray("data").toString();
                                break;
                            case CMBCHINA_QUERY_BONUS_BY_SESSION:
                                msg.what = MSG_UPDATE_BONUS_TEXT;
                                JSONArray tmpJson = json.getJSONArray("bonusDetails");
                                JSONObject tmp = tmpJson.getJSONObject(0);
                                msg.obj = tmp.optString("bonus");
                                break;
                            case CMBCHINA_QUERY_LIMIT_BY_SESSION:
                                msg.what = MSG_UPDATE_LIMIT_TEXT;
                                msg.obj = json.optString("creditLimit");
                                break;
                            case CMBCHINA_VALID_CODE:
                                // TODO: 17/2/23
                                break;
                            case CMBCHINA_SESSION_SESS:
                                uuId = json.getString("uuid");
                                msg.what = MSG_CMBCHINA_DO_LOGIN;
                                break;
                            case CMBCHINA_LOGIN:
                                userSessionId = json.getString("userSessionId");
                                CookieManager cookieManager = CookieManager.getInstance();
                                cookieManager.setCookie(cmbchina_url, "userSessionId=" +userSessionId);
                                CookieSyncManager.getInstance().sync();
                                msg.what = MSG_CMBCHINA_DO_ALL_REQUEST;
                            default:
                                break;
                        }
                        handler.sendMessage(msg);
                    } else {
                        if (mWebView.getVisibility() != View.VISIBLE)
                            handler.sendEmptyMessage(MSG_SHOW_WEB_VIEW);
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private class BankCommTask extends  AsyncTask<String,Integer,String>{

        private String currentActionName;

        //onPreExecute方法用于在执行后台任务前做一些UI操作
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute() called");

        }

        @Override
        protected String doInBackground(String... params) {
            currentActionName = params[0];
            String requestUrl = null;
            JSONObject paramsObj = null;
            String method = "GET";
            switch (currentActionName){
                case BANKCOMM_GET_CARD_LIST:
                    requestUrl = bankcomm_basic_url + currentActionName;
                    break;
                case BANKCOMM_INTEGRATION_INFO:
                    String cardNo = params[1];
                    requestUrl = bankcomm_basic_url + currentActionName + "?v="+ String.valueOf(Math.random()) + "&cardNo=" + cardNo;
                    break;
                case BANKCOMM_FINISHED:
                    requestUrl = bankcomm_basic_url + currentActionName + "?cardNo=" + params[1] + "&billDate=" + params[2];
                    break;
                case BANKCOMM_BALANCE_QRY:
                case BANKCOMM_BILLING_INFO_QRY:
                case BANKCOMM_POINT_INFO_QRY:
                case BANKCOMM_LIMIT_QRY:
                    requestUrl = bankcomm_basic_url + currentActionName + "?cardNo=" + params[1];
                    break;
                case BANKCOMM_BILLING_DETIAL:
                    requestUrl = bankcomm_basic_url + currentActionName ;
                    break;
                default:break;
            }
            return sendRequest(requestUrl,method,paramsObj);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            switch (currentActionName){

                case BANKCOMM_GET_CARD_LIST:

                    break;
                case BANKCOMM_BILLING_INFO_QRY:
                    Document doc = Jsoup.parse(s);//解析HTML字符串返回一个Document实现
                    Elements liSet = doc.select("li");//查找第一个a元素
                    String billMsg = "没有查到账单信息";
                    if (liSet.size()>0){
                        billMsg = "";
                        for(Element element : liSet){
                            billMsg = billMsg + element.text() + "\r\n";
                        }
                    }

                    Message msg2 = Message.obtain();
                    msg2.what = MSG_UPDATE_BILL_TEXT;
                    msg2.obj = billMsg;
                    handler.sendMessage(msg2);
                    break;
                case BANKCOMM_POINT_INFO_QRY:
                    Pattern pattern1 = Pattern.compile("[0-9]+分");
                    Matcher matcher = pattern1.matcher(s);
                    String pointMsg = "未查到您的积分信息";
                    while (matcher.find()) {
                        Log.d(TAG,"groupCount:" + String.valueOf(matcher.groupCount()));
                        pointMsg = matcher.group();
                        Log.d(TAG,"group" + pointMsg);
                        break;
                    }
                    Message msg3 = Message.obtain();
                    msg3.what = MSG_UPDATE_BONUS_TEXT;
                    msg3.obj = pointMsg;
                    handler.sendMessage(msg3);
                    break;
                case BANKCOMM_BALANCE_QRY:
                    Pattern pattern2 = Pattern.compile("￥[0-9]+.[0-9]+");
                    Matcher matcher2 = pattern2.matcher(s);
                    while (matcher2.find()) {
                        Log.d(TAG,"groupCount:" + String.valueOf(matcher2.groupCount()));
                        String tmp = matcher2.group();
                        Log.d(TAG,"group" + tmp);
                        Message msg = Message.obtain();
                        msg.what = MSG_UPDATE_LIMIT_TEXT;
                        msg.obj = tmp;
                        handler.sendMessage(msg);
                        break;
                    }
                    break;
                case BANKCOMM_LIMIT_QRY:
                    String limitMsg = "未能查到信用额度信息";
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        limitMsg = "（可用）: " + jsonObject.optString("avacrdlmt") + " (总)： " + jsonObject.optString("crdLmt");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Message msg33 = Message.obtain();
                    msg33.what = MSG_UPDATE_LIMIT_TEXT;
                    msg33.obj =  limitMsg;
                    handler.sendMessage(msg33);
                    break;
                case BANKCOMM_FINISHED:
                    break;
                case BANKCOMM_BILLING_DETIAL:
                    if(s.contains("登录部分")){
                        handler.sendEmptyMessage(MSG_SHOW_WEB_VIEW);
                    }
                    else{
                        if(bankCommCardList.contains("none")){
                            bankCommCardList.clear();
                        }
                        // bankCommCardList.add("none");
                        Pattern pattern = Pattern.compile("[0-9]{4} \\*{4} \\*{4} [0-9]{4}");
                        Matcher matcher1 = pattern.matcher(s);
                        while (matcher1.find()) {
                            Log.d(TAG,"groupCount:" + String.valueOf(matcher1.groupCount()));
                            String tmp = matcher1.group();
                            Log.d(TAG,"group" + tmp);
                            if(!bankCommCardList.contains(tmp)){
                                bankCommCardList.add(tmp);
                            }
                        }
                        handler.sendEmptyMessage(MSG_BANKCOMM_UPDATE_CARD_SELECT_LIST);
                    }
                    break;
                default:break;
            }
        }
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
                CookieSyncManager.getInstance().sync();
            }

            // set WebResourceResponse to return
            String charset = conn.getContentEncoding() != null ? conn.getContentEncoding() : Charset.defaultCharset().displayName();
            String mime = conn.getContentType();
            InputStream inputStream = conn.getInputStream();

            if (url.contains(CMBCHINA_VALID_CODE)){
                codePic = BitmapFactory.decodeStream(inputStream);
            }

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
}
