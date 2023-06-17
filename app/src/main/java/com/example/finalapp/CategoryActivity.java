package com.example.finalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.finalapp.Adapters.CategoryAdapter;
import com.example.finalapp.Models.Category;
import com.example.finalapp.Utilities.BottomNavigationManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private TextView Category_tve_expanse, Category_tve_income;
    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private BottomNavigationView bottomNavigationView;
    private List<Category> incomeCategories;
    private List<Category> expenseCategories;
    private boolean showIncomeList = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        findViews();
        navigationBar();
        chooseIncomeExpanse();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(expenseCategories);
        recyclerView.setAdapter(categoryAdapter);
        loadDataFromDatabase();
    }
    private void chooseIncomeExpanse() {
        Category_tve_expanse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Category_tve_expanse.setBackgroundColor(0xFF03DAC5);
                Category_tve_income.setBackgroundColor(Color.WHITE);
                showIncomeList = false;
                categoryAdapter.updateData(expenseCategories);
            }
        });

        Category_tve_income.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Category_tve_expanse.setBackgroundColor(Color.WHITE);
                Category_tve_income.setBackgroundColor(0xFF03DAC5);
                showIncomeList = true;
                categoryAdapter.updateData(incomeCategories);
            }
        });
    }
    private void navigationBar() {
        bottomNavigationView.setSelectedItemId(R.id.menu_category);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int menuItemId = item.getItemId();
                BottomNavigationManager.navigate(CategoryActivity.this, menuItemId);
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
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerView = findViewById(R.id.recyclerViewCategories);
        Category_tve_expanse = findViewById(R.id.Category_tve_expanse);
        Category_tve_income = findViewById(R.id.Category_tve_income);
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
                    } else {
                        expenseCategories = getDefaultExpenseCategories();
                        writeExpenseCategoriesToDatabase(databaseRef);
                    }
                    categoryAdapter.updateData(expenseCategories);
                } else {
                    expenseCategories = getDefaultExpenseCategories();
                    writeExpenseCategoriesToDatabase(databaseRef);
                    categoryAdapter.updateData(expenseCategories);
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
                    } else {
                        incomeCategories = getDefaultIncomeCategories();
                        writeIncomeCategoriesToDatabase(databaseRef);
                    }
                } else {
                    incomeCategories = getDefaultIncomeCategories();
                    writeIncomeCategoriesToDatabase(databaseRef);
                }
            }
        });
    }
    private List<Category> getDefaultExpenseCategories() {
        List<Category> defaultCategories = new ArrayList<>();
        defaultCategories.add(new Category("Food", Color.RED));
        defaultCategories.add(new Category("Transportation", Color.BLUE));
        defaultCategories.add(new Category("Rent", Color.GREEN));
        defaultCategories.add(new Category("Utilities", Color.YELLOW));
        defaultCategories.add(new Category("Entertainment", Color.MAGENTA));
        defaultCategories.add(new Category("Healthcare", Color.CYAN));
        defaultCategories.add(new Category("Shopping", Color.GRAY));
        defaultCategories.add(new Category("Travel", Color.DKGRAY));
        defaultCategories.add(new Category("Education", Color.LTGRAY));
        defaultCategories.add(new Category("Miscellaneous", Color.BLACK));
        return defaultCategories;
    }
    private List<Category> getDefaultIncomeCategories() {
        List<Category> defaultCategories = new ArrayList<>();
        defaultCategories.add(new Category("Salary", Color.GREEN));
        defaultCategories.add(new Category("Freelance", Color.YELLOW));
        defaultCategories.add(new Category("Investments", Color.BLUE));
        return defaultCategories;
    }
    private void writeExpenseCategoriesToDatabase(DatabaseReference databaseRef) {
        for (Category category : expenseCategories) {
            String key = databaseRef.child("expenses").push().getKey();
            databaseRef.child("expenses").child(key).setValue(category);
        }
    }
    private void writeIncomeCategoriesToDatabase(DatabaseReference databaseRef) {
        for (Category category : incomeCategories) {
            String key = databaseRef.child("income").push().getKey();
            databaseRef.child("income").child(key).setValue(category);
        }
    }



}
