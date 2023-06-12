package other;

import entity.ConnectedRelation;
import entity.Node;
import javafx.util.Pair;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static other.Constants.NODE_NUM;
import static other.Constants.THREAD_NUM;

public class NodeUtils {
    /*
    所有节点列表
     */
    private static List<Node> nodeList = new ArrayList<>(NODE_NUM);

    /*
    节点id与node的map
     */
    private static Map<Integer,Node> nodeIdMap = new HashMap<>();
    //这个key太大了，equals相当费时，弃用
//    private static Map<List<Node>,Map<Integer,Node>> listMapMap = new HashMap<>();

    //改用ThreadLocal存线程与副本关系,该类用于实现线程之间副本数据的隔离
    public static ThreadLocal<Map<Integer,Node>> mapThreadLocal = new ThreadLocal<>();
    /*
    使用阻塞队列存放线程数个副本。使得每个线程都有副本可用
     */
    private static BlockingQueue<Pair<List<Node>, Map<Integer, Node>>> pairBlockingQueue;

    /*
    类初始化时执行
     */
    static {
        load();
        pairBlockingQueue = new ArrayBlockingQueue<>(THREAD_NUM);
        for (int i = 0; i < THREAD_NUM; i++) {
            //pair是用于存放两个值的容器，List是节点列表，Map是节点id和节点的字典，根据id查节点性能高
            //生成一个数据副本
            Pair<List<Node>, Map<Integer, Node>> pair = NodeUtils.generatePairCopy();
            try {
                //放入阻塞队列中
                pairBlockingQueue.put(pair);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
    获得节点个数
     */
    public static int getSize(){
        return nodeList.size();
    }
    /*
    通过id获得节点。这里需要去ThreadLocal中取到本线程对应的副本
     */
    public static Node getNodeById(int id){
        return mapThreadLocal.get().get(id);
    }
    /*
    在队列中取一个图数据,并将其放入ThreadLocal中
     */
    public static List<Node> getNodeListCopy(){
        Pair<List<Node>, Map<Integer, Node>> pair = null;
        try {
            pair = pairBlockingQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        mapThreadLocal.set(pair.getValue());
        return pair.getKey();
    }
    /*
    把使用完的副本重新放入队列，并从ThreadLocal中移除，清理内存
     */
    public static void returnNodeListCopy(List<Node> nodeList){
        try {
            Pair<List<Node>, Map<Integer, Node>> pair = new Pair<>(nodeList, mapThreadLocal.get());
            pairBlockingQueue.put(pair);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        mapThreadLocal.remove();
    }
    /*
    生成一个图数据的拷贝
     */
    private static Pair<List<Node>,Map<Integer,Node>> generatePairCopy() {
        List<Node> copiedList = new ArrayList<>(nodeList.size());
        Map<Integer,Node> copiedMap = new HashMap<>();
        for (Node node : nodeList) {
            Node copiedNode = new Node(node.getId());
            copiedList.add(copiedNode);
            copiedMap.put(copiedNode.getId(),copiedNode);
        }
        for (int i = 0; i < nodeList.size(); i++) {
            Node node = nodeList.get(i);
            Node copiedNode = copiedList.get(i);
            for (ConnectedRelation connectedRelation : node.getConnectedNodeList()) {
                Node connectedNode = connectedRelation.getConnectedNode();
                BigDecimal probability = connectedRelation.getProbability();
                copiedNode.addConnectedNode(copiedMap.get(connectedNode.getId()), probability);
            }
        }
        return new Pair<>(copiedList,copiedMap);
    }
    /*
    从txt文件中加载节点信息
    */
    private static void load() {
        //加载结点
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(Constants.FILE_ALLUSERS_PATH));
            while (scanner.hasNext()){
                int id = scanner.nextInt();
                Node node = new Node(id);
                nodeList.add(node);
                nodeIdMap.put(id,node);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(scanner != null) {
                scanner.close();
            }
            mapThreadLocal.set(nodeIdMap);
        }
        //加载入度出度信息
        try {
            scanner = new Scanner(new File(Constants.FILE_LINKS_PATH));
            while (scanner.hasNextLine()){
                String s = scanner.nextLine();
                String[] sArray = s.split(" ");
                int leftNodeId = Integer.parseInt(sArray[0]);
                int rightNodeId = Integer.parseInt(sArray[1]);
                BigDecimal probability = null;
                if("1".equals(sArray[2])){
                    probability = new BigDecimal(sArray[3]);
                }else if ("-1".equals(sArray[2])){
                    //取反
                    probability = new BigDecimal(sArray[3]).negate();
                }
                Node leftNode = getNodeById(leftNodeId);
                leftNode.addConnectedNode(getNodeById(rightNodeId),probability);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(scanner != null) {
                scanner.close();
            }
        }
    }

}
