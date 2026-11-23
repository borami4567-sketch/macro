package com.example.autoclicker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
        guide.setText("1. 최초 1회 아래 두 권한을 허용해 주세요.\n2. 아래에서 사용할 매크로 모드를 선택하세요.\n3. [대기 모드 켜기]를 누른 뒤 볼륨 내리기로 시작, 볼륨 올리기로 중지합니다.\n\n");
        guide.setTextSize(15f);
        layout.addView(guide);

        // 모드 선택 라디오 그룹
        TextView modeTitle = new TextView(this);
        modeTitle.setText("📌 매크로 모드 선택:");
        modeTitle.setTextSize(16f);
        layout.addView(modeTitle);

        RadioGroup radioGroup = new RadioGroup(this);

        RadioButton rbMode1 = new RadioButton(this);
        rbMode1.setId(1);
        rbMode1.setText("모드 1: 1개 지점 / 초당 1회 클릭");
        rbMode1.setTextSize(15f);

        RadioButton rbMode2 = new RadioButton(this);
        rbMode2.setId(2);
        rbMode2.setText("모드 2: 2개 지점 번갈아 / 초당 20회 클릭");
        rbMode2.setTextSize(15f);

        radioGroup.addView(rbMode1);
        radioGroup.addView(rbMode2);

        if (AutoClickService.macroMode == 2) {
            rbMode2.setChecked(true);
        } else {
            rbMode1.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            AutoClickService.macroMode = checkedId;
            if (AutoClickService.instance != null) {
                AutoClickService.instance.forceStopAll();
            }
            Toast.makeText(this, checkedId == 1 ? "모드 1 선택됨 (1지점 / 초당 1회)" : "모드 2 선택됨 (2지점 / 초당 20회)", Toast.LENGTH_SHORT).show();
        });
        layout.addView(radioGroup);

        TextView spacer = new TextView(this);
        spacer.setText("\n");
        layout.addView(spacer);

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
