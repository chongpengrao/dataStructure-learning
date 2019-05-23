package bplustree;

import javafx.util.Pair;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@Slf4j
public class LeafNode extends AbstractNode {

    //前叶节点
    private LeafNode previous;

    //后叶节点
    private LeafNode next;

    //每个叶节点存放的数据的个数限制l（处于l/2到l之间）
    protected int limit;

    //节点存放的数据
    private List<Pair<Comparable,Object>> data;

    public LeafNode(int limit,boolean isRoot){
        super(isRoot);
        this.limit = limit;
        data = new ArrayList<>();
    }


    @Override
    public Object find(Comparable key) {
        noNullRequire(key);
        Optional<Pair<Comparable, Object>> optional = data.stream()
                .filter(pair -> pair.getKey().compareTo(key) == 0)
                .findFirst();
        return optional.map(Pair::getValue).orElse(null);
    }

    @Override
    public void insertOrUpdate(Comparable key, Object obj, BPlusTree tree) {
        noNullRequire(key);
        //key存在则在该节点进行更新 || 该节点存放的数据没有超过限制时直接插入
        if (keyExists(key) || data.size()<limit){
            //叶节点的插入/更新
            insertOrUpdate(key, obj);
            if (parent!=null){
                parent.updateInsert(tree);
            }
            //否则节点数据超过限制需要分裂
        }else {
            LeafNode left = new LeafNode(limit,false);
            LeafNode right = new LeafNode(limit,false);
            //设置叶节点之间的关联关系
            //叶节点不是链表头部
            if (previous != null){
                previous.setNext(left);
                left.setPrevious(previous);
                //否则left应当设置为链表头部节点
            }else {
                tree.setHeadLeaf(left);
            }
            //叶节点不是链表尾部
            if (next != null){
                next.setPrevious(right);
                right.setNext(next);
            }
            left.setNext(right);
            right.setPrevious(left);

            //对象回收
            previous = null;
            next = null;

            //插入操作后data的size超过limit
            insertOrUpdate(key, obj);
            //复制原节点数据到新分裂出来的叶节点
            int size = limit + 1;
            int leftSize = size/2 + size%2;
            int rightSize = size/2;
            for (int i=0;i<leftSize;i++){
                left.getData().add(data.get(i));
            }
            for (int i=0;i<rightSize;i++){
                right.getData().add(data.get(leftSize+i));
            }

            //调整父子节点的关联
            if (parent != null){
                int index = parent.getChildList().indexOf(this);
                parent.getChildList().remove(this);
                left.setParent(parent);
                right.setParent(parent);
                parent.getChildList().add(index,left);
                parent.getChildList().add(index+1,right);
                setData(null);
                parent.updateInsert(tree);
                setParent(null);
            }else {
                //parent为null则为root节点
                isRoot = false;
                Node root = new Node(true);
                tree.setRoot(root);
                left.setParent(root);
                right.setParent(root);
                root.getChildList().add(left);
                root.getChildList().add(right);
                setData(null);
                //todo 根节点有两个子节点，更新根节点的索引
                root.updateInsert(tree);
            }

        }
    }

    private boolean keyExists(Comparable key){
        return data.stream().map(Pair::getKey).collect(Collectors.toList()).contains(key);
    }

    /**
     * 所有叶节点都位于同一层并且有L/2到L个数据项
     * @param key
     * @param tree
     */
    @Override
    public void remove(Comparable key,BPlusTree tree) {
        //如果叶节点不存在该关键字，则直接返回
        if (!keyExists(key)){
            return;
        }

        //如果是根节点直接删除该数据即可 || 关键字大于L/2,直接删除
        if (isRoot || (data.size()>limit/2 && data.size()>2)){
            remove(key);
        }else {
            //如果自身关键字小于L/2,找统一父节点下前/后节点借或者合并
            if (isNodeSuit1(previous)){
                int size = previous.getData().size();
                Pair<Comparable, Object> pair = previous.getData().get(size - 1);
                //前节点删除该数据,将该数据保存到当前节点
                previous.getData().remove(pair);
                //前节点的关键字都小于后节点...
                data.add(0,pair);
                //此时再删除key数据,不会影响b+树的平衡
                remove(key);
            }else if (isNodeSuit1(next)){
                Pair<Comparable, Object> pair = next.getData().get(0);
                //next节点的关键字都大于当前节点;next节点删除该数据并放入当前节点的末尾
                next.getData().remove(pair);
                data.add(pair);
                remove(key);
            }else {
                //此时只能合并前/后节点后再来执行删除操作保存树的平衡
                if (isNodeSuit2(previous)){
                    //删除前节点以及其关联,前节点数据保存到当前节点
                    previous.getData().forEach(pair-> data.add(0,pair));
                    remove(key);
                    previous.setParent(null);
                    previous.setData(null);
                    parent.getChildList().remove(previous);

                    if (previous.getPrevious() != null){
                        LeafNode newPrevious = previous.getPrevious();
                        previous.setPrevious(null);
                        previous.setNext(null);
                        previous = newPrevious;
                        newPrevious.setNext(this);
                    }else {
                        //该节点为头叶节点
                        tree.setHeadLeaf(this);
                        previous.setNext(null);
                        previous = null;
                    }
                }else if (isNodeSuit2(next)){
                    //合并next节点
                    data.addAll(next.getData());
                    remove(key);
                    next.setParent(null);
                    next.setData(null);
                    parent.getChildList().remove(next);
                    //更新链表
                    if (next.getNext() != null){
                        LeafNode newNext = this.next.getNext();
                        next.setNext(null);
                        next = newNext;
                        newNext.setPrevious(this);
                        next.setPrevious(null);
                    }else {
                        //next节点是最后一个节点,合并后当前节点成了最后一个节点
                        next.setPrevious(null);
                        next = null;
                    }
                }
            }
        }
        parent.updateRemove(tree);
    }

    /**
     * 删除该数据
     * @param key
     */
    private void remove(Comparable key){
        Optional<Pair<Comparable, Object>> removeOption = data.stream()
                .filter(e -> e.getKey().compareTo(key) == 0)
                .findFirst();
        removeOption.ifPresent(pair -> data.remove(pair));
    }

    private boolean isNodeSuit1(LeafNode node){
        return node!=null && node.getData().size()>limit/2
                && node.getData().size()>2
                && node.getParent() == parent;
    }

    private boolean isNodeSuit2(LeafNode node){
        return node!=null && node.getParent() == parent
                && (node.getData().size()<=limit/2
                || node.getData().size()<=2);
    }

    /**
     * 往叶节点插入/更新数据
     * @param key
     * @param obj
     */
    protected void insertOrUpdate(Comparable key, Object obj) {
        noNullRequire(key);
        Pair<Comparable, Object> pair = new Pair<>(key, obj);
        //判断是update还是insert
        Optional<Pair<Comparable, Object>> updateOption = data.stream()
                .filter(e -> e.getKey().compareTo(key) == 0)
                .findFirst();
        //更新操作
        if (updateOption.isPresent()){
            int index = data.indexOf(updateOption.get());
            data.set(index,pair);
            return;
        }

        //找到insert节点应该在的位置
        Optional<Pair<Comparable, Object>> insertOption = data.stream()
                .filter(e -> e.getKey().compareTo(key) > 0)
                .findFirst();
        if (insertOption.isPresent()){
            int index = data.indexOf(insertOption.get());
            data.add(index,pair);
        }else {
            data.add(pair);
        }

    }

}