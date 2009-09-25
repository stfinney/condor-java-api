package samples;


import condorAPI.Cluster;
import condorAPI.Condor;
import condorAPI.CondorException;
import condorAPI.Event;
import condorAPI.Handler;
import condorAPI.Job;
import condorAPI.JobDescription;

public class NonBlockingStatusCheck {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws CondorException{
		Condor.setDebug(true);
		Condor condor = new Condor();
		
		JobDescription jd2 = new JobDescription();
		jd2.addAttribute("executable", "/bin/date");
		jd2.addAttribute("universe", "vanilla");
		jd2.addQueue();
		jd2.setHandlerOnSuccess(new Handler(){
		  public void handle(Event e){
			System.err.println("success " + e);
		  }
		});

		Cluster c2 = condor.submit(jd2);
		System.out.println("submitted");
		Job j = c2.getJob(0);

		while (! j.isCompleted()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		System.out.println("done");	
	}

}

