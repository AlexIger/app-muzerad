//package com.cyberia.radio;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.ProgressBar;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.app.AppCompatDelegate;
//
//import com.cyberia.radio.global.MyHandler;
//import com.cyberia.radio.helpers.ExceptionHandler;
//import com.cyberia.radio.helpers.MyPrint;
//import com.cyberia.radio.io.ServerLookup;
//
//public class SplashActivity extends AppCompatActivity
//{
//    private volatile ProgressBar  progress;
//
//    static
//    {
//        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_splash);
////        progress = findViewById(R.id.progress_bar_splash);
//    }
//
//    @Override
//    protected void onStart()
//    {
//        super.onStart();
//
////        synchronized (this)
////        {
////            startProgress();
////            Thread initServer = new Thread(() -> ServerLookup.setCurrentServer());
////            initServer.start();
////            try
////            {
////                initServer.join();
////            } catch (InterruptedException ie)
////            {
////                ExceptionHandler.onException("Splash", ie);
////            }
////        }
//
//        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
//        Intent intentReceive = getIntent();
//        intent.putExtra(Intent.EXTRA_INTENT, intentReceive);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        cancelProgress();
//        // close splash activity
//        finish();
//    }
//
//    private void cancelProgress()
//    {
//        MyHandler.post(() -> {
//            if (progress != null)
//            {
//                MyPrint.printOut("Splash close", "Progress not null");
//                progress.setVisibility(ProgressBar.GONE);
//            }
//
//            else
//                MyPrint.printOut("Splash close", "Progress  = null");
//        });
//    }
//
//    private void startProgress()
//    {
//        MyHandler.postDelayed(() -> {
//            if (progress != null)
//            {
//                MyPrint.printOut("Splash start", "Progress not null");
//                progress.setVisibility(ProgressBar.VISIBLE);
//            }
//            else
//                MyPrint.printOut("Splash start", "Progress == null");
//        }, 500);
//    }
//
//}
