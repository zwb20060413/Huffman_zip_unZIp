import java.io.*;
import java.util.*;

public class HuffmanFolderCompressor {
    private File sourceFolder;
    private String outputFile;
    private boolean ifEncrypted;
    private String password = null;

    int currentNum = 0;
    int filledNum = 0;
    int bufferIndex = 0;
    byte[] buffer = new byte[10240*1024];

    public HuffmanFolderCompressor(String folderPath, String outputPath, boolean ifEncrypted, String password) {
        this.sourceFolder = new File(folderPath);
        this.outputFile = outputPath;
        this.ifEncrypted = ifEncrypted;
        this.password = password;
    }

    public void compressFolder() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            if (!sourceFolder.exists()) {
                throw new FileNotFoundException("源文件夹不存在: " + sourceFolder.getAbsolutePath());
            }

            Map<String, byte[]> compressedData = new HashMap<>(); // 保存压缩数据
            Map<String, Map<Byte, String>> codingSets = new HashMap<>(); // 保存每个文件的哈夫曼编码表
            Map<String, Boolean> ifDirectory = new HashMap<>();

            // 遍历文件夹
            traverseAndCompress(sourceFolder, "", compressedData, codingSets, ifDirectory);

            // 保存压缩数据和编码表
            oos.writeObject(this.ifEncrypted);
            oos.writeObject(this.password);
            oos.writeObject(ifDirectory);
            oos.writeObject(compressedData);
            oos.writeObject(codingSets);

            System.out.println("文件夹压缩成功，输出文件: " + outputFile);
        }catch (FileNotFoundException e){
            System.out.println("文件不存在，请检查文件路径");
        }
    }

    private void traverseAndCompress(File file, String relativePath,
                                     Map<String, byte[]> compressedData,
                                     Map<String, Map<Byte, String>> codingSets, Map<String, Boolean> ifDirectory) throws IOException {
        if (file.isDirectory()) {
            // 如果是文件夹，递归遍历子文件
            ifDirectory.put(relativePath, true);
            File[] children = file.listFiles();
            if (children.length != 0) {
                for (File child : children) {
                    traverseAndCompress(child, relativePath + "/" + child.getName(), compressedData, codingSets, ifDirectory);
                }
            } else {
                compressedData.put(relativePath, new byte[0]);
                 // 对空文件记录填充位数
            }
        } else {
            // 如果是文件，压缩内容
            ifDirectory.put(relativePath, false);
            currentNum = 0;
            filledNum = 0;
            bufferIndex = 0;
            buffer = new byte[10240*1024];
            CodingSet codingSet = new CodingSet(file);
            codingSet.NodeSet();
            codingSet.getTheCode();

            codingSets.put(relativePath, codingSet.getCodingSet());

            // 获取文件压缩后的字节流
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos, 102400 * 1024);
            InputStream is = new FileInputStream(file);
            BufferedInputStream os = new BufferedInputStream(is, 102400 * 1024);

            int data;
            while ((data = os.read()) != -1) {
                String encodedBits = codingSet.getCodingSet().get((byte) data);
                writeBits(bos, encodedBits);
            }

            // 记录填充的位数

            // 如果有填充的零，将其补齐并存储
            if (filledNum > 0) {
                currentNum = currentNum << (8 - filledNum);
                buffer[bufferIndex] = (byte) currentNum;
                bufferIndex++;
            }

            if (bufferIndex > 0) {
                bos.write(buffer, 0, bufferIndex);
            }

            bos.flush();
            compressedData.put(relativePath, baos.toByteArray());

            is.close();
            bos.close();
        }
    }


    private void writeBits(BufferedOutputStream bos, String matchCode) throws IOException {
      // 预分配一个较大的缓冲区来存储位数据
        for (int i = 0; i < matchCode.length(); i++) {
            int bit = matchCode.charAt(i) - '0';  // 获取当前位（0 或 1）//4000
            currentNum = (currentNum << 1) | bit;  // 将当前位加入当前字节
            filledNum++;  // 增加已填充的位数
            //System.out.println(bufferIndex);
            // 如果一个字节已经满（8位），就将其写入缓冲区
            if (filledNum == 8) {
                buffer[bufferIndex] = (byte) currentNum;  // 将当前字节存入缓冲区
                currentNum = 0;  // 重置当前字节
                filledNum = 0;   // 重置已填充位数
                bufferIndex++;   // 移动到下一个字节位置

                // 如果缓冲区满了，立即写入磁盘并重置缓冲区
                if (bufferIndex == 10240*1024) {
                    bos.write(buffer);  // 将缓冲区内容写入输出流
                    bufferIndex = 0;    // 重置缓冲区索引
                }
            }
        }
    }
}
