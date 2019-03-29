//package com.alvindrakes.emergencyapp_final;
//
//import android.os.AsyncTask;
//import android.util.Log;
//import android.view.View;
//
//import com.google.api.client.extensions.android.http.AndroidHttp;
//import com.google.api.client.extensions.android.json.AndroidJsonFactory;
//import com.google.api.services.speech.v1beta1.Speech;
//import com.google.api.services.speech.v1beta1.SpeechRequestInitializer;
//import com.google.api.services.speech.v1beta1.model.RecognitionAudio;
//import com.google.api.services.speech.v1beta1.model.RecognitionConfig;
//import com.google.api.services.speech.v1beta1.model.SpeechRecognitionResult;
//import com.google.api.services.speech.v1beta1.model.SyncRecognizeRequest;
//import com.google.api.services.speech.v1beta1.model.SyncRecognizeResponse;
//import com.google.firebase.storage.StorageReference;
//
//import java.io.IOException;
//
//public class RetrieveAudioTask extends AsyncTask<StorageReference, Integer, byte[]> {
//
//    private static String RECORD_TAG = "Audio_recording";
//    private final String CLOUD_API_KEY = "AIzaSyBOdewjvLRdSX1KCfds3jMUX9ZUV9kMrPk";
//
//    protected byte[] doInBackground(StorageReference... filepath) {
//
//        byte[] AudioBytes = filepath.
//
//        return AudioBytes;
//    }
//
//    protected void onPostExecute(byte[] bytes) {
//        // Receive the result of the operation done in the new thread in the main UI thread and
//        // use it to update the UI.
//        Speech speechService = new Speech.Builder(
//                AndroidHttp.newCompatibleTransport(),
//                new AndroidJsonFactory(),
//                null
//        ).setSpeechRequestInitializer(
//                new SpeechRequestInitializer(CLOUD_API_KEY))
//                .build();
//        RecognitionConfig recognitionConfig = new RecognitionConfig();
//        recognitionConfig.setLanguageCode("en-US");
//        RecognitionAudio recognitionAudio = new RecognitionAudio();
//        recognitionAudio.setContent(bytes.toString());
//
//        // Create request
//        SyncRecognizeRequest request = new SyncRecognizeRequest();
//        request.setConfig(recognitionConfig);
//        request.setAudio(recognitionAudio);
//
//        // Generate response
//        SyncRecognizeResponse response = null;
//        try {
//            response = speechService.speech()
//                    .syncrecognize(request)
//                    .execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.d(RECORD_TAG, "Sync regconize response not working ");
//        }
//
//        // Extract transcript
//        SpeechRecognitionResult result = response.getResults().get(0);
//        final String transcript = result.getAlternatives().get(0)
//                .getTranscript();
//
//        Log.d(RECORD_TAG, "Transcribed text: " + transcript);
//    }
//}
