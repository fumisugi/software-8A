import java.io.*;
import java.net.*;

public class ClassroomServer {
    public static final int PORT = 8080; //ポート番号設定
    private static final int Seat_num = 100; //席番号の上限指定

    private static int[] seats = new int[Seat_num]; // 座席の状況を管理する配列


    public static void main(String[] args) 
    throws IOException {
        for (int i = 0; i < Seat_num; i++) {
           seats[i] = 0; //0が入力なしの状態
        }
        ServerSocket s = new ServerSocket(PORT);
        try {
            while (true) {
                Socket socket = s.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } finally {
            s.close();
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
                    out.println("1. 席の情報を入力する 2. 席の状況を閲覧する 3.終了する 選択してください：");
                    String choice = in.readLine();

                    switch (choice) {
                        case "1":
                            out.println("情報を入力する席番号(1~100)を入力してください。");
                            String seatInfo = in.readLine();
                            int info = Integer.parseInt(seatInfo);
                            if(info <= Seat_num && info > 0){
                                out.println("空席なら1を,使用中なら2を選択してください。");
                                String b = in.readLine();
                                if(Integer.parseInt(b) == 1 || Integer.parseInt(b)==2){
                                    seats[info] = Integer.parseInt(b);
                                }else{
                                    out.println("入力エラーです。1か2の数字を入力してください。");
                                    break;
                                }

                            }else{
                                out.println("入力エラーです。1~100までの数字を入力してください。");
                                break;
                            }
                            out.println("空席情報を入力しました。");
                            break;
                        case "2":
                            out.println("現在の空席状況:");
                            for (int i = 0; i < Seat_num; i++) {
                                    if (seats[i] == 1) {
                                        out.println("空席: " + i);
                                    }
                                    else if(seats[i] == 2){
                                        out.println("使用中："+ i);
                                    }
                            }
                            out.println("end");
                            break;
                        case "3":
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
    }

}
