package person.kinman.cogame.server;

import java.util.ArrayList;
import java.util.List;

/**
 * 房间类 - 管理单个房间内的玩家和游戏状态
 */
public class Room {
    private final String roomId; // 房间ID
    private final List<ClientHandler> players = new ArrayList<>(2); // 房间内玩家（最多2人）
    private boolean isGameStarted = false; // 游戏是否已开始

    public Room(String roomId) {
        this.roomId = roomId;
    }

    /**
     * 添加玩家到房间
     */
    public void addPlayer(ClientHandler client) {
        if (canJoin()) {
            players.add(client);
            client.setRoom(this); // 将房间信息关联到客户端
            broadcast("玩家 " + client.getClientId() + " 已加入房间");

            // 检查是否已满2人，通知准备
            if (players.size() == 2) {
                broadcast("房间已满，请准备！（发送 'ready' 表示准备完成）");
            }
        }
    }

    /**
     * 移除玩家（玩家断开连接时）
     */
    public void removePlayer(ClientHandler client) {
        players.remove(client);
        broadcast("玩家 " + client.getClientId() + " 已离开房间");

        // 房间为空时，通知管理器删除房间
        if (players.isEmpty()) {
            client.getRoomManager().removeRoom(roomId);
        } else if (isGameStarted) {
            // 游戏中有人离开，通知另一方胜利
            players.get(0).sendMessage("对手已离开，你获胜！");
            isGameStarted = false;
        }
    }

    /**
     * 广播消息给房间内所有玩家
     */
    public void broadcast(String message) {
        String fullMsg = "[房间 " + roomId + "] " + message;
        for (ClientHandler player : players) {
            player.sendMessage(fullMsg);
        }
        System.out.println(fullMsg); // 服务端日志
    }

    /**
     * 处理玩家准备状态（当两人都准备后，游戏开始）
     */
    public void handlePlayerReady(ClientHandler client) {
        if (isGameStarted) {
            client.sendMessage("游戏已开始，无需准备");
            return;
        }

        client.setReady(true);
        broadcast("玩家 " + client.getClientId() + " 已准备");

        // 检查是否两人都已准备
        if (players.stream().allMatch(ClientHandler::isReady)) {
            startGame();
        }
    }

    /**
     * 开始游戏
     */
    private void startGame() {
        isGameStarted = true;
        broadcast("===== 游戏开始！ =====");
        // 此处可添加游戏初始化逻辑（如发送初始游戏状态）
    }

    /**
     * 检查房间是否可加入（未满2人且游戏未开始）
     */
    public boolean canJoin() {
        return players.size() < 2 && !isGameStarted;
    }

    // Getter
    public String getRoomId() {
        return roomId;
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }
}