package samples;


import condorAPI.Cluster;
import condorAPI.Condor;
import condorAPI.CondorException;
import condorAPI.Event;
import condorAPI.Handler;
import condorAPI.Job;
import condorAPI.JobDescription;

public class Remove {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws CondorException{
		Condor.setDebug(true);
		Condor condor = new Condor("/tmp/condor/log");
		condor.setTmpDir("/tmp/condor");
		
		JobDescription jd2 = new JobDescription();
		jd2.addAttribute("executable", "/bin/date");
		jd2.addAttribute("universe", "vanilla");
		jd2.addQueue(30);
		jd2.setHandlerOnSuccess(new Handler(){
		  public void handle(Event e){
			System.err.println("success " + e);
		  }
		});

		Cluster c2 = condor.submit(jd2);
		System.out.println("submitted");
		System.out.println("go sleep");
		try {
			Thread.sleep(20000); // sleep 10 sec
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		System.out.println("wakeup");
		
		for (Job j: c2){
			if (j.isHeld() || j.isIdle()) { 
				System.out.println(j.getCommand());
				condor.rm(j);
				System.out.println("removing " + j);
			}
		}
		System.out.println("done");
	}
}

