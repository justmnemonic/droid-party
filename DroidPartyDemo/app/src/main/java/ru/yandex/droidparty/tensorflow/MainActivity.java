package ru.yandex.droidparty.tensorflow;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DrawView.OnDrawFinishedListener {

    private static final int INPUT_SIZE = 28;
    private static final int OUTPUT_SIZE = 10;
    private static final String INPUT_NAME = "x";
    private static final String OUTPUT_NAME = "out";

    private static final String MODEL_FILE = "file:///android_asset/mnist_model.pb";

    private DigitClassifier mClassifier;

    private TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DrawView view = (DrawView) findViewById(R.id.draw_view);
        view.setListener(this);
        mText = (TextView) findViewById(R.id.text);
        initTenserFlow();
    }

    private void initTenserFlow() {
        mClassifier = new DigitClassifier();
        try {
            mClassifier.initializeTensorFlow(
                    getAssets(), MODEL_FILE, INPUT_SIZE,OUTPUT_SIZE, INPUT_NAME, OUTPUT_NAME);
        } catch (final IOException e) {
            Log.e("MainActivity", "ooops" , e);
        }
    }

    @Override
    public void onBitmapReady(Bitmap bitmap) {
        ArrayList<Classifier.Recognition> recognitions = mClassifier.recognize(bitmap);
        StringBuilder builder = new StringBuilder();
        for (Classifier.Recognition rec: recognitions) {
            builder.append(String.format("%d=%.1f%%", rec.getDigit(), rec.getConfidence() * 100));
            builder.append(" ");
        }
        mText.setText(builder.toString());
    }
}

