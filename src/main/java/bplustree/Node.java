package bplustree;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 非叶节点
 */

@Data
@Slf4j
@NoArgsConstructor
public class Node extends AbstractNode {

    //子节点
    private List<AbstractNode> childList;

    //节点保存的关键字
    private List<Comparable> keys;

    public Node(boolean isRoot){
        super(isRoot);
        childList = new ArrayList<>();
        keys = new ArrayList<>();
    }

    @Override
    public Object find(Comparable key) {
        noNullRequire(key);
        //如果小于最左关键字则往最小的子节点去找
        if (key.compareTo(keys.get(0)) < 0){
            return childList.get(0).find(key);
            //如果大于最右关键字则往最右边的子节点去查找
        }else if (key.compareTo(keys.get(keys.size()-1)) >= 0){
            return childList.get(childList.size()-1).find(key);
            //否则在中间找（i处关键字 < key< i+1关键字）
        }else {
            //前面的判断条件可以确定keys中一定存在键值大于key
            Comparable comparable = keys.stream().filter(e -> key.compareTo(e) < 0).findFirst().get();
            int index = keys.indexOf(comparable);
            return childList.get(index).find(key);
        }
    }

    //todo 索引的插入问题待解决 :: 只有子节点分裂了父节点才会去更新索引或是分裂
    @Override
    public void insertOrUpdate(Comparable key, Object obj ,BPlusTree tree) {
        if (key.compareTo(keys.get(0)) < 0){
            childList.get(0).insertOrUpdate(key, obj, tree);
        }else if (key.compareTo(keys.get(keys.size()-1)) >= 0){
            childList.get(childList.size()-1).insertOrUpdate(key, obj, tree);
        }else {
            //否则找到key要放的位置进行插入/更新
            for (int i=0;i<keys.size();i++){
                if (key.compareTo(keys.get(i)) >= 0 && key.compareTo(keys.get(i+1)) < 0){
                    childList.get(i+1).insertOrUpdate(key, obj, tree);
                    break;
                }
            }
        }
    }

    @Override
    public void remove(Comparable key,BPlusTree tree) {
        if (key.compareTo(keys.get(0)) < 0){
            childList.get(0).remove(key, tree);
        }else if (key.compareTo(keys.get(keys.size()-1)) >= 0){
            childList.get(childList.size()-1).remove(key,  tree);
        }else {
            //否则找到key要放的位置进行插入/更新
            for (int i=0;i<keys.size();i++){
                if (key.compareTo(keys.get(i)) >= 0 && key.compareTo(keys.get(i+1)) < 0){
                    childList.get(i+1).remove(key, tree);
                    break;
                }
            }
        }
    }

    /**
     * 更新索引：更新后每个关键字对应一个子节点
     * 每个节点中子节点的个数不能超过 m，也不能小于 m/2；
     * 根节点的子节点个数可以不超过 m/2，这是一个例外
     * 非叶节点存储m-1个关键字以指示搜索的方向
     * 关键字的个数为m-1;子节点的个数为不超过m;
     * @param tree
     */

    protected void updateIndex(BPlusTree tree) {

        //只有当子节点分裂时才需要判断是否添加索引:索引的个数=子节点的个数-1
        //且当子节点个数超过m时,此时节点分裂,索引无需更新
        if (childList.size()<=tree.getSteps()){

            keys.clear();

            for (int i=1;i<childList.size();i++){
                Comparable realKey = getRealKey(childList.get(i));
                keys.add(realKey);
            }

//            if (childList.size()-keys.size() != 1){
//                for (int i=keys.size();i<childList.size()-1;i++){
//                    keys.add(getRealKey(childList.get(i+1)));
//                }
//            }
//
//            for (int i=1;i<childList.size();i++){
//                Comparable key = getRealKey(childList.get(i));
//                if (key.compareTo(keys.get(i-1))<=0){
//                    //then doNothing
//                }else {
//                    keys.set(i-1,key);
//                }
//            }
        }
    }

