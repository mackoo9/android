package com.example.myapplication.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.data.SpeechResult;
import com.example.myapplication.repository.SpeechRepository;

public class SpeechViewModel extends AndroidViewModel {
    private final SpeechRepository speechRepository;
    private final MutableLiveData<String> speechText = new MutableLiveData<>();

    public SpeechViewModel(@NonNull Application application) {
        super(application);
        speechRepository = new SpeechRepository();
    }

    public LiveData<String> getSpeechText() {
        return speechText;
    }

    public void startSpeech(Context context) {
        speechRepository.startListening(context, new SpeechRepository.SpeechCallback() {
            @Override
            public void onResult(SpeechResult result) {
                speechText.postValue(result.getText());
            }

            @Override
            public void onError(String error) {
                speechText.postValue("오류: " + error);
            }
        });
    }

    public void stopSpeech() {
        speechRepository.stopListening(new SpeechRepository.SpeechCallback() {
            @Override
            public void onResult(SpeechResult result) {
                // 중지 시에는 결과를 처리하지 않음
            }

            @Override
            public void onError(String error) {
                speechText.postValue("오류: " + error);
            }
        });
    }
} 