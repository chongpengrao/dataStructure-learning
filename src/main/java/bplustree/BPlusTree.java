package bplustree;

import lombok.Data;

import java.util.Random;

/**
 * m阶b+树特性：
 * 1.数据储存在树叶上
 * 2.非叶节点储存m-1个关键字以指示搜索的方向；关键字i代表第i+1棵树中最小的关键字
 * 3.树的根是一片树叶 或者 其子节点数在2和m之间
 * 4.除根节点外所有非叶节点的子节点数在m/2和m之间
 * 5.所有叶节点都位于同一层并且有L/2到L个数据项（通过链表串联起来，方便按区间查找）
 */
@Data
public class BPlusTree {

    //根节点
    private AbstractNode root;

    //叶节点的链表头部
    private AbstractNode headLeaf;

    //b+数的阶数
    private int steps;

    public BPlusTree(int steps,int limit){
        if (steps<=2){
            throw new RuntimeException("b+数的阶数必须大于2");
        }
        this.steps = steps;
        root = new LeafNode(limit,true);
        headLeaf = root;
    }

    public static void main(String[] args) {
        BPlusTree tree = new BPlusTree(4, 4);
        Random random = new Random();
        long current = System.currentTimeMillis();
        for (int j = 0; j < 1000; j++) {
            for (int i = 0; i < 10; i++) {
                int randomNumber = random.nextInt(1000);
                tree.insertOrUpdate(randomNumber, randomNumber);
            }
        }

        long duration = System.currentTimeMillis() - current;
        System.err.println("********消耗时间: " + duration+" *********");

        tree.insertOrUpdate(11,111);
        System.err.println("INSERT*******插入key为11的位置的数据为111******");
        Object result1 = tree.find(11);
        System.err.println("FIND*******查找索引为11的数据的值为："+result1+" ********");
        System.err.println("---------------------------------");

        tree.insertOrUpdate(11,666);
        System.err.println("UPDATE*******更新key为11的位置的数据为666******");
        Object result2 = tree.find(11);
        System.err.println("FIND*******查找索引为11的数据的值为："+result2+" ********");
        System.err.println("---------------------------------");

        tree.remove(11);
        System.err.println("DELETE*******删除key为11的位置的数据******");
        Object result3 = tree.find(11);
        System.err.println("FIND*******查找索引为11的数据的值为："+result3+" ********");
        System.err.println("---------------------------------");

    }

    public Object find(Comparable key) {
        return root.find(key);
    }

    public void remove(Comparable key) {
        root.remove(key,this);
    }

    public void insertOrUpdate(Comparable key, Object obj) {
        root.insertOrUpdate(key,obj,this);
    }
}
