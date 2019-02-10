import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
//import java.util.Scanner;

public class ImprovedGA {

	private Chromo[] chromosomes;
	private Chromo[] nextGeneration;
	private int N;
	private double p_c_t;
	private double p_m_t;
	private int MAX_GEN;
	public Chromo bestPlan;
	private double bestFitness;
	private double[] averageFitness;
	private double[] bestFitnesses;

	/**
	 * Constructor of GA class
	 * 
	 * @param n
	 *            种群规模
	 * @param g
	 *            运行代数
	 * @param p_c
	 *            交叉率
	 * @param p_m
	 *            变异率
	 * @param filename
	 *            数据文件名
	 */
	public ImprovedGA(int n, int g, double p_c, double p_m) {
		this.N = n;
		this.MAX_GEN = g;
		this.p_c_t = p_c;
		this.p_m_t = p_m;
		averageFitness = new double[MAX_GEN];
		bestFitnesses = new double[MAX_GEN];
		bestFitness = 0.0;
		chromosomes = new Chromo[N];
		nextGeneration = new Chromo[N];
	}

	public void solve(int j) throws IOException {
		System.out.println("<-------------------Start initilization------------------->");
		init();
		System.out.println("<--------------------End initilization-------------------->");
		//Scanner sc = new Scanner(System.in);
		//System.out.println("Press any key to continue...");
		//sc.nextLine();
		System.out.println("<---------------------Start evolution--------------------->");
		for (int i = 0; i <= MAX_GEN; i++) {
			if(i!=MAX_GEN){
				System.out.println("<----------Start generation " + (i+1) + "--------->");
			}
			evolve(i);
			if(i!=MAX_GEN){
				System.out.println("<-----------End generation " + (i+1) + "---------->");
				//System.out.println("Press any key to continue...");
				//sc.nextLine();
			}
		}
		System.out.println("<----------------------End evolution---------------------->");
		outputResults(YardState.path + "Result_" + j + ".txt");
	}

	/**
	 * 初始化GA
	 * 
	 * @throws IOException
	 */
	private void init() throws IOException {

		for (int i = 0; i < N; i++) {
			Chromo chromosome = new Chromo();
			chromosomes[i] = chromosome;
			System.out.println(">> The " + (i+1) + "th initial chromosome is:");
			chromosome.print();
		}
	}

