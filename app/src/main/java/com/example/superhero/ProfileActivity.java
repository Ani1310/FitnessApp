package com.example.superhero;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText calorieBurnInput;
    private Button addCalorieButton, calculateAverageButton, analyzeButton, startDateButton, endDateButton;
    private TextView resultTextView;
    private String startDate = "", endDate = "";
    private BarChart barChart;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        calorieBurnInput = findViewById(R.id.calorieBurnInput);
        addCalorieButton = findViewById(R.id.addCalorieButton);
        calculateAverageButton = findViewById(R.id.calculateAverageButton);
        analyzeButton = findViewById(R.id.analyzeButton);
        startDateButton = findViewById(R.id.startDateButton);
        endDateButton = findViewById(R.id.endDateButton);
        resultTextView = findViewById(R.id.resultTextView);
        barChart = findViewById(R.id.barChart);

        // Add daily calorie burn
        addCalorieButton.setOnClickListener(v -> addDailyCalorieBurn());

        // Calculate average calorie burn
        calculateAverageButton.setOnClickListener(v -> calculateAverageCalorieBurn());

        // Analyze calorie burn between dates
        analyzeButton.setOnClickListener(v -> analyzeCalorieBurn());

        // Set date pickers
        startDateButton.setOnClickListener(v -> pickDate(true));
        endDateButton.setOnClickListener(v -> pickDate(false));
    }

    private void pickDate(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            if (isStartDate) {
                startDate = selectedDate;
                startDateButton.setText("Start Date: " + startDate);
            } else {
                endDate = selectedDate;
                endDateButton.setText("End Date: " + endDate);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void addDailyCalorieBurn() {
        String calorieBurn = calorieBurnInput.getText().toString();
        if (calorieBurn.isEmpty()) {
            Toast.makeText(this, "Please enter calorie burn value", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = Map.of(
                "calorieBurn", Integer.parseInt(calorieBurn),
                "date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime())
        );

        db.collection("calorieBurn")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Calorie burn added successfully!", Toast.LENGTH_SHORT).show();
                    calorieBurnInput.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add calorie burn", Toast.LENGTH_SHORT).show());
    }

    private void calculateAverageCalorieBurn() {
        db.collection("calorieBurn")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalCalories = 0;
                        int count = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            totalCalories += document.getLong("calorieBurn").intValue();
                            count++;
                        }
                        if (count > 0) {
                            int average = totalCalories / count;
                            resultTextView.setText("Average Calorie Burn: " + average + " cal");
                        } else {
                            resultTextView.setText("No data available for average calculation");
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void analyzeCalorieBurn() {
        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("calorieBurn")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<BarEntry> entries = new ArrayList<>();
                        ArrayList<String> dates = new ArrayList<>();
                        int index = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            int calorieBurn = document.getLong("calorieBurn").intValue();
                            String date = document.getString("date");
                            entries.add(new BarEntry(index, calorieBurn));
                            dates.add(date);
                            index++;
                        }

                        BarDataSet dataSet = new BarDataSet(entries, "Calorie Burn");
                        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                        dataSet.setValueTextSize(12f);

                        BarData barData = new BarData(dataSet);
                        barChart.setData(barData);

                        // Customize x-axis labels
                        XAxis xAxis = barChart.getXAxis();
                        xAxis.setValueFormatter(new ValueFormatter() {
                            @Override
                            public String getFormattedValue(float value) {
                                return dates.get((int) value);
                            }
                        });
                        xAxis.setGranularity(1f);
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                        YAxis leftAxis = barChart.getAxisLeft();
                        YAxis rightAxis = barChart.getAxisRight();
                        leftAxis.setAxisMinimum(0f);
                        rightAxis.setEnabled(false);

                        barChart.invalidate(); // Refresh chart
                    } else {
                        Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
