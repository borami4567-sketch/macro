package com.example.autoclicker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 100, 60, 60);

        TextView guide = new TextView(this);
        guide.setText("1. 최초 1회 아래 두 권한을 허용해 주세요.\n2. 사용할 때만 [대기 모드 켜기]를 누르세요.\n3. 다 쓴 뒤 [대기 모드 끄기]를 누르면 음량 버튼이 원래대로 돌아옵니다.\n\n");
        guide.setTextSize(16f);
        layout.addView(guide);

        Button btnToggle = new Button(this);
        updateToggleText(btnToggle);
        btnToggle.setOnClickListener(v -> {
            AutoClickService.isServiceArmed = !AutoClickService.isServiceArmed;
            if (!AutoClickService.isServiceArmed && AutoClickService.instance != null) {
                AutoClickService.instance.forceStopAll();
            }
            updateToggleText(btnToggle);
            Toast.makeText(this, AutoClickService.isServiceArmed ? "매크로 대기 모드 켜짐" : "매크로 완전히 꺼짐 (일반 음량 모드)", Toast.LENGTH_SHORT).show();
        });
        layout.addView(btnToggle);

        Button btnOverlay = new Button(this);
        btnOverlay.setText("권한 1: 다른 앱 위에 표시 권한 허용");
        btnOverlay.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });
        layout.addView(btnOverlay);

        Button btnAccess = new Button(this);
        btnAccess.setText("권한 2: 접근성 서비스 켜기 (나만의 오토클리커)");
        btnAccess.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
        layout.addView(btnAccess);

        setContentView(layout);
    }

    private void updateToggleText(Button btn) {
        if (AutoClickService.isServiceArmed) {
            btn.setText("🟢 현재 상태: 매크로 대기 중 (눌러서 끄기)");
        } else {
            btn.setText("⚪ 현재 상태: 매크로 꺼짐 (눌러서 켜기)");
        }
    }
}
