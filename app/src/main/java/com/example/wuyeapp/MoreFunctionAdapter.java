package com.example.wuyeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MoreFunctionAdapter extends RecyclerView.Adapter<MoreFunctionAdapter.ViewHolder> {
    
    private List<FunctionItem> items = new ArrayList<>();
    private boolean isManageMode = false;
    private final Context context;
    private final boolean isSmartDoor;
    
    public MoreFunctionAdapter(Context context, boolean isSmartDoor) {
        this.context = context;
        this.isSmartDoor = isSmartDoor;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_more_function, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FunctionItem item = items.get(position);
        
        holder.icon.setImageResource(item.getIconResId());
        holder.name.setText(item.getName());
        
        // 管理模式下显示选择状态
        holder.selectIcon.setVisibility(isManageMode ? View.VISIBLE : View.GONE);
        holder.selectIcon.setImageResource(item.isSelected() ? 
                R.drawable.ic_check : R.drawable.ic_add);
        
        holder.itemView.setOnClickListener(v -> {
            if (isManageMode) {
                item.setSelected(!item.isSelected());
                notifyItemChanged(position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public void setItems(List<FunctionItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }
    
    public void setManageMode(boolean manageMode) {
        isManageMode = manageMode;
        notifyDataSetChanged();
    }
    
    public int getSelectedCount() {
        int count = 0;
        for (FunctionItem item : items) {
            if (item.isSelected()) count++;
        }
        return count;
    }
    
    public void resetDefaultSelection() {
        int count = 0;
        for (FunctionItem item : items) {
            item.setSelected(count < 7);
            count++;
        }
        notifyDataSetChanged();
    }
    
    public List<FunctionItem> getItems() {
        return items;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        ImageView selectIcon;
        
        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.function_icon);
            name = itemView.findViewById(R.id.function_name);
            selectIcon = itemView.findViewById(R.id.select_icon);
        }
    }
} 