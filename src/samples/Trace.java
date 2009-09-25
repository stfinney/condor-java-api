package samples;


import condorAPI.Condor;
import condorAPI.CondorException;
import condorAPI.Job;

public class Trace {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws CondorException{
		Condor.setDebug(true);
		Condor condor = new Condor(true);
		

		for (int i = 0; i < 10; i++) {
			System.out.println("go sleep");
			try {
				Thread.sleep(10000); // sleep 10 sec
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			System.out.println("wakeup");
			
			for (Job j: condor.getAliveJobs()){
				System.out.println(j.getCommand());
				condor.rm(j);
				System.out.println("removing " + j);
			}
		}
	}
}

