import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class SeatinfoServer {
    public static final int PORT = 32000; // ポート番号設定
    private static final int SEAT_NUM = 100; // 席番号の上限指定
    private static final int TIMER_DELAY = 30 * 1000; // タイマーの遅延時間（30秒）

    private static int[] seats = new int[SEAT_NUM]; // 座席の状況を管理する配列
    private static Timer[] seatTimers = new Timer[SEAT_NUM]; // 座席のタイマーを管理する配列

    public static void main(String[] args) throws IOException {
        //席の初期値
        for (int i = 0; i < SEAT_NUM; i++) {
            seats[i] = 0; // 0が未選択の状態
        }
        //通信
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

                //席の情報をクライアント側とやり取り
                while (true) {
                    String choice = in.readLine();

                    if (choice == null) {
                        break;
                    }

                    switch (choice) {
                        case "1":
                            String seatInfo = in.readLine();
                            int seatNumber = Integer.parseInt(seatInfo);
                            if (seatNumber <= SEAT_NUM && seatNumber > 0) {
                                String status = in.readLine();
                                int statusInt = Integer.parseInt(status);
                                if (statusInt == 1 || statusInt == 2) {
                                    seats[seatNumber - 1] = statusInt;
                                    if (statusInt == 2) {
                                        startTimerForSeat(seatNumber - 1);
                                    } else if (seatTimers[seatNumber - 1] != null) {
                                        seatTimers[seatNumber - 1].cancel();
                                        seatTimers[seatNumber - 1] = null;
                                    }
                                    out.println("ok");
                                    
                                } else {
                                    out.println("入力エラー");
                                }
                            } else {
                                out.println("入力エラー");
                            }
                            break;
                        case "2":
                            out.println("席状況:");
                            for (int i = 0; i < SEAT_NUM; i++) {
                                if (seats[i] == 1) {
                                    out.println("空席: " + (i + 1));
                                } else if (seats[i] == 2) {
                                    out.println("使用中: " + (i + 1));
                                } else {
                                    out.println("未選択: " + (i + 1));
                                }
                            }
                            out.println("end");
                            break;
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

        //時間管理
        private void startTimerForSeat(int seatIndex) {
            if (seatTimers[seatIndex] != null) {
                seatTimers[seatIndex].cancel();
            }
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    seats[seatIndex] = 0; // 未選択に戻す
                }
            }, TIMER_DELAY);
            seatTimers[seatIndex] = timer;
        }
    }
}


