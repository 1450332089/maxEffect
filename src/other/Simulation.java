package other;

import entity.ConnectedRelation;
import entity.Node;

import java.math.BigDecimal;
import java.util.*;

public class Simulation {
    private Random random = new Random();

    //模拟传播过程，S为初始点集合
    public int simulate(List<Node> S,List<Node> nodeList, int times){
        double avgNum = 0;
        //这样计算平均数，防止溢出
        for (int i = 0; i < times; i++) {
            int num = simulate(S,nodeList);
            avgNum += (1.0 * num )/ times;
        }
        return (int) avgNum;
    }
    private int simulate(List<Node> S,List<Node> nodeList){
        int positiveNodeCount = 0;
        //先把初始节点激活
        for (Node node : S) {
            node.setActivated(true);
            positiveNodeCount++;
        }
        //存放当前时刻新激活节点的队列
        Queue<Node> newActivatedNodeQueue = new LinkedList<>(S);
        while (!newActivatedNodeQueue.isEmpty()){
            //取出一个新激活节点。并激活它的邻接节点
            Node node = newActivatedNodeQueue.poll();
            List<ConnectedRelation> connectedRelationList = node.getConnectedNodeList();
            //遍历该节点的邻接节点们
            for (int i = 0; i < connectedRelationList.size(); i++) {
                ConnectedRelation connectedRelation = connectedRelationList.get(i);
                BigDecimal probability = connectedRelation.getProbability();
                //尝试激活
                if(!tryActive(probability)){
                    continue;
                }
                Node activatedNode =  connectedRelation.getConnectedNode();
                //如果已被其他节点激活过，跳过
                if(activatedNode.isActivated()){
                    continue;
                }
                //激活成功，将其加入队列
                activatedNode.setActivated(true);
                newActivatedNodeQueue.offer(activatedNode);
                positiveNodeCount++;
            }
        }
        //模拟完这一次，还需要把所有激活的节点都恢复为未激活
        for (Node node : nodeList) {
            node.setActivated(false);
        }
        return positiveNodeCount;
    }
    private boolean tryActive(BigDecimal probability){
        BigDecimal bigDecimal = BigDecimal.valueOf(random.nextDouble());
        return bigDecimal.compareTo(probability.abs()) <= 0;
    }

}
