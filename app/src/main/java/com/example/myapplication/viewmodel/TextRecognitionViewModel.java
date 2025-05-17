package com.example.myapplication.viewmodel;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.repository.TextRecognitionRepository;

import java.io.File;

public class TextRecognitionViewModel extends AndroidViewModel {
    private static final String TAG = "TextRecognitionViewModel";
    private final TextRecognitionRepository repository;
    private final MutableLiveData<String> recognizedText = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isNetworkAvailable = new MutableLiveData<>(true);

    public TextRecognitionViewModel(@NonNull Application application) {
        super(application);
        repository = new TextRecognitionRepository(application);
        // 초기 네트워크 상태 확인
        checkNetworkStatus();
    }

    public LiveData<String> getRecognizedText() {
        return recognizedText;
    }

    public LiveData<Boolean> getIsNetworkAvailable() {
        return isNetworkAvailable;
    }

    /**
     * 네트워크 연결 상태를 확인하는 메서드
     */
    public void checkNetworkStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplication()
                .getSystemService(Application.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            isNetworkAvailable.postValue(isConnected);
        } else {
            isNetworkAvailable.postValue(false);
        }
    }

    /**
     * 이미지 파일을 처리하여 텍스트를 인식하는 메서드
     * @param imageFile 처리할 이미지 파일
     */
    public void processImage(File imageFile) {
        // 네트워크 상태 재확인
        checkNetworkStatus();
        
        if (Boolean.FALSE.equals(isNetworkAvailable.getValue())) {
            recognizedText.setValue("네트워크 연결이 필요합니다.");
            return;
        }

        if (!imageFile.exists()) {
            recognizedText.setValue("이미지 파일을 찾을 수 없습니다.");
            return;
        }

        repository.processImage(imageFile, new TextRecognitionRepository.TextRecognitionCallback() {
            @Override
            public void onSuccess(String text) {
                recognizedText.postValue(text);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "텍스트 인식 오류: " + error);
                recognizedText.postValue("오류: " + error);
            }
        });
    }
} 