package com.alvindrakes.emergencyapp_final;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alvindrakes.emergencyapp_final.directionhelpers.FetchURL;
import com.alvindrakes.emergencyapp_final.directionhelpers.TaskLoadedCallback;
import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.api.services.speech.v1beta1.model.SpeechRecognitionResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Location;
import android.support.v7.widget.Toolbar;


public class CustomerMapActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    private Button  mRequest;
    private FloatingActionButton mCallMERS;

    public LatLng pickupLocation;
    private Boolean requestBol = false;
    private Marker pickupMarker;
    private SupportMapFragment mapFragment;
    private String destination, requestService;
    private LatLng destinationLatLng;
    private LinearLayout mDriverInfo;
    private ImageView mDriverProfileImage;
    private TextView mDriverName, mDriverPhone, mDriverCar;
    private RadioGroup mRadioGroup;
    private RatingBar mRatingBar;

    // audio
    private MediaRecorder mRecorder;
    private String mFilename = null;
    File local_OutputFile = null;
    private static String LOG_TAG = "Audio_recording";
    private int AUDIO_CODE = 0;
    private int STORAGE_CODE = 0;
    private StorageReference mStorage;
    private StorageReference filepath = null;
    private final static String RECORD_TAG = "transcribing audio phase";


    private final int REQUEST_SPEECH_RECOGNIZER = 3000;
    Double currentLatitude, currentLongitude;
    private String emergencyPlaceName = "";
    private boolean cameraFirstTime = true;

    // for drawer
    DrawerLayout myDrawer;
    ActionBarDrawerToggle myToggle;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        // for drawer
        myDrawer = findViewById(R.id.myDrawer);
        myToggle = new ActionBarDrawerToggle(this, myDrawer, R.string.open, R.string.close);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        myDrawer.addDrawerListener(myToggle);
        myToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().show();

        // for maps
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        destinationLatLng = new LatLng(0.0,0.0);

        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);

        mFilename = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilename += "/transcribed_text.txt";
        mStorage = FirebaseStorage.getInstance().getReference();

        mRequest = (Button) findViewById(R.id.request);
        mCallMERS = (FloatingActionButton) findViewById(R.id.callMERS);

        mCallMERS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dexter.withActivity(CustomerMapActivity.this)
                        .withPermission(Manifest.permission.CALL_PHONE)
                        .withListener(new PermissionListener() {
                            @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                                    String number = "999";  // MERS 999 number
                                    Intent intent = new Intent(Intent.ACTION_CALL);
                                    intent.setData(Uri.parse("tel:" +number));
                                    startActivity(intent);
                            }
                            @Override public void onPermissionDenied(PermissionDeniedResponse response) { Toast.makeText(CustomerMapActivity.this, "Please grant permission", Toast.LENGTH_SHORT).show();}
                            @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                        }).check();
            }
        });

        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                    int selectId = mRadioGroup.getCheckedRadioButtonId();
