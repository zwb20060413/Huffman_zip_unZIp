import java.io.*;
import java.util.Scanner;
public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("欢迎使用哈夫曼压缩工具");
        while (true) {
            System.out.println("如果你想压缩，请输入zip initPath zippedPath; 如果你想解压缩，请输入upZip zippedPath unzippedPath");
            System.out.println("请输入操作：");

            String choice = scanner.nextLine();
            String[] input = choice.trim().split(" ");

            //先进行压缩操作
            if (input[0].equals("zip")) {
                if (input.length != 3) {
                    System.out.println("输入格式错误，请重新操作");
                } else {
                    System.out.println("是否进行加密压缩，若是加密则输入Yes,并在后面输入你的密码；若不加密则输入No");
                    String ifEncryptedString = scanner.nextLine();

                    String password = null;
                    boolean ifEncrypted = false;

                    if (ifEncryptedString.equals("Yes")) {
                        System.out.println("请输入你要设置的密码：");
                        password = scanner.nextLine();
                        ifEncrypted = true;
                    }
                    String initName = input[1];
                    String zippedName = input[2];
                    HuffmanFolderCompressor compressor = new HuffmanFolderCompressor(initName, zippedName, ifEncrypted, password);
                    try {
                        compressor.compressFolder();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (input[0].equals("unZip")) {
                String zippedName = input[1];
                FileInputStream fis = new FileInputStream(zippedName);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(bis);

                boolean ifEncrypted = (boolean) ois.readObject();
                boolean ifContinued = true;

                if (ifEncrypted == true) {
                    String password = (String) ois.readObject();
                    System.out.println("请输入密码来继续压缩过程");
                    String inputPassword = scanner.nextLine();
                    if (!inputPassword.equals(password)) {
                        System.out.println("密码错误，请重新进行操作：");
                        ifContinued = false;
                    }
                }
                if (ifContinued) {
                    String unzippedName = input[2];
                    File fileL = new File(unzippedName);
                    if (fileL.exists()) {
                        System.out.println("文件已存在，是否覆盖文件路径，是输入Yes，否输入No并在提示后输入新文件路径");
                        String ifCover = scanner.nextLine();
                        if (ifCover.equals("No")) {
                            System.out.println("请输入新的文件路径");
                            String newName = scanner.nextLine();
                            unzippedName = newName;
                        }
                    }
                    HuffmanFolderDecompressor decompressor = new HuffmanFolderDecompressor(zippedName, unzippedName);
                    try {
                        decompressor.decompressFolder();
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}


