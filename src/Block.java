
public class Block {

	public int id;
	public int earliestIn;
	public int latestIn;
	//public int intime;//in chromo
	public int outtime;
	//public Map<Integer, Integer> positions;//ÿ�ε������ŵ�λ�ã����������
	//public Map<Integer, List<Integer>> routes;//�洢��ÿ�ε������ƶ���·�������������
	 
	
	public Block(int id, int ear, int lat, int out){//��ear=-1, ��lat ��ʾ0ʱ�����볡�ֶε�λ��
		this.id = id;
		this.earliestIn = ear;
		this.latestIn = lat;
		this.outtime = out;
	}
	
}
