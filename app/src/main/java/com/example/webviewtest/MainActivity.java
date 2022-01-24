/*
 @ SDK 버전을 "build.gradle(Module..."에서
 "compilesdk"와 "targetsdk"를 31로 바꾼다.
 다른 버전을 쓰면 뭔가 이상했음. - 22-01-19

 @ 웹뷰 인터넷 사용 설정
 <uses-permission android:name="android.permission.INTERNET"/>
 를 AndroidManifest.xml에 추가한다.

 @타이틀바 제거하기
 res/values 우클릭 > new > XML > Values XML
 이름에 styles 라고 입력해서 styles.xml 생성

 아래 내용 붙여넣음.

 <?xml version="1.0" encoding="utf-8"?>
 <resources>
    <!-- Base application theme. -->
    <style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">

        <!-- Customize your theme here. -->
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
        <item name="windowActionBar">false</item>
        <item name="windowNoTitle">true</item>
    </style>
 </resources>

 그리고 values/colors.xml 에 아래 내용 추가

 @color/colorPrimary
 <color name="colorPrimary">#3F51B5</color>
 @color/colorPrimaryDark
 <color name="colorPrimaryDark">#303F9F</color>
 @color/colorAccent
 <color name="colorAccent">#FF4081</color>
*/

package com.example.webviewtest;

// 웹뷰에 필요한 기본 import
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

// 메세지 박스 다이얼로그
import android.app.AlertDialog;
import android.content.DialogInterface;

// 스크롤 Disable
import android.view.View;
import android.view.MotionEvent;

// 자바스크립트 인터페이스
import android.webkit.JavascriptInterface;
import android.content.Context;

// 디버그 로그뷰어 (하단 logcat에서 확인)
import android.util.Log;

public class MainActivity extends AppCompatActivity
{
    private WebView webView;
    private DBHelper helper;

    // Thread test
    private Thread thread_test;
    private boolean flag_thread_test;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(android.R.style.Theme_NoTitleBar);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webView);

        // Init SQLite Database
        helper = new DBHelper(MainActivity.this, "newdb.db");
        helper.DBInit();

        // 부트스트랩 css를 위한 처리
        // HTML 헤더에
        // <meta name="viewport" content="width=device-width" />
        // 추가 해야됨 (assets/index.html 참고)
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        // 스크롤 안되게 하는 코드
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setOnTouchListener(new View.OnTouchListener()
        {
            public boolean onTouch(View v, MotionEvent event)
            {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }

        });

        /* 웹 세팅 작업하기 */
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setSaveFormData(false);
        webSettings.setSavePassword(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        // jQuery Ajax에 필요
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        // 리다이렉트 할 때 브라우저 열리는 것 방지
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        // 자바스크립트 인터페이스 등록
        webView.addJavascriptInterface(new WebAppInterface(this), "android");

        // 내부 html 파일을 사용
        // "assets"의 경로는 "android_asset"이 된다.
        // 프로젝트 인스펙터에서
        // app -> 오른쪽 클릭 -> new -> folder -> assets folder
        webView.loadUrl("file:///android_asset/index.html");
    }

    // 뒤로가기 버튼 눌렸을때 처리
    @Override
    public void onBackPressed()
    {
        if(webView.canGoBack())
        {
            // 여기를 주석처리하면 이전 페이지로 안간다.
            // 종료 버튼을 따로 두어 처리하는것도 좋을듯.
            webView.goBack();
        }
        else
        {
            // 뒤로 버튼 눌렸을때 프로그램 종료 물어보기
            AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
            dlg.setTitle("종료할거임?"); //제목
            dlg.setMessage("프로그램을 종료 하시겠습니까?"); // 메시지
            //dlg.setIcon(R.drawable.deum); // 아이콘 설정

            // 버튼 클릭시 동작
            // 종료함
            dlg.setPositiveButton("확인",new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    finish();
                }
            });
            // 취소
            dlg.setNegativeButton("취소",new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                }
            });
            dlg.show();
        }
    }

    // 자바스크립트 인터페이스 이너클래스 구현
    public class WebAppInterface
    {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c)
        {
            mContext = c;

            // 러너블 클래스로 쓰레드 생성
            thread_test = new Thread(new rThreadClass(this));
            thread_test.start();
        }

        @JavascriptInterface
        public void setTextView(final String msg)
        {
            // 로그보기
            Log.d("TAG", msg);

            // 자바스크립트에서 호출되며 같이온 파라메타를 이용해서 무엇인가 하고.
            // (자바의 기능과 네이티브 코드가 사용 가능해짐/백엔드 처리 개념)
            String msg_out = msg + " 네이티브 코드에서 무엇인가 가공됨 :)";

            // 쓰레드 간섭문제로 자바스크립트 이벤트 처리시 아래와 같이 post/Runnable 인터페이스 사용
            webView.post(new Runnable()
            {
                public void run()
                {
                    // 결과를 자바스크립트로 리턴
                    webView.loadUrl("javascript:setReceivedMessage('" + msg_out + "')");
                }
            });
        }

        // DB Test
        @JavascriptInterface
        public void dbtest(final String txt)
        {
            Log.d("DBTEST", txt);
            helper.insert_data(txt);
        }

        // 쓰레드에서 사용될 자바스크립트 인터페이스
        @JavascriptInterface
        public void thredtest(final String msg)
        {
            webView.post(new Runnable()
            {
                public void run()
                {
                    // 결과를 자바스크립트로 리턴
                    webView.loadUrl("javascript:threadtest('" + msg + "')");
                }
            });
        }
    } // public class WebAppInterface

    // 쓰레드 러너블 클래스
    private class rThreadClass implements Runnable
    {
        int i;
        WebAppInterface wi; // 웹뷰 연동을위한 브릿지

        public rThreadClass(WebAppInterface wi)
        {
            i = 0;
            this.wi = wi;
        }
        public void run()
        {
            flag_thread_test = true;

            while(flag_thread_test)
            {
                // Sleep 사용시 try/catch
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e){ }

                wi.thredtest("Thread >> " + i);
                i++;
            }
            Log.d("THREAD", "Thread End");
        } //public void run()
    } // private class rThreadClass implements Runnable

    // 각종 오버라이드들
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("callBack", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("callBack", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("callBack", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("callBack", "onStop");
    }

    // 프로그램 종료시 쓰레드도 같이 종료되게
    @Override
    protected void onDestroy()
    {
        flag_thread_test = false;
        try
        {
            thread_test.join();
        }
        catch (InterruptedException e) { }

        super.onDestroy();
        Log.d("Click", "onDestroy");
    }
}