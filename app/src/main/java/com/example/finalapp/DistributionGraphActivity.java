package com.example.finalapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.example.finalapp.Models.Transaction;
import com.example.finalapp.Utilities.BottomNavigationManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DistributionGraphActivity extends AppCompatActivity {

    private Spinner spinnerTimePeriod;
    private Button buttonApply;
    private LineChart lineChart;

    private List<String> labels;

    private List<Entry> entries;

    private BottomNavigationView bottomNavigationView;

    private List<Transaction> incomesList;
    private List<Transaction> expansesList;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;
    private String[] timePeriods = {"Days", "Weeks", "Months", "Years"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distribution_graph);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        findViews();
        navigationBar();
        setupLineChart();
        setupSpinner();
        setupApplyButton();
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setExtraLeftOffset(15f); // Add extra left offset for better visibility

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawZeroLine(true); // Add zero line to the left axis

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Set x-axis position to bottom
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // Set x-axis granularity to 1

        lineChart.invalidate(); // Refresh the chart
    }
    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timePeriods);
        spinnerTimePeriod.setAdapter(spinnerAdapter);
    }
    private void setupApplyButton() {
        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateChart();
            }
        });
    }
    private void updateChart() {
        String selectedTimePeriod = spinnerTimePeriod.getSelectedItem().toString();

        List<Transaction> incomeTransactions = incomesList;
        List<Transaction> expenseTransactions = expansesList;

        List<Transaction> filteredIncomeTransactions = filterTransactionsByTimePeriod(incomeTransactions, selectedTimePeriod);
        List<Transaction> filteredExpenseTransactions = filterTransactionsByTimePeriod(expenseTransactions, selectedTimePeriod);

        List<Transaction> filteredTransactions = new ArrayList<>();
        filteredTransactions.addAll(filteredIncomeTransactions);
        filteredTransactions.addAll(filteredExpenseTransactions);

        Collections.sort(filteredTransactions, new TransactionDateComparator());

        entries = prepareEntries(filteredTransactions, selectedTimePeriod);
        LineDataSet dataSet = createDataSet();

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

        setupLineChart();
        lineChart.invalidate();

        formatXAxisLabels(selectedTimePeriod);
    }
    private List<Entry> prepareEntries(List<Transaction> transactions, String selectedTimePeriod) {
        List<Entry> entries = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        Date currentDate = calendar.getTime();
        calendar.setTime(currentDate);
        int entryIndex = 0;
        float totalAmount = 0;

        calendar.add(getCalendarField(selectedTimePeriod), getCalendarAmount(selectedTimePeriod));
        Date startDate = calendar.getTime();

        while (!startDate.after(currentDate)) {
            totalAmount += calculateTotalAmount(transactions, startDate, selectedTimePeriod);
            entries.add(new Entry(entryIndex, totalAmount));

            entryIndex++;
            calendar.add(getCalendarField(selectedTimePeriod), 1);
            startDate = calendar.getTime();
        }

        Log.d("entries",entries.toString());

        return entries;
    }
    private float calculateTotalAmount(List<Transaction> transactions, Date date, String selectedTimePeriod) {
        float totalAmount = 0f;

        for (Transaction transaction : transactions) {
            Date transactionDate;
            try {
                transactionDate = dateFormat.parse(transaction.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
                continue;
            }
            if (isSamePeriod(transactionDate, date, selectedTimePeriod)) {
                if (transaction.getType() == Transaction.Type.INCOME) {
                    totalAmount += transaction.getAmount();
                } else {
                    totalAmount -= transaction.getAmount();
                }
            }
        }

        return totalAmount;
    }
    private boolean isSamePeriod(Date transactionDate, Date date, String selectedTimePeriod) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.add(getCalendarField(selectedTimePeriod), -1);
        Date startDate = calendar.getTime();

        if(!(transactionDate.before(startDate) || transactionDate.after(date)) || transactionDate.equals(startDate) || transactionDate.equals(date)) {
            Log.d("enter","here");
            return true;
        }else{
            return false;
        }
    }
    private int getCalendarField(String selectedTimePeriod) {
        if (selectedTimePeriod.equals("Days")) {
            return Calendar.DAY_OF_YEAR;
        } else if (selectedTimePeriod.equals("Weeks")) {
            return Calendar.WEEK_OF_YEAR;
        } else if (selectedTimePeriod.equals("Months")) {
            return Calendar.MONTH;
        } else if (selectedTimePeriod.equals("Years")) {
            return Calendar.YEAR;
        }

        return -1;
    }
    private int getCalendarAmount(String selectedTimePeriod) {
        if (selectedTimePeriod.equals("Days")) {
            return -7;
        } else if (selectedTimePeriod.equals("Weeks")) {
            return -3;
        } else if (selectedTimePeriod.equals("Months")) {
            return -11;
        } else if (selectedTimePeriod.equals("Years")) {
            return -4;
        }

        return 0;
    }
    private LineDataSet createDataSet() {
        LineDataSet dataSet = new LineDataSet(entries, "Total Amount");
        dataSet.setColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.RED);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        return dataSet;
    }
    private void formatXAxisLabels(String selectedTimePeriod) {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);

        // Calculate the start and end dates for the selected time period
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();

        labels = new ArrayList<>();
        SimpleDateFormat labelFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM, yyyy", Locale.getDefault());

        int calendarField = getCalendarField(selectedTimePeriod);
        int calendarAmount = getCalendarAmount(selectedTimePeriod);

        if (calendarField != -1) {
            startDate.add(calendarField, calendarAmount);

            while (!startDate.after(endDate)) {
                String label;
                if (calendarField == Calendar.MONTH) {
                    label = monthFormat.format(startDate.getTime());
                } else if(calendarField == Calendar.WEEK_OF_YEAR) {
                    label = labelFormat.format(startDate.getTime()) + " - " + labelFormat.format(endDate.getTime());
                } else{
                    label = labelFormat.format(startDate.getTime());
                }
                labels.add(label);
                startDate.add(calendarField, 1);
            }

            xAxis.setLabelCount(labels.size());
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        }
    }
    private List<Transaction> filterTransactionsByTimePeriod(List<Transaction> transactions, String selectedTimePeriod) {
        List<Transaction> filteredTransactions = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int calendarField = getCalendarField(selectedTimePeriod);
        int calendarAmount = getCalendarAmount(selectedTimePeriod);

        calendar.add(calendarField, calendarAmount);
        Date startDate = calendar.getTime();
        Date endDate = Calendar.getInstance().getTime();

        for (Transaction transaction : transactions) {
            Date transactionDate;
            try {
                transactionDate = dateFormat.parse(transaction.getDate());
                calendar.setTime(transactionDate);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                transactionDate = calendar.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                continue;
            }
            System.out.println("Start Date: " + dateFormat.format(startDate));
            System.out.println("End Date: " + dateFormat.format(endDate));
            System.out.println("Transaction Date: " + dateFormat.format(transactionDate));


            if ((transactionDate.after(startDate) || isSameDay(transactionDate, startDate)) && (transactionDate.before(endDate) || isSameDay(transactionDate, endDate))) {
                filteredTransactions.add(transaction);
                System.out.println("entered: ");

            }
        }

        return filteredTransactions;
    }
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
    private class TransactionDateComparator implements Comparator<Transaction> {
        @Override
        public int compare(Transaction t1, Transaction t2) {
            Date date1, date2;
            try {
                date1 = dateFormat.parse(t1.getDate());
                date2 = dateFormat.parse(t2.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }

            return date1.compareTo(date2);
        }
    }
    private void navigationBar() {
        bottomNavigationView.setSelectedItemId(R.id.menu_distribution);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int menuItemId = item.getItemId();
                BottomNavigationManager.navigate(DistributionGraphActivity.this, menuItemId);
                return true;
            }
        });
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuItemId = item.getItemId();
        BottomNavigationManager.navigate(this, menuItemId);
        return super.onOptionsItemSelected(item);
    }
    private void findViews() {
        spinnerTimePeriod = findViewById(R.id.Graph_sp_TimePeriod);
        buttonApply = findViewById(R.id.buttonApply);
        lineChart = findViewById(R.id.lineChart);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    public void getLists() {
        String userId = user.getUid();
        ArrayList<Transaction> incomeList = new ArrayList<>();
        ArrayList<Transaction> expenseList = new ArrayList<>();

        databaseRef = database.getReference().child("transactions").child(userId).child("incomes");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                incomeList.clear(); // Clear the list before adding transactions
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = snapshot.getValue(Transaction.class);
                    incomeList.add(transaction);
                }
                incomesList = incomeList;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        databaseRef = database.getReference().child("transactions").child(userId).child("expenses");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                expenseList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = snapshot.getValue(Transaction.class);
                    expenseList.add(transaction);
                    Log.d("expenseList", "" + expenseList);

                }
                expansesList = expenseList;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        getLists();
    }





}