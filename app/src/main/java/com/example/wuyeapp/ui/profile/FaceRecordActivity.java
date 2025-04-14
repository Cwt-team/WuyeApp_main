package com.example.wuyeapp.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.wuyeapp.network.client.RetrofitClient;
import com.example.wuyeapp.network.api.ApiService;
import com.example.wuyeapp.databinding.ActivityFaceRecordBinding;
import com.example.wuyeapp.model.user.FaceUploadResponse;
import com.example.wuyeapp.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaceRecordActivity extends AppCompatActivity {

    private static final String TAG = "FaceRecordActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int REQUEST_TAKE_PHOTO = 1002;
    private static final int REQUEST_PICK_IMAGE = 1003;
    
    private ActivityFaceRecordBinding binding;
    private ApiService apiService;
    private long ownerId;
    private Uri imageUri;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFaceRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 初始化API服务
        apiService = RetrofitClient.getInstance().getApiService();
        
        // 获取传递的业主ID
        ownerId = getIntent().getLongExtra("ownerId", 0);
        if (ownerId == 0) {
            Toast.makeText(this, "业主ID错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 设置返回按钮
        binding.btnBack.setOnClickListener(v -> finish());
        
        // 设置拍照按钮
        binding.btnTakePhoto.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                takePicture();
            } else {
                requestCameraPermission();
            }
        });
        
        // 设置从相册选择按钮
        binding.btnSelectFromGallery.setOnClickListener(v -> selectFromGallery());
        
        // 设置上传按钮
        binding.btnUpload.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadFaceImage();
            } else {
                Toast.makeText(this, "请先拍照或从相册选择照片", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION
        );
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else {
                Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        } else {
            Toast.makeText(this, "无法启动相机", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void selectFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO && data != null) {
                // 处理拍照结果
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        binding.faceImageView.setImageBitmap(imageBitmap);
                        binding.faceImageView.setVisibility(View.VISIBLE);
                        binding.btnUpload.setEnabled(true);
                        
                        // 保存临时文件用于上传
                        imageUri = saveBitmapToCache(imageBitmap);
                    }
                }
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                // 处理相册选择结果
                imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        binding.faceImageView.setImageBitmap(bitmap);
                        binding.faceImageView.setVisibility(View.VISIBLE);
                        binding.btnUpload.setEnabled(true);
                    } catch (IOException e) {
                        LogUtil.e(TAG + " 加载图片失败: " + e.getMessage());
                        Toast.makeText(this, "加载图片失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    
    private Uri saveBitmapToCache(Bitmap bitmap) {
        File outputDir = getCacheDir();
        File outputFile = null;
        
        try {
            outputFile = File.createTempFile("face_", ".jpg", outputDir);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] bitmapData = outputStream.toByteArray();
            
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            
            return Uri.fromFile(outputFile);
        } catch (IOException e) {
            LogUtil.e(TAG + " 保存图片失败: " + e.getMessage());
            Toast.makeText(this, "保存图片失败", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    
    private void uploadFaceImage() {
        if (imageUri == null) {
            Toast.makeText(this, "请先选择或拍摄人脸图像", Toast.LENGTH_SHORT).show();
            return;
        }
        
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnUpload.setEnabled(false);
        
        try {
            // 准备文件
            File file = new File(getPathFromUri(imageUri));
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            
            // 发送请求
            apiService.uploadFace(ownerId, body).enqueue(new Callback<FaceUploadResponse>() {
                @Override
                public void onResponse(Call<FaceUploadResponse> call, Response<FaceUploadResponse> response) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnUpload.setEnabled(true);
                    
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(FaceRecordActivity.this, "人脸图像上传成功", Toast.LENGTH_SHORT).show();
                        // 返回上一页
                        finish();
                    } else {
                        String errorMsg = "上传失败";
                        if (response.body() != null) {
                            errorMsg = response.body().getMessage();
                        }
                        Toast.makeText(FaceRecordActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<FaceUploadResponse> call, Throwable t) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnUpload.setEnabled(true);
                    LogUtil.e(TAG + " 网络请求失败: " + t.getMessage());
                    Toast.makeText(FaceRecordActivity.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnUpload.setEnabled(true);
            LogUtil.e(TAG + " 上传失败: " + e.getMessage());
            Toast.makeText(this, "上传失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private String getPathFromUri(Uri uri) {
        if ("file".equals(uri.getScheme())) {
            return uri.getPath();
        }
        
        // 如果是content类型的Uri，转换为文件路径
        String[] projection = {MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        
        return uri.toString();
    }
}
