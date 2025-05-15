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

    /*
        전체적인 flow
        1. onCreate로 화면및 리스너 와 LiveData 초기화화
        2. 리스너에 따른 메소드 호출
        3. viewModel에서 음성인식 시작하는 SpeechRepository 호출
        4. 음성 인식 결과를 LiveData에 저장
        5. MainActivity에서 LiveData에 저장된 음성 인식 결과를 텍스트뷰에 표시
        6. 5초 후 음성 인식 중지 및 버튼 복원
        7. 중지될떄 viewModel에서 음성인식 중지 메소드 호출
        8. SpeechRepository에서 음성인식 중지 메소드 호출
        9. 중지될떄 음성인식 결과를 LiveData에 저장
        10. MainActivity에서 LiveData에 저장된 음성 인식 결과를 텍스트뷰에 표시
    */  

    // 시작할때 여기로 제일 처음 들어옴옴
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 뷰 바인딩
        btnRecord  = findViewById(R.id.btnRecord);
        textResult = findViewById(R.id.textResult);

        // ViewModel 초기화
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getSpeechText().observe(this, result -> {
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
