
public class Move implements Comparable<Move> {
	
	protected Block block;
	protected Double optime;
	protected double zeropropotion;//�볡�ֶη���������˳�����еĿն�λ�ĵ�x�ٷֱȽص���
	protected int from;
	protected int to;
	protected Role type;
	protected int hinderMoveStrategy;
	protected String route;
	
	enum Role{
		inbound, outbound, inner
	}
	
	public Move(Block blc, double time, Role role){
		this.block = blc;
		this.optime = time;
		this.type = role;
	}
	
	public void setInPosi (double in){
		this.zeropropotion = in;
	}
	
	public void setStrategy (int stra){
		this.hinderMoveStrategy = stra;
	}
	
	public void setFrom (int from){
		this.from = from;
	}
	
	public void setTo (int to){
		this.to = to;
	}
	
	public int compareTo(Move arg0) {
        return this.optime.compareTo(arg0.optime);
    }

}
