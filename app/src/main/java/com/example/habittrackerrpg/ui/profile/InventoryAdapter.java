package com.example.habittrackerrpg.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittrackerrpg.R;
import com.example.habittrackerrpg.data.model.EquipmentItem;
import com.example.habittrackerrpg.data.model.EquipmentType;
import com.example.habittrackerrpg.data.model.UserEquipment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<UserEquipment> userInventory = new ArrayList<>();
    private Map<String, EquipmentItem> equipmentDefinitions = new HashMap<>();

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory_equipment, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        UserEquipment userItem = userInventory.get(position);
        EquipmentItem definition = equipmentDefinitions.get(userItem.getEquipmentId());
        holder.bind(userItem, definition);
    }

    @Override
    public int getItemCount() {
        return userInventory.size();
    }

    public void setData(List<UserEquipment> userInventory, Map<String, EquipmentItem> equipmentDefinitions) {
        this.userInventory = userInventory;
        this.equipmentDefinitions = equipmentDefinitions;
        notifyDataSetChanged();
    }

    class InventoryViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewName;
        private TextView textViewState;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewItemName);
            textViewState = itemView.findViewById(R.id.textViewItemState);
        }

        public void bind(UserEquipment userItem, EquipmentItem definition) {
            if (definition != null) {
                textViewName.setText(definition.getName());
            } else {
                textViewName.setText("Unknown Item");
            }

            String state = "State: In inventory";
            if(userItem.isActive()) {
                state = "State: Active";
                if(userItem.getType() == EquipmentType.CLOTHING) {
                    state += " (" + userItem.getBattlesRemaining() + " battles remaining)";
                }
            }
            textViewState.setText(state);
        }
    }
}