	private void evolve(int g) throws IOException {
		double[] selectionP = new double[N];// 选择概率
		double sum = 0.0;
		double tmp = 0.0;
		double localbest = 0.0;
		for (int i = 0; i < N; i++) {
			sum += chromosomes[i].fitness;
			if (chromosomes[i].fitness > bestFitness) {
				bestFitness = chromosomes[i].fitness;
				bestPlan = chromosomes[i];
			}
			if(chromosomes[i].fitness > localbest) {
				localbest = chromosomes[i].fitness;
			}
		}
		if(g==MAX_GEN){
			return;
		}
		bestFitnesses[g] = localbest;
		averageFitness[g] = sum / N;
		for (int i = 0; i < N; i++) {
			tmp += chromosomes[i].fitness / sum;
			selectionP[i] = tmp;
		}
		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < N; i = i + 2) {
			Chromo[] children = new Chromo[2];
			// 轮盘赌选择两个染色体
			System.out.println("<-------start selection------>");
			for (int j = 0; j < 2; j++) {
				int selected = 0;
				for (int k = 0; k < N - 1; k++) {
					double p = random.nextDouble();
					if (k == 0 && p <= selectionP[k]) {
						selected = 0;
						break;
					} else {
						if (p > selectionP[k] && p <= selectionP[k + 1]) {
							selected = k + 1;
							break;
						}
					}
				}
				children[j] = chromosomes[selected].copy();
				children[j].fitness = 0.0;
				children[j].print();//
			}
			int length = children[0].moveSq.size();
			List<Move> tour1 = new ArrayList<Move>();
			List<Move> tour2 = new ArrayList<Move>();
			// 交叉操作(OX1)
			// Random random = new Random(System.currentTimeMillis());
			if (random.nextDouble() < p_c_t) {
				System.out.println("<-------Start crossover------>");
				// 定义两个cut点
				int cutPoint1 = random.nextInt(length);
				int incre = random.nextInt(length - cutPoint1);
				int cutPoint2 = cutPoint1 + incre;
				System.out.println("Two cut points are " + cutPoint1 + " " + cutPoint2);//
				List<Move> noncommon1 = new ArrayList<Move>();
				List<Move> noncommon2 = new ArrayList<Move>();
				for (int k = cutPoint1; k <= cutPoint2; k++) {
					Move mv1 = children[0].moveSq.get(k);
					boolean containedinSect2 = false;
					for(int p = cutPoint1; p <= cutPoint2; p++){
						if(mv1.block.equals(children[1].moveSq.get(p).block)
								&& mv1.type.equals(children[1].moveSq.get(p).type)){
							containedinSect2 = true;
							break;
						}
					}
					if(!containedinSect2){
						noncommon1.add(mv1);
					}
					Move mv2 = children[1].moveSq.get(k);
					boolean containedinSect1 = false;
					for(int p = cutPoint1; p <= cutPoint2; p++){
						if(mv2.block.equals(children[0].moveSq.get(p).block)
								&& mv2.type.equals(children[0].moveSq.get(p).type)){
							containedinSect1 = true;
							break;
						}
					}
					if(!containedinSect1){
						noncommon2.add(mv2);
					}
				}
				int sub1 = 0;//已经替换了几个重复Move
				int sub2 = 0;
				for (int j = 0; j < length; j++) {
					Move mv1 = children[0].moveSq.get(j);
					Move mv2 = children[1].moveSq.get(j);
					if (j < cutPoint1 || j > cutPoint2){
						boolean dup1 = false;
						for(Move mv : noncommon2){
							if(mv1.block.equals(mv.block) && mv1.type.equals(mv.type)){
								tour1.add(noncommon1.get(sub1));
								sub1 ++;
								dup1 = true;
								break;
							}
						}
						if(!dup1){
							tour1.add(children[0].moveSq.get(j));
						}
						boolean dup2 = false;
						for(Move mv : noncommon1){
							if(mv2.block.equals(mv.block) && mv2.type.equals(mv.type)){
								tour2.add(noncommon2.get(sub2));
								sub2 ++;
								dup2 = true;
								break;
							}
						}
						if(!dup2){
							tour2.add(children[1].moveSq.get(j));
						}
					} else {
						tour1.add(children[1].moveSq.get(j));
						tour2.add(children[0].moveSq.get(j));
					}
				}
				Collections.sort(tour1);
				Collections.sort(tour2);
				children[0].moveSq = tour1;
				children[1].moveSq = tour2;
			}
			System.out.println("The crossed children are ");//
			children[0].print();
			children[1].print();

			// 变异操作(DM)
			// random = new Random(System.currentTimeMillis());
			if (random.nextDouble() < p_m_t) {
				System.out.println("<-------Start mutation------>");
				for (int j = 0; j < 2; j++) {
					// 定义两个cut点
					int cutPoint1 = random.nextInt(length);
					int incre = random.nextInt(length - cutPoint1);
					int cutPoint2 = cutPoint1 + incre;
					System.out.println("Two cut points are " + cutPoint1 + " " + cutPoint2);//
					for (int k = 0; k < length; k++) {
						Move mv = children[j].moveSq.get(k);
						if (cutPoint1 <= k && k <= cutPoint2){
							Block bk = mv.block;
							if(mv.type.equals(Move.Role.inbound)){
								int in = random.nextInt(bk.latestIn-bk.earliestIn+1) +bk.earliestIn;
								Double intime = in + (double)random.nextInt(100)/200 +0.5;
								mv.optime = intime;
								mv.setInPosi((double)random.nextInt(100)/100);
								//mv.setStrategy(random.nextInt(2));
							}else if(mv.type.equals(Move.Role.outbound)){
								Double outtime = bk.outtime + (double)random.nextInt(100)/200;
								mv.optime = outtime;
								//mv.setStrategy(random.nextInt(2));
							}
						}
					}
					Collections.sort(children[j].moveSq);
				}
			}
			System.out.println("The mutated children are ");//
			children[0].print();
			children[1].print();
			children[0].decode(false);
			children[1].decode(false);
			System.out.println("Update the fitness and from-to of the two children: ");//
			children[0].print();
			children[1].print();
			nextGeneration[i] = children[0]; 
			nextGeneration[i+1] = children[1];
		}
		//
		List<Chromo> listold = Arrays.asList(chromosomes);
		Collections.sort(listold, new Comparator<Chromo>(){
			public int compare(Chromo o1, Chromo o2){
				return (-1) * ((Double)o1.fitness).compareTo((Double)o2.fitness);
			}
		});
		List<Chromo> listnew = Arrays.asList(nextGeneration);
		Collections.sort(listnew, new Comparator<Chromo>(){
			public int compare(Chromo o1, Chromo o2){
				return ((Double)o1.fitness).compareTo((Double)o2.fitness);
			}
		});
		//
		for (int k = 0; k < N; k++) {
			//System.out.println("                           old " + chromosomes[k].fitness + 
			//		" ;  new " + nextGeneration[k].fitness);//
			if(k >= N/10){
				chromosomes[k] = nextGeneration[k];//.copy();//no need to copy ?
			}
			//System.out.println("NextGen " + chromosomes[k].fitness);//
		}
	}
	
	public void solvebyrule() throws IOException{
		for (int k = 0; k < N; k++) {
			chromosomes[k].fitness = 0.0;
			for(Move mv : chromosomes[k].moveSq){
				mv.setStrategy(2);
			}
			chromosomes[k].decode(false);
		}
		List<Chromo> list = Arrays.asList(chromosomes);
		Collections.sort(list, new Comparator<Chromo>(){
			public int compare(Chromo o1, Chromo o2){
				return (-1) * ((Double)o1.fitness).compareTo((Double)o2.fitness);
			}
		});
		System.out.println("<---------The top 20% best plans obtained with stratege 3---------->");//
		for(int j = 0; j < N/5; j++){
			chromosomes[j].print();
		}
	}

	private void outputResults(String path) {
		String filename = path;
		try {
			FileOutputStream outputStream = new FileOutputStream(filename);
			for (int i = 0; i < averageFitness.length; i++) {
				String line = String.valueOf(averageFitness[i]) + " " + String.valueOf(bestFitnesses[i]) + "\r\n";
				//String line = "第 " + (i+1) + " 代种群的平均适应度为: " + String.valueOf(averageFitness[i]) + "\r\n";
				outputStream.write(line.getBytes());
			}
			String rst = "The best plan is:\r\n" + bestPlan.print();
			outputStream.write(rst.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}