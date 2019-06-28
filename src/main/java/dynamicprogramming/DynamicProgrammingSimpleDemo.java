package dynamicprogramming;

import java.util.Arrays;

/**
 * Created by scorpio.rao on 2019/6/28
 * 初识动态规划
 * 背包问题:
 * 对于一组不同重量、不可分割的物品，我们需要选择一些装入背包，在满足背包最大重量限制的前提下，背包中物品总重量的最大值是多少呢？
 */
public class DynamicProgrammingSimpleDemo {

    public static void main(String[] args) {
        int[] weight = {2,2,4,6,3};
        int maxWeight = 16;
        System.out.println("背包问题:背包最大承载重量:"+maxWeight+",一共有"+weight.length+"个物品,重量分别为"+ Arrays.toString(weight));

        int result = knapsack(weight.length, weight, maxWeight);
        System.out.println("背包能最多能装的重量是:"+result);
    }

    /**
     * @param n >> 物品个数
     * @param weight >> 物品重量
     * @param maxWeight >>　背包可承载重量
     * @return
     * 动态规划的思路：将问题拆解为多个阶段,每一个阶段对应一个决策.
     * 记录每一个阶段可达到的状态集合(去掉重复的),通过当前阶段的状态集合,
     * 推导下一阶段的状态集合,动态的往前推进.
     */
    private static int knapsack(int n,int[] weight,int maxWeight){
        //第几个物品,此时的重量  >> 背包装的重量可以是0到maxWeight
        boolean[][] states = new boolean[n][maxWeight+1];
        //初始状态
        states[0][0] = true;
        //当第一个物品的重量背包能够承载时,记录下这个状态为true
        if (weight[0] <= maxWeight){
            states[0][weight[0]] = true;
        }

        for (int i=1;i<n;i++){

            //第i个物品不放入背包
            for (int j=0;j<=maxWeight;j++){
                //若上一次在此处的决策状态是true,那么此处不放物体,状态也会是true
                if (states[i-1][j]){
                    states[i][j] = true;
                }
            }

            //第i个物品放入背包(j表示背包此时的重量)
            for (int j=0;j<=maxWeight-weight[i];j++){
                //上一阶段在某个位置的状态为true,在这个位置的基础上判断此时再添加这个物品状态修改为true
                if (states[i-1][j]){
                    states[i][j+weight[i]] = true;
                }
            }
        }
        //到最后一次决策的位置拿出最终的结果>>此处i表示当前状态下背包的重量,倒过来遍历拿出最大值
        for (int i=maxWeight;i>=0;i--){
            if (states[n-1][i]){
                return i;
            }
        }

        return 0;
    }

    
}
