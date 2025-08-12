package person.kinman.cogame.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 房间管理器 - 负责创建房间、匹配玩家、管理房间状态
 */
public class RoomManager {
    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>(); // 房间ID -> 房间
    private final ReentrantLock lock = new ReentrantLock(); // 保证线程安全的锁
    private int roomIdGenerator = 1000; // 房间ID生成器

    /**
     * 为客户端分配房间（优先加入未满的房间，否则创建新房间）
     */
    public Room assignRoom(ClientHandler client) {
        lock.lock();
        try {
            // 尝试加入已有未满房间
            for (Room room : rooms.values()) {
                if (room.canJoin()) {
                    room.addPlayer(client);
                    System.out.println("客户端 " + client.getClientId() + " 加入房间 " + room.getRoomId());
                    return room;
                }
            }

            // 没有未满房间，创建新房间
            String roomId = "ROOM_" + (roomIdGenerator++);
            Room newRoom = new Room(roomId);
            newRoom.addPlayer(client);
            rooms.put(roomId, newRoom);
            System.out.println("客户端 " + client.getClientId() + " 创建并加入房间 " + roomId);
            return newRoom;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 移除房间（当房间所有玩家离开时）
     */
    public void removeRoom(String roomId) {
        rooms.remove(roomId);
        System.out.println("房间 " + roomId + " 已解散");
    }

    /**
     * 获取房间信息
     */
    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }
}