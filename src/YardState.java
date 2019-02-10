import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

	
public class YardState {
	
	public static int XLen = 12;
	public static int YLen = 5;
	public static Map<Integer, Block> Blocks;
	public static YardState InitialState;
	public static int EndTime = 10;
	public static String path = "C:\\eclipse\\JavaOutput\\";
	
	List<Vertex> nodes;
	
	public static void yardInitialization(String filepath) throws Exception{
		path = filepath.substring(0, filepath.lastIndexOf("\\")+1);
		Blocks = new HashMap<Integer, Block>();
		File inputfile = new File(filepath);
		FileReader fin = new FileReader(inputfile);
		BufferedReader br = new BufferedReader(fin);
		String templine = null;
		while((templine = br.readLine()) != null){
			String[] data = templine.split("\\s");
			Blocks.put(Integer.parseInt(data[0]), new Block(Integer.parseInt(data[0]),Integer.parseInt(data[1]),Integer.parseInt(data[2]),Integer.parseInt(data[3])));
		}
		br.close();
		fin.close();
		
		List<Vertex> vts = new ArrayList<Vertex>();
		for(int i = 1; i < XLen*YLen+1; i++){
			Vertex vt = new Vertex(Integer.toString(i), null);
			for(Block bk : Blocks.values()){
				if((bk.earliestIn==-1)&&(bk.latestIn==i)){
					vt.occupiedby = bk;
				}
			}
			vts.add(vt);
		}
		InitialState = new YardState(vts);
		//System.out.println(InitialState.toString());//
	}
	
	public YardState(List<Vertex> positionstatus){
		this.nodes = positionstatus;
	}
	
	public YardState copy(){
		List<Vertex> newVxs = new ArrayList<Vertex>();
		for(Vertex vx : this.nodes){
			newVxs.add(vx.copy());
		}
		return new YardState(newVxs);
	}
	/*
	public void nextstate(Move mv){
		for(Vertex vx : this.nodes){
			if(vx.getId()==mv.to){
				vx.occupiedby = mv.block;
			}
			if(vx.getId()==mv.from){
				vx.occupiedby = null;
			}
		}
	}
	*/
	
	protected int[][] getCntMatrix(){
		List<Set<Integer>> cntmatrix = new ArrayList<Set<Integer>>();
		for(int i=0; i<this.nodes.size()+1; i++){
			cntmatrix.add(new HashSet<Integer>());//每个list存当前堆位可到达的下一个堆位
		}
		for(int i=1; i<this.nodes.size()+1; i++){
			Vertex v = this.nodes.get(i-1);
			Set<Integer> vto = cntmatrix.get(i);
			for(int j=1; j<this.nodes.size()+1; j++){
				Vertex x = this.nodes.get(j-1);
				if(j!=i){
					if((v.getY()==x.getY() && Math.abs(v.getX()-x.getX())==1)||(v.getX()==x.getX() && Math.abs(v.getY()-x.getY())==1)){
						if(x.occupiedby==null){
							vto.add(j);
						}
					}
				}
			}
			if(v.getY()==1){
				vto.add(0);
			}
		}
		int[][] intMatrix = new int[this.nodes.size()+1][];
		for(int i=0; i<this.nodes.size()+1; i++){
			Set<Integer> st = cntmatrix.get(i);
			int[] introw = new int[st.size()];
			int j = 0;
			for(int it : st){
				introw[j] = it;
				j++;
			}
			intMatrix[i] = introw;
		}
		return intMatrix;
	}
	
	protected int[][] getDisMatrix(){
		int[][] dismatrix = new int[this.nodes.size()+1][this.nodes.size()+1];//+1表示路（单方通行）所在节点
		int[] roadto1stRow = dismatrix[0];
		roadto1stRow[0] = 0;
		for(int i=1; i<this.nodes.size()+1; i++){
			Vertex v = this.nodes.get(i-1);
			if(v.getY()==1){
				if(v.occupiedby!=null){
					roadto1stRow[i] = 501;
				}else{
					roadto1stRow[i] = 1;
				}
			}else{
				roadto1stRow[i] = -1;
			}
		}
		for(int i=1; i<this.nodes.size()+1; i++){
			Vertex v = this.nodes.get(i-1);
			int[] dists = dismatrix[i];
			if(v.getY()==1){
				if(v.occupiedby!=null){
					dists[0] = 501;
				}else{
					dists[0] = 1;
				}
			}else{
				dists[0] = -1;
			}
			for(int j=1; j<this.nodes.size()+1; j++){
				Vertex x = this.nodes.get(j-1);
				if((v.getY()==x.getY() && Math.abs(v.getX()-x.getX())==1)||(v.getX()==x.getX() && Math.abs(v.getY()-x.getY())==1)){
					if(v.occupiedby!=null && x.occupiedby!=null){
						dists[j] = 1001;
					}else{
						if(v.occupiedby==null && x.occupiedby==null){
							dists[j] = 1;
						}else{
							dists[j] = 501;
						}
					}
				}else{
					if(i==j){
						dists[j] = 0;
					}else{
						dists[j] = -1;
					}
				}
			}
		}
		return dismatrix;
	}
	
	//
	
	public List<Vertex> getEmptyPosi(){
		List<Vertex> emptyposi = new ArrayList<Vertex>();
		for(Vertex vx : this.nodes){
			if(vx.occupiedby==null){
				emptyposi.add(vx);
			}
		}
		return emptyposi;
	}
	/*
	public double findMinPath(int from, int to, Move.Role type){
		double pathcost = MoveStrategy.dijkstra(this.getDisMatrix(), from, to);
		//if(pathcost > 999){
		//	System.out.print(this.toString());//输出带阻挡分段的路径//
		//}
		if(type==Move.Role.outbound){
			pathcost = (pathcost-500)/1000;//-500因为把自己所在位置导致的路径成本算进去了
			System.out.println("，移动成本：" + pathcost);//
			return pathcost;
		}else{
			System.out.println("，移动成本：" + pathcost/1000);//
			return pathcost/1000;
		}
	}
	*/
	@Override
	public String toString() {
		String out = "";
		for(int i=1; i<YardState.YLen+1; i++){
			for (Vertex vx : this.nodes) {
				if(vx.getY()==i){
					String occupiedby = (vx.occupiedby == null) ? "0" : Integer.toString(vx.occupiedby.id);
					out += vx.getId() + "(" + occupiedby + ")\t";
				}
			}
			out += (i==YardState.YLen)? "\r\n\r\n" : "\r\n\r\n\r\n";
		}
		return out;
	}
	
}
