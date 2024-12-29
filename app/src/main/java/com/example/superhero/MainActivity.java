package com.example.superhero;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText ageInput, heightInput, weightInput;
    private RadioGroup genderGroup;
    private Button saveProfileBtn, btnGoalPredictor, btnProfile, btnRecommendations;
    private TextView heartRateText, oxygenText, calorieText, recommendationText;
    private LineChart calorieChart;

    private FirebaseFirestore db;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        ageInput = findViewById(R.id.ageInput);
        heightInput = findViewById(R.id.heightInput);
        weightInput = findViewById(R.id.weightInput);
        genderGroup = findViewById(R.id.genderGroup);
        saveProfileBtn = findViewById(R.id.saveProfileBtn);
        btnGoalPredictor = findViewById(R.id.btnGoalPredictor);
        btnProfile = findViewById(R.id.btnProfile); // Initialize btnProfile
        btnRecommendations = findViewById(R.id.btnRecommendations); // Initialize btnRecommendations
        heartRateText = findViewById(R.id.heartRateText);
        oxygenText = findViewById(R.id.oxygenText);
        calorieText = findViewById(R.id.calorieText);
        recommendationText = findViewById(R.id.recommendationText);
        calorieChart = findViewById(R.id.calorieChart);

        saveProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });

        btnGoalPredictor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GoalPredictionActivity.class);
                startActivity(intent);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        btnRecommendations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String heightStr = heightInput.getText().toString();
                String weightStr = weightInput.getText().toString();

                if (heightStr.isEmpty() || weightStr.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter both height and weight.", Toast.LENGTH_SHORT).show();
                    return;
                }

                float height = Float.parseFloat(heightStr) / 100; // Convert cm to meters
                float weight = Float.parseFloat(weightStr);
                float bmi = weight / (height * height);

                String bmiCategory;
                if (bmi < 18.5) {
                    bmiCategory = "Underweight";
                } else if (bmi >= 18.5 && bmi <= 24.9) {
                    bmiCategory = "Normal weight";
                } else if (bmi >= 25 && bmi <= 29.9) {
                    bmiCategory = "Overweight";
                } else {
                    bmiCategory = "Obesity";
                }

                // Redirect to RecommendationActivity
                Intent intent = new Intent(MainActivity.this, RecommendationActivity.class);
                intent.putExtra("bmi", bmi);
                intent.putExtra("bmiCategory", bmiCategory);
                startActivity(intent);
            }
        });


        handler = new Handler();
        simulateHealthData();
        setupCalorieChart();
    }

    private void saveUserProfile() {
        String age = ageInput.getText().toString();
        String height = heightInput.getText().toString();
        String weight = weightInput.getText().toString();
        String gender = ((RadioButton) findViewById(genderGroup.getCheckedRadioButtonId())).getText().toString();

        // Validate input
        if (age.isEmpty() || height.isEmpty() || weight.isEmpty()) {
            recommendationText.setText("Please fill in all fields.");
            return;
        }

        float heightInMeters = Float.parseFloat(height) / 100; // Convert cm to meters
        float weightInKg = Float.parseFloat(weight);
        float bmi = weightInKg / (heightInMeters * heightInMeters);

        String bmiCategory;
        String exerciseRecommendation;
        if (bmi < 18.5) {
            bmiCategory = "Underweight";
            exerciseRecommendation = "Focus on strength training, yoga, and a calorie-dense diet.";
        } else if (bmi >= 18.5 && bmi <= 24.9) {
            bmiCategory = "Normal weight";
            exerciseRecommendation = "Maintain fitness with cardio and strength training.";
        } else if (bmi >= 25 && bmi <= 29.9) {
            bmiCategory = "Overweight";
            exerciseRecommendation = "Engage in low-impact cardio, cycling, and resistance training.";
        } else {
            bmiCategory = "Obesity";
            exerciseRecommendation = "Start with walking, water aerobics, and low-impact exercises.";
        }

        // Display BMI and Recommendations
        recommendationText.setText(String.format("Your BMI: %.2f (%s)\nRecommendation: %s", bmi, bmiCategory, exerciseRecommendation));

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("age", age);
        userProfile.put("height", height);
        userProfile.put("weight", weight);
        userProfile.put("gender", gender);
        userProfile.put("bmi", bmi);
        userProfile.put("bmiCategory", bmiCategory);

        // Save profile to Firestore
        db.collection("users").document("profile")
                .set(userProfile)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void simulateHealthData() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Generate random health data
                int heartRate = 70 + (int) (Math.random() * 30); // Random heart rate
                int oxygenLevel = 95 + (int) (Math.random() * 5); // Random oxygen level
                int calorieBurn = 400 + (int) (Math.random() * 200); // Random calorie burn

                // Update UI
                heartRateText.setText("Heart Rate: " + heartRate + " bpm");
                oxygenText.setText("Oxygen Level: " + oxygenLevel + "%");
                calorieText.setText("Calorie Burn: " + calorieBurn + " cal");

                // Update chart with new calorie data
                updateCalorieChart(calorieBurn);

                handler.postDelayed(this, 3000); // Update every 3 seconds
            }
        }, 0);
    }

    private void setupCalorieChart() {
        ArrayList<Entry> entries = new ArrayList<>();
        LineDataSet dataSet = new LineDataSet(entries, "Calorie Burn");
        dataSet.setColor(ContextCompat.getColor(this, R.color.design_default_color_primary)); // Use resolved color
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.black)); // Text color

        LineData lineData = new LineData(dataSet);
        calorieChart.setData(lineData);

        XAxis xAxis = calorieChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        calorieChart.invalidate();
    }

    private void updateCalorieChart(int calorieBurn) {
        LineData data = calorieChart.getData();
        if (data != null) {
            LineDataSet dataSet = (LineDataSet) data.getDataSetByIndex(0);
            if (dataSet != null) {
                dataSet.addEntry(new Entry(dataSet.getEntryCount(), calorieBurn));
                data.notifyDataChanged();
                calorieChart.notifyDataSetChanged();
                calorieChart.invalidate(); // Refresh chart
            }
        }
    }
}