//
//                    final RadioButton radioButton = (RadioButton) findViewById(selectId);
//
//                    if (radioButton.getText() == null){
//                        return;
//                    }
//
//                    requestService = radioButton.getText().toString();

                    // record audio for limited amount of time
                    Dexter.withActivity(CustomerMapActivity.this)
                            .withPermission(Manifest.permission.RECORD_AUDIO)
                            .withListener(new PermissionListener() {
                                @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                                    AUDIO_CODE = 1;
                                    Dexter.withActivity(CustomerMapActivity.this)
                                            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            .withListener(new PermissionListener() {
                                                @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                                                    STORAGE_CODE = 1;
                                                }
                                                @Override public void onPermissionDenied(PermissionDeniedResponse response) { Toast.makeText(CustomerMapActivity.this, "Please grant permission", Toast.LENGTH_SHORT).show();}
                                                @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                                            }).check();
                                }
                                @Override public void onPermissionDenied(PermissionDeniedResponse response) { Toast.makeText(CustomerMapActivity.this, "Please grant permission", Toast.LENGTH_SHORT).show();}
                                @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                            }).check();


                    if (AUDIO_CODE == 1 && STORAGE_CODE == 1) {
                        startSpeechRecognizer();
//                        final String logTag = "intent";
//                        final ArrayList<String> content = new ArrayList<>();
//
//                        final HashMap<String, String> intentIdMap = new HashMap<>();
//                        intentIdMap.put("1", "Utilities.Help");
//                        intentIdMap.put("2", "Utilities.Confirm");
//
//                        try {
//                            final SpeechConfig intentConfig = SpeechConfig.fromSubscription(LanguageUnderstandingSubscriptionKey, LanguageUnderstandingServiceRegion);
//
//                            // final AudioConfig audioInput = AudioConfig.fromDefaultMicrophoneInput();
//                            final AudioConfig audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
//                            final IntentRecognizer reco = new IntentRecognizer(intentConfig, audioInput);
//
//                            LanguageUnderstandingModel intentModel = LanguageUnderstandingModel.fromAppId(LanguageUnderstandingAppId);
//                            for (Map.Entry<String, String> entry : intentIdMap.entrySet()) {
//                                reco.addIntent(intentModel, entry.getValue(), entry.getKey());
//                            }
//
//                            final Future<IntentRecognitionResult> task = reco.recognizeOnceAsync();
//                            setOnTaskCompletedListener(task, result -> {
//                                String s = result.getText();
//
//                                Log.d(RECORD_TAG, "onClick: " + s);
//
//                                if (result.getReason() != ResultReason.RecognizedIntent) {
//                                    String errorDetails = (result.getReason() == ResultReason.Canceled) ? CancellationDetails.fromResult(result).getErrorDetails() : "";
//                                    s = "Intent failed with " + result.getReason() + ". Did you enter your Language Understanding subscription?" + System.lineSeparator() + errorDetails;
//                                }
//
//                                String intentId = result.getIntentId();
//                                String intent = "";
//                                if (intentIdMap.containsKey(intentId)) {
//                                    intent = intentIdMap.get(intentId);
//                                }
//
//                                content.add(0, s);
//                                content.add(1, " [intent: " + intent + "]");
//                                Log.d(RECORD_TAG, "content: " + content);
//
//                            });
//                        } catch (Exception ex) {
//                            System.out.println(ex.getMessage());
//                        }
                    } else {
                        Toast.makeText(CustomerMapActivity.this, "Functions are not available", Toast.LENGTH_SHORT).show();
                        Log.d(LOG_TAG, "onClick audio code: " + AUDIO_CODE + "storage code = " + STORAGE_CODE);
                    }

                    requestBol = true;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));

                    mRequest.setText("Getting Help now....");

                }
        });
    }

    private void sendHelpNow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this, R.style.CustomAlertDialog)
                .setTitle("EMERGENCY HELP REQUESTED")
                .setMessage("Your help is on the way!");

        final AlertDialog alert = builder.create();
        alert.show();

        final Timer timer2 = new Timer();
        timer2.schedule(new TimerTask() {
            public void run() {
                alert.dismiss();
                timer2.cancel(); //this will cancel the timer of the system
            }
        }, 8000); // the timer will count 5 seconds....
    }

