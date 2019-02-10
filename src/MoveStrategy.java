
public class MoveStrategy {

	public static String dijkstra(int[][] W1, int start, int end) {
		
		boolean[] isLabel = new boolean[W1[0].length];// �Ƿ���
		int[] indexs = new int[W1[0].length];// ���б�ŵĵ���±꼯�ϣ��Ա�ŵ��Ⱥ�˳����д洢��ʵ������һ���������ʾ��ջ
		int i_count = -1;// ջ�Ķ���
		int[] distance = W1[start].clone();// v0���������̾���ĳ�ʼֵ
		int index = start;// �ӳ�ʼ�㿪ʼ
		int presentShortest = 0;// ��ǰ��ʱ��̾���

		indexs[++i_count] = index;// ���Ѿ���ŵ��±�����±꼯��
		isLabel[index] = true;

		while (i_count < W1[0].length) {
			// ��һ�����õ���ԭ�������ĳ����
			int min = Integer.MAX_VALUE;
			for (int i = 0; i < distance.length; i++) {
				if (!isLabel[i] && distance[i] != -1 && i != index) {//i!=index ���ࣿ
					// �����������б�,����û�б����
					if (distance[i] < min) {
						min = distance[i];
						index = i;// ���±��Ϊ��ǰ�±�
					}
				}
			}
			i_count = i_count + 1;
			if (i_count == W1[0].length) {
				break;
			}
			isLabel[index] = true;// �Ե���б��
			indexs[i_count] = index;// ���Ѿ���ŵ��±�����±꼯��

			if (W1[indexs[i_count - 1]][index] == -1
					|| presentShortest + W1[indexs[i_count - 1]][index] > distance[index]) {
				// ���������û��ֱ�������������������·���������·��
				presentShortest = distance[index];
			} else {
				presentShortest += W1[indexs[i_count - 1]][index];
			}

			// �ڶ���������vi�����¼���distance�еľ���
			for (int i = 0; i < distance.length; i++) {

				// ���vi���Ǹ����бߣ���v0�������ľ����
				if (distance[i] == -1 && W1[index][i] != -1) {// �����ǰ���ɴ�����ڿɴ���
					distance[i] = presentShortest + W1[index][i];
				} else if (W1[index][i] != -1
						&& presentShortest + W1[index][i] < distance[i]) {
					// �����ǰ�ɴ�����ڵ�·������ǰ���̣�������ɸ��̵�·��
					distance[i] = presentShortest + W1[index][i];
				}
			}

		}
		int pathcost = distance[end] - distance[start];
		return "" + pathcost + ":" + getRoute(W1, indexs, end).trim();
	}

	/**
	 * Songbo: ���� Dijkstra ����Ѱ�����·��
	 * @param WW Ȩ��
	 * @param indexs ��ʼ
	 * @param end ����
	 * @return ���·������ɱ�
	 */
	public static String getRoute(int[][] WW, int[] indexs, int end) {
		String[] routeArray = new String[indexs.length];
		for (int i = 0; i < routeArray.length; i++) {
			routeArray[i] = "";
		}
		// �Լ���·��
		routeArray[indexs[0]] = indexs[0] + " ";
		for (int i = 1; i < indexs.length; i++) {
			// ���õ���ǰ�����е���������е����·����Ȼ��õ������·���������������ĸ��㣬�����˵��route�����ҳ��ǵ��route+�˵�
			int[] thePointDis = WW[indexs[i]];
			int prePoint = 0;
			int tmp = 9999;
			for (int j = 0; j < thePointDis.length; j++) {
				boolean chooseFlag = false;
				// �ߵľ�����̣����ң������ĵ���ǰ��ĵ㵱��
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
		return " -- ";//�Ų���ʱ?
	}
	
	public static int routeCount(int[][] WW, int[] indexs, int end) {
		
		return 0;
	}
	
}
