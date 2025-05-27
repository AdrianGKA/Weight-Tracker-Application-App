package com.school.finalProject;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.*;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class DashboardActivity extends AppCompatActivity {
    private LineChart chart;
    private DBHelper dbHelper;
    private Button addWeigtButton;
    private long userId;
    private ActivityResultLauncher<Intent> addWeightLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboardActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView dateText = findViewById(R.id.dateText);
        dbHelper = new DBHelper(this);
        chart = findViewById(R.id.lineChart);
        addWeigtButton = findViewById(R.id.addWeightButton);

        //Gets current Date
        Date currentDate = new Date();

        //Formats date to weekday, Month, day
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
        String formatedDate = dateFormat.format(currentDate);

        //sets the formatted date to textview
        dateText.setText(formatedDate);

        // Gets the user ID from the intent
        userId = getIntent().getLongExtra("USER_ID", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if the user ID is invalid
            return;
        }

        //builds the graph when DashboardActivity fist built
        buildGraph();

        addWeightLauncher = registerForActivityResult( //method to update graph is adding a weight is successful
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        buildGraph();
                    }
        });

        addWeigtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, AddWeightActivity.class);
                intent.putExtra("USER_ID", userId);
                addWeightLauncher.launch(intent);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        buildGraph();
    }

    private void buildGraph(){ //method for building the graph
        // Fetches weight entries for logged-in user
        Cursor cursor = dbHelper.getWeightByUserID(userId);

        ArrayList<Entry> entries = new ArrayList<>();  //Array for storing weight entries
        ArrayList<String> labels = new ArrayList<>(); // For storing formatted dates
        int index = 0;

        while (cursor.moveToNext()) {
            double weight = cursor.getDouble(cursor.getColumnIndexOrThrow("weight"));
            String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));

            entries.add(new Entry(index, (float) weight));
            labels.add(dateStr); // Add the formatted date to the labels list
            index++;
        }
        cursor.close();

        if (entries.isEmpty()) {
            Toast.makeText(this, "No weight data found.", Toast.LENGTH_SHORT).show();
        } else {
            // Create a LineDataSet and set it to the chart
            LineDataSet dataSet = new LineDataSet(entries, "Weight Progress");
            LineData lineData = new LineData(dataSet);

            // Configure X-axis labels
            XAxis xAxis = chart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Use the formatted dates as labels
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Place labels at the bottom
            xAxis.setGranularity(1f); // Ensure all labels are displayed
            xAxis.setLabelCount(labels.size()); // Set the number of labels

            chart.setData(lineData);
            chart.invalidate(); // Refresh the chart
        }
    }
}
