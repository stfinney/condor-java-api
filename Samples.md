Here is some sample programs to give you the idea.

# Sample 1 #
Here, we show a simple usage of the API. This program submits a jobcluster using an existing submission file and wait for the cluster to complete its execution and print out the status of the jobs in the cluster.
```
import condorAPI.*;

public class TestSimple{

  public static void main(String [] args) 
  throws CondorException{
    // create a Condor object
    Condor condor = new Condor();

    // create a JobDescription object using exsiting file 
    // 'test.submit'
    JobDescription jd = new JobDescription("test.submit");

    // submit the jobDescription and get Cluster
    Cluster c = condor.submit(jd);

    System.out.println("submitted " + c);

    // wait for done
    c.waitFor();

    // print out cluster status
    System.out.println(c.dump());
  }
}
```
# Sample 2 #
Here is another sample. this code uses a handler to monitor a job.
```
import condorAPI.*;

public class TestSimple2{
  
  public static void main(String [] args) throws CondorException{
	// create a Condor object
	Condor condor = new Condor();

	// create a JobDescription object using exsiting file 
	// 'test.submit'
	JobDescription jd = new JobDescription("test.submit");

	// create a handler as inline object.
	Handler handler = new Handler(){
	  public void handle(Event e){
		System.out.println("got event: " + e);
		System.out.println(e.getJob());
	  }
	};

	jd.setHandler(EventType.SUBMIT, handler);
	jd.setHandler(EventType.EXECUTE, handler);
	jd.setHandler(EventType.JOB_TERMINATED, handler);
	jd.setHandler(EventType.JOB_EVICTED, handler);
	jd.setHandler(EventType.JOB_ABORTED, handler);

	// submit the job and get Cluster
	Cluster c = condor.submit(jd);

	System.out.println("submitted " + c);
  }
}
```

# Sample 3 #
This code shows usage of handler for success and failure.
```
import condorAPI.*;

public class TestSimple3{
  
  public static void main(String [] args) throws CondorException{
	// create a Condor object
	Condor condor = new Condor();

	// create a JobDescription object using exsiting file 
	// 'test.submit'
	JobDescription jd = new JobDescription("test.submit");

	jd.setHandlerOnSuccess(new Handler(){
	  public void handle(Event e){
		System.out.println(e.getJobId() + " success");
	  }
	});

	jd.setHandlerOnFailure(new Handler(){
	  public void handle(Event e){
		System.out.println(e.getJobId() + " failed");
	  }
	});

	// submit the job and get Cluster
	Cluster c = condor.submit(jd);

	System.out.println("submitted " + c);
  }
}
```
# Sample 4 #
This code will 'attach' an exisiting job that is submitted by some other way, such as condor\_submit.
```
import condorAPI.*;

public class TestSimple4{
  
  public static void main(String [] args) throws CondorException{
	// create a Condor object
	Condor condor = new Condor();

	// create a job from id
	Job job = 
	  condor.getJob(new JobId(Integer.parseInt(args[0]), 
                                  Integer.parseInt(args[1])));

	// start to monitor a log file
	condor.setLogFile("log.other", 5);

	job.setHandlerOnSuccess(new Handler(){
	  public void handle(Event e){
		System.out.println(e.getJobId() + " success");
	  }
	});

	job.setHandlerOnFailure(new Handler(){
	  public void handle(Event e){
		System.out.println(e.getJobId() + " failed");
	  }
	});
  }
}
```
# Sample 5 #
This code is equivalent to the fist one, except the way to create a JobDescription.
```
import condorAPI.*;

public class TestSimple5{
  
  public static void main(String [] args) 
  throws CondorException{
	// create a Condor object
	Condor condor = new Condor();

	// create a JobDescription object in the code
	JobDescription jd = new JobDescription();
	jd.addAttribute("executable", "/bin/date");
	jd.addAttribute("universe", "vanilla");
	jd.addQueue();

	// submit the job and get Cluster
	Cluster c = condor.submit(jd);

	System.out.println("submitted " + c);

	// print out cluster
	System.out.println(c.dump());

	// wait for done
	c.waitFor();

	// print out cluster
	System.out.println(c.dump());

	System.out.println("done");
  }
}

```