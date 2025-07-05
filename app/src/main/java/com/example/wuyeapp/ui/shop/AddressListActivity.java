package com.example.wuyeapp.ui.shop;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wuyeapp.model.user.Address;
import com.example.wuyeapp.network.api.AddressApiService;
import com.example.wuyeapp.network.client.RetrofitClient;
import com.example.wuyeapp.model.base.BaseResponse;
import com.example.wuyeapp.session.SessionManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressListActivity extends AppCompatActivity {
    private LinearLayout addressListLayout;
    private AddressApiService addressApiService;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);
        Button addBtn = new Button(this);
        addBtn.setText("+ 新增收货地址");
        addBtn.setTextColor(0xFFFFFFFF);
        addBtn.setBackgroundColor(0xFF009688);
        addBtn.setPadding(32, 16, 32, 16);
        addBtn.setTextSize(18);
        addBtn.setOnClickListener(v -> startActivityForResult(new Intent(this, EditAddressActivity.class), 1001));
        root.addView(addBtn);
        ScrollView scrollView = new ScrollView(this);
        addressListLayout = new LinearLayout(this);
        addressListLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(addressListLayout);
        root.addView(scrollView, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
        setTitle("收货地址管理");
        userId = SessionManager.getInstance(this).getOwnerInfo().getId();
        addressApiService = RetrofitClient.getInstance().getAddressApiService();
        loadAddressList();
    }

    private void loadAddressList() {
        addressListLayout.removeAllViews();
        addressApiService.getAddressList(userId).enqueue(new Callback<BaseResponse<List<Address>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Address>>> call, Response<BaseResponse<List<Address>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Address> list = response.body().getData();
                    if (list.isEmpty()) {
                        TextView empty = new TextView(AddressListActivity.this);
                        empty.setText("暂无收货地址");
                        empty.setTextSize(16);
                        empty.setTextColor(0xFF888888);
                        empty.setPadding(0, 64, 0, 0);
                        addressListLayout.addView(empty);
                    }
                    for (Address addr : list) {
                        LinearLayout item = new LinearLayout(AddressListActivity.this);
                        item.setOrientation(LinearLayout.VERTICAL);
                        item.setPadding(24, 24, 24, 24);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 0, 32);
                        item.setLayoutParams(lp);
                        item.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
                        TextView tv = new TextView(AddressListActivity.this);
                        String info = addr.getReceiverName() + " " + addr.getPhone() + "\n" + addr.getProvince() + addr.getCity() + addr.getDistrict() + addr.getDetailAddress();
                        if (addr.isDefault()) {
                            info += " [默认]";
                            tv.setTextColor(0xFF009688);
                        } else {
                            tv.setTextColor(0xFF222222);
                        }
                        tv.setTextSize(17);
                        tv.setText(info);
                        item.addView(tv);
                        LinearLayout btnRow = new LinearLayout(AddressListActivity.this);
                        btnRow.setOrientation(LinearLayout.HORIZONTAL);
                        btnRow.setPadding(0, 16, 0, 0);
                        Button editBtn = new Button(AddressListActivity.this);
                        editBtn.setText("编辑");
                        editBtn.setTextColor(0xFF009688);
                        editBtn.setOnClickListener(v -> {
                            Intent intent = new Intent(AddressListActivity.this, EditAddressActivity.class);
                            intent.putExtra("address_id", addr.getId());
                            startActivityForResult(intent, 1002);
                        });
                        btnRow.addView(editBtn);
                        Button delBtn = new Button(AddressListActivity.this);
                        delBtn.setText("删除");
                        delBtn.setTextColor(0xFFE91E63);
                        delBtn.setOnClickListener(v -> {
                            addressApiService.deleteAddress(addr.getId()).enqueue(new Callback<BaseResponse<Void>>() {
                                @Override
                                public void onResponse(Call<BaseResponse<Void>> call, Response<BaseResponse<Void>> response) {
                                    loadAddressList();
                                }
                                @Override
                                public void onFailure(Call<BaseResponse<Void>> call, Throwable t) {
                                    loadAddressList();
                                }
                            });
                        });
                        btnRow.addView(delBtn);
                        if (!addr.isDefault()) {
                            Button setDefaultBtn = new Button(AddressListActivity.this);
                            setDefaultBtn.setText("设为默认");
                            setDefaultBtn.setTextColor(0xFF009688);
                            setDefaultBtn.setOnClickListener(v -> {
                                addressApiService.setDefaultAddress(addr.getId()).enqueue(new Callback<BaseResponse<Address>>() {
                                    @Override
                                    public void onResponse(Call<BaseResponse<Address>> call, Response<BaseResponse<Address>> response) {
                                        loadAddressList();
                                    }
                                    @Override
                                    public void onFailure(Call<BaseResponse<Address>> call, Throwable t) {
                                        loadAddressList();
                                    }
                                });
                            });
                            btnRow.addView(setDefaultBtn);
                        }
                        item.addView(btnRow);
                        addressListLayout.addView(item);
                    }
                } else {
                    TextView empty = new TextView(AddressListActivity.this);
                    empty.setText("暂无收货地址");
                    empty.setTextSize(16);
                    empty.setTextColor(0xFF888888);
                    empty.setPadding(0, 64, 0, 0);
                    addressListLayout.addView(empty);
                }
            }
            @Override
            public void onFailure(Call<BaseResponse<List<Address>>> call, Throwable t) {
                addressListLayout.removeAllViews();
                TextView error = new TextView(AddressListActivity.this);
                error.setText("加载失败: " + t.getMessage());
                error.setTextColor(0xFFE91E63);
                error.setTextSize(16);
                addressListLayout.addView(error);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadAddressList();
        }
    }
} 