package com.example.wuyeapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wuyeapp.ui.maintenance.MaintenanceDetailActivity;
import com.example.wuyeapp.R;
import com.example.wuyeapp.model.maintenance.MaintenanceRequest;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.ViewHolder> {
    private Context context;
    private List<MaintenanceRequest> maintenanceList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    
    public MaintenanceAdapter(Context context, List<MaintenanceRequest> maintenanceList) {
        this.context = context;
        this.maintenanceList = maintenanceList;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_maintenance, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MaintenanceRequest maintenance = maintenanceList.get(position);
        
        holder.tvTitle.setText(maintenance.getTitle());
        holder.tvDescription.setText(maintenance.getDescription());
        
        // 设置状态文本和颜色
        holder.tvStatus.setText(getStatusText(maintenance.getStatus()));
        holder.tvStatus.setTextColor(getStatusColor(maintenance.getStatus()));
        
        // 设置报修时间
        if (maintenance.getReportTime() != null) {
            holder.tvTime.setText(dateFormat.format(maintenance.getReportTime()));
        }
        
        // 设置报修类型
        holder.tvType.setText(getTypeText(maintenance.getType()));
        
        // 设置点击事件
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MaintenanceDetailActivity.class);
            intent.putExtra("maintenanceId", maintenance.getId());
            context.startActivity(intent);
        });
    }
    
    @Override
    public int getItemCount() {
        return maintenanceList.size();
    }
    
    private String getStatusText(String status) {
        if (status == null) return "未知";
        
        switch (status) {
            case "pending": return "待处理";
            case "assigned": return "已分配";
            case "processing": return "处理中";
            case "completed": return "已完成";
            case "cancelled": return "已取消";
            case "rejected": return "已驳回";
            default: return "未知";
        }
    }
    
    private int getStatusColor(String status) {
        if (status == null) return context.getResources().getColor(R.color.gray);
        
        switch (status) {
            case "pending": return context.getResources().getColor(R.color.orange);
            case "assigned": return context.getResources().getColor(R.color.blue);
            case "processing": return context.getResources().getColor(R.color.blue);
            case "completed": return context.getResources().getColor(R.color.green);
            case "cancelled": return context.getResources().getColor(R.color.gray);
            case "rejected": return context.getResources().getColor(R.color.red);
            default: return context.getResources().getColor(R.color.gray);
        }
    }
    
    private String getTypeText(String type) {
        if (type == null) return "其他";
        
        switch (type) {
            case "water_electric": return "水电维修";
            case "decoration": return "装修维修";
            case "public_facility": return "公共设施";
            case "clean": return "保洁服务";
            case "security": return "安保服务";
            case "personal_residence": return "个人住所";
            default: return "其他";
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle, tvDescription, tvStatus, tvTime, tvType;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvType = itemView.findViewById(R.id.tv_type);
        }
    }
} 