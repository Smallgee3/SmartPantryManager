package com.example.smartpantrymanager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom adapter that binds Ingredient objects from the database
 * to the rows of the pantry RecyclerView.
 */
public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {

    /** Lets the Activity react when a row is tapped or its delete button is pressed. */
    public interface OnItemActionListener {
        void onEdit(Ingredient item);
        void onDelete(Ingredient item);
    }

    private final List<Ingredient> items = new ArrayList<>();
    private final OnItemActionListener listener;
    private boolean highlightExpiring = true;

    public IngredientAdapter(OnItemActionListener listener) {
        this.listener = listener;
    }

    /** Turns the expiring-soon highlighting on or off (controlled by the Settings screen). */
    public void setHighlightExpiring(boolean highlightExpiring) {
        this.highlightExpiring = highlightExpiring;
    }

    /** Replaces the displayed data and refreshes the list. */
    public void setItems(List<Ingredient> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Ingredient item = items.get(position);

        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(formatQuantity(item.getQuantity()) + " " + item.getUnit());

        String expiry = item.getExpiryDate();
        if (expiry != null && !expiry.trim().isEmpty()) {
            String label = "Expires: " + expiry;
            int color = holder.defaultExpiryColor;

            if (highlightExpiring) {
                long days = ExpiryUtil.daysUntil(expiry);
                if (days < 0 && days != Long.MAX_VALUE) {
                    label = "Expired: " + expiry;
                    color = Color.parseColor("#C62828");
                } else if (days <= ExpiryUtil.SOON_DAYS) {
                    String when = days == 0 ? "today" : "in " + days + (days == 1 ? " day" : " days");
                    label = "Expiring soon (" + when + "): " + expiry;
                    color = Color.parseColor("#E65100");
                }
            }

            holder.tvExpiry.setText(label);
            holder.tvExpiry.setTextColor(color);
            holder.tvExpiry.setVisibility(View.VISIBLE);
        } else {
            holder.tvExpiry.setVisibility(View.GONE);
        }

        holder.rowContainer.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Shows 6 instead of 6.0 for whole numbers. */
    private static String formatQuantity(double q) {
        if (q == Math.floor(q)) {
            return String.valueOf((long) q);
        }
        return String.valueOf(q);
    }

    /** Holds references to the views of one row so they are looked up only once. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final View rowContainer;
        final TextView tvName, tvQuantity, tvExpiry;
        final ImageButton btnDelete;
        final int defaultExpiryColor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowContainer = itemView.findViewById(R.id.rowContainer);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            defaultExpiryColor = tvExpiry.getCurrentTextColor();
        }
    }
}