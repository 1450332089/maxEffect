package entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class ConnectedRelation implements Serializable {
    Node connectedNode;
    BigDecimal probability;

    public ConnectedRelation(Node connectedNode, BigDecimal probability) {
        this.connectedNode = connectedNode;
        this.probability = probability;
    }

    public Node getConnectedNode() {
        return connectedNode;
    }

    public void setConnectedNode(Node connectedNode) {
        this.connectedNode = connectedNode;
    }

    public BigDecimal getProbability() {
        return probability;
    }

    public void setProbability(BigDecimal probability) {
        this.probability = probability;
    }
}
