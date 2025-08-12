package person.kinman.cogame.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务端主类 - 启动服务并监听客户端连接
 */
public class ServerMain {
    private static final int PORT = 12345; // 服务端端口（需与客户端一致）
    private static final RoomManager roomManager = new RoomManager(); // 房间管理器
    private static final ExecutorService clientExecutor = Executors.newCachedThreadPool(); // 客户端处理线程池
    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("服务端已启动，监听端口: {}", PORT);

            // 循环监听客户端连接
            while (true) {
                Socket clientSocket = serverSocket.accept(); // 阻塞等待新连接
                logger.info("新客户端连接: {}", clientSocket.getInetAddress());

                // 为每个客户端创建处理线程
                ClientHandler clientHandler = new ClientHandler(clientSocket, roomManager);
                clientExecutor.submit(clientHandler);
            }
        } catch (IOException e) {
            logger.error("服务端启动失败: {}", e.getMessage());
        } finally {
            clientExecutor.shutdown();
        }
    }
}
