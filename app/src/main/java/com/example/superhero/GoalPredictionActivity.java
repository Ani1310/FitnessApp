package com.example.superhero;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class GoalPredictionActivity extends AppCompatActivity {

    private EditText calorieInput, ageInput, weightInput, heightInput, goalWeightInput;
    private Button predictButton;
    private TextView predictionResult;

    private Interpreter tflite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_prediction);


        calorieInput = findViewById(R.id.calorieInput);
        ageInput = findViewById(R.id.ageInput);
        weightInput = findViewById(R.id.weightInput);
        heightInput = findViewById(R.id.heightInput);
        goalWeightInput = findViewById(R.id.goalWeightInput); // New field for goal weight
        predictButton = findViewById(R.id.predictButton);
        predictionResult = findViewById(R.id.predictionResult);


        try {
            tflite = new Interpreter(loadModelFile());
        } catch (Exception e) {
            e.printStackTrace();
        }


        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                predictGoalTime();
            }
        });
    }

    private void predictGoalTime() {
        try {

            float calorieBurn = Float.parseFloat(calorieInput.getText().toString());
            float age = Float.parseFloat(ageInput.getText().toString());
            float weight = Float.parseFloat(weightInput.getText().toString());
            float height = Float.parseFloat(heightInput.getText().toString());
            float goalWeight = Float.parseFloat(goalWeightInput.getText().toString());


            float[][] input = new float[1][5];
            input[0][0] = calorieBurn;
            input[0][1] = age;
            input[0][2] = weight;
            input[0][3] = height;
            input[0][4] = goalWeight;


            float[][] output = new float[1][1];


            tflite.run(input, output);


            predictionResult.setText("Predicted time to reach goal: " + output[0][0] + " weeks");
        } catch (Exception e) {
            predictionResult.setText("Please enter valid inputs!");
        }
    }

    private MappedByteBuffer loadModelFile() throws Exception {
        FileInputStream inputStream = new FileInputStream(getAssets().openFd("time_prediction_model.tflite").getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = getAssets().openFd("time_prediction_model.tflite").getStartOffset();
        long declaredLength = getAssets().openFd("time_prediction_model.tflite").getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
        }
    }
}
