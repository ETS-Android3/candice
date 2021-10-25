package com.example.candice;

import static android.Manifest.permission.CAMERA;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.candice.utility.FragmentChangeListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;


//Much of this is implemented from the documentation of
//http://com.google.android.gms.vision.text
//http://com.google.android.gms.vision
//For things such as the text recognizer, etc. The issue with these are they're deprecated or merged
//into MLKit (https://developers.google.com/ml-kit) - this is something I should look to update
//Once the text is recognized its exported to the logic for translations
public class ImageFragment extends Fragment {

    //SurfaceView is provided from https://developer.android.com/reference/android/view/SurfaceView
    //"Provides a dedicated drawing surface embedded inside of a view hierarchy."
    //This allows me to propagate the view on a Z level, meaning I can still have my activity on top
    //While the camera displays in the background
    SurfaceView surfaceView;

    //String result is the result of the text being read
    //and stringText is the text itself
    //More on the differences of these later
    String result;
    String stringText;

    //The view along with the Text view
    View view;
    TextView textView;
    TextInputEditText srcTextView;

    //FloatingActionButton is from https://material.io/
    //This time it's being used for the camera button, this takes a "picture" of the text
    //This is explained in the method below
    FloatingActionButton floatingCamera;

    //Camera
    private CameraSource cameraSource;

    //Initializing the the fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_image_translation, container, false);
        surfaceView = view.findViewById(R.id.surfaceView);

        floatingCamera = view.findViewById(R.id.fabCamera);

        floatingCamera.setOnClickListener(v -> {

            if (stringText == null) {
                Toast.makeText(getActivity(), "Text could not be recognized", Toast.LENGTH_SHORT).show();
            } else {

                MainFragment fragment = new MainFragment();
                Bundle bundle = new Bundle();
                bundle.putString("key", stringText);
                fragment.setArguments(bundle);

                FragmentChangeListener fc = (FragmentChangeListener) getContext();
                //Android Studio recommendation: might produce a null pointer so this:
                assert fc != null;
                //Otherwise continue:
                fc.replaceFragment(fragment);
            }
        });


        ActivityCompat.requestPermissions(requireActivity(), new String[]{CAMERA}, PackageManager.PERMISSION_GRANTED);

        textView = view.findViewById(R.id.textViewImage);
        TextRecognizer();


        return view;
    }


    //The text recognizing method
    private void TextRecognizer() {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(requireContext()).build();
        cameraSource = new CameraSource.Builder(requireContext(), textRecognizer)
                .setRequestedPreviewSize(300, 300)
                .setAutoFocusEnabled(true)
                .build();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            //https://developer.android.com/reference/android/view/SurfaceHolder.Callback
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        //referenced from https://developers.google.com/android/reference/com/google/android/gms/vision/text/TextRecognizer
        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {

            }

            //This method grabs the text and puts it into a String (aka stringText)
            @Override
            public void receiveDetections(@NonNull Detector.Detections<TextBlock> detections) {

                SparseArray<TextBlock> sparseArray = detections.getDetectedItems();
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i < sparseArray.size(); ++i) {
                    TextBlock textBlock = sparseArray.valueAt(i);
                    if (textBlock != null) {
                        textBlock.getValue();
                        stringBuilder.append(textBlock.getValue()).append(" ");
                    }
                }

                stringText = stringBuilder.toString();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    result = stringText;
                    textView.setText(stringText);
//                    srcTextView = view.findViewById(R.id.source_text);
//                    textView.setText((CharSequence) srcTextView);

                });


            }
        });
    }

    //Stop the camera from being used once the fragment is gone
    //Stops battery draining + issues with other apps using it
    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraSource.release();
    }


}