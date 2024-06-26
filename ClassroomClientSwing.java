import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class ClassroomClientSwing extends JFrame {
    private static final String SERVER_ADDRESS = "192.168.11.9"; // サーバーのIPアドレスに変更
    private static final int SERVER_PORT = 8080;
    private static final int GRID_SIZE = 10;
    private JButton[][] seatButtons = new JButton[GRID_SIZE][GRID_SIZE];
    private JLabel messageLabel; // メッセージを表示するためのラベル
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClassroomClientSwing() {
        setTitle("Classroom Client");
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

        JButton refreshButton = new JButton("更新情報");
        refreshButton.addActionListener(new RefreshButtonListener());
        mainPanel.add(refreshButton, BorderLayout.SOUTH);

        add(mainPanel);
        
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
    }

    private void refreshSeatInfo() {
        try {
            out.println("2");
            in.readLine(); // "現在の空席状況:" を読み飛ばす
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
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SeatButtonListener implements ActionListener {
        private int seatNumber;

        public SeatButtonListener(int seatNumber) {
            this.seatNumber = seatNumber;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String[] options = {"空席", "使用中"};
            int status = JOptionPane.showOptionDialog(
                ClassroomClientSwing.this,
                "席番号 " + seatNumber + " の状態を選択してください:",
                "席の状態変更",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (status != -1) { // ユーザーが選択を行った場合
                out.println("1");
                out.println(seatNumber);
                out.println(status + 1); // 空席は1、使用中は2
                try {
                    in.readLine(); // 空席情報を入力しました。 を読み飛ばす
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
            // メッセージの更新
            messageLabel.setText("最新の情報を取得しています...");
            refreshSeatInfo();
            messageLabel.setText("<html>席の情報を仲間に共有しましょう！<br>情報を入力する席を選んでください。<br><br>入力した情報を更新する，もしくは他の人が入力した最新の情報を閲覧するためには情報更新ボタンをクリックしてください。</html>");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClassroomClientSwing client = new ClassroomClientSwing();
            client.setVisible(true);
        });
    }
}
