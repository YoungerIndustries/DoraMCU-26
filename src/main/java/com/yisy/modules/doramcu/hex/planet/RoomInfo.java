package com.yisy.modules.doramcu.hex.planet;

/**
 * Information about a room in a building interior.
 */
public class RoomInfo {

    private String roomId;
    private String roomType;
    private String name;
    private String description;
    private String category;
    private String size;

    public RoomInfo() {}

    public RoomInfo(String roomId, String roomType, String name, String description, String category, String size) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.name = name;
        this.description = description;
        this.category = category;
        this.size = size;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
}
