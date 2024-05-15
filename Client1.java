import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client1 {
    private static final String SERVER_ADDRESS = "localhost"; // サーバーのアドレス
    private static final int SERVER_PORT = 12345; // サーバーのポート

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected to server. Type 'quit' to exit.");

            // サーバーからのメッセージを読み取り、表示するスレッドを開始
            Thread serverListenerThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = serverReader.readLine()) != null) {
                        System.out.println("Server: " + message);
                    }
                } catch (IOException e) {
                    System.err.println("Error reading from server: " + e.getMessage());
                }
            });
            serverListenerThread.start();

            // ユーザーからの入力を読み取り、サーバーに送信する
            String userInput;
            while ((userInput = userInputReader.readLine()) != null) {
                if (userInput.equalsIgnoreCase("quit")) {
                    break;
                }
                writer.println(userInput);
            }
        } catch (IOException e) {
            System.err.println("Error in client: " + e.getMessage());
        }
    }
}