    /**
     * 3.树的根是一片树叶 或者 其子节点数在2和m之间
     * 4.除根节点外所有非叶节点的子节点数在m/2和m之间
     * @param tree
     */
    protected void updateRemove(BPlusTree tree){
        //子节点删除操作后更新父节点的索引
        updateIndex(tree);
        if (childList.size()<tree.getSteps()/2 || childList.size()<2){
            if (isRoot){
                if (childList.size()<2){
                    //根节点只有一个子节点时,删除根节点,子节点变为新的根节点
                    AbstractNode newRoot = childList.get(0);
                    tree.setRoot(newRoot);
                    isRoot = false;
                    keys = null;
                    childList = null;
                }
            }else {
                //非根节点时 && 自己的子节点再删除的话无法保证b+树的平衡时从同一父节点的前后节点节或者合并
                int index = parent.getChildList().indexOf(this);

                //获取同一父节点下的兄弟前后节点
                Node previous = null;
                Node next = null;
                if (index > 0){
                    previous = (Node)parent.getChildList().get(index-1);
                }
                if (index < parent.getChildList().size()-1){
                    next = (Node)parent.getChildList().get(index+1);
                }

                // 如果前节点子节点数大于M / 2并且大于2，则从其处借补
                if (isNodeSuitable1(previous,tree)){
                    int borrowIndex = previous.getChildList().size() - 1;
                    AbstractNode borrow = previous.getChildList().get(borrowIndex);
                    previous.getChildList().remove(borrow);
                    borrow.setParent(this);
                    childList.add(0,borrow);
                    //更新索引
                    previous.updateIndex(tree);
                    updateIndex(tree);

                    parent.updateRemove(tree);
                    // 如果后节点子节点数大于M / 2并且大于2，则从其处借补
                }else if (isNodeSuitable1(next,tree)){
                    AbstractNode borrow = next.getChildList().get(0);
                    next.getChildList().remove(0);
                    borrow.setParent(this);
                    childList.add(borrow);
                    //更新索引
                    next.updateIndex(tree);
                    updateIndex(tree);

                    parent.updateRemove(tree);
                }else {
                    //合并节点
                    if (isNodeSuitable2(previous,tree)){
                        //父节点childList删除previous节点
                        parent.getChildList().remove(index-1);
                        //previous节点的关键字放到当前节点
                        previous.getKeys().forEach(e->keys.add(0,e));
                        //更新索引
                        updateIndex(tree);
                        parent.updateRemove(tree);
                    }else if (isNodeSuitable2(next,tree)){
                        //父节点childList删除previous节点
                        parent.getChildList().remove(index+1);
                        //previous节点的关键字放到当前节点
                        next.getKeys().forEach(e->keys.add(e));
                        //更新索引
                        updateIndex(tree);
                        parent.updateRemove(tree);
                    }
                }

            }
        }

    }

    //插入数据后上级节点的更新
    protected void updateInsert(BPlusTree tree) {
        updateIndex(tree);
        //子节点超过限制则当前节点进行分裂
        if (childList.size() > tree.getSteps()){
            Node left = new Node(false);
            Node right = new Node(false);
            //复制原节点数据到新分裂出来的节点
            int size = tree.getSteps() + 1;
            int leftSize = size/2 + size%2;
            int rightSize = size/2;
            //1）更新子节点
            for (int i = 0; i < leftSize; i++){
                left.getChildList().add(childList.get(i));
                childList.get(i).setParent(left);
            }
            for (int i = 0; i < rightSize; i++){
                right.getChildList().add(childList.get(leftSize+i));
                childList.get(leftSize+i).setParent(right);
            }

            //2)更新索引
            int kSize = tree.getSteps()-1;
            int leftKeySize = kSize/2 + kSize%2;
            int rightKeySize = kSize/2;
            for (int i=0;i<leftKeySize;i++){
                left.getKeys().add(keys.get(i));
            }
            for (int i=0;i<rightKeySize;i++){
                right.getKeys().add(keys.get(leftKeySize+i));
            }

            //重新设置父节点关联关系
            if (parent != null){
                int index = parent.getChildList().indexOf(this);
                parent.getChildList().remove(index);
                parent.getChildList().add(index,left);
                parent.getChildList().add(index+1,right);
                left.setParent(parent);
                right.setParent(parent);
                setKeys(null);
                setChildList(null);
                //父节点增加了子节点进行更新
                parent.updateInsert(tree);
                setParent(null);
            }else {
                //root节点
                isRoot = false;
                Node root = new Node(true);
                tree.setRoot(root);
                left.setParent(root);
                right.setParent(root);
                root.getChildList().add(left);
                root.getChildList().add(right);
                setKeys(null);
                setChildList(null);
                //更新父节点
                root.updateInsert(tree);
            }

        }
    }

    /**
     * 树的根是一片树叶 或者 其子节点数在2和m之间
     * 除根节点外所有非叶节点的子节点数在m/2和m之间
     * @param tree
     */
    private void ruleValidate(BPlusTree tree) {
        if (isRoot && childList.size()>=2
                || childList.size()>=tree.getSteps()/2
                && childList.size()<=tree.getSteps()
                && childList.size()>2){

        }else {

        }
    }
}