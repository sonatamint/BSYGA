
public class Block {

	public int id;
	public int earliestIn;
	public int latestIn;
	//public int intime;//in chromo
	public int outtime;
	//public Map<Integer, Integer> positions;//每次调度完存放的位置，仅保存最佳
	//public Map<Integer, List<Integer>> routes;//存储在每次调度中移动的路径，仅保存最佳
	 
	
	public Block(int id, int ear, int lat, int out){//若ear=-1, 则lat 表示0时刻已入场分段的位置
		this.id = id;
		this.earliestIn = ear;
		this.latestIn = lat;
		this.outtime = out;
	}
	
}
