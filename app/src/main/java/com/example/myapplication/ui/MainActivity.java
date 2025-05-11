package com.example.myapplication.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.viewmodel.MainViewModel;

public class MainActivity extends ComponentActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int RECORD_DURATION_MS         = 5000;  // 5초

    private Button        btnRecord;
    private TextView      textResult;
    private MainViewModel viewModel;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 뷰 바인딩
        btnRecord  = findViewById(R.id.btnRecord);
        textResult = findViewById(R.id.textResult);

        // ViewModel 초기화
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // ViewModel 의 LiveData 관찰
        viewModel.getSpeechText().observe(this, result -> {
            // 음성 인식 결과가 들어오면 TextView 에 표시
            textResult.setText(result);
        });

        // 버튼 클릭 리스너
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
        textResult.setText("음성 인식을 시작합니다...");
        // 2) 버튼 숨기기
        btnRecord.setVisibility(View.GONE);
        // 3) 음성 인식 시작
        viewModel.startSpeech(this);

        // 4) RECORD_DURATION_MS 후 자동 중지 및 버튼 복원
        handler.postDelayed(() -> {
            viewModel.stopSpeech();
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
                new String[]{ Manifest.permission.RECORD_AUDIO },
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
                // 퍼미션이 허용되었으면 바로 플로우 실행
                startRecognitionFlow();
            } else {
                // 퍼미션 거부 시 안내
                textResult.setText("음성 녹음 권한이 필요합니다.");
            }
        }
    }
}
