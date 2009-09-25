package samples;

import condorAPI.Cluster;
import condorAPI.Condor;
import condorAPI.CondorException;
import condorAPI.Event;
import condorAPI.Handler;
import condorAPI.JobDescription;

public class SubmitExistingFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws CondorException {
		// TODO Auto-generated method stub
		Condor.setDebug(true);
		Condor condor = new Condor();

		JobDescription jd = new JobDescription("/tmp/test.submit");
		jd.setHandlerOnSuccess(new Handler(){
		  public void handle(Event e){
			System.err.println("success " + e);
		  }
		});

		Cluster c = condor.submit(jd);
		System.out.println("submitted");

		condor.rm(c);
		c.waitFor();
		System.out.println("done");
	}

}
