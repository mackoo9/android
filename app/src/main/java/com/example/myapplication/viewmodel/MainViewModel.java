package com.example.myapplication.viewmodel;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
            public void onResult(String text) {
                speechText.postValue(text);
            }

            @Override
            public void onError(String message) {
                speechText.postValue("에러 발생: " + message);
            }
        });
    }

    public void stopSpeech() {
        speechRepo.stopListening();
    }
}
