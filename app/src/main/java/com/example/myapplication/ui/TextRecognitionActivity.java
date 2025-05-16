package com.example.myapplication.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.viewmodel.TextRecognitionViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TextRecognitionActivity extends ComponentActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 201;

    private Button btnCamera;
    private TextView textRecognitionResult;
    private TextRecognitionViewModel textRecognitionViewModel;
    private String currentPhotoPath;  // 현재 촬영한 사진의 경로를 저장

    // 카메라 앱의 결과를 처리하기 위한 ActivityResultLauncher
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    processImage();  // 사진 촬영이 성공적으로 완료되면 이미지 처리 시작
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recognition);

        // 뷰 바인딩
        btnCamera = findViewById(R.id.btnCamera);
        textRecognitionResult = findViewById(R.id.textRecognitionResult);

        // ViewModel 초기화
        textRecognitionViewModel = new ViewModelProvider(this).get(TextRecognitionViewModel.class);

        // LiveData 관찰
        textRecognitionViewModel.getRecognizedText().observe(this, result -> {
            textRecognitionResult.setText(result);
        });

        // 네트워크 상태 관찰
        textRecognitionViewModel.getIsNetworkAvailable().observe(this, isAvailable -> {
            if (!isAvailable) {
                showNetworkSettingsDialog();
            }
        });

        // 카메라 버튼 클릭 리스너
        btnCamera.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                dispatchTakePictureIntent();
            } else {
                requestCameraPermission();
            }
        });
    }

    /**
     * 네트워크 설정 다이얼로그를 표시하는 메서드
     */
    private void showNetworkSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("네트워크 연결 필요")
                .setMessage("텍스트 인식을 위해 네트워크 연결이 필요합니다. 네트워크 설정으로 이동하시겠습니까?")
                .setPositiveButton("설정으로 이동", (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 촬영된 이미지를 처리하는 메서드
     * ViewModel을 통해 ML Kit를 사용하여 텍스트 인식 수행
     */
    private void processImage() {
        File imageFile = new File(currentPhotoPath);
        textRecognitionViewModel.processImage(imageFile);
    }

    /**
     * 이미지를 저장할 파일을 생성하는 메서드
     * @return 생성된 이미지 파일
     * @throws IOException 파일 생성 실패 시 예외 발생
     */
    private File createImageFile() throws IOException {
        // 파일명에 현재 시간을 포함하여 고유한 이름 생성
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        
        // 앱의 전용 저장소에 Pictures 디렉토리 사용
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        
        // 임시 파일 생성
        File image = File.createTempFile(
                imageFileName,  // 파일명 접두사
                ".jpg",        // 파일 확장자
                storageDir     // 저장될 디렉토리
        );
        
        // 생성된 파일의 절대 경로 저장
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * 카메라 앱을 실행하여 사진을 촬영하는 메서드
     * 1. 카메라 Intent 생성
     * 2. 이미지 파일 생성
     * 3. FileProvider를 통해 URI 생성
     * 4. 카메라 앱 실행
     */
    private void dispatchTakePictureIntent() {
        // 카메라 앱을 실행하기 위한 Intent 생성
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        // 카메라 앱이 존재하는지 확인
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                // 이미지를 저장할 파일 생성
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "이미지 파일 생성 실패", Toast.LENGTH_SHORT).show();
            }
            
            // 파일이 성공적으로 생성되었다면
            if (photoFile != null) {
                // FileProvider를 사용하여 URI 생성
                // Android 7.0 이상에서는 보안상의 이유로 FileProvider 사용 필요
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.myapplication.fileprovider",
                        photoFile);
                
                // 카메라 앱에 이미지 저장 위치 전달
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                
                // 카메라 앱 실행
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }

    /**
     * 카메라 권한이 있는지 확인하는 메서드
     * @return 카메라 권한이 있으면 true, 없으면 false
     */
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 카메라 권한을 요청하는 메서드
     */
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    /**
     * 권한 요청 결과를 처리하는 메서드
     * @param requestCode 요청 코드
     * @param permissions 요청한 권한 목록
     * @param grantResults 권한 허용 여부 결과
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                dispatchTakePictureIntent();  // 권한이 허용되면 카메라 실행
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 