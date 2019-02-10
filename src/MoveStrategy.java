
public class MoveStrategy {

	public static String dijkstra(int[][] W1, int start, int end) {
		
		boolean[] isLabel = new boolean[W1[0].length];// 是否标号
		int[] indexs = new int[W1[0].length];// 所有标号的点的下标集合，以标号的先后顺序进行存储，实际上是一个以数组表示的栈
		int i_count = -1;// 栈的顶点
		int[] distance = W1[start].clone();// v0到各点的最短距离的初始值
		int index = start;// 从初始点开始
		int presentShortest = 0;// 当前临时最短距离

		indexs[++i_count] = index;// 把已经标号的下标存入下标集中
		isLabel[index] = true;

		while (i_count < W1[0].length) {
			// 第一步：得到与原点最近的某个点
			int min = Integer.MAX_VALUE;
			for (int i = 0; i < distance.length; i++) {
				if (!isLabel[i] && distance[i] != -1 && i != index) {//i!=index 多余？
					// 如果到这个点有边,并且没有被标号
					if (distance[i] < min) {
						min = distance[i];
						index = i;// 把下标改为当前下标
					}
				}
			}
			i_count = i_count + 1;
			if (i_count == W1[0].length) {
				break;
			}
			isLabel[index] = true;// 对点进行标号
			indexs[i_count] = index;// 把已经标号的下标存入下标集中

			if (W1[indexs[i_count - 1]][index] == -1
					|| presentShortest + W1[indexs[i_count - 1]][index] > distance[index]) {
				// 如果两个点没有直接相连，或者两个点的路径大于最短路径
				presentShortest = distance[index];
			} else {
				presentShortest += W1[indexs[i_count - 1]][index];
			}

			// 第二步：加入vi后，重新计算distance中的距离
			for (int i = 0; i < distance.length; i++) {

				// 如果vi到那个点有边，则v0到后面点的距离加
				if (distance[i] == -1 && W1[index][i] != -1) {// 如果以前不可达，则现在可达了
					distance[i] = presentShortest + W1[index][i];
				} else if (W1[index][i] != -1
						&& presentShortest + W1[index][i] < distance[i]) {
					// 如果以前可达，但现在的路径比以前更短，则更换成更短的路径
					distance[i] = presentShortest + W1[index][i];
				}
			}

		}
		int pathcost = distance[end] - distance[start];
		return "" + pathcost + ":" + getRoute(W1, indexs, end).trim();
	}

	/**
	 * Songbo: 按照 Dijkstra 方法寻找最短路径
	 * @param WW 权重
	 * @param indexs 起始
	 * @param end 结束
	 * @return 最短路径及其成本
	 */
	public static String getRoute(int[][] WW, int[] indexs, int end) {
		String[] routeArray = new String[indexs.length];
		for (int i = 0; i < routeArray.length; i++) {
			routeArray[i] = "";
		}
		// 自己的路线
		routeArray[indexs[0]] = indexs[0] + " ";
		for (int i = 1; i < indexs.length; i++) {
			// 看该点与前面所有点的连接线中的最短路径，然后得到该最短路径到底是连接了哪个点，进而此点的route就是找出那点的route+此点
			int[] thePointDis = WW[indexs[i]];
			int prePoint = 0;
			int tmp = 9999;
			for (int j = 0; j < thePointDis.length; j++) {
				boolean chooseFlag = false;
				// 边的距离最短，而且，所连的点在前面的点当中
				for (int m = 0; m < i; m++) {
					if (j == indexs[m]) {
						chooseFlag = true;
					}
				}
				if (chooseFlag == false) {
					continue;
				}
				if (thePointDis[j] < tmp && thePointDis[j] > 0) {
					prePoint = j;
					tmp = thePointDis[j];
				}
			}
			routeArray[indexs[i]] = routeArray[prePoint] + indexs[i] + " ";
			if(indexs[i]==end){
				return routeArray[indexs[i]];
			}
		}
		return " -- ";//放不下时?
	}
	
	public static int routeCount(int[][] WW, int[] indexs, int end) {
		
		return 0;
	}
	
}
