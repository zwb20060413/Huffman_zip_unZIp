import java.io.*;
import java.util.*;


public class CodingSet {

    private File file;
    private Map<Byte, Integer> NodeSet = new HashMap<>();
    private Map<Byte, String> CodingSet = new HashMap<>();

    public CodingSet(File file){
        this.file = file;
        // 并行线程池
    }

    public String getFileName(){
        return this.file.getName();
    }

    public void NodeSet() throws IOException {
        InputStream isf = new FileInputStream(this.file);
        BufferedInputStream bif = new BufferedInputStream(isf);
        int data = bif.read();
        while(data != -1){
            if(NodeSet.containsKey((byte)data)){
                NodeSet.put((byte)data, NodeSet.get((byte)data) + 1);
            }else{
                NodeSet.putIfAbsent((byte) data, 1);
            }

            data = bif.read();
        }
    }

    //下面就是构造哈夫曼编码的确定操作了

    //先构造一个私有辅助类，用于数据的封装与辅助操作
    private class Node implements Comparable<Node>{
        private byte data;
        private int frequency;
        private Node left;
        private Node right;
        private String code;

        public Node(){
            this.left = null;
            this.right = null;
            this.code = "";
        };

        public byte getData(){
            return this.data;
        }

        public void setData(byte data){
            this.data = data;
        }

        public int getFrequency(){
            return this.frequency;
        }

        public void setFrequency(int frequency){
            this.frequency = frequency;
        }

        public Node getLeft(){
            return this.left;
        }

        public void setLeft(Node left){
            this.left = left;
        }

        public Node getRight(){
            return this.right;
        }

        public void setRight(Node right){
            this.right = right;
        }

        public String getCode(){
            return this.code;
        }

        public void setCode(String code){
            this.code = code;
        }

        @Override
        public String toString(){
            return "data " + this.data + " frequency " + this.frequency;
        }

        @Override
        public int compareTo(Node o){
            if(this.frequency == o.frequency){
                return this.data - o.data;
            }else{
                return this.frequency - o.frequency;
            }
        }
    }

    //构造哈夫曼树的主要操作如下；
    public void getTheCode() {
        PriorityQueue<Node> pQueue = new PriorityQueue<>();

        for (byte key : NodeSet.keySet()) {
            Node node = new Node();
            node.setData(key);
            node.setFrequency(NodeSet.get(key));
            pQueue.add(node);
        }

        while (pQueue.size() > 1) {
            if (pQueue.isEmpty()) {
                break;
            }
            Node node = new Node();
            Node leftNode = pQueue.remove();
            Node rightNode = pQueue.remove();
            node.setData(leftNode.getData());
            node.setFrequency(leftNode.frequency + rightNode.frequency);
            node.setLeft(leftNode);
            node.setRight(rightNode);
            pQueue.add(node);
        }
        if (!pQueue.isEmpty()) {
            Node finalTree = pQueue.remove();
            Queue<Node> queue = new LinkedList<>();

            if (finalTree != null) {
                queue.add(finalTree);
            }

            while (!queue.isEmpty()) {
                Node node = queue.poll();

                if (node.getLeft() != null) {
                    node.getLeft().setCode(node.getCode() + "0");
                    queue.add(node.getLeft());
                }

                if (node.getRight() != null) {
                    node.getRight().setCode(node.getCode() + "1");
                    queue.add(node.getRight());
                }

                if (node.getRight() == null && node.getLeft() == null) {
                    this.CodingSet.putIfAbsent(node.getData(), node.getCode());
                }
            }
        }else{
            this.CodingSet = null;
        }
    }
    //得到哈夫曼编码集
    public Map<Byte, String> getCodingSet(){
        return this.CodingSet;
    }
}
