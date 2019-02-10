import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
 
public class Chromo {

	List<Move> moveSq;
	double fitness;
	static FileOutputStream outputStream;
	
	public Chromo(List<Move> sq){//need decode
		this.moveSq = sq;
		this.fitness = 0.0;
	}
	
	public Chromo(Chromo ori){//for copy use
		this.moveSq = new ArrayList<Move>();
		for(Move mv : ori.moveSq){
			//
			Move newmv = new Move(mv.block, mv.optime, mv.type);
			newmv.setInPosi(mv.zeropropotion);
			newmv.setStrategy(mv.hinderMoveStrategy);
			newmv.setFrom(mv.from);
			newmv.setTo(mv.to);
			//
			this.moveSq.add(newmv);
		}
		this.fitness = ori.fitness;
	}
	
	public Chromo() throws IOException  {
		this.moveSq = new ArrayList<Move>();
		this.fitness = 0.0;
		Random r = new Random(System.currentTimeMillis());
		for(Block bk : YardState.Blocks.values()){
			if(bk.earliestIn < 0 && bk.outtime < YardState.EndTime+1){
				Double outtime = bk.outtime + (double)r.nextInt(100)/200;
				//出场分段在计划时间基础上加0-0.5的随机值，入场分段在入场时间基础上加0.5-1的随机值
				Move mvout = new Move(bk, outtime, Move.Role.outbound);
				mvout.setStrategy(3);//r.nextInt(3));///0张志英式不放回移动策略，1park式，2陶宁蓉式，3张志英式全放回
				this.moveSq.add(mvout);
				
			}else{
				if(bk.latestIn < YardState.EndTime+1){
					int in = r.nextInt(bk.latestIn-bk.earliestIn+1) +bk.earliestIn;
					Double intime = in + (double)r.nextInt(100)/200 +0.5;
					Move mvin = new Move(bk, intime, Move.Role.inbound);
					mvin.setStrategy(r.nextInt(3));///
					mvin.setInPosi((double)r.nextInt(100)/100);
					this.moveSq.add(mvin);
					if(bk.outtime < YardState.EndTime+1){
						Double outtime = bk.outtime + (double)r.nextInt(100)/200;
						Move mvout = new Move(bk, outtime, Move.Role.outbound);
						mvout.setStrategy(r.nextInt(3));///
						this.moveSq.add(mvout);
					}
				}
			}
		}
		Collections.sort(this.moveSq);
		this.decode(false);
	}
	
