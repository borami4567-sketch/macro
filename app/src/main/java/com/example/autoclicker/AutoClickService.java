package com.example.autoclicker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class AutoClickService extends AccessibilityService {
    public static boolean isServiceArmed = false;
    public static AutoClickService instance = null;

    private boolean isClicking = false;
    private float targetX = -1f;
    private float targetY = -1f;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private View overlayView;
    private WindowManager windowManager;

    private final Runnable clickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isClicking || !isServiceArmed || targetX < 0 || targetY < 0) return;
            performTap(targetX, targetY);
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (!isServiceArmed) {
            return super.onKeyEvent(event);
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if (!isClicking) {
                    showTargetOverlay();
                }
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                stopClicking();
                return true;
            }
        }
        return super.onKeyEvent(event);
    }

    public void forceStopAll() {
        isClicking = false;
        handler.removeCallbacks(clickRunnable);
        removeOverlay();
    }

    private void showTargetOverlay() {
        if (overlayView != null) return;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = new View(this);
        overlayView.setBackgroundColor(Color.argb(60, 0, 150, 255));

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        overlayView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                targetX = event.getRawX();
                targetY = event.getRawY();
                removeOverlay();
                startClicking();
                return true;
            }
            return false;
        });

        windowManager.addView(overlayView, params);
        Toast.makeText(this, "반복 클릭할 위치를 터치하세요", Toast.LENGTH_SHORT).show();
    }

    private void removeOverlay() {
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception ignored) {}
            overlayView = null;
        }
    }

    private void startClicking() {
        isClicking = true;
        Toast.makeText(this, "자동 클릭 시작 (볼륨 올리기로 중지)", Toast.LENGTH_SHORT).show();
        handler.post(clickRunnable);
    }

    private void stopClicking() {
        isClicking = false;
        handler.removeCallbacks(clickRunnable);
        removeOverlay();
        Toast.makeText(this, "자동 클릭 중지됨", Toast.LENGTH_SHORT).show();
    }

    private void performTap(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 50));
        dispatchGesture(builder.build(), null, null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {
        forceStopAll();
        instance = null;
    }
}
