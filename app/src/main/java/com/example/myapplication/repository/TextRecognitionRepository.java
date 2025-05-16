package com.example.myapplication.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions;

import java.io.File;
import java.io.IOException;

public class TextRecognitionRepository {
    private static final String TAG = "TextRecognitionRepository";
    private final TextRecognizer recognizer;
    private final Context context;

    public TextRecognitionRepository(Context context) {
        this.context = context;
        // 한국어 텍스트 인식을 위한 옵션 설정
        recognizer = TextRecognition.getClient(new KoreanTextRecognizerOptions.Builder().build());
    }

    public void processImage(File imageFile, TextRecognitionCallback callback) {
        try {
            // 이미지 파일을 InputImage로 변환
            InputImage image = InputImage.fromFilePath(context, Uri.fromFile(imageFile));
            
            // 텍스트 인식 시작
            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        // 인식된 텍스트 처리
                        StringBuilder result = new StringBuilder();
                        for (Text.TextBlock block : visionText.getTextBlocks()) {
                            result.append(block.getText()).append("\n");
                        }
                        callback.onSuccess(result.toString().trim());
                    })
                    .addOnFailureListener(e -> {
                        // 오류 발생 시 처리
                        Log.e(TAG, "텍스트 인식 실패", e);
                        callback.onError(e.getMessage());
                    });
        } catch (IOException e) {
            Log.e(TAG, "이미지 로드 실패", e);
            callback.onError("이미지를 불러올 수 없습니다: " + e.getMessage());
        }
    }

    public interface TextRecognitionCallback {
        void onSuccess(String text);
        void onError(String error);
    }
} 