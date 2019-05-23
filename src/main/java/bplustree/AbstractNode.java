package bplustree;

import javafx.util.Pair;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@NoArgsConstructor
@Slf4j
public abstract class AbstractNode {

    //是否根节点
    protected boolean isRoot;

    //父节点
    protected Node parent;


    public AbstractNode(boolean isRoot){
        this.isRoot = isRoot;
    }

    //查找
    public abstract Object find(Comparable key);

    //在b+树上执行数据更新或插入
    public abstract void insertOrUpdate(Comparable key,Object obj,BPlusTree tree);

    //删除
    public abstract void remove(Comparable key,BPlusTree tree);

    protected void noNullRequire(Object obj){
        if (obj == null){
            log.error("key不能为null");
            throw new RuntimeException("key不能为null");
        }
    }

    protected Comparable getRealKey(AbstractNode node){
        if (node instanceof LeafNode){
            List<Pair<Comparable, Object>> data = ((LeafNode) node).getData();
            return data.get(0).getKey();
        }else {
            List<Comparable> keys = ((Node) node).getKeys();
            return keys.get(0);
        }
    }

    protected boolean isNodeSuitable1(AbstractNode node, BPlusTree tree){
        if (node instanceof LeafNode){
            LeafNode leaf = (LeafNode) node;
            return leaf!=null && leaf.getData().size()>leaf.getLimit()/2
                    && leaf.getData().size()>2
                    && leaf.getParent() == parent;
        }else {
            Node keyNode = (Node) node;
            return keyNode!=null
                    && keyNode.getChildList().size()>tree.getSteps()/2
                    && keyNode.getChildList().size()>2;
        }
    }

    protected boolean isNodeSuitable2(AbstractNode node, BPlusTree tree){
        if (node instanceof LeafNode){
            LeafNode leaf = (LeafNode) node;
            return leaf!=null && leaf.getParent() == parent
                    && (leaf.getData().size()<=leaf.getLimit()/2
                    || leaf.getData().size()<=2);
        }else {
            Node keyNode = (Node) node;
            return keyNode!=null
                    && keyNode.getChildList().size()<=tree.getSteps()/2
                    && keyNode.getChildList().size()<=2;
        }
    }

}
