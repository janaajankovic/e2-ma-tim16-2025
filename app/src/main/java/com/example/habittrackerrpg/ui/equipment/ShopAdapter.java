package com.example.habittrackerrpg.ui.equipment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.Clothing;
import com.example.habittrackerrpg.data.model.EquipmentItem;
import com.example.habittrackerrpg.data.model.Potion;
import java.util.ArrayList;
import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private List<EquipmentItem> items = new ArrayList<>();
    private OnBuyButtonClickListener listener;

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop_equipment, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        EquipmentItem currentItem = items.get(position);
        holder.bind(currentItem);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<EquipmentItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    // ViewHolder klasa
    class ShopViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewName, textViewDescription, textViewEffect, textViewCost;
        private Button buttonBuy;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewItemName);
            textViewDescription = itemView.findViewById(R.id.textViewItemDescription);
            textViewEffect = itemView.findViewById(R.id.textViewItemEffect);
            textViewCost = itemView.findViewById(R.id.textViewItemCost);
            buttonBuy = itemView.findViewById(R.id.buttonBuy);

            buttonBuy.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onBuyButtonClick(items.get(position));
                }
            });
        }

        // Povezujemo podatke sa View-ovima
        public void bind(EquipmentItem item) {
            textViewName.setText(item.getName());
            textViewDescription.setText(item.getDescription());
            textViewCost.setText(String.format("%d Coins", item.getCost()));

            // Prikazujemo različit tekst efekta u zavisnosti od tipa predmeta
            if (item instanceof Potion) {
                Potion potion = (Potion) item;
                String duration = potion.isPermanent() ? "Permanent" : "Single Use";
                textViewEffect.setText(String.format("Effect: +%d%% PP (%s)", potion.getPpBoostPercent(), duration));
            } else if (item instanceof Clothing) {
                Clothing clothing = (Clothing) item;
                textViewEffect.setText(String.format("Effect: +%d%% %s", clothing.getEffectValue(), clothing.getClothingType().name()));
            } else {
                textViewEffect.setText("Effect: Special");
            }
        }
    }

    public interface OnBuyButtonClickListener {
        void onBuyButtonClick(EquipmentItem item);
    }

    public void setOnBuyButtonClickListener(OnBuyButtonClickListener listener) {
        this.listener = listener;
    }
}