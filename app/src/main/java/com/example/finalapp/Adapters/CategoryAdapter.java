package com.example.finalapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalapp.Models.Category;
import com.example.finalapp.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> selectedCategories;

    public CategoryAdapter(List<Category> selectedCategories) {
        this.selectedCategories = selectedCategories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = selectedCategories.get(position);
        holder.categoryNameTextView.setText(category.getName());
        holder.categoryColorView.setBackgroundColor(category.getColor());
    }

    @Override
    public int getItemCount() {
        if (selectedCategories != null) {
            return selectedCategories.size();
        }
        return 0;
    }

    public void updateData(List<Category> selectedCategories) {
        this.selectedCategories = selectedCategories;
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        View categoryColorView;

        CategoryViewHolder(View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.textViewCategoryName);
            categoryColorView = itemView.findViewById(R.id.viewCategoryColor);
        }
    }
}
