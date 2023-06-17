package com.example.finalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.finalapp.Models.Category;
import com.example.finalapp.Models.Transaction;
import com.example.finalapp.Utilities.BottomNavigationManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Add_expense_income_Activity extends AppCompatActivity {

    private TextView Add_tve_expanse,Add_tve_income;
    private Spinner Add_sp_Category;
    private Button Add_btn_Submit;
    private List<String> categoriesList;
    private EditText Add_et_TransactionName,Add_et_Amount,Add_et_Date,Add_et_Note;
    private SimpleDateFormat dateFormat;
    private Calendar calendar;
    private String currentDate;
    private List<Category> incomeCategories;
    private List<Category> expenseCategories;
    private BottomNavigationView bottomNavigationView;
    private boolean showIncomeList = false;
    private FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense_income);
        database = FirebaseDatabase.getInstance();
        findViews();
        initViews();
        navigationBar();
    }
    private void navigationBar(){
        bottomNavigationView.setSelectedItemId(R.id.menu_add);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int menuItemId = item.getItemId();
                BottomNavigationManager.navigate(Add_expense_income_Activity.this, menuItemId);
                return true;
            }
        });
    }
    private void showDatePickerDialog() {
        calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        Add_et_Date.setText(selectedDate);
                    }
                },
                year, month, day);

        datePickerDialog.show();
    }
    private void initViews() {
        AddCategory();
        chooseIncomeExpanse();
        submit();
        fetchCategoriesFromDatabase();
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        currentDate = dateFormat.format(calendar.getTime());
        Add_et_Date.setText(currentDate);

        Add_et_Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }
    private void chooseIncomeExpanse() {
        Add_tve_expanse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Add_tve_expanse.setBackgroundColor(0xFF03DAC5);
                Add_tve_income.setBackgroundColor(Color.WHITE);
                showIncomeList = false;
                fetchCategoriesFromDatabase();
            }
        });

        Add_tve_income.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Add_tve_expanse.setBackgroundColor(Color.WHITE);
                Add_tve_income.setBackgroundColor(0xFF03DAC5);
                showIncomeList = true;
                fetchCategoriesFromDatabase();
            }
        });
    }

    private void populateCategories(List<Category> Categories) {
        List<Category> categories = Categories;

        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Add_sp_Category.setAdapter(categoryAdapter);
    }

    private void AddCategory() {
        categoriesList = new ArrayList<>();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoriesList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Add_sp_Category.setAdapter(categoryAdapter);
    }
    private void submit() {
        Add_btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String transactionName = Add_et_TransactionName.getText().toString();
                float amount = Float.parseFloat(Add_et_Amount.getText().toString());
                String date = Add_et_Date.getText().toString();
                String category = Add_sp_Category.getSelectedItem().toString();
                String note = Add_et_Note.getText().toString();
                Transaction.Type type = showIncomeList ? Transaction.Type.INCOME : Transaction.Type.EXPENSE;


                Transaction transaction = new Transaction(transactionName, amount, date, category, note, Color.WHITE, type);

                // Save the transaction to the database under the user's ID
                saveTransactionToDatabase(transaction);

                Intent intent = new Intent(Add_expense_income_Activity.this, Income_Expanse_Activity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void saveTransactionToDatabase(Transaction transaction) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        DatabaseReference databaseReference;
        if(transaction.getType() == Transaction.Type.EXPENSE)
             databaseReference = database.getInstance().getReference().child("transactions").child(userId).child("expenses");
        else{
             databaseReference = database.getInstance().getReference().child("transactions").child(userId).child("incomes");
        }
        String transactionId = databaseReference.push().getKey();
        databaseReference.child(transactionId).setValue(transaction);

    }




    private void findViews() {
        Add_tve_expanse = findViewById(R.id.Add_tve_expanse);
        Add_tve_income  = findViewById(R.id.Add_tve_income);
        Add_et_TransactionName = findViewById(R.id.Add_et_TransactionName);
        Add_et_Amount = findViewById(R.id.Add_et_Amount);
        Add_et_Note = findViewById(R.id.Add_et_Note);
        Add_et_Date = findViewById(R.id.Add_et_Date);
        Add_sp_Category = findViewById(R.id.Add_sp_Category);
        Add_btn_Submit = findViewById(R.id.Add_btn_Submit);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuItemId = item.getItemId();
        BottomNavigationManager.navigate(this, menuItemId);
        return super.onOptionsItemSelected(item);
    }

    private void fetchCategoriesFromDatabase() {

        if(showIncomeList) {
            DatabaseReference incomeRef = database.getReference().child("categories").child("income");
            incomeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    incomeCategories = new ArrayList<>();
                    for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                        Category category = categorySnapshot.getValue(Category.class);
                        incomeCategories.add(category);
                    }
                    populateCategories(incomeCategories);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Database Error", "Failed to retrieve income categories: " + error.getMessage());
                }
            });
        }else{
        DatabaseReference expenseRef = database.getReference().child("categories").child("expenses");
        expenseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseCategories = new ArrayList<>();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    Category category = categorySnapshot.getValue(Category.class);
                    expenseCategories.add(category);
                }
                populateCategories(expenseCategories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Database Error", "Failed to retrieve expense categories: " + error.getMessage());
            }
        });
    }
    }

    protected void onResume() {
        super.onResume();
        fetchCategoriesFromDatabase();
    }


}