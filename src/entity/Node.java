package entity;


import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/*
节点信息
 */
public class Node implements Serializable {
    private int id;
    /*
    相邻节点id及激活概率 用ArrayList存储
     */
    private List<ConnectedRelation> connectedRelationList = new ArrayList<>();
    /*
    是否被激活，默认为否
     */
    private boolean Activated = false;


    public Node(int id) {
        this.id = id;
    }

    /*
    添加邻接节点
     */
    public void addConnectedNode(Node node, BigDecimal probability){
        connectedRelationList.add(new ConnectedRelation(node,probability));
    }

    public int getId() {
        return id;
    }

    public boolean isActivated() {
        return Activated;
    }

    public void setActivated(boolean activated) {
        Activated = activated;
    }

    public List<ConnectedRelation> getConnectedNodeList() {
        return connectedRelationList;
    }

    public void setConnectedRelationList(List<ConnectedRelation> connectedRelationList) {
        this.connectedRelationList = connectedRelationList;
    }




    @Override
    public String toString() {
        return "entity.Node{" +
                "id=" + id +
                '}';
    }
}
