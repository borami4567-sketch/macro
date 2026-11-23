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
    public static int macroMode = 1; // 1: 1지점(초당 1회), 2: 2지점 번갈아(초당 20회)
    public static AutoClickService instance = null;

    private boolean isClicking = false;
    private float targetX1 = -1f, targetY1 = -1f;
    private float targetX2 = -1f, targetY2 = -1f;
    private int pointStep = 0; // 좌표 입력 단계 (0: 첫 번째 대기, 1: 두 번째 대기)
    private boolean clickFirstNext = true; // 2지점 번갈아 클릭 순서 제어

    private final Handler handler = new Handler(Looper.getMainLooper());
    private View overlayView;
    private WindowManager windowManager;

    private final Runnable clickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isClicking || !isServiceArmed) return;

            if (macroMode == 1) {
                if (targetX1 < 0 || targetY1 < 0) return;
                performTap(targetX1, targetY1, 50);
                handler.postDelayed(this, 1000); // 초당 1회 (1000ms)
            } else {
                if (targetX1 < 0 || targetY1 < 0 || targetX2 < 0 || targetY2 < 0) return;
                if (clickFirstNext) {
                    performTap(targetX1, targetY1, 15);
                } else {
                    performTap(targetX2, targetY2, 15);
                }
                clickFirstNext = !clickFirstNext;
                handler.postDelayed(this, 50); // 초당 20회 (1000ms / 20 = 50ms)
            }
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
                if (!isClicking && overlayView == null) {
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
        pointStep = 0;
        handler.removeCallbacks(clickRunnable);
        removeOverlay();
    }

    private void showTargetOverlay() {
        if (overlayView != null) return;
        pointStep = 0;
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
                if (macroMode == 1) {
                    targetX1 = event.getRawX();
                    targetY1 = event.getRawY();
                    removeOverlay();
                    startClicking();
                } else {
                    if (pointStep == 0) {
                        targetX1 = event.getRawX();
                        targetY1 = event.getRawY();
                        pointStep = 1;
                        overlayView.setBackgroundColor(Color.argb(60, 255, 100, 0));
                        Toast.makeText(this, "두 번째 클릭할 위치를 터치하세요", Toast.LENGTH_SHORT).show();
                    } else {
                        targetX2 = event.getRawX();
                        targetY2 = event.getRawY();
                        pointStep = 0;
                        removeOverlay();
                        startClicking();
                    }
                }
                return true;
            }
            return false;
        });

        windowManager.addView(overlayView, params);
        if (macroMode == 1) {
            Toast.makeText(this, "반복 클릭할 위치를 터치하세요", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "첫 번째 클릭할 위치를 터치하세요", Toast.LENGTH_SHORT).show();
        }
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
        clickFirstNext = true;
        Toast.makeText(this, "자동 클릭 시작 (볼륨 올리기로 중지)", Toast.LENGTH_SHORT).show();
        handler.post(clickRunnable);
    }

    private void stopClicking() {
        isClicking = false;
        pointStep = 0;
        handler.removeCallbacks(clickRunnable);
        removeOverlay();
        Toast.makeText(this, "자동 클릭 중지됨", Toast.LENGTH_SHORT).show();
    }

    private void performTap(float x, float y, long durationMs) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, durationMs));
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