//    @Override
//    public boolean onMarkerClick(Marker marker) {
//            Toast.makeText(this, "Marker is clicked", Toast.LENGTH_SHORT).show();
//            new FetchURL(this).execute(getUrl(pickupMarker.getPosition(), marker.getPosition(), "driving"), "driving");
//
//        return false;
//    }
//
//    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
//        // Origin of route
//        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
//        // Destination of route
//        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
//        // Mode
//        String mode = "mode=" + directionMode;
//        // Building the parameters to the web service
//        String parameters = str_origin + "&" + str_dest + "&" + mode;
//        // Output format
//        String output = "json";
//        // Building the url to the web service
//        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
//        return url;
//    }
//
//    private Polyline currentPolyline;
//    @Override
//    public void onTaskDone(Object... values) {
//        if (currentPolyline != null)
//            currentPolyline.remove();
//        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
//    }


    // drawer open & close
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (myToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Side Pane navigation items selected
    public boolean onNavigationItemSelected (MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case R.id.action_setting:
                Intent a = new Intent(CustomerMapActivity.this, CustomerSettingsActivity.class);
                startActivity(a);
                break;

            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                Intent b = new Intent(CustomerMapActivity.this, CustomerLoginActivity.class);
                startActivity(b);
                finish();
                break;
        }
        return true;
    }

    public void getEmergencyPlaces() {

        if (emergencyPlaceName.equals("None")) {
            Toast.makeText(this, "Sorry, no emergency is found", Toast.LENGTH_SHORT).show();
            return;
        }

            StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            stringBuilder.append("location=").append(currentLatitude).append(",").append(currentLongitude);
            stringBuilder.append("&radius=" + 40000);
            stringBuilder.append("&types=").append(emergencyPlaceName);
            stringBuilder.append("&sensor=true");
            stringBuilder.append("&key=").append(getResources().getString(R.string.google_places_key));

            String url = stringBuilder.toString();
        Log.d(RECORD_TAG, "URL for places: " + url);

            Object dataTransfer[] = new Object[2];
            dataTransfer[0] = mMap;
            dataTransfer[1] = url;

            GetRequiredPlaces getRequiredPlaces = new GetRequiredPlaces(this);
            getRequiredPlaces.execute(dataTransfer);
            Toast.makeText(this, "Nearby " + emergencyPlaceName + " are listed on the map", Toast.LENGTH_SHORT).show();

            // store the help being deployed
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference("emergencyHelpDeployed");
            locationRef.child("helpDeployed").child(userId).setValue(emergencyPlaceName);
    }

    private void startSpeechRecognizer() {
        Intent intent = new Intent
                (RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, REQUEST_SPEECH_RECOGNIZER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SPEECH_RECOGNIZER) {
            if (resultCode == RESULT_OK) {
                List results = data.getStringArrayListExtra
                        (RecognizerIntent.EXTRA_RESULTS);
                Log.d(RECORD_TAG, "onActivityResult: " + results.get(0));

                                String removeWhiteSpaceText = results.get(0).toString().replaceAll("\\s+","%20");
                                new JsonTask().execute("https://westus.api.cognitive.microsoft.com/luis/v2.0/apps/a7f254ec-7157-4f0e-96b9-fd2d2ef16e00?verbose=true&timezoneOffset=-360&subscription-key=8dc2333b797c41b5885924b129fa18a2&q=" + removeWhiteSpaceText);
            }

                //appendLog(results.get(0).toString());
                mRequest.setText("CALL FOR EMERGENCY");

                sendHelpNow();

        }
        }

    ProgressDialog pd;
    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(CustomerMapActivity.this, R.style.MyAlertDialogStyle);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            if (result != null ) {
                try
                {
                    JSONObject parentObject = new JSONObject(result);
                   // JSONArray resultsArray = parentObject.getJSONArray("topScoringIntent");

                        JSONObject topIntent = parentObject.getJSONObject("topScoringIntent");

                         emergencyPlaceName = topIntent.getString("intent");
                        Log.d(RECORD_TAG, "emergencyPlaceName: " + emergencyPlaceName);
                        getEmergencyPlaces();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


//    private void startRecording() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(CustomerMapActivity.this)
//                .setTitle("EMERGENCY AUDIO")
//                .setMessage("Tell us what emergency are you facing now");
//        final AlertDialog alert = builder.create();
//        alert.show();
//        final Timer timer2 = new Timer();
//        timer2.schedule(new TimerTask() {
//            public void run() {
//                alert.dismiss();
//                timer2.cancel(); //this will cancel the timer of the system
//            }
//        }, 8000); // the timer will count 5 seconds....
//
//        mRecorder = new MediaRecorder();
//        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
//        mRecorder.setOutputFile(mFilename);
//        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//
//
//        try {
//            mRecorder.setMaxDuration(8000);
//            mRecorder.prepare();
//            mRecorder.start();
//
//            mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
//                @Override
//                public void onInfo(MediaRecorder mr, int what, int extra) {
//                    if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
//                        Toast.makeText(CustomerMapActivity.this, "Recording stops. Limit reached", Toast.LENGTH_LONG).show();
//
//                        stopRecording(mr);
//                    }
//                }
//            });
//        } catch (IOException e) {
//            Log.e(LOG_TAG, "prepare() failed");
//        }
//
//        // mRecorder.start();
//    }
//
//    private void stopRecording(MediaRecorder mediaRecorder) {
//        mediaRecorder.stop();
//        mediaRecorder.release();
//        mRecorder = null;
//
//        uploadAudioFirebase();
//    }




    private void uploadTextFirebase() {

        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Uri uri = Uri.fromFile(new File(mFilename));

        filepath = mStorage.child("Text").child(userUid).child(mFilename);

        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(getApplicationContext(), "Text file uploaded to firebase", Toast.LENGTH_SHORT).show();

                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        final String downloadUrl = uri.toString();
                        Log.d(RECORD_TAG, "Download url:" + downloadUrl);  // download url is correct !!!!

                      //  transcribeText(uri);
                    }
                }) .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CustomerMapActivity.this, "AUDIO TRANSCRIPTION NOT WORKING", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

