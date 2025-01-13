package com.example.wuyeapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wuyeapp.databinding.ActivityScanQrBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import android.graphics.Color;
import java.util.UUID;

public class ScanQrActivity extends AppCompatActivity {
    
    private ActivityScanQrBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 返回按钮
        binding.btnBack.setOnClickListener(v -> finish());
        
        // 生成并显示二维码
        generateQRCode();
        
        // 刷新按钮
        binding.btnRefresh.setOnClickListener(v -> {
            generateQRCode();
            Toast.makeText(this, "二维码已刷新", Toast.LENGTH_SHORT).show();
        });
        
        // 分享按钮
        binding.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, 
                "雀氏科技-1区-1栋-1单元\n" +
                "有效期到2025-01-07 16:23");
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "分享二维码"));
        });
    }
    
    private void generateQRCode() {
        try {
            // 生成随机内容作为二维码数据
            String qrContent = UUID.randomUUID().toString();
            
            // 创建二维码
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                qrContent,
                BarcodeFormat.QR_CODE,
                512,
                512
            );
            
            // 转换为Bitmap
            Bitmap qrBitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
            for (int x = 0; x < 512; x++) {
                for (int y = 0; y < 512; y++) {
                    qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            // 显示二维码
            binding.qrCode.setImageBitmap(qrBitmap);
            
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "生成二维码失败", Toast.LENGTH_SHORT).show();
        }
    }
} 