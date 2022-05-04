package lkh.labs.scalper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyPythonInterface myPythonInterface = new MyPythonInterface(this);
        String auth_code_url = myPythonInterface.get_authcode_url();
        Fyers_login_(auth_code_url, myPythonInterface);

    }

    public void Fyers_login_(String authcode_url, MyPythonInterface myPythonInterface){
        final String[] fyers_auth_code = {""};

        WebView webView = (WebView) findViewById(R.id.login_page_wv);
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        CookieManager.getInstance().removeAllCookies(null);

        final boolean[] set_client_id = {false};
        final boolean[] set_pwd = {false};

        webView.loadUrl(authcode_url);

        Runnable pin_injector = new Runnable() {
            @Override
            public void run(){
                webView.evaluateJavascript("javascript:"+
                        "var pin_boxes = document.getElementById('pin-container').children;"+
                        "pin_boxes[0].value = 1;"+
                        "pin_boxes[1].value = 2;"+
                        "pin_boxes[2].value = 3;"+
                        "pin_boxes[3].value = 4;", null);
                webView.evaluateJavascript("javascript:document.getElementById('verifyPinSubmit').click();", null);
            }
        };

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(url.equals(authcode_url) && !set_client_id[0]){
                    view.evaluateJavascript("javascript:document.getElementById('fy_client_id').value = 'client_id';", null);
                    view.evaluateJavascript("javascript:document.getElementById('clientIdSubmit').click();", null);
                    set_client_id[0] = true;
                }
                else if(url.equals(authcode_url) && !set_pwd[0]){
                    view.evaluateJavascript("javascript:document.getElementById('fy_client_pwd').value = 'password';", null);
                    view.evaluateJavascript("javascript:document.getElementById('loginSubmit').click();", null);
                    set_pwd[0] = true;

                    Handler pin_injector_handler = new Handler();
                    pin_injector_handler.postDelayed(pin_injector, 5000);
                }
                else{
                    Uri uri=Uri.parse(url);
                    fyers_auth_code[0] = uri.getQueryParameter("auth_code");

                    Integer status_code = myPythonInterface.fetch_access_token(fyers_auth_code[0]);
                    if(status_code != 200) {
                        Toast.makeText(MainActivity.this, "Fail: Access token not generated", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    static class MyPythonInterface {
        Python py_instance;
        PyObject Fyers;
        Context context;

        public MyPythonInterface(Context context) {
            if (!Python.isStarted()) {
                Python.start(new AndroidPlatform(context));
            }
            py_instance = Python.getInstance();
            this.context = context;
            Fyers = py_instance.getModule("Fyers").callAttr("fyers");
        }

        public String get_authcode_url() {
            return Fyers.callAttr("fetch_authcode_url").toString();
        }

        public Integer fetch_access_token(String fyers_auth_code) {
            String access_token = Fyers.callAttr("fetch_access_token", fyers_auth_code).toString();
            return Fyers.callAttr("init_fyersmodel", access_token, context.getFilesDir()).toInt();
        }
    }
}