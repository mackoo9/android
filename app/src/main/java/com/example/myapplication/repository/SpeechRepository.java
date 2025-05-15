package com.example.myapplication.repository;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import com.example.myapplication.data.SpeechResult;

public class SpeechRepository {

    private static final String TAG = "SpeechRepository";
    private boolean isNormalStop = false;  // 정상적인 종료인지 구분하는 플래그
    private SpeechResult currentResult = null;  // 현재까지 인식된 결과

    public interface SpeechCallback {
        void onResult(SpeechResult text);
        void onError(String message);
    }

    private SpeechRecognizer speechRecognizer;

    public void startListening(Context context, SpeechCallback callback) {
        isNormalStop = false;  // 시작할 때 플래그 초기화
        currentResult = null;  // 인식 결과 초기화
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        }

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "onReadyForSpeech");
            }
            @Override public void onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech");
            }
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {
                Log.d(TAG, "onBufferReceived");
            }
            @Override public void onEndOfSpeech() {
                Log.d(TAG, "onEndOfSpeech");
            }
            @Override public void onError(int error) {
                // ERROR_CLIENT가 발생했을 때 정상적인 종료인지 확인
                if (error == SpeechRecognizer.ERROR_CLIENT) {
                    if (isNormalStop) {
                        Log.d(TAG, "정상적인 음성 인식 종료");
                        return;
                    } else {
                        Log.e(TAG, "비정상적인 음성 인식 종료");
                        if (callback != null) {
                            callback.onError("음성 인식이 비정상적으로 종료되었습니다.");
                        }
                        return;
                    }
                }

                String message;
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        message = "오디오 에러";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        message = "권한 없음";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        message = "네트워크 에러";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        message = "네트워크 시간 초과";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        message = "일치하는 결과 없음";
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        message = "음성인식 서비스 사용중";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        message = "서버 에러";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        message = "음성 입력 시간 초과";
                        break;
                    default:
                        message = "알 수 없는 에러";
                        break;
                }
                Log.e(TAG, "onError: " + message);
                if (callback != null) {
                    callback.onError("음성 인식 오류: " + message);
                }
            }

            @Override public void onResults(Bundle results) {
                Log.d(TAG, "onResults");
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0); // 가장 유력한 결과
                    Log.d(TAG, "Recognized text: " + recognizedText);

                    // SpeechResult 객체 생성 및 저장
                    currentResult = new SpeechResult(recognizedText);

                    if (callback != null) {
                        callback.onResult(currentResult);
                    }
                } else {
                    Log.d(TAG, "음성 입력이 없습니다.");
                }
            }
            @Override
            public void onPartialResults(Bundle partialResults) {
                Log.d(TAG, "onPartialResults");
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                Log.d(TAG, "onEvent");
            }
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        speechRecognizer.startListening(intent);
    }

    public void stopListening(SpeechCallback callback) {
        if (speechRecognizer != null) {
            isNormalStop = true;  // 정상적인 종료임을 표시
            
            // 현재까지 인식된 결과가 없으면 에러 메시지 전달
            if (currentResult == null && callback != null) {
                callback.onError("음성 입력이 없습니다.");
            }
            
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
        }
    }
}
