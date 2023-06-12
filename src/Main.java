import entity.Node;
import other.Constants;
import other.NodeUtils;
import other.Simulation;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static other.Constants.*;

public class Main {
    static int maxCount;
    static Node bestNode;
    static ExecutorService pool;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        long l = System.currentTimeMillis();
        bestNode = null;
        //使用线程池进行模拟，旨在加快运行速度
        //但每个线程所使用的图数据必须为副本。这就是空间换时间
        pool = Executors.newFixedThreadPool(THREAD_NUM);
        //最终最优初始节点List
        LinkedList<Integer> finalS = new LinkedList<>();
        //总共迭代k次，每次获得最优的一个节点
        for (int i = 0; i < K; i++) {
            Node oneBestNode = getOneBestNode(finalS);
            finalS.add(oneBestNode.getId());
            //打印结果
            System.out.println("第"+(i+1)+"次模拟完成");
            System.out.println("最佳初始节点列表为"+ Arrays.toString(finalS.toArray()));
            System.out.println("最大正向影响力为"+maxCount);
        }
        long l1 = System.currentTimeMillis();
        System.out.println("运行结束，总耗时"+TimeUnit.MILLISECONDS.toMinutes(l1-l) + "分钟");
    }

    private static Node getOneBestNode(LinkedList<Integer> finalS) throws InterruptedException {
        //计数功能，使主线程等待所有线程运行完毕
        CountDownLatch countDownLatch = new CountDownLatch(NodeUtils.getSize());
        for (int i = 0; i < NodeUtils.getSize(); i++) {
            int i1 = i;
            //将任务交给线程池执行
            pool.submit(() -> {
                //获得图数据副本
                List<Node> nodeListCopy = NodeUtils.getNodeListCopy();
                //生成初试节点，用S保存
                Node node = nodeListCopy.get(i1);
                //如果当前节点不在初始节点中，则进行模拟
                if(!finalS.contains(node.getId())){
                    //获得副本列表中初始节点对象的List
                    List<Node> S = finalS.stream().map((nodeId) -> {
                        return NodeUtils.getNodeById(nodeId);
                    }).collect(Collectors.toList());
                    S.add(node);
                    //多次模拟
                    int count = new Simulation().simulate(S, nodeListCopy, ITERATE_NUM_TEST);
                    // 获得模拟的最大值
                    // 加锁，防止并发安全问题
                    synchronized (Main.class) {
                        if (count > maxCount) {
                            bestNode = node;
                            maxCount = count;
                        }
                    }
                }
                countDownLatch.countDown();
                //返还图数据副本
                NodeUtils.returnNodeListCopy(nodeListCopy);
            });
        }
        //等待所有线程模拟完成
        countDownLatch.await();
        return bestNode;
    }

//    public static void main(String[] args) throws ExecutionException, InterruptedException {
////        long l1 = System.currentTimeMillis();
////        NodeLoader.load();
////        List<Node> nodeList = NodeLoader.nodeList;
////        long l2 = System.currentTimeMillis();
////        System.out.println(l1-l2);
////        List<Node> nodeListCopy = NodeLoader.getNodeListCopy();
//
//        List<Node> nodeListCopy1 = NodeUtils.getNodeListCopy();
//        Node node = NodeUtils.getNodeById(1810);
//        LinkedList<Node> S = new LinkedList<>();
//        S.add(node);
//        int simulate = new other.Simulation().simulate(S, nodeListCopy1, 1000);
//        System.out.println(simulate);
//    }



}
