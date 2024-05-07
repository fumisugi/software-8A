import java.io.*;
import java.net.*;

public class ClassroomClient {
    private static final int Seat_num = 100; //席番号の上限指定
    public static void main(String[] args) throws IOException {
        InetAddress addr = InetAddress.getByName("localhost");
        Socket socket = new Socket(addr, ClassroomServer.PORT);
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String choice;

            do {
                // サーバーからのメニューを表示
                String menu = in.readLine();
                System.out.println(menu);

                // ユーザーの選択を送信 
                choice = userInput.readLine();
                out.println(choice);

                switch (choice) {
                    case "1":
                        // 空席の入力を行う
                        String a = in.readLine();
                        System.out.println(a);
                        String seatInfo = userInput.readLine();
                        out.println(seatInfo);
                        int info = Integer.parseInt(seatInfo);
                        if(info <= Seat_num && info > 0){
                            System.out.println(in.readLine());
                            String d = userInput.readLine();
                            out.println(d);
                            if(Integer.parseInt(d) != 1 && Integer.parseInt(d) != 2){
                                System.out.println(in.readLine());
                            }
                        }
                        String con = in.readLine();
                        System.out.println(con);
                        break;
                    case "2":
                        // 空席の状況を受信して表示
                        String seatStatus = in.readLine();
                        System.out.println(seatStatus);
                        while(true){
                        String num = in.readLine();
                        if(num.equals("end")){
                            break;
                        }
                        System.out.println(num);
                        }
                        break;
                    case "3":
                        break;
                    default:
                        String e = in.readLine();
                        System.out.println(e);
                        break;
                }
            } while (!choice.equals("3"));
        } finally {
            System.out.println("closing...");
            socket.close();
        }
    }
}
