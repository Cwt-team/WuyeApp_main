package com.example.wuyeapp.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wuyeapp.databinding.ActivityHouseBindingBinding;
import com.example.wuyeapp.model.base.BaseResponse;
import com.example.wuyeapp.model.community.Community;
import com.example.wuyeapp.model.community.CommunitiesResponse;
import com.example.wuyeapp.model.building.BuildingListResponse;
import com.example.wuyeapp.model.unit.UnitListResponse;
import com.example.wuyeapp.model.room.RoomListResponse;
import com.example.wuyeapp.network.client.RetrofitClient;
import com.example.wuyeapp.session.SessionManager;
import com.example.wuyeapp.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HouseBindingActivity extends AppCompatActivity {

    private static final String TAG = "HouseBindingActivity";
    private ActivityHouseBindingBinding binding;
    private SessionManager sessionManager;
    private List<Community> communities = new ArrayList<>();
    private List<BuildingListResponse.Building> buildings = new ArrayList<>();
    private List<UnitListResponse.Unit> units = new ArrayList<>();
    private List<RoomListResponse.Room> rooms = new ArrayList<>();
    private int selectedCommunityId = -1;
    private int selectedBuildingId = -1;
    private int selectedUnitId = -1;
    private int selectedRoomId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHouseBindingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        sessionManager = SessionManager.getInstance(this);
        
        // 设置返回按钮
        binding.ivBack.setOnClickListener(v -> finish());
        
        // 加载社区列表
        loadCommunities();
        
        // 设置社区下拉选择监听
        binding.spinnerCommunity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= communities.size()) {
                    selectedCommunityId = communities.get(position - 1).getId();
                    loadBuildings(selectedCommunityId);
                } else {
                    selectedCommunityId = -1;
                    clearBuildings();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCommunityId = -1;
                clearBuildings();
            }
        });
        // 楼栋下拉监听
        binding.spinnerBuilding.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= buildings.size()) {
                    selectedBuildingId = buildings.get(position - 1).getId();
                    loadUnits(selectedBuildingId);
                } else {
                    selectedBuildingId = -1;
                    clearUnits();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedBuildingId = -1;
                clearUnits();
            }
        });
        // 单元下拉监听
        binding.spinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= units.size()) {
                    selectedUnitId = units.get(position - 1).getId();
                    loadRooms(selectedUnitId);
                } else {
                    selectedUnitId = -1;
                    clearRooms();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUnitId = -1;
                clearRooms();
            }
        });
        // 房间下拉监听
        binding.spinnerRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= rooms.size()) {
                    selectedRoomId = rooms.get(position - 1).getId();
                } else {
                    selectedRoomId = -1;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRoomId = -1;
            }
        });
        
        // 设置提交按钮点击事件
        binding.btnSubmitApplication.setOnClickListener(v -> {
            if (validateInputs()) {
                submitApplication();
            }
        });
    }
    
    // 加载社区列表
    private void loadCommunities() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getInstance().getApiService()
                .getCommunities()
                .enqueue(new Callback<CommunitiesResponse>() {
                    @Override
                    public void onResponse(Call<CommunitiesResponse> call, Response<CommunitiesResponse> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            communities = response.body().getCommunities();
                            setupCommunitySpinner();
                        } else {
                            Toast.makeText(HouseBindingActivity.this, "获取社区列表失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CommunitiesResponse> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(HouseBindingActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    // 设置社区下拉选择器
    private void setupCommunitySpinner() {
        List<String> communityNames = new ArrayList<>();
        communityNames.add("请选择社区");
        
        for (Community community : communities) {
            communityNames.add(community.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, 
                android.R.layout.simple_spinner_item, 
                communityNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCommunity.setAdapter(adapter);
    }
    
    // 加载楼栋
    private void loadBuildings(int communityId) {
        clearBuildings();
        RetrofitClient.getInstance().getApiService().getBuildings(communityId)
            .enqueue(new Callback<BuildingListResponse>() {
                @Override
                public void onResponse(Call<BuildingListResponse> call, Response<BuildingListResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        buildings = response.body().getBuildings();
                        List<String> names = new ArrayList<>();
                        names.add("请选择楼栋");
                        for (BuildingListResponse.Building b : buildings) {
                            names.add(b.getName());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(HouseBindingActivity.this, android.R.layout.simple_spinner_item, names);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerBuilding.setAdapter(adapter);
                    } else {
                        clearBuildings();
                    }
                }
                @Override
                public void onFailure(Call<BuildingListResponse> call, Throwable t) {
                    clearBuildings();
                }
            });
    }
    private void clearBuildings() {
        buildings.clear();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"请选择楼栋"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerBuilding.setAdapter(adapter);
        clearUnits();
    }
    // 加载单元
    private void loadUnits(int buildingId) {
        clearUnits();
        RetrofitClient.getInstance().getApiService().getUnits(buildingId)
            .enqueue(new Callback<UnitListResponse>() {
                @Override
                public void onResponse(Call<UnitListResponse> call, Response<UnitListResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        units = response.body().getUnits();
                        List<String> names = new ArrayList<>();
                        names.add("请选择单元");
                        for (UnitListResponse.Unit u : units) {
                            names.add(u.getName());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(HouseBindingActivity.this, android.R.layout.simple_spinner_item, names);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerUnit.setAdapter(adapter);
                    } else {
                        clearUnits();
                    }
                }
                @Override
                public void onFailure(Call<UnitListResponse> call, Throwable t) {
                    clearUnits();
                }
            });
    }
    private void clearUnits() {
        units.clear();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"请选择单元"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerUnit.setAdapter(adapter);
        clearRooms();
    }
    // 加载房间
    private void loadRooms(int unitId) {
        clearRooms();
        RetrofitClient.getInstance().getApiService().getRooms(unitId)
            .enqueue(new Callback<RoomListResponse>() {
                @Override
                public void onResponse(Call<RoomListResponse> call, Response<RoomListResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        rooms = response.body().getRooms();
                        List<String> names = new ArrayList<>();
                        names.add("请选择房间");
                        for (RoomListResponse.Room r : rooms) {
                            names.add(r.getName());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(HouseBindingActivity.this, android.R.layout.simple_spinner_item, names);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerRoom.setAdapter(adapter);
                    } else {
                        clearRooms();
                    }
                }
                @Override
                public void onFailure(Call<RoomListResponse> call, Throwable t) {
                    clearRooms();
                }
            });
    }
    private void clearRooms() {
        rooms.clear();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"请选择房间"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRoom.setAdapter(adapter);
    }
    
    // 验证输入
    private boolean validateInputs() {
        boolean isValid = true;
        if (selectedCommunityId == -1) {
            Toast.makeText(this, "请选择社区", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (selectedBuildingId == -1) {
            Toast.makeText(this, "请选择楼栋", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (selectedUnitId == -1) {
            Toast.makeText(this, "请选择单元", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        if (selectedRoomId == -1) {
            Toast.makeText(this, "请选择房间", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        String idCard = binding.etIdCard.getText().toString().trim();
        if (TextUtils.isEmpty(idCard)) {
            binding.tilIdCard.setError("请输入身份证号");
            isValid = false;
        } else if (!idCard.matches("^\\d{17}[\\dX]$")) {
            binding.tilIdCard.setError("请输入正确的身份证号码");
            isValid = false;
        } else {
            binding.tilIdCard.setError(null);
        }
        return isValid;
    }
    
    // 提交申请
    private void submitApplication() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmitApplication.setEnabled(false);
        long ownerId = sessionManager.getOwnerInfo().getId();
        String idCard = binding.etIdCard.getText().toString().trim();
        String buildingName = "";
        String unitName = "";
        String houseNumber = "";
        if (selectedBuildingId != -1) {
            for (BuildingListResponse.Building b : buildings) {
                if (b.getId() == selectedBuildingId) {
                    buildingName = b.getBuildingNumber();
                    break;
                }
            }
        }
        if (selectedUnitId != -1) {
            for (UnitListResponse.Unit u : units) {
                if (u.getId() == selectedUnitId) {
                    unitName = u.getUnitNumber();
                    break;
                }
            }
        }
        if (selectedRoomId != -1) {
            for (RoomListResponse.Room r : rooms) {
                if (r.getId() == selectedRoomId) {
                    houseNumber = r.getRoomNumber();
                    break;
                }
            }
        }
        RetrofitClient.getInstance().getApiService()
                .submitHousingApplication(
                        ownerId,
                        selectedCommunityId,
                        buildingName,
                        unitName,
                        houseNumber,
                        idCard
                )
                .enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSubmitApplication.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            BaseResponse baseResponse = response.body();
                            if (baseResponse.isSuccess()) {
                                LogUtil.i(TAG + " 申请提交成功");
                                Toast.makeText(HouseBindingActivity.this, baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                if (baseResponse.getHouseExists() != null) {
                                    if (baseResponse.getHouseExists()) {
                                        Toast.makeText(HouseBindingActivity.this, "房屋信息已在系统中，申请将加快审核。", Toast.LENGTH_LONG).show();
                                        finish();
                                    } else {
                                        Toast.makeText(HouseBindingActivity.this, "未找到该房屋信息，请核对后再提交。", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    finish();
                                }
                            } else {
                                LogUtil.w(TAG + " 申请提交失败: " + baseResponse.getMessage());
                                Toast.makeText(HouseBindingActivity.this, baseResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            LogUtil.w(TAG + " 申请提交失败: 服务器响应错误");
                            Toast.makeText(HouseBindingActivity.this, "服务器响应错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSubmitApplication.setEnabled(true);
                        LogUtil.e(TAG + " 申请提交请求失败: " + t.getMessage());
                        Toast.makeText(HouseBindingActivity.this, "网络连接失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
} 