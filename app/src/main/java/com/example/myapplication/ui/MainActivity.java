package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class MainActivity extends AppCompatActivity {

    private Button btnSpeech;
    private Button btnTextRecognition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 뷰 바인딩
        btnSpeech = findViewById(R.id.btnSpeech);
        btnTextRecognition = findViewById(R.id.btnTextRecognition);

        // 음성 인식 버튼 클릭 리스너
        btnSpeech.setOnClickListener(v -> {
            Intent intent = new Intent(this, SpeechActivity.class);
            startActivity(intent);
        });

        // 텍스트 인식 버튼 클릭 리스너
        btnTextRecognition.setOnClickListener(v -> {
            Intent intent = new Intent(this, TextRecognitionActivity.class);
            startActivity(intent);
        });
    }
}
