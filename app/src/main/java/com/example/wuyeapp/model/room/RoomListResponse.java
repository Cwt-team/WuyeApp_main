package com.example.wuyeapp.model.room;
import com.example.wuyeapp.model.base.BaseResponse;
import java.util.List;

public class RoomListResponse extends BaseResponse {
    private List<Room> rooms;
    public List<Room> getRooms() { return rooms; }
    public void setRooms(List<Room> rooms) { this.rooms = rooms; }
    public static class Room {
        private int id;
        private String roomNumber;
        private String name;
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
} 