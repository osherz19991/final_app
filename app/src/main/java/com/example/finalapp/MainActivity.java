package com.example.finalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.finalapp.Models.Category;
import com.example.finalapp.Models.Transaction;
import com.example.finalapp.Utilities.BottomNavigationManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView Main_tve_expanse, Main_tve_income;
    private Button Main_btn_logout;
    private TextView Main_tv_financeStatus,Main_tv_name;
    private PieChart pieChart;
    private BottomNavigationView bottomNavigationView;
    private boolean showIncomeList = false;

    private List<Category> incomeCategories;
    private List<Category> expenseCategories;

    private int[] categoryColors;
    private List<Transaction> incomesList;
    private List<Transaction> expansesList;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        findViews();
        initViews();
        getLists();
        loadDataFromDatabase();
        createChart();
        navigationBar();

    }
    private void navigationBar(){
        bottomNavigationView.setSelectedItemId(R.id.menu_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int menuItemId = item.getItemId();
                BottomNavigationManager.navigate(MainActivity.this, menuItemId);
                return true;
            }
        });
    }
    private void createChart() {
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        List<Transaction> transactions = showIncomeList ? incomesList : expansesList;

        float totalIncome = calculateTotalAmount(incomesList);
        float totalExpense = calculateTotalAmount(expansesList);

        Main_tv_financeStatus.setText("Balance:\n" + String.format("%.2f", (totalIncome - totalExpense)));

        if (totalIncome - totalExpense < 0) {
            Main_tv_financeStatus.setTextColor(Color.RED);
        } else if (totalIncome - totalExpense > 0) {
            Main_tv_financeStatus.setTextColor(Color.GREEN);
        } else {
            Main_tv_financeStatus.setTextColor(Color.WHITE);
        }

        HashMap<String, Float> categorySumMap = new HashMap<>();
        if(transactions!=null) {
            for (Transaction transaction : transactions) {
                String category = transaction.getCategory();
                float amount = transaction.getAmount();

                if (categorySumMap.containsKey(category)) {
                    float sum = categorySumMap.get(category);
                    categorySumMap.put(category, sum + amount);
                } else {
                    categorySumMap.put(category, amount);
                }
            }

            ArrayList<String> categories = new ArrayList<>(categorySumMap.keySet());

            createCategoryColors(categories,showIncomeList);

            for (String category : categories) {
                float amount = categorySumMap.get(category);
                PieEntry entry = new PieEntry(amount, category);
                entry.setData(category);
                pieEntries.add(entry);
            }

            PieDataSet pieDataSet = new PieDataSet(pieEntries, "Money");
            pieDataSet.setValueTextColor(Color.BLACK);
            pieDataSet.setValueTextSize(25f);
            if(categoryColors != null)
                pieDataSet.setColors(categoryColors);

            PieData pieData = new PieData(pieDataSet);
            pieChart.setHoleRadius(70);
            pieChart.setData(pieData);
            pieChart.getDescription().setEnabled(false);
            pieChart.setCenterTextSize(24f);
            pieChart.setUsePercentValues(true);
            pieChart.animate();
        }
    }
    private void createCategoryColors(ArrayList<String> categories, boolean showIncomeList) {
        List<Category> categoriesList = showIncomeList ? incomeCategories : expenseCategories;

        if (categoriesList != null) {
            categoryColors = new int[categories.size()];

            for (int i = 0; i < categories.size(); i++) {
                String category = categories.get(i);
                for (int j = 0; j < categoriesList.size(); j++) {
                    if (categoriesList.get(j).getName().equals(category)) {
                        int color = categoriesList.get(j).getColor();
                        categoryColors[i] = color;
                        break;
                    }
                }
            }
        }
    }
    private float calculateTotalAmount(List<Transaction> transactions) {
        float totalAmount = 0f;

        if (transactions != null) {
            for (Transaction transaction : transactions) {
                float amount = transaction.getAmount();
                totalAmount += amount;
            }
        }

        return totalAmount;
    }
    private void initViews() {
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
            startActivity(intent);
            finish();
        } else {
            DatabaseReference userRef = database.getReference().child("Users").child(user.getUid());
            userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.getValue(String.class);
                        Main_tv_name.setText("Username:" + username);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        Main_btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login_Activity.class);
                startActivity(intent);
                finish();
            }
        });
        chooseIncomeExpanse();
        Main_tv_financeStatus.setText("0.00");
    }
    private void chooseIncomeExpanse() {
        Main_tve_expanse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Main_tve_expanse.setBackgroundColor(0xFF03DAC5);
                Main_tve_income.setBackgroundColor(Color.WHITE);
                showIncomeList = false;
                createChart();
                pieChart.invalidate();
            }
        });

        Main_tve_income.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Main_tve_expanse.setBackgroundColor(Color.WHITE);
                Main_tve_income.setBackgroundColor(0xFF03DAC5);
                showIncomeList = true;
                createChart();
                pieChart.invalidate();
            }
        });
    }
    private void findViews() {
        Main_tve_expanse = findViewById(R.id.Main_tve_expanse);
        Main_tve_income = findViewById(R.id.Main_tve_income);
        pieChart = findViewById(R.id.Main_pc_pieChart);
        Main_btn_logout = findViewById(R.id.logout);
        Main_tv_name = findViewById(R.id.Main_tv_name);
        Main_tv_financeStatus = findViewById(R.id.Main_tv_financeStatus);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuItemId = item.getItemId();
        BottomNavigationManager.navigate(this, menuItemId);
        return super.onOptionsItemSelected(item);
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
                    Log.d("incomeList", "Income list retrieved: " + incomeList);
                }
                incomesList = incomeList;
                createChart();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MainActivity", "Failed to retrieve income list: " + databaseError.getMessage());
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
                createChart();
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
        loadDataFromDatabase();
        createChart();
    }

    private void loadDataFromDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference().child("categories");

        databaseRef.child("expenses").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot != null && snapshot.exists()) {
                        expenseCategories = new ArrayList<>();
                        for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                            Category category = categorySnapshot.getValue(Category.class);
                            expenseCategories.add(category);
                        }
                        createChart();
                        pieChart.invalidate();
                    }
                }
            }
        });

        databaseRef.child("income").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot != null && snapshot.exists()) {
                        incomeCategories = new ArrayList<>();
                        for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                            Category category = categorySnapshot.getValue(Category.class);
                            incomeCategories.add(category);
                        }
                        createChart();
                        pieChart.invalidate();
                    }
                }
            }
        });
    }


}