import java.io.*;
import java.util.*;

public class HuffmanFolderDecompressor {
    private String inputFile;  // 压缩文件名
    private String outputFolder; // 解压目标文件夹

    public HuffmanFolderDecompressor(String inputPath, String outputPath) {
        this.inputFile = inputPath;
        this.outputFolder = outputPath;
    }

    public void decompressFolder() throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(inputFile);
             BufferedInputStream bis = new BufferedInputStream(fis, 1024*1024);
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            // 读取压缩数据和编码表
            boolean buffer1 = (boolean) ois.readObject();
            String buffer2 = (String) ois.readObject();


            // 读取压缩的数据和编码集
            Map<String, Boolean> ifDirectory = (Map<String, Boolean>) ois.readObject();
            Map<String, byte[]> compressedData = (Map<String, byte[]>) ois.readObject();
            Map<String, Map<Byte, String>> codingSets = (Map<String, Map<Byte, String>>) ois.readObject();

            for (String relativePath : compressedData.keySet()) {
                File outputFile = new File(outputFolder+relativePath);
                if (compressedData.get(relativePath).length == 0){
                    if(ifDirectory.get(relativePath)) {
                        outputFile.mkdirs();
                    }else{
                        outputFile.createNewFile();
                        File parentDir = new File(outputFile.getParent());
                        if (!parentDir.exists()) {
                            parentDir.mkdirs();
                        }
                    }
                } else {
                    // 解压文件内容
                    Map<Byte, String> codingSet = codingSets.get(relativePath);
                    Map<String, Byte> decodingSet = invertCodingSet(codingSet);

                    byte[] compressedBytes = compressedData.get(relativePath);
                    decompressFile(compressedBytes, decodingSet, outputFile);
                }
            }

            System.out.println("文件夹解压成功，输出文件夹: " + outputFolder);
        }catch (FileNotFoundException e){
            System.out.println("文件不存在");
        }
    }


    private Map<String, Byte> invertCodingSet(Map<Byte, String> codingSet) {
        Map<String, Byte> decodingSet = new HashMap<>();
        for (Map.Entry<Byte, String> entry : codingSet.entrySet()) {
            decodingSet.put(entry.getValue(), entry.getKey());
        }
        return decodingSet;
    }

    private void decompressFile(byte[] compressedBytes, Map<String, Byte> decodingSet, File outputFile) throws IOException {
        // 创建文件夹
        File parentDir = new File(outputFile.getParent());
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        // 创建输出流
        try (FileOutputStream fos = new FileOutputStream(outputFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos, 512*1024)) {
            StringBuilder currentBits = new StringBuilder();
            long batchSize = 100000000L;  // 批量处理，防止内存溢出
            long index = 0;  // 当前字节索引
            long totalLength = compressedBytes.length;
            // 批量读取并解压
            while (index < totalLength) {
                long end = Math.min(index + batchSize, totalLength);
                byte[] batch = Arrays.copyOfRange(compressedBytes, (int) index, (int) end);
                index = end;

                // 逐字节解压

                for (byte b : batch) {
                    for (int i = 7; i >= 0; i--) {
                        char bit = ((b >> i) & 1) == 1 ? '1' : '0';
                        currentBits.append(bit);

                        if (decodingSet.containsKey(currentBits.toString())) {
                            bos.write(decodingSet.get(currentBits.toString()));
                            currentBits.setLength(0);  // 重置当前位
                        }
                    }
                }
            }
            bos.flush();
        }
    }
}

