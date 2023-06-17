package com.example.finalapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalapp.Adapters.TransactionAdapter;
import com.example.finalapp.Models.Transaction;
import com.example.finalapp.Utilities.BottomNavigationManager;
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
import java.util.List;

public class Income_Expanse_Activity extends AppCompatActivity implements TransactionAdapter.TransactionAdapterListener {

    private EditText searchBar;
    private RecyclerView recyclerView;
    private TextView List_tve_expanse,List_tve_income;
    private TransactionAdapter transactionAdapter;
    private boolean showIncomeList = false;
    private List<Transaction> incomesList;
    private List<Transaction> expansesList;
    private List<Transaction> transactions;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_expense);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        transactions = new ArrayList<>();
        findViews();
        initView();
        getLists();
        chooseIncomeExpanse();
        loadTransactions();
        setupSearchBar();
        navigationBar();
    }


    private void initView() {
        transactionAdapter = new TransactionAdapter(new ArrayList<>(), showIncomeList, this);
        recyclerView.setAdapter(transactionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void findViews() {
        searchBar = findViewById(R.id.search_bar);
        recyclerView = findViewById(R.id.recycler_view);
        List_tve_expanse = findViewById(R.id.List_tve_expanse);
        List_tve_income = findViewById(R.id.List_tve_income);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void navigationBar(){
        bottomNavigationView.setSelectedItemId(R.id.menu_list);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int menuItemId = item.getItemId();
                BottomNavigationManager.navigate(Income_Expanse_Activity.this, menuItemId);
                return true;
            }
        });
    }

    private void chooseIncomeExpanse() {
        List_tve_expanse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List_tve_expanse.setBackgroundColor(Color.parseColor("#03DAC5"));
                List_tve_income.setBackgroundColor(Color.WHITE);
                showIncomeList = false;
                loadTransactions();
            }
        });

        List_tve_income.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List_tve_expanse.setBackgroundColor(Color.WHITE);
                List_tve_income.setBackgroundColor(Color.parseColor("#03DAC5"));
                showIncomeList = true;
                loadTransactions();
            }
        });
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed in this case
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the list based on the search query
                String searchText = s.toString().trim();
                if (searchText.isEmpty()) {
                    // Show all items when the search bar is empty
                    transactionAdapter.showAllItems(transactions);
                } else {
                    transactionAdapter.filter(searchText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed in this case
            }
        });
    }

    private void loadTransactions() {
        transactions = showIncomeList ? incomesList : expansesList;
        if(transactions != null) {
            transactionAdapter.showAllItems(transactions);
            transactionAdapter.filter("");
        }
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
                }
                incomesList = incomeList;
                loadTransactions();
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
                }
                expansesList = expenseList;
                loadTransactions();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }

    protected void onResume() {
        super.onResume();
        getLists();
    }

    @Override
    public void onTransactionSelected(Transaction transaction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Transaction Details");

        // Inflate the layout for the dialog
        View view = getLayoutInflater().inflate(R.layout.transaction_details_dialog, null);

        // Find the views in the dialog layout
        TextView transactionNameTextView = view.findViewById(R.id.transaction_name_text_view);
        TextView amountTextView = view.findViewById(R.id.amount_text_view);
        TextView dateTextView = view.findViewById(R.id.date_text_view);
        TextView categoryTextView = view.findViewById(R.id.category_text_view);
        TextView noteTextView = view.findViewById(R.id.note_text_view);

        // Set the transaction details to the views
        transactionNameTextView.setText(" Name: " + transaction.getTransactionName());
        amountTextView.setText(" Amount: " + String.valueOf(transaction.getAmount()));
        dateTextView.setText(" Date: " + transaction.getDate());
        categoryTextView.setText(" Category: " + transaction.getCategory());
        noteTextView.setText("Note: " + transaction.getNote());

        builder.setView(view);
        builder.setPositiveButton("OK", null);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onTransactionDeleteClicked(int position) {
        // Get the transaction at the specified position
        Transaction transaction = transactions.get(position);

        // Delete the transaction from Firebase
        deleteTransactionFromFirebase(transaction);
    }

    private void deleteTransactionFromFirebase(Transaction transaction) {
        String userId = user.getUid();
        String transactionId = transaction.getId(); // Assuming a unique identifier (e.g., generated by Firebase) is available in the Transaction model

        DatabaseReference transactionRef = database.getReference()
                .child("transactions")
                .child(userId)
                .child(showIncomeList ? "incomes" : "expenses")
                .child(transactionId);

        transactionRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Transaction successfully deleted
                    Toast.makeText(Income_Expanse_Activity.this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                } else {
                    // Failed to delete transaction
                    Toast.makeText(Income_Expanse_Activity.this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



}