//    public void appendLog(String text)
//    {
//        try {
//            final File path = new File(
//                    Environment.getExternalStorageDirectory(), "Text");
//            if (!path.exists()) {
//                path.mkdir();
//            }
//            Runtime.getRuntime().exec(
//                    "logcat  -d -f " + path + File.separator
//                            + "transcribed_text"
//                            + ".txt");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    // transcribe the audio file into text
//    private void transcribeText(Uri uri) {
//        new AsyncJob.AsyncJobBuilder<Boolean>()
//                .doInBackground(new AsyncJob.AsyncAction<Boolean>() {
//                    @Override
//                    public Boolean doAsync() {
//                        // Do some background work
//                        filepath.getFile(uri).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                            @Override
//                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//
//                                Toast.makeText(CustomerMapActivity.this, "Got the file from firebase", Toast.LENGTH_SHORT).show();
//
//                                Task<Uri> audioUrlTask = taskSnapshot.getStorage().getDownloadUrl();
//                                while (!audioUrlTask.isSuccessful());
//                                Uri downloadUrl = audioUrlTask.getResult();
//
//                                Speech speechService = new Speech.Builder(
//                                        AndroidHttp.newCompatibleTransport(),
//                                        new AndroidJsonFactory(),
//                                        null
//                                ).setSpeechRequestInitializer(
//                                        new SpeechRequestInitializer(CLOUD_API_KEY))
//                                        .build();
//                                RecognitionConfig recognitionConfig = new RecognitionConfig();
//                                recognitionConfig.setLanguageCode("en-US");
//                                RecognitionAudio recognitionAudio = new RecognitionAudio();
//                                recognitionAudio.setContent(downloadUrl.toString());
//
//                                // Create request
//                                SyncRecognizeRequest request = new SyncRecognizeRequest();
//                                request.setConfig(recognitionConfig);
//                                request.setAudio(recognitionAudio);
//
//                                // Generate response
//                                SyncRecognizeResponse response = null;
//                                try {
//                                    response = speechService.speech()
//                                            .syncrecognize(request)
//                                            .execute();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                    Log.d(RECORD_TAG, "Sync regconize response not working ");
//                                }
//
//                                // Extract transcript
//                                SpeechRecognitionResult result = response.getResults().get(0);
//                                final String transcript = result.getAlternatives().get(0)
//                                        .getTranscript();
//
//                                Log.d(RECORD_TAG, "Transcribed text: " + transcript);
//                            }
//                        });
//                        return true;
//                    }
//                })
//                .doWhenFinished(new AsyncJob.AsyncResultAction<Boolean>() {
//                    @Override
//                    public void onResult(Boolean result) {
//                        Toast.makeText(CustomerMapActivity.this, "Result was: " + result, Toast.LENGTH_SHORT).show();
//                    }
//                }).create().start();
//    }


