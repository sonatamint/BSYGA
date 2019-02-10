

public class Main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	//Create branch G1
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			YardState.yardInitialization("C:\\eclipse\\JavaInput\\input.txt");
			int n = 3;
			int bestrun = -1;
			Chromo bestofn = null;
			double bestcost = 100000.0;
			double avrgcost = 0.0;
			double[] nfit = new double[n];
			double[] ntim = new double[n];
			for(int i=0; i<n; i++){
				long start = System.currentTimeMillis();
				ImprovedGA ga = new ImprovedGA(100, 100, 0.9, 0.1);
				ga.solve(i);
				long end = System.currentTimeMillis();
				nfit[i] = (double)1/ga.bestPlan.fitness;
				ntim[i] = (double)(end - start);
				if(nfit[i] < bestcost){
					bestcost = nfit[i];
					bestofn = ga.bestPlan;
					bestrun = i;
				}
			}
			bestofn.decode(true);
			//ga.solvebyrule();
			/*
			Chromo bestplan = new Chromo();
			double fitness = bestplan.fitness;
			for(int i=1; i<1000; i++){
				Chromo chromo = new Chromo();
				if(chromo.fitness < fitness){
					fitness = chromo.fitness;
					bestplan = chromo;
				}
			}
			*/
			for(int j=0; j<n; j++){
				avrgcost += nfit[j]/n;
				System.out.println("Run " + j + " : mininum cost is " + 
						new java.text.DecimalFormat("#.000").format(nfit[j]) + " consumed time is " + 
						new java.text.DecimalFormat("#.000").format(ntim[j]));
			}
			System.out.println("Average value of the best costs is " + new java.text.DecimalFormat("#.000").format(avrgcost)
					+ " and the best run is " + bestrun);
			System.out.println("FINISHED.");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
