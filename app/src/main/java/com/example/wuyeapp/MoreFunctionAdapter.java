package com.example.wuyeapp;

import android.content.Context;
import android.graphics.Color;
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
    
    private List<FunctionItem> items;
    private boolean isManageMode;
    private Context context;
    
    public MoreFunctionAdapter(Context context, boolean isManageMode) {
        this.context = context;
        this.isManageMode = isManageMode;
        this.items = new ArrayList<>();
    }
    
    public void setManageMode(boolean manageMode) {
        this.isManageMode = manageMode;
        notifyDataSetChanged();
    }
    
    public void setItems(List<FunctionItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_more_function, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FunctionItem item = items.get(position);
        
        holder.icon.setImageResource(item.getIconResId());
        holder.name.setText(item.getName());
        
        // 管理模式下显示选择状态
        if (isManageMode) {
            holder.selectIcon.setVisibility(View.VISIBLE);
            if (item.isHomeApp()) {
                // 如果是首页应用，显示删除图标
                holder.selectIcon.setImageResource(R.drawable.ic_remove);
                holder.selectIcon.setColorFilter(Color.RED);
            } else {
                // 如果不是首页应用，显示添加图标
                holder.selectIcon.setImageResource(R.drawable.ic_add);
                holder.selectIcon.setColorFilter(Color.BLUE);
            }
        } else {
            holder.selectIcon.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (isManageMode) {
                item.setHomeApp(!item.isHomeApp());
                item.setSelected(item.isHomeApp());
                notifyItemChanged(position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
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
            boolean shouldBeSelected = count < 7;
            item.setSelected(shouldBeSelected);
            item.setHomeApp(shouldBeSelected);
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