//    private void transcribeText() {
//        try {
//            final SpeechConfig intentConfig = SpeechConfig.fromSubscription(LanguageUnderstandingSubscriptionKey, LanguageUnderstandingServiceRegion);
//
//            final String logTag = "intent";
//            final ArrayList<String> content = new ArrayList<>();
//
//            final HashMap<String, String> intentIdMap = new HashMap<>();
//            intentIdMap.put("1", "Utilities.Help");
//            intentIdMap.put("2", "Utilities.Confirm");
//
//            // final AudioConfig audioInput = AudioConfig.fromDefaultMicrophoneInput();
//            final AudioConfig audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
//            final IntentRecognizer reco = new IntentRecognizer(intentConfig, audioInput);
//
//            LanguageUnderstandingModel intentModel = LanguageUnderstandingModel.fromAppId(LanguageUnderstandingAppId);
//            for (Map.Entry<String, String> entry : intentIdMap.entrySet()) {
//                reco.addIntent(intentModel, entry.getValue(), entry.getKey());
//            }
//
//            final Future<IntentRecognitionResult> task = reco.recognizeOnceAsync();
//            setOnTaskCompletedListener(task, result -> {
//                String s = result.getText();
//
//                if (result.getReason() != ResultReason.RecognizedIntent) {
//                    String errorDetails = (result.getReason() == ResultReason.Canceled) ? CancellationDetails.fromResult(result).getErrorDetails() : "";
//                    s = "Intent failed with " + result.getReason() + ". Did you enter your Language Understanding subscription?" + System.lineSeparator() + errorDetails;
//                }
//
//                String intentId = result.getIntentId();
//                String intent = "";
//                if (intentIdMap.containsKey(intentId)) {
//                    intent = intentIdMap.get(intentId);
//                }
//
//                content.add(0, s);
//                content.add(1, " [intent: " + intent + "]");
//               // recognizedTextView.setText(TextUtils.join(System.lineSeparator(), content));
//                Log.d(RECORD_TAG, "transcribeText: " + content );
//                //Toast.makeText(this, "done transcribing", Toast.LENGTH_SHORT).show();
//
////                if(intentId!=null && intentId.equals("1")){
////                    m_syn.SpeakToAudio("Successfully switched off the light");
////                }else{
////                    m_syn.SpeakToAudio("Successfully switched on the light");
////                }
//            });
//        } catch (Exception ex) {
//            System.out.println(ex.getMessage());
//        }
//    }