	public void decode(boolean needsoutput) throws IOException{
		if(needsoutput){
			outputStream = new FileOutputStream(YardState.path + "Schedule.txt");
		}
		String output = "";
		YardState yardstate = YardState.InitialState.copy();
		List<Move> allmoves = new ArrayList<Move>();
		Vertex road = new Vertex("0", null);
		
		for(int i=0; i<this.moveSq.size(); i++){
			List<Vertex> empties = yardstate.getEmptyPosi();
			Move mv = this.moveSq.get(i);
			double time = mv.optime;
			if(mv.type==Move.Role.inbound){
				Double putin = mv.zeropropotion * empties.size();
				mv.setTo(empties.get(putin.intValue()).getId());
				mv.setFrom(0);
			}
			if(mv.type==Move.Role.outbound){
				mv.setTo(0);
				for(Vertex vx : yardstate.nodes){
					if(mv.block.equals(vx.occupiedby)){
						mv.setFrom(vx.getId());
						break;
					}
				}
			}
			String shortest = MoveStrategy.dijkstra(yardstate.getDisMatrix(), mv.from, mv.to);
			int baseroutecost = Integer.parseInt(shortest.split(":")[0]);
			String r = shortest.split(":")[1];
			mv.route = r;
			String[] baserouteS = r.split(" ");
			List<Vertex> baseroute = new ArrayList<Vertex>();
			for(int j=0; j<baserouteS.length; j++){
				int index = Integer.parseInt(baserouteS[j]);
				if(index==0){
					baseroute.add(road);
				}else{
					baseroute.add(yardstate.nodes.get(index-1));
				}
			}
			//
			if(needsoutput){
				output += "********  Move task " + (i+1) + " starts with following yardstate:\r\n\r\n";
				output += yardstate.toString();
				output += mv.type + ", time: " + new java.text.DecimalFormat("#.0000").format(mv.optime) + ", Block: " + mv.block.id + ", From " + mv.from + " to " + mv.to;
				output += "  Move route: " + r + "\r\n";
				//System.out.print(mv.type + ", time: " + mv.optime + ", Block: " + mv.block.id + ", From " + mv.from + " to " + mv.to);//
				//System.out.println("  Move route: " + shortest.split(":")[1]);//
				if(baseroutecost > 999){
					//output += "<Hindrance blocks encountered>\r\n";
					//System.out.println("<Hindrance blocks encountered>");//
				}
			}
			//
			for(int k=0; k<baseroute.size(); k++){
				if(k>0 && k<baseroute.size()-1 && baseroute.get(k).occupiedby!=null){//发现阻挡分段
					Vertex hinderat = baseroute.get(k);
					Block hinder = baseroute.get(k).occupiedby;
					if(mv.hinderMoveStrategy == 3){//benchmark Zhang Zhiying
						Vertex left = hinderat.getLeft()>0 ? yardstate.nodes.get(hinderat.getLeft()-1) : null;
						if(left!=null && left.occupiedby==null && !baseroute.contains(left)){
							//Move temp = new Move(hinder, time-0.001+0.00001*k, Move.Role.inner);//阻挡分段左移一格
							//temp.setFrom(hinderat.getId());
							//temp.setTo(left.getId());
							//allmoves.add(temp);
							//hinderat.occupiedby = null;
							//left.occupiedby = hinder;
							baseroutecost += 1002;
							if(needsoutput)
								output += "Hindrance block " + hinder.id + " is moved to left and then back.\r\n";
								//System.out.println("Hindrance block " + hinder.id + " is moved to left.\r\n");//
						}else{
							Vertex right = hinderat.getRight()>0 ? yardstate.nodes.get(hinderat.getRight()-1) : null;
							if(right!=null && right.occupiedby==null && !baseroute.contains(right)){
								//Move temp = new Move(hinder, time-0.001+0.00001*k, Move.Role.inner);//阻挡分段右移一格
								//temp.setFrom(hinderat.getId());
								//temp.setTo(right.getId());
								//allmoves.add(temp);
								//hinderat.occupiedby = null;
								//right.occupiedby = hinder;
								baseroutecost += 1002;
								if(needsoutput)
									output += "Hindrance block " + hinder.id + " is moved to right and then back.\r\n";
									//System.out.println("Hindrance block " + hinder.id + " is moved to right.\r\n");//
							}else{
								Vertex up = hinderat.getUp()>0 ? yardstate.nodes.get(hinderat.getUp()-1) : null;
								if(up!=null && up.occupiedby==null && !baseroute.contains(up)){
									//Move temp = new Move(hinder, time-0.001+0.00001*k, Move.Role.inner);//阻挡分段上移一格
									//temp.setFrom(hinderat.getId());
									//temp.setTo(up.getId());
									//allmoves.add(temp);
									//hinderat.occupiedby = null;
									//up.occupiedby = hinder;
									baseroutecost += 1002;
									if(needsoutput)
										output += "Hindrance block " + hinder.id + " is moved up and then back.\r\n";
										//System.out.println("Hindrance block " + hinder.id + " is moved up.\r\n");//
								}else{
									Vertex down = hinderat.getDown()>0 ? yardstate.nodes.get(hinderat.getDown()-1) : null;
									if(down!=null && down.occupiedby==null && !baseroute.contains(down)){
										//Move temp = new Move(hinder, time-0.001+0.00001*k, Move.Role.inner);//阻挡分段下移一格
										//temp.setFrom(hinderat.getId());
										//temp.setTo(down.getId());
										//allmoves.add(temp);
										//hinderat.occupiedby = null;
										//down.occupiedby = hinder;
										baseroutecost += 1002;
										if(needsoutput)
											output += "Hindrance block " + hinder.id + " is moved down and then back.\r\n";
											//System.out.println("Hindrance block " + hinder.id + " is moved down.\r\n");//
									}else{
										baseroutecost += 1000;//取出后放回原地，成本增加一倍
										if(mv.type.equals(Move.Role.outbound)){
											baseroutecost += 2*(baseroute.size()-1-k);
										}else{
											baseroutecost += 2*k;
										}
										if(needsoutput)
											output += "Hindrance block  " + hinder.id + " cannot be moved in the yard!\r\n";
											//System.out.println("Hindrance block  " + hinder.id + " cannot be moved in the yard!\r\n");//
									}
								}
							}
						}
					}
					if(mv.hinderMoveStrategy == 0){
						Vertex left = hinderat.getLeft()>0 ? yardstate.nodes.get(hinderat.getLeft()-1) : null;
						if(left!=null && left.occupiedby==null && !baseroute.contains(left)){
							Move temp = new Move(hinder, time-0.001+0.00001*k, Move.Role.inner);//阻挡分段左移一格
							temp.setFrom(hinderat.getId());
							temp.setTo(left.getId());
							allmoves.add(temp);
							hinderat.occupiedby = null;
							left.occupiedby = hinder;
							baseroutecost += 1;
							if(needsoutput)
								output += "Hindrance block " + hinder.id + " is moved to left.\r\n";
								//System.out.println("Hindrance block " + hinder.id + " is moved to left.\r\n");//
						}else{
							Vertex right = hinderat.getRight()>0 ? yardstate.nodes.get(hinderat.getRight()-1) : null;
							if(right!=null && right.occupiedby==null && !baseroute.contains(right)){
								Move temp = new Move(hinder, time-0.001+0.00001*k, Move.Role.inner);//阻挡分段右移一格
								temp.setFrom(hinderat.getId());
								temp.setTo(right.getId());
								allmoves.add(temp);
								hinderat.occupiedby = null;
								right.occupiedby = hinder;
								baseroutecost += 1;
								if(needsoutput)
									output += "Hindrance block " + hinder.id + " is moved to right.\r\n";
									//System.out.println("Hindrance block " + hinder.id + " is moved to right.\r\n");//
							}else{
								Vertex up = hinderat.getUp()>0 ? yardstate.nodes.get(hinderat.getUp()-1) : null;
								if(up!=null && up.occupiedby==null && !baseroute.contains(up)){
									Move temp = new Move(hinder, time-0.001+0.00001*k, Move.Role.inner);//阻挡分段上移一格
									temp.setFrom(hinderat.getId());
									temp.setTo(up.getId());
									allmoves.add(temp);
									hinderat.occupiedby = null;
									up.occupiedby = hinder;
									baseroutecost += 1;
									if(needsoutput)
										output += "Hindrance block " + hinder.id + " is moved up.\r\n";
										//System.out.println("Hindrance block " + hinder.id + " is moved up.\r\n");//
								}else{
									Vertex down = hinderat.getDown()>0 ? yardstate.nodes.get(hinderat.getDown()-1) : null;
									if(down!=null && down.occupiedby==null && !baseroute.contains(down)){
										Move temp = new Move(hinder, time-0.001+0.00001*k, Move.Role.inner);//阻挡分段下移一格
										temp.setFrom(hinderat.getId());
										temp.setTo(down.getId());
										allmoves.add(temp);
										hinderat.occupiedby = null;
										down.occupiedby = hinder;
										baseroutecost += 1;
										if(needsoutput)
											output += "Hindrance block " + hinder.id + " is moved down.\r\n";
											//System.out.println("Hindrance block " + hinder.id + " is moved down.\r\n");//
									}else{
										baseroutecost += 1000;//取出后放回原地，成本增加一倍
										if(mv.type.equals(Move.Role.outbound)){
											baseroutecost += 2*(baseroute.size()-1-k);
										}else{
											baseroutecost += 2*k;
										}
										if(needsoutput)
											output += "Hindrance block  " + hinder.id + " cannot be moved in the yard!\r\n";
											//System.out.println("Hindrance block  " + hinder.id + " cannot be moved in the yard!\r\n");//
									}
								}
							}
						}
					}
					if(mv.hinderMoveStrategy == 1){
						int[][] dismatrix = yardstate.getDisMatrix();
						List<Vertex> candivxs = yardstate.getEmptyPosi();
						int countoffeasibleslot = 0;
						int[] labels = new int[candivxs.size()];
						int[] costs = new int[candivxs.size()];
						for(int p=0; p<candivxs.size(); p++){
							int downid = candivxs.get(p).getDown();
							if(!baseroute.contains(candivxs.get(p)) && downid==-1){
								labels[p] = 1;//除任务分段所选路径上的堆位外，将被占用堆位之前的第一个空堆位放入候选集
								countoffeasibleslot ++;
							}else{
								if(!baseroute.contains(candivxs.get(p)) && (yardstate.nodes.get(downid-1).occupiedby!=null 
										|| baseroute.contains(yardstate.nodes.get(downid-1)))){
									labels[p] = 1;
									countoffeasibleslot ++;
								}
							}
						}
						if(countoffeasibleslot > 0){
							for(int q=0; q<candivxs.size(); q++){
								if(labels[q] > 0){
									String routeA = MoveStrategy.dijkstra(dismatrix, hinderat.getId(), candivxs.get(q).getId());
									int cost = Integer.parseInt(routeA.split(":")[0]);
									if(cost > 999){
										labels[q] = 0;
										countoffeasibleslot --;//无法到达的候选空堆位，去掉
									}else{
										costs[q] = cost-500;
									}
								}
							}
							for(Vertex v : yardstate.nodes){
								if(v.occupiedby!=null && v.occupiedby.outtime > hinder.outtime){//小数点~
									String routeB = MoveStrategy.dijkstra(dismatrix, v.getId(), 0);
									String[] baserouteB = routeB.split(":")[1].split(" ");
									for(String s : baserouteB){
										int passby = Integer.parseInt(s);
										for(int l=0; l<candivxs.size(); l++){
											if(labels[l] > 0){
												if(passby == candivxs.get(l).getId()){
													labels[l] = 2;//符合1的条件但是阻碍了晚出场分段的当前最优路径
												}
											}
										}
									}
								}
							}
							if(countoffeasibleslot > 0){
								int type1mincost = 100000;
								int besttype1 = -1;
								int type2mincost = 100000;
								int besttype2 = -1;
								for(int m=0; m<candivxs.size(); m++){
									if(labels[m] == 1){
										if (costs[m] < type1mincost){
											type1mincost = costs[m];
											besttype1 = m;
										}
									}
									if(labels[m] == 2){
										if (costs[m] < type2mincost){
											type2mincost = costs[m];
											besttype2 = m;
										}
									}
								}
								if(besttype1 != -1){
									Move temp = new Move(hinder, time-0.001+0.00001*k, Move.Role.inner);
									temp.setFrom(hinderat.getId());
									temp.setTo(candivxs.get(besttype1).getId());
									allmoves.add(temp);
									hinderat.occupiedby = null;
									candivxs.get(besttype1).occupiedby = hinder;
									baseroutecost += type1mincost;
									if(needsoutput)
										output += "Hindrance block " + hinder.id + " is moved to position " + candivxs.get(besttype1).getId() + " .\r\n";
										//System.out.println("Hindrance block " + hinder.id + " is moved to position " + candivxs.get(besttype1).getId() + " .\r\n");//
								}else{
									Move temp = new Move(hinder, time-0.001+0.00001*k, Move.Role.inner);
									temp.setFrom(hinderat.getId());
									temp.setTo(candivxs.get(besttype2).getId());//
									allmoves.add(temp);
									hinderat.occupiedby = null;
									candivxs.get(besttype2).occupiedby = hinder;
									baseroutecost += type2mincost;// care for 100000
									if(needsoutput)
										output += "Hindrance block " + hinder.id + " is moved to position " + candivxs.get(besttype2).getId() + " .\r\n";
										//System.out.println("Hindrance block " + hinder.id + " is moved to position " + candivxs.get(besttype2).getId() + " .\r\n");//
								}
							}else{
								baseroutecost += 1000;//取出后放回原地，成本增加一倍
								if(mv.type.equals(Move.Role.outbound)){
									baseroutecost += 2*(baseroute.size()-1-k);
								}else{
									baseroutecost += 2*k;
								}
								if(needsoutput)
									output += "Hindrance block  " + hinder.id + " cannot be moved in the yard!\r\n";
									//System.out.println("Hindrance block  " + hinder.id + " cannot be moved in the yard!\r\n");//
							}
						}else{
							baseroutecost += 1000;//取出后放回原地，成本增加一倍
							if(mv.type.equals(Move.Role.outbound)){
								baseroutecost += 2*(baseroute.size()-1-k);
							}else{
								baseroutecost += 2*k;
							}
							if(needsoutput)
								output += "Hindrance block  " + hinder.id + " cannot be moved in the yard!\r\n";
								//System.out.println("Hindrance block  " + hinder.id + " cannot be moved in the yard!\r\n");//
						}
					}
					if(mv.hinderMoveStrategy == 2){
						int[][] dismatrix = yardstate.getDisMatrix();
						List<Vertex> candivxs = yardstate.getEmptyPosi();
						int maxpathsum = 0;
						int bestidx = -1;
						int bestcost = 100000;// care for 100000
						for(int p=0; p<candivxs.size(); p++){
							if(!baseroute.contains(candivxs.get(p))){
								String routeA = MoveStrategy.dijkstra(dismatrix, hinderat.getId(), candivxs.get(p).getId());
								int cost = Integer.parseInt(routeA.split(":")[0]);
								if(cost < 999){
									int pathsum = 0;
									YardState yst = yardstate.copy();
									yst.nodes.get(candivxs.get(p).getId()-1).occupiedby = hinder;
									yst.nodes.get(hinderat.getId()-1).occupiedby = null;
									int[][] cntmatrix = yst.getCntMatrix();//
									PathCounter pctr = new PathCounter(cntmatrix);//
									for(Vertex v : yst.nodes){
										if(v.occupiedby == null){
											pathsum += pctr.countpath(v.getId(), 0);
										}else{
											if(v.occupiedby.outtime > hinder.outtime){//小数点~
												pathsum += pctr.countpath(v.getId(), 0);
											}
										}
									}
									if(pathsum > maxpathsum){
										maxpathsum = pathsum;
										bestidx = p;
										bestcost = cost;
									}
								}
							}
						}
						if(bestidx != -1){
							Move tempmax = new Move(hinder, time-0.001+0.00001*k, Move.Role.inner);
							tempmax.setFrom(hinderat.getId());
							tempmax.setTo(candivxs.get(bestidx).getId());
							allmoves.add(tempmax);
							hinderat.occupiedby = null;
							candivxs.get(bestidx).occupiedby = hinder;
							baseroutecost += bestcost;
							if(needsoutput){
								output += "Hindrance block " + hinder.id + " is moved to position " + candivxs.get(bestidx).getId() + "\r\n";
								output += "Sum of pathes is " + maxpathsum + " .\r\n";
								//System.out.println("Hindrance block " + hinder.id + " is moved to position " + candivxs.get(bestidx).getId());//
								//System.out.println("Sum of pathes is " + maxpathsum + " .\r\n");//
							}
						
						}else{
							baseroutecost += 1000;//取出后放回原地，成本增加一倍
							if(mv.type.equals(Move.Role.outbound)){
								baseroutecost += 2*(baseroute.size()-1-k);
							}else{
								baseroutecost += 2*k;
							}
							if(needsoutput)
								output += "Hindrance block  " + hinder.id + " cannot be moved in the yard!\r\n";
								//System.out.println("Hindrance block  " + hinder.id + " cannot be moved in the yard!\r\n");//
						}
					}
				}
			}
			double accost;
			if(mv.type==Move.Role.outbound){
				yardstate.nodes.get(mv.from-1).occupiedby = null;
				accost = (double)(baseroutecost-500)/1000;//-500因为把自己所在位置导致的路径成本算进去了
				this.fitness += accost;
			}else{
				yardstate.nodes.get(mv.to-1).occupiedby = mv.block;
				accost = (double)baseroutecost/1000;
				this.fitness += accost;
			}
			if(needsoutput)
				output += ">>>Accumulated cost is: " + new java.text.DecimalFormat("#.000").format(accost) + "\r\n\r\n";
				//System.out.println(" > Accumulated cost: " + new java.text.DecimalFormat("#.0000").format(accost));//
		}
		if(needsoutput){
			output += "-----Total cost of this plan is " + new java.text.DecimalFormat("#.000").format(this.fitness) + "\r\n";
			outputStream.write(output.getBytes());
			outputStream.close();
			//System.out.println(">>>Total cost of this plan is " + new java.text.DecimalFormat("#.0000").format(this.fitness));//
		}
		this.fitness = 1.0/this.fitness;
	}

	public String print() {
		String output = "";
		for (int i=0; i<this.moveSq.size(); i++) {
			Move mv = this.moveSq.get(i);
			String s = "" + (i+1)+ "\tB" + mv.block.id + "\t" + mv.type + " \t@" + 
			new java.text.DecimalFormat("#.000").format(mv.optime) + "\tS" + mv.hinderMoveStrategy + 
			"\tZ" + new java.text.DecimalFormat("#.000").format(mv.zeropropotion) + "\tRoute: " + mv.route;
			output += s + "\r\n";
			System.out.println(s);//
		}
		String t = "Fitness (reciprocal cost) is: " + new java.text.DecimalFormat("#.000").format(this.fitness);
		output += t + "\r\n";
		System.out.println(t);//
		return output;
	}


	protected Chromo copy() {
		Chromo chromo = new Chromo(this);
		return chromo;
	}
}
