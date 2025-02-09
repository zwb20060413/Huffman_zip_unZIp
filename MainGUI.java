import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MainGUI {
    private static final JFrame frame = new JFrame("哈夫曼压缩工具");
    private static final JPanel panel = new JPanel();
    private static final JTextArea outputArea = new JTextArea(10, 30);
    private static final JTextField initPathField = new JTextField(20);
    private static final JTextField zippedPathField = new JTextField(20);
    private static final JTextField unzippedPathField = new JTextField(20);
    private static final JButton compressButton = new JButton("压缩");
    private static final JButton decompressButton = new JButton("解压缩");
    private static final JButton exitButton = new JButton("退出");
    private static final JCheckBox encryptCheckBox = new JCheckBox("加密压缩");

    public static void main(String[] args) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);

        panel.setLayout(new BorderLayout());
        panel.add(createInputPanel(), BorderLayout.NORTH);
        panel.add(createOutputPanel(), BorderLayout.CENTER);
        panel.add(createControlPanel(), BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    private static JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 设置控件间隔

        // Row 1: 初始化路径
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("初始化路径:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(initPathField, gbc);

        // Row 2: 压缩文件路径
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("压缩文件路径:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(zippedPathField, gbc);

        // Row 3: 解压后路径
        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("解压后路径:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(unzippedPathField, gbc);

        // Row 4: 加密选项
        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(encryptCheckBox, gbc);

        return inputPanel;
    }

    private static JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        compressButton.addActionListener(new CompressButtonListener());
        decompressButton.addActionListener(new DecompressButtonListener());
        exitButton.addActionListener(e -> System.exit(0));

        controlPanel.add(compressButton);
        controlPanel.add(decompressButton);
        controlPanel.add(exitButton);

        return controlPanel;
    }

    private static JScrollPane createOutputPanel() {
        outputArea.setEditable(false);
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BorderLayout());
        outputPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        return new JScrollPane(outputPanel);
    }

    private static class CompressButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 将这些变量标记为 final，确保 lambda 表达式可以安全引用它们
            final String initPath = initPathField.getText().trim();
            final String zippedPath = zippedPathField.getText().trim();
            final boolean ifEncrypted = encryptCheckBox.isSelected();

            if (initPath.isEmpty() || zippedPath.isEmpty()) {
                outputArea.setText("请填写所有路径！");
                return;
            }

            final String password;
            if (ifEncrypted) {
                password = JOptionPane.showInputDialog(frame, "请输入压缩密码");
            } else {
                password = null;
            }

            // 使用线程来执行压缩任务，以防止界面卡顿
            new Thread(() -> {
                try {
                    outputArea.setText("正在压缩文件...\n");

                    // 调用压缩操作
                    HuffmanFolderCompressor compressor = new HuffmanFolderCompressor(initPath, zippedPath, ifEncrypted, password);
                    compressor.compressFolder();

                    outputArea.append("压缩完成！压缩文件路径：" + zippedPath + "\n");
                } catch (IOException ex) {
                    outputArea.append("压缩过程中发生错误: " + ex.getMessage() + "\n");
                }
            }).start();
        }
    }

    private static class DecompressButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 将这些变量标记为 final，确保 lambda 表达式可以安全引用它们
            final String zippedPath = zippedPathField.getText().trim();
            final String unzippedPath = unzippedPathField.getText().trim();

            if (zippedPath.isEmpty() || unzippedPath.isEmpty()) {
                outputArea.setText("请填写所有路径！");
                return;
            }

            // 使用线程来执行解压任务，以防止界面卡顿
            new Thread(() -> {
                try {
                    outputArea.setText("正在解压文件...\n");

                    // 调用解压操作
                    HuffmanFolderDecompressor decompressor = new HuffmanFolderDecompressor(zippedPath, unzippedPath);
                    decompressor.decompressFolder();

                    outputArea.append("解压完成！解压文件路径：" + unzippedPath + "\n");
                } catch (IOException | ClassNotFoundException ex) {
                    outputArea.append("解压过程中发生错误: " + ex.getMessage() + "\n");
                }
            }).start();
        }
    }
}
