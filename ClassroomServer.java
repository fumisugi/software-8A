import java.io.*;
import java.net.*;

public class ClassroomServer {
    public static final int PORT = 8080; // ポート番号設定
    private static final int SEAT_NUM = 100; // 席番号の上限指定

    private static int[] seats = new int[SEAT_NUM]; // 座席の状況を管理する配列

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < SEAT_NUM; i++) {
            seats[i] = 0; // 0が入力なしの状態
        }
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } finally {
            serverSocket.close();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true)) {

                while (true) {
                    String choice = in.readLine();

                    switch (choice) {
                        case "1":
                            String seatInfo = in.readLine();
                            int info = Integer.parseInt(seatInfo);
                            if (info <= SEAT_NUM && info > 0) {
                                String status = in.readLine();
                                int statusInt = Integer.parseInt(status);
                                if (statusInt == 1 || statusInt == 2) {
                                    seats[info - 1] = statusInt;
                                    out.println("空席情報を入力しました。");
                                } else {
                                    out.println("入力エラーです。1か2の数字を入力してください。");
                                }
                            } else {
                                out.println("入力エラーです。1~100までの数字を入力してください。");
                            }
                            break;
                        case "2":
                            out.println("現在の空席状況:");
                            for (int i = 0; i < SEAT_NUM; i++) {
                                if (seats[i] == 1) {
                                    out.println("空席: " + (i + 1));
                                } else if (seats[i] == 2) {
                                    out.println("使用中: " + (i + 1));
                                }
                            }
                            out.println("end");
                            break;
                        case "3":
                            return;
                        default:
                            out.println("無効な選択です。");
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
