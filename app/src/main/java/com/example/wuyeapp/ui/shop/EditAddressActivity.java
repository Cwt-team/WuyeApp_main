package com.example.wuyeapp.ui.shop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wuyeapp.model.user.Address;
import com.example.wuyeapp.network.api.AddressApiService;
import com.example.wuyeapp.network.client.RetrofitClient;
import com.example.wuyeapp.model.base.BaseResponse;
import com.example.wuyeapp.session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class EditAddressActivity extends AppCompatActivity {
    private EditText etName, etPhone, etProvince, etCity, etDistrict, etDetail;
    private Button btnSave;
    private AddressApiService addressApiService;
    private long userId;
    private int addressId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        etName = new EditText(this); etName.setHint("收货人姓名"); root.addView(etName);
        etPhone = new EditText(this); etPhone.setHint("手机号"); root.addView(etPhone);
        etProvince = new EditText(this); etProvince.setHint("省份"); root.addView(etProvince);
        etCity = new EditText(this); etCity.setHint("城市"); root.addView(etCity);
        etDistrict = new EditText(this); etDistrict.setHint("区县"); root.addView(etDistrict);
        etDetail = new EditText(this); etDetail.setHint("详细地址"); root.addView(etDetail);
        btnSave = new Button(this); btnSave.setText("保存"); root.addView(btnSave);
        setContentView(root);
        setTitle("编辑收货地址");
        userId = SessionManager.getInstance(this).getOwnerInfo().getId();
        addressApiService = RetrofitClient.getInstance().getAddressApiService();
        addressId = getIntent().getIntExtra("address_id", -1);
        if (addressId != -1) {
            // 加载并回显已有地址信息
            addressApiService.getAddressList(userId).enqueue(new Callback<BaseResponse<List<Address>>>() {
                @Override
                public void onResponse(Call<BaseResponse<List<Address>>> call, Response<BaseResponse<List<Address>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        for (Address addr : response.body().getData()) {
                            if (addr.getId() == addressId) {
                                etName.setText(addr.getReceiverName());
                                etPhone.setText(addr.getPhone());
                                etProvince.setText(addr.getProvince());
                                etCity.setText(addr.getCity());
                                etDistrict.setText(addr.getDistrict());
                                etDetail.setText(addr.getDetailAddress());
                                break;
                            }
                        }
                    }
                }
                @Override
                public void onFailure(Call<BaseResponse<List<Address>>> call, Throwable t) {}
            });
        }
        btnSave.setOnClickListener(v -> saveAddress());
    }
    private void saveAddress() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String province = etProvince.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String district = etDistrict.getText().toString().trim();
        String detail = etDetail.getText().toString().trim();
        if (name.isEmpty()) { Toast.makeText(this, "请填写收货人姓名", Toast.LENGTH_SHORT).show(); return; }
        if (phone.isEmpty() || !phone.matches("1[3-9]\\d{9}")) { Toast.makeText(this, "请填写正确的手机号", Toast.LENGTH_SHORT).show(); return; }
        if (detail.isEmpty()) { Toast.makeText(this, "请填写详细地址", Toast.LENGTH_SHORT).show(); return; }
        Address addr = new Address();
        addr.setUserId(userId);
        addr.setReceiverName(name);
        addr.setPhone(phone);
        addr.setProvince(province);
        addr.setCity(city);
        addr.setDistrict(district);
        addr.setDetailAddress(detail);
        if (addressId == -1) {
            addressApiService.addAddress(addr).enqueue(new Callback<BaseResponse<Address>>() {
                @Override
                public void onResponse(Call<BaseResponse<Address>> call, Response<BaseResponse<Address>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(EditAddressActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(EditAddressActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<BaseResponse<Address>> call, Throwable t) {
                    Toast.makeText(EditAddressActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            addressApiService.updateAddress(addressId, addr).enqueue(new Callback<BaseResponse<Address>>() {
                @Override
                public void onResponse(Call<BaseResponse<Address>> call, Response<BaseResponse<Address>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(EditAddressActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(EditAddressActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<BaseResponse<Address>> call, Throwable t) {
                    Toast.makeText(EditAddressActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
} 