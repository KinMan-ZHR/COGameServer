package person.kinman.cogame.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

/**
 * 客户端处理器 - 每个客户端连接对应一个实例，处理消息收发
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final RoomManager roomManager;
    private final String clientId; // 客户端唯一标识（UUID生成）
    private BufferedReader in;
    private PrintWriter out;
    private Room room; // 客户端所在房间
    private boolean isReady = false; // 是否准备

    public ClientHandler(Socket socket, RoomManager roomManager) {
        this.clientSocket = socket;
        this.roomManager = roomManager;
        this.clientId = "PLAYER_" + UUID.randomUUID().toString().substring(0, 8); // 简化ID
    }

    @Override
    public void run() {
        try {
            // 初始化输入输出流
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // 发送连接成功消息
            sendMessage("已连接到服务器！你的ID: " + clientId);

            // 分配房间
            Room assignedRoom = roomManager.assignRoom(this);

            // 循环读取客户端消息
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("收到来自 " + clientId + " 的消息: " + message);

                // 处理准备指令（简化逻辑：发送 "ready" 表示准备）
                if (message.trim().equalsIgnoreCase("ready")) {
                    if (room != null) {
                        room.handlePlayerReady(this);
                    } else {
                        sendMessage("你不在任何房间中，无法准备");
                    }
                } else {
                    // 其他消息广播给房间内玩家
                    if (room != null) {
                        room.broadcast("玩家 " + clientId + " 说: " + message);
                    } else {
                        sendMessage("你不在任何房间中，无法发送消息");
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("客户端 " + clientId + " 连接异常: " + e.getMessage());
        } finally {
            // 客户端断开连接，清理资源
            closeConnection();
        }
    }

    /**
     * 发送消息给客户端
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    /**
     * 关闭客户端连接
     */
    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }

            // 从房间中移除玩家
            if (room != null) {
                room.removePlayer(this);
            }

            System.out.println("客户端 " + clientId + " 已断开连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getter & Setter
    public String getClientId() {
        return clientId;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public RoomManager getRoomManager() {
        return roomManager;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }
}