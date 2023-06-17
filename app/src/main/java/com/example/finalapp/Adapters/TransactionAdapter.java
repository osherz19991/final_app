package com.example.finalapp.Adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalapp.Models.Transaction;
import com.example.finalapp.R;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private List<Transaction> filteredList;
    private TransactionAdapterListener  listener;


    public TransactionAdapter(List<Transaction> transactionList, boolean showIncomeList, TransactionAdapterListener listener) {
        this.transactionList = new ArrayList<>(transactionList);
        this.filteredList = new ArrayList<>(transactionList);
        if (!showIncomeList) {
            filterExpanseList(transactionList);
        }
        this.listener = listener;
    }

    private void filterExpanseList(List<Transaction> transactionList) {
        List<Transaction> expanses = transactionList;
        transactionList.retainAll(expanses);
        filteredList.retainAll(expanses);
    }

    public void filter(String searchText) {
        filterList(searchText);
    }

    private void filterList(String searchText) {
        filteredList.clear();
        if (TextUtils.isEmpty(searchText)) {
            filteredList.addAll(transactionList);
        } else {
            String searchLowerCase = searchText.toLowerCase();
            for (Transaction transaction : transactionList) {
                String transactionName = transaction.getTransactionName().toLowerCase();
                if (transactionName.contains(searchLowerCase)) {
                    filteredList.add(transaction);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void showAllItems(List<Transaction> transactions) {
        transactionList.clear();
        transactionList.addAll(transactions);
        filterList("");
    }


    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = filteredList.get(holder.getAdapterPosition());

        holder.tvTransactionName.setText(" Name:"+transaction.getTransactionName());
        holder.tvTransactionAmount.setText(" Amount:"+ String.valueOf(transaction.getAmount()));
        holder.tvTransactionDate.setText(" Date:"+transaction.getDate());
        holder.tvTransactionCategory.setText(" Category:"+transaction.getCategory());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTransactionSelected(transaction);
                }
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        listener.onTransactionDeleteClicked(adapterPosition);
                    }
                }
            }
        });
        holder.deleteButton.setTag(position);
    }


    @Override
    public int getItemCount() {
        return filteredList.size();
    }


    public class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTransactionName;
        private TextView tvTransactionAmount;
        private TextView tvTransactionDate;
        private TextView tvTransactionCategory;
        private Button deleteButton;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            tvTransactionName = itemView.findViewById(R.id.tvTransactionName);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionCategory = itemView.findViewById(R.id.tvTransactionCategory);
            deleteButton = itemView.findViewById(R.id.btnDelete);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onTransactionDeleteClicked(position);
                    }
                }
            });
        }

    }

    public interface TransactionAdapterListener {
        void onTransactionSelected(Transaction transaction);

        void onTransactionDeleteClicked(int position);

    }


}
