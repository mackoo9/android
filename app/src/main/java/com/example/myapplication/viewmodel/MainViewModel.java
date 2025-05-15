package com.example.myapplication.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.data.SpeechResult;
import com.example.myapplication.repository.SpeechRepository;

public class MainViewModel extends ViewModel {

    private final SpeechRepository speechRepo = new SpeechRepository();
    private final MutableLiveData<String> speechText = new MutableLiveData<>();

    public LiveData<String> getSpeechText() {
        return speechText;
    }

    public void startSpeech(Context context) {
        speechRepo.startListening(context, new SpeechRepository.SpeechCallback() {
            @Override
            public void onResult(SpeechResult result) { // 파라미터 타입이 SpeechResult로 변경됨
                if (result != null) {
                    String recognizedText = result.getText(); // SpeechResult 객체에서 텍스트를 가져옴
                    speechText.postValue(recognizedText);
                    Log.d("MainViewModel", "음성 인식 결과 (from SpeechResult): " + recognizedText);
                } else {
                    speechText.postValue("결과 객체가 null입니다."); // 또는 다른 오류 처리
                }
            }

            @Override
            public void onError(String message) {
                speechText.postValue(message);
                Log.e("MainViewModel", "음성 인식 오류: " + message);
            }
        });
    }

    public void stopSpeech() {
        speechRepo.stopListening(new SpeechRepository.SpeechCallback() {
            @Override
            public void onResult(SpeechResult result) {
                // 결과는 startListening에서만 처리됨
            }

            @Override
            public void onError(String message) {
                speechText.postValue(message);
                Log.e("MainViewModel", "음성 인식 오류: " + message);
            }
        });
    }
}
