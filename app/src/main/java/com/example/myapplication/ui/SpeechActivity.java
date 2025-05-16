package com.example.myapplication.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.viewmodel.SpeechViewModel;

public class SpeechActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int RECORD_DURATION_MS = 5000;  // 5초

    private Button btnRecord;
    private TextView textSpeechResult;
    private SpeechViewModel speechViewModel;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        // 뷰 바인딩
        btnRecord = findViewById(R.id.btnRecord);
        textSpeechResult = findViewById(R.id.textSpeechResult);

        // ViewModel 초기화
        speechViewModel = new ViewModelProvider(this).get(SpeechViewModel.class);

        // LiveData 관찰
        speechViewModel.getSpeechText().observe(this, result -> {
            textSpeechResult.setText(result);
        });

        // 음성 인식 버튼 클릭 리스너
        btnRecord.setOnClickListener(v -> {
            if (checkAudioPermission()) {
                startRecognitionFlow();
            } else {
                requestAudioPermission();
            }
        });
    }

    /** 음성 인식 전체 플로우 실행 */
    private void startRecognitionFlow() {
        // 1) 안내 메시지
        textSpeechResult.setText("음성 인식을 시작합니다...");
        // 2) 버튼 숨기기
        btnRecord.setVisibility(View.GONE);
        // 3) 음성 인식 시작
        speechViewModel.startSpeech(this);

        // 4) RECORD_DURATION_MS 후 자동 중지 및 버튼 복원
        handler.postDelayed(() -> {
            speechViewModel.stopSpeech();
            btnRecord.setVisibility(View.VISIBLE);
        }, RECORD_DURATION_MS);
    }

    /** 오디오 녹음 퍼미션 체크 */
    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /** 오디오 녹음 퍼미션 요청 */
    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION
        );
    }

    /** 퍼미션 요청 결과 처리 */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                startRecognitionFlow();
            } else {
                textSpeechResult.setText("음성 녹음 권한이 필요합니다.");
            }
        }
    }
} 