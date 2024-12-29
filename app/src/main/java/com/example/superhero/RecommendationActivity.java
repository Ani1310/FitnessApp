package com.example.superhero;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RecommendationActivity extends AppCompatActivity {

    private TextView bmiCategoryText, dietRecommendationText, exerciseRecommendationText;
    private ImageView dietImage, exerciseImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommendationactivity);

        bmiCategoryText = findViewById(R.id.bmiCategoryText);
        dietRecommendationText = findViewById(R.id.dietRecommendationText);
        exerciseRecommendationText = findViewById(R.id.exerciseRecommendationText);
        dietImage = findViewById(R.id.dietImage);
        exerciseImage = findViewById(R.id.exerciseImage);

        // Get BMI passed from MainActivity
        float bmi = getIntent().getFloatExtra("bmi", 0);

        // Determine BMI category and set recommendations
        if (bmi < 18.5) {
            setRecommendations("Underweight",
                    R.drawable.underweight_diet,
                    "Include calorie-dense foods like nuts, dairy, and whole grains.",
                    R.drawable.underweight_exercise,
                    "Focus on strength training to build muscle mass.");
        } else if (bmi >= 18.5 && bmi <= 24.9) {
            setRecommendations("Normal Weight",
                    R.drawable.normal_diet,
                    "Maintain a balanced diet with proteins, carbs, and healthy fats.",
                    R.drawable.normal_exercise,
                    "Combine cardio and strength training to stay fit.");
        } else if (bmi >= 25 && bmi <= 29.9) {
            setRecommendations("Overweight",
                    R.drawable.overweight_diet,
                    "Opt for low-calorie, high-fiber meals like vegetables and lean proteins.",
                    R.drawable.overweight_exercise,
                    "Engage in moderate cardio like cycling or brisk walking.");
        } else {
            setRecommendations("Obesity",
                    R.drawable.obesity_diet,
                    "Adopt portion-controlled, low-carb, high-protein meals.",
                    R.drawable.obesity_exercise,
                    "Start with low-impact exercises like walking or swimming.");
        }
    }

    private void setRecommendations(String category, int dietImgRes, String dietText, int exerciseImgRes, String exerciseText) {
        bmiCategoryText.setText("BMI Category: " + category);
        dietImage.setImageResource(dietImgRes);
        dietRecommendationText.setText("Diet Recommendation: " + dietText);
        exerciseImage.setImageResource(exerciseImgRes);
        exerciseRecommendationText.setText("Exercise Recommendation: " + exerciseText);
    }
}