//    private <T> void setOnTaskCompletedListener(Future<T> task, OnTaskCompletedListener<T> listener) {
//        s_executorService.submit(() -> {
//            T result = task.get();
//            listener.onCompleted(result);
//            return null;
//        });
//    }
//
//    private interface OnTaskCompletedListener<T> {
//        void onCompleted(T taskResult);
//    }
//
//    private static ExecutorService s_executorService;
//    static {
//        s_executorService = Executors.newCachedThreadPool();
//    }
//
//    public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        byte[] buffer = new byte[0xFFFF];
//        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
//            os.write(buffer, 0, len);
//        }
//        return os.toByteArray();
//    }

    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;

    GeoQuery geoQuery;
    private void getClosestDriver(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestBol){
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (driverFound){
                                    return;
                                }

                                if(driverMap.get("service").equals(requestService)){
                                    driverFound = true;
                                    driverFoundID = dataSnapshot.getKey();

                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest");
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map = new HashMap();
                                    map.put("customerRideId", customerId);
                                    map.put("destination", destination);
                                    map.put("destinationLat", destinationLatLng.latitude);
                                    map.put("destinationLng", destinationLatLng.longitude);
                                    driverRef.updateChildren(map);

                                    getDriverLocation();
                                    getDriverInfo();
                                    getHasRideEnded();
                                    mRequest.setText("Looking for Driver Location....");
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    /*-------------------------------------------- Map specific functions -----
    |  Function(s) getDriverLocation
    |
    |  Purpose:  Get's most updated driver location and it's always checking for movements.
    |
    |  Note:
    |	   Even tho we used geofire to push the location of the driver we can use a normal
    |      Listener to get it's location with no problem.
    |
    |      0 -> Latitude
    |      1 -> Longitudde
    |
    *-------------------------------------------------------------------*/
    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocation(){
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance<100){
                        mRequest.setText("Driver's Here");
                    }else{
                        mRequest.setText("Driver Found: " + String.valueOf(distance));
                    }



                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    /*-------------------------------------------- getDriverInfo -----
    |  Function(s) getDriverInfo
    |
    |  Purpose:  Get all the user information that we can get from the user's database.
    |
    |  Note: --
    |
    *-------------------------------------------------------------------*/
    private void getDriverInfo(){
        mDriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    if(dataSnapshot.child("name")!=null){
                        mDriverName.setText(dataSnapshot.child("name").getValue().toString());
                    }
                    if(dataSnapshot.child("phone")!=null){
                        mDriverPhone.setText(dataSnapshot.child("phone").getValue().toString());
                    }
                    if(dataSnapshot.child("car")!=null){
                        mDriverCar.setText(dataSnapshot.child("car").getValue().toString());
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        Glide.with(getApplication()).load(dataSnapshot.child("profileImageUrl").getValue().toString()).into(mDriverProfileImage);
                    }

                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        mRatingBar.setRating(ratingsAvg);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private void getHasRideEnded(){
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest").child("customerRideId");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide(){
        requestBol = false;
        geoQuery.removeAllListeners();
        driverLocationRef.removeEventListener(driverLocationRefListener);
        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);

        if (driverFoundID != null){
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest");
            driverRef.removeValue();
            driverFoundID = null;

        }
        driverFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (mDriverMarker != null){
            mDriverMarker.remove();
        }
        mRequest.setText("call Uber");

        mDriverInfo.setVisibility(View.GONE);
        mDriverName.setText("");
        mDriverPhone.setText("");
        mDriverCar.setText("Destination: --");
        mDriverProfileImage.setImageResource(R.mipmap.ic_default_user);
    }

    /*-------------------------------------------- Map specific functions -----
    |  Function(s) onMapReady, buildGoogleApiClient, onLocationChanged, onConnected
    |
    |  Purpose:  Find and update user's location.
    |
    |  Note:
    |	   The update interval is set to 1000Ms and the accuracy is set to PRIORITY_HIGH_ACCURACY,
    |      If you're having trouble with battery draining too fast then change these to lower values
    |
    |
    *-------------------------------------------------------------------*/
    @SuppressLint("RestrictedApi")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();

                    if (cameraFirstTime) {
                        cameraFirstTime = false;
                        float zoomLevel = 16.0f; //This goes up to 21
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                    }

//                    if(!getDriversAroundStarted)
//                        getDriversAround();
                }
            }
        }
    };

    /*-------------------------------------------- onRequestPermissionsResult -----
    |  Function onRequestPermissionsResult
    |
    |  Purpose:  Get permissions for our app if they didn't previously exist.
    |
    |  Note:
    |	requestCode: the nubmer assigned to the request that we've made. Each
    |                request has it's own unique request code.
    |
    *-------------------------------------------------------------------*/
    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    private int requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            return 1;
        }
        return 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        startSpeechRecognizer();
                    }


//                } else{
//                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
//                    boolean showRationale = shouldShowRequestPermissionRationale( Manifest.permission.ACCESS_FINE_LOCATION );
//                    if (! showRationale) {
//                        openSettingsDialog();
//                    }
//                }
                    break;
                }
            }
        }
    }




//    boolean getDriversAroundStarted = false;
//    List<Marker> markers = new ArrayList<Marker>();
//    private void getDriversAround(){
//        getDriversAroundStarted = true;
//        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
//
//        GeoFire geoFire = new GeoFire(driverLocation);
//        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation( mLastLocation.getLatitude(), mLastLocation.getLongitude()), 999999999);
//
//        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
//            @Override
//            public void onKeyEntered(String key, GeoLocation location) {
//
//                for(Marker markerIt : markers){
//                    if(markerIt.getTag().equals(key))
//                        return;
//                }
//
//                LatLng driverLocation = new LatLng(location.latitude, location.longitude);
//
//                Marker mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title(key).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
//                mDriverMarker.setTag(key);
//
//                markers.add(mDriverMarker);
//
//
//            }
//
//            @Override
//            public void onKeyExited(String key) {
//                for(Marker markerIt : markers){
//                    if(markerIt.getTag().equals(key)){
//                        markerIt.remove();
//                    }
//                }
//            }
//
//            @Override
//            public void onKeyMoved(String key, GeoLocation location) {
//                for(Marker markerIt : markers){
//                    if(markerIt.getTag().equals(key)){
//                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
//                    }
//                }
//            }
//
//            @Override
//            public void onGeoQueryReady() {
//            }
//
//            @Override
//            public void onGeoQueryError(DatabaseError error) {
//
//            }
//        });
//    }
}
