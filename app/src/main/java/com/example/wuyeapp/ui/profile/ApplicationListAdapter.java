package com.example.wuyeapp.ui.profile;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wuyeapp.R;
import com.example.wuyeapp.model.application.HousingApplication;

import java.util.List;

public class ApplicationListAdapter extends RecyclerView.Adapter<ApplicationListAdapter.ViewHolder> {
    private final List<HousingApplication> data;

    public ApplicationListAdapter(List<HousingApplication> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HousingApplication app = data.get(position);
        holder.tvCommunity.setText(app.getCommunityName());
        holder.tvHouse.setText(app.getBuildingName() + "-" + app.getUnitName() + "-" + app.getHouseNumber());
        holder.tvTime.setText(app.getApplicationTime());
        holder.tvStatus.setText(app.getStatus());
        // 状态颜色区分
        switch (app.getStatus()) {
            case "待审核":
                holder.tvStatus.setTextColor(Color.parseColor("#FFA500")); // 橙色
                holder.tvCallback.setVisibility(View.GONE);
                break;
            case "已审核":
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // 绿色
                holder.tvCallback.setVisibility(View.GONE);
                break;
            case "已拒绝":
                holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // 红色
                holder.tvCallback.setVisibility(View.VISIBLE);
                holder.tvCallback.setText("拒绝原因: " + (app.getCallbackMessage() == null ? "无" : app.getCallbackMessage()));
                break;
            default:
                holder.tvStatus.setTextColor(Color.GRAY);
                holder.tvCallback.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommunity, tvHouse, tvTime, tvStatus, tvCallback;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommunity = itemView.findViewById(R.id.tvCommunity);
            tvHouse = itemView.findViewById(R.id.tvHouse);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCallback = itemView.findViewById(R.id.tvCallback);
        }
    }
} 