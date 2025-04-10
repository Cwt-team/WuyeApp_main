package com.example.wuyeapp.model;

public class OwnerDetailResponse {
    private boolean success;
    private String message;
    private OwnerDetailData data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OwnerDetailData getData() {
        return data;
    }

    public void setData(OwnerDetailData data) {
        this.data = data;
    }

    public static class OwnerDetailData {
        private long id;
        private String name;
        private String phoneNumber;
        private String account;
        private String gender;
        private String idCard;
        private String email;
        private String city;
        private String address;
        private String ownerType;
        private String faceImage;
        private int faceStatus;
        private CommunityInfo communityInfo;
        private HouseInfo houseInfo;
        private PermissionInfo permissions;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getOwnerType() {
            return ownerType;
        }

        public void setOwnerType(String ownerType) {
            this.ownerType = ownerType;
        }

        public String getFaceImage() {
            return faceImage;
        }

        public void setFaceImage(String faceImage) {
            this.faceImage = faceImage;
        }

        public int getFaceStatus() {
            return faceStatus;
        }

        public void setFaceStatus(int faceStatus) {
            this.faceStatus = faceStatus;
        }

        public CommunityInfo getCommunityInfo() {
            return communityInfo;
        }

        public void setCommunityInfo(CommunityInfo communityInfo) {
            this.communityInfo = communityInfo;
        }

        public HouseInfo getHouseInfo() {
            return houseInfo;
        }

        public void setHouseInfo(HouseInfo houseInfo) {
            this.houseInfo = houseInfo;
        }

        public PermissionInfo getPermissions() {
            return permissions;
        }

        public void setPermissions(PermissionInfo permissions) {
            this.permissions = permissions;
        }
        
        public static class CommunityInfo {
            private int id;
            private String name;
            private String city;
            
            public int getId() {
                return id;
            }
            
            public void setId(int id) {
                this.id = id;
            }
            
            public String getName() {
                return name;
            }
            
            public void setName(String name) {
                this.name = name;
            }
            
            public String getCity() {
                return city;
            }
            
            public void setCity(String city) {
                this.city = city;
            }
        }
        
        public static class HouseInfo {
            private int id;
            private String fullName;
            
            public int getId() {
                return id;
            }
            
            public void setId(int id) {
                this.id = id;
            }
            
            public String getFullName() {
                return fullName;
            }
            
            public void setFullName(String fullName) {
                this.fullName = fullName;
            }
        }
        
        public static class PermissionInfo {
            private long id;
            private String permissionStatus;
            private String validPeriod;
            private boolean callingEnabled;
            private boolean pstnEnabled;
            
            public long getId() {
                return id;
            }
            
            public void setId(long id) {
                this.id = id;
            }
            
            public String getPermissionStatus() {
                return permissionStatus;
            }
            
            public void setPermissionStatus(String permissionStatus) {
                this.permissionStatus = permissionStatus;
            }
            
            public String getValidPeriod() {
                return validPeriod;
            }
            
            public void setValidPeriod(String validPeriod) {
                this.validPeriod = validPeriod;
            }
            
            public boolean isCallingEnabled() {
                return callingEnabled;
            }
            
            public void setCallingEnabled(boolean callingEnabled) {
                this.callingEnabled = callingEnabled;
            }
            
            public boolean isPstnEnabled() {
                return pstnEnabled;
            }
            
            public void setPstnEnabled(boolean pstnEnabled) {
                this.pstnEnabled = pstnEnabled;
            }
        }
    }
}
