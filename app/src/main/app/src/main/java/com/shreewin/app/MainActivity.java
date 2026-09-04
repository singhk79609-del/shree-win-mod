package com.shreewin.app;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        
        webView.setWebViewClient(new WebViewClient());
        // Updated with your referral invitation link
        webView.loadUrl("https://www.shreewin46.com/#/register?invitationCode=26752188803");

        // Check for overlay permission to start the floating widget
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!android.provider.Settings.CanDrawOverlays(this)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            } else {
                startService(new Intent(MainActivity.this, FloatingWidgetService.class));
            }
        } else {
            startService(new Intent(MainActivity.this, FloatingWidgetService.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (android.provider.Settings.CanDrawOverlays(this)) {
                    startService(new Intent(MainActivity.this, FloatingWidgetService.class));
                } else {
                    Toast.makeText(this, "Permission denied for Overlay!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // ==========================================
    // FLOATING WIDGET SERVICE (Merged Inside)
    // ==========================================
    public static class FloatingWidgetService extends Service {
        private WindowManager mWindowManager;
        private View mFloatingView;
        private View mCollapsedView;
        private View mExpandedView;

        private int simulatedBalance = 100; // Initial balance setup for gate logic

        public FloatingWidgetService() {
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();

            mFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_ball_layout, null);

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                            WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 0;
            params.y = 100;

            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            mWindowManager.addView(mFloatingView, params);

            mCollapsedView = mFloatingView.findViewById(R.id.collapse_view);
            mExpandedView = mFloatingView.findViewById(R.id.expanded_container);

            // Dragging logic for the floating ball
            mCollapsedView.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;
                        case MotionEvent.ACTION_UP:
                            int Xdiff = (int) (event.getRawX() - initialTouchX);
                            int Ydiff = (int) (event.getRawY() - initialTouchY);
                            if (Xdiff < 10 && Ydiff < 10) {
                                // Open Panel on Click
                                mCollapsedView.setVisibility(View.GONE);
                                mExpandedView.setVisibility(View.VISIBLE);
                            }
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            mWindowManager.updateViewLayout(mFloatingView, params);
                            return true;
                    }
                    return false;
                }
            });

            // Close panel button
            ImageView closeButton = mFloatingView.findViewById(R.id.close_button);
            if (closeButton != null) {
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mExpandedView.setVisibility(View.GONE);
                        mCollapsedView.setVisibility(View.VISIBLE);
                    }
                });
            }

            // Start / Prediction Button Logic
            Button btnStart = mFloatingView.findViewById(R.id.btn_start);
            final TextView txtResult = mFloatingView.findViewById(R.id.txt_result);

            if (btnStart != null) {
                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (simulatedBalance >= 100) {
                            // 1-minute time-locked prediction algorithm using current time stamp seed
                            long currentMinuteWindow = System.currentTimeMillis() / 60000;
                            Random random = new Random(currentMinuteWindow);
                            String[] outcomes = {"BIG 🔴", "SMALL 🟢", "DRAGON 🐉", "TIGER 🐅"};
                            String prediction = outcomes[random.nextInt(outcomes.length)];
                            
                            txtResult.setText("Prediction: " + prediction);
                        } else {
                            txtResult.setText("Low Balance! Please Deposit.");
                        }
                    }
                });
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
        }
    }
}
