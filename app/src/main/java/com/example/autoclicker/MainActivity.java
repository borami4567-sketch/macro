package com.example.autoclicker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 100, 60, 60);

        TextView guide = new TextView(this);
        guide.setText("1. 아래 두 권한을 모두 허용해 주세요.\n2. 볼륨 내리기 버튼: 터치 위치 지정 후 1초마다 반복 클릭 시작\n3. 볼륨 올리기 버튼: 반복 클릭 즉시 중지\n\n");
        guide.setTextSize(16f);
        layout.addView(guide);

        Button btnOverlay = new Button(this);
        btnOverlay.setText("1단계: 다른 앱 위에 표시 권한 허용");
        btnOverlay.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });
        layout.addView(btnOverlay);

        Button btnAccess = new Button(this);
        btnAccess.setText("2단계: 접근성 서비스 켜기 (나만의 오토클리커)");
        btnAccess.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
        layout.addView(btnAccess);

        setContentView(layout);
    }
}
