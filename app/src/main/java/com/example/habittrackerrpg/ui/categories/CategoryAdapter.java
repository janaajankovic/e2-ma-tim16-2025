package com.example.habittrackerrpg.ui.categories;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.Category;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryActionsListener {
        void onEditClick(Category category);
        void onDeleteClick(Category category);
    }

    private final List<Category> categories;
    private final OnCategoryActionsListener listener;

    public CategoryAdapter(List<Category> categories, OnCategoryActionsListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public void setCategories(List<Category> newCategories) {
        this.categories.clear();
        this.categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, listener);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryName;
        private final View categoryColorView;
        private final ImageView editButton;
        private final ImageView deleteButton;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.text_view_category_name);
            categoryColorView = itemView.findViewById(R.id.view_category_color);
            editButton = itemView.findViewById(R.id.button_edit_category);
            deleteButton = itemView.findViewById(R.id.button_delete_category);
        }

        public void bind(final Category category, final OnCategoryActionsListener listener) {
            Log.d("CategoryAdapter", "Binding category: Name=" + category.getName() + ", Color=" + category.getColor());

            if (category.getName() != null) {
                categoryName.setText(category.getName());
            }
            if (category.getColor() != null) {
                GradientDrawable background = (GradientDrawable) categoryColorView.getBackground().mutate();
                background.setColor(Color.parseColor(category.getColor()));
            }

            editButton.setOnClickListener(v -> listener.onEditClick(category));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(category));
        }
    }
}