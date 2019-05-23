# dataStructure-learning
变强之路一:数据结构的学习

1)用java实现一棵b+树
  m阶b+树特性：
 * 1.数据储存在树叶上
 * 2.非叶节点储存m-1个关键字以指示搜索的方向；关键字i代表第i+1棵树中最小的关键字
 * 3.树的根是一片树叶 或者 其子节点数在2和m之间
 * 4.除根节点外所有非叶节点的子节点数在m/2和m之间
 * 5.所有叶节点都位于同一层并且有L/2到L个数据项（通过链表串联起来，方便按区间查找）
 
 思路:
 
 b+树有以下属性:
 
 Filed:根节点,叶节点(叶节点链表头部),阶数m
 
 Function:提供find,insertOrUpdate,remove等基本的增删改查方法
 
 节点:分为非叶节点Node,叶节点LeafNode
 
 commom Field:父节点parent,是否根节点isRoot
 
 common Function:find,insertOrUpdate,remove
 
 叶节点存放数据:
 
  其他属性有:每片叶节点最多存放的数据个数L,存放的数据List
  
 非叶节点存放索引:
 
  其他属性有:索引list,子节点List

  
