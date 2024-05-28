import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class SeatinfoClient extends JFrame {
    private static final String SERVER_ADDRESS = "127.0.0.1"; // サーバーのIPアドレスに変更
    private static final int SERVER_PORT = 32000;
    private static final int GRID_SIZE = 10;
    private static final int REFRESH_INTERVAL = 1000; // 1秒
    private JButton[][] seatButtons = new JButton[GRID_SIZE][GRID_SIZE]; // 席の情報
    private JLabel messageLabel; // メッセージを表示するためのラベル
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Timer refreshTimer;

    public SeatinfoClient() {
        setTitle("Searinfo Client");
        setSize(600, 600); // ウィンドウの高さを固定
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // フォントの設定
        Font seatFont = new Font("Arial", Font.BOLD, 16);
        Color lightGray = new Color(211, 211, 211);

        JPanel mainPanel = new JPanel(new BorderLayout()); 

        // メッセージ表示パネルを作成し、メッセージと凡例を表示するラベルを配置
        JPanel messagePanel = new JPanel();
        messageLabel = new JLabel("<html>席の情報を仲間に共有しましょう！<br>情報を入力する席を選んでください。<br><br>入力した情報を更新する，もしくは他の人が入力した最新の情報を閲覧するためには情報更新ボタンをクリックしてください。</html>");
        JLabel legendLabel = new JLabel("<html><font color='red'>■</font> 使用中　<font color='blue'>■</font> 空席　<font color='#D3D3D3'>■</font> 未選択</html>");
        messagePanel.add(messageLabel);
        messagePanel.add(legendLabel); // 凡例を追加
        mainPanel.add(messagePanel, BorderLayout.NORTH);

        //座席部分
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(GRID_SIZE, GRID_SIZE));

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int seatNumber = i * GRID_SIZE + j + 1;
                seatButtons[i][j] = new JButton(String.valueOf(seatNumber));
                seatButtons[i][j].setBackground(lightGray); // 薄いグレーを設定
                seatButtons[i][j].setFont(seatFont); // フォントを設定
                seatButtons[i][j].addActionListener(new SeatButtonListener(seatNumber));
                gridPanel.add(seatButtons[i][j]);
            }
        }
        mainPanel.add(gridPanel, BorderLayout.CENTER);


        add(mainPanel);

        //通信
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // 初回起動時に情報を更新
        refreshSeatInfo();

        // タイマーを設定して定期的に座席情報を更新
        refreshTimer = new Timer(REFRESH_INTERVAL, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshSeatInfo();
            }
        });
        refreshTimer.start();
    }

    //席情報の更新
    private void refreshSeatInfo() {
        try {
            out.println("2");
            in.readLine(); // 読み飛ばす
            String line;
            while (!(line = in.readLine()).equals("end")) {
                String[] parts = line.split(": ");
                String status = parts[0];
                int seatNumber = Integer.parseInt(parts[1]);
                int row = (seatNumber - 1) / GRID_SIZE;
                int col = (seatNumber - 1) % GRID_SIZE;
                switch (status) {
                    case "空席":
                        seatButtons[row][col].setBackground(Color.BLUE);
                        break;
                    case "使用中":
                        seatButtons[row][col].setBackground(Color.RED);
                        break;
                    default:
                        seatButtons[row][col].setBackground(new Color(211, 211, 211));
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //席のボタン押された時
    private class SeatButtonListener implements ActionListener {
        private int seatNumber;

        public SeatButtonListener(int seatNumber) {
            this.seatNumber = seatNumber;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String[] options = {"空席", "使用中"};
            int status = JOptionPane.showOptionDialog(null,
                    "座席 " + seatNumber + " の状態を選択してください:",
                    "座席状態",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (status == 0 || status == 1) {
                try {
                    out.println("1");
                    out.println(seatNumber);
                    out.println(status + 1);
                    in.readLine();
                    refreshSeatInfo();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private class RefreshButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            refreshSeatInfo();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SeatinfoClient().setVisible(true);
            }
        });
    }
}

