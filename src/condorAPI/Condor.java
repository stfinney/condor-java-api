package condorAPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import condor.classad.ClassAdParser;
import condor.classad.Constant;
import condor.classad.Expr;
import condor.classad.ListExpr;
import condor.classad.RecordExpr;

/**
 * This class abstracts the condor system 
 */
public class Condor implements Runnable{
	static final String SUBMIT_OUTPUT_PATTERN =
		"(\\d*) job\\(s\\) submitted to cluster (\\d*)\\.";
	
	static final String CONDOR_SUBMIT     = "condor_submit";
	static final String CONDOR_RM         = "condor_rm";
	static final String CONDOR_STATUS     = "condor_status";
	static final String CONDOR_Q          = "condor_q";
	static final String CONDOR_RESCHEDULE = "condor_reschedule";
	static final String CONDOR_DEFAULT_LOG = "log";
	static final int    CONDOR_DEFAULT_INTERVAL = 5;
	
	static boolean debug = false;
	
	protected Map<JobId, Job> map = new HashMap<JobId, Job>();
	protected Map<String, LogMonitor> monitors = new HashMap<String, LogMonitor>();
	protected String logfile;
	protected boolean traceAll = false;
	protected File tmpDir = null; 
	
	/**********************************************************************
	 *                         PUBLIC FUNCTIONS
	 **********************************************************************/
	
	/**
	 * set debug flag
	 * @param debug set this true to enable debug output
	 */
	public static void setDebug(boolean debug){
		Condor.debug = debug;
	}
	

	/**
	 * Creates a condor instance
	 */
	public Condor(){
		this(CONDOR_DEFAULT_LOG, false);
	}

	public Condor(String logname) {
		this(logname, false);
	}

	public Condor(boolean traceAll) {
		this(CONDOR_DEFAULT_LOG, traceAll);
	}
	
	public Condor(String logname, boolean traceAll){
		this.traceAll = traceAll;
		setLogFile(logname, CONDOR_DEFAULT_INTERVAL);  
	}
	
	
  /**
   * 
   */
  public void setTmpDir(String tmpDirStr){
	  this.tmpDir = new File(tmpDirStr);
  }
  
  
  /**
   * just set interval
   * @param sec - interval in seconds.
   */
  public void setInterval(double sec){
	  setLogFile(logfile, sec);
  }

  /**
   *  Sends a 'reschedule' signal to make rescheduling process happens.
   *  Simply invokes 'condor_reschedule '
   */
  public void reschedule() throws CondorException {
	Runtime runtime = Runtime.getRuntime();
	try {
	  Process proc = runtime.exec(new String[]{CONDOR_RESCHEDULE});
	  proc.waitFor();
	} catch (IOException e){
	  throw new CondorException(e.toString());
	} catch (InterruptedException e){
	}

  }

  /**
   *  Removes a job.
   *  This method will block till the removal finishes.
   *  Just invokes 'condor_rm'
   *  @param job - a Job to remove 
   */
  public void rm(Job job) throws CondorException {
	Runtime runtime = Runtime.getRuntime();
	try {
	  Process proc = runtime.exec(new String[]{CONDOR_RM, job.jobId.toString()});
	  proc.waitFor();
	} catch (IOException e){
	  throw new CondorException(e.toString());
	} catch (InterruptedException e){
	}
  }

  /** 
   * Removes jobs in a cluster.
   * This method will block till the removal finishes.
   * Just invokes 'condor_rm'
   * @param cluster - A Cluster to remove
   */
  public void rm(Cluster cluster) throws CondorException {
	Runtime runtime = Runtime.getRuntime();
	try {
	  Process proc = runtime.exec(new String[]{CONDOR_RM, "" + cluster.id});
	  proc.waitFor();
	} catch (IOException e){
	  throw new CondorException(e.toString());
	} catch (InterruptedException e){
	}
  }

  /**
   * Sets logfile name and monitor interval.
   * @param logfile - log file name to monitor
   * @param interval - interval to monitor in seconds 
   */
  public void setLogFile(String logfile, double interval){
	this.logfile = logfile;
	LogMonitor monitor;
	if ((monitor = (LogMonitor)monitors.get(logfile)) == null){
	  monitor = new LogMonitor(this, logfile, interval);
	  monitor.start();
	  monitors.put(logfile, monitor);
	} else {
	  // already have the monitor, just change the interval
	  monitor.setInterval(interval);
	}
  }


  /**
   * Submits a job description to the condor and returns a cluster of job 
   * @param jd - JobDescription to submit
   */ 
  public Cluster submit(JobDescription jd) throws CondorException{
	return jd.submit(this);
  }

  /**
   * Lookups job map with jobId and returns a Job.
   * If there is no job corresponds to the jobId registered, create it.
   * This is useful if you want to keep track of a job
   * that is not submitted by the API.
   * @param jobId - JobId to lookup
   */
  synchronized 
  public Job getJob(JobId jobId){
	Job job = (Job)map.get(jobId);
	if (job == null) {
	  job = new Job(jobId, this);
	  jobSubmitted(job);
	}
	return job;
  }


  /**
   * getAliveJobs:
   * returns alive (not completed and not removed) jobs
   */
  
  synchronized
  public Collection<Job> getAliveJobs(){
	  Set<Job> result = new HashSet<Job>();
	  for (Job job: map.values())
		  if (!job.isCompleted() && !job.isRemoved())
			  result.add(job);
	  return result;
  }

  /**********************************************************************
   *                         PACKAGE FUNCTIONS
   **********************************************************************/
  synchronized 
  void informEvent(Event e){
	Job job = (Job)map.get(e.jobId);
	if (job == null && traceAll) {
		job = new Job(e.jobId, this);
		map.put(e.jobId, job);
	}
	if (job != null){
	  e.job = job;
	  if (debug)
		System.err.println(e);
	  job.event(e);
	} 
  }

  
  /**
   *     tries to get job.command
   *     with 'condor_q -xml'
   */
  synchronized 
  void updateJobStatus() throws CondorException{
	Runtime runtime = Runtime.getRuntime();
	try {
	  Process proc = runtime.exec(new String[]{CONDOR_Q, "-xml"});
	  LineNumberReader lnr = 
		new LineNumberReader(new InputStreamReader(proc.getInputStream()));
	  for (int i = 0; i < 5; i++)
		lnr.readLine(); // skip 
	  ClassAdParser parser = new ClassAdParser(lnr, ClassAdParser.XML);
	  Expr expr = parser.parse();
	  ListExpr list = (ListExpr)expr;
	  Iterator iter = list.iterator();
	  while (iter.hasNext()){
		RecordExpr tmp = (RecordExpr)iter.next();
		
		int clusterId = ((Constant)(tmp.lookup("ClusterId"))).intValue();
		int procId = ((Constant)(tmp.lookup("ProcId"))).intValue();
		String cmd = ((Constant)(tmp.lookup("Cmd"))).stringValue();
		
		Job j = map.get(new JobId(clusterId, procId));
		if (j != null) 
			j.setCommand(cmd);
	  }
	} catch (IOException e){
	  throw new CondorException(e.toString());
	}
  }

  synchronized 
  Cluster submitFile(String filename) throws CondorException {
	Runtime runtime = Runtime.getRuntime();
	try {
	  Process proc = runtime.exec(new String[]{CONDOR_SUBMIT, filename});
	  int exitCode = 0;
	  while (true){
		try {
		  exitCode = proc.waitFor(); break;
		} catch (InterruptedException e){}
	  }
	  LineNumberReader lnr = 
		new LineNumberReader(new InputStreamReader(proc.getInputStream()));
	  if (exitCode != 0) { // failed
		StringBuffer sb = new StringBuffer();
		String tmp;
		while ((tmp = lnr.readLine()) != null)
		  sb.append(tmp + "\n");
		lnr = 
		  new LineNumberReader(new InputStreamReader(proc.getErrorStream()));
		while ((tmp = lnr.readLine()) != null)
		  sb.append(tmp + "\n");
		throw new CondorException(sb.toString());
	  }
	  String line;
	  line = lnr.readLine(); // skip 'Submitting job(s).'
	  //line = lnr.readLine(); // skip 'Logging submit event(s).'
	  line = lnr.readLine(); // read 'X job(s) submitted to cluster XXX'.
	  Pattern pattern = Pattern.compile(SUBMIT_OUTPUT_PATTERN);
	  Matcher matcher = pattern.matcher(line);
	  if (!matcher.matches()){
		throw new CondorException("faied to parse the cluster number.");
	  }
	  int noJobs = Integer.parseInt(matcher.group(1));
	  int clusterId = Integer.parseInt(matcher.group(2));
	  Cluster cluster = new Cluster(clusterId, noJobs, this);
	  Iterator<Job> iterator = cluster.iterator();
	  while (iterator.hasNext()){
		Job job = (Job)iterator.next();
		this.jobSubmitted(job);
	  }
	  return cluster;
	} catch (IOException e){
	  throw new CondorException(e.toString());
	}
  }

  int activeJobCounter = 0;

  synchronized void jobSubmitted(Job job){
	map.put(job.jobId, job);
	activeJobCounter++;
	if (dummyThread == null){
	  dummyThread = new Thread(this, "dummyThread");
	  dummyThread.start();
	}
  }

  synchronized void jobTerminated(Job job){
	activeJobCounter--;
	notifyAll();
  }

  Thread dummyThread; // to keep the VM alive while active job(s) exist.

  /**
   * Just to implement Runnable, ignore.
   */
  public void run(){
	synchronized(this){
	  while (activeJobCounter > 0){
		try {wait();}catch (InterruptedException e){}
	  }
	}
	dummyThread = null; // clear reference for myself
  }


  /**
   * Test main method
   */
  public static void main(String [] args) throws CondorException{
	Condor.setDebug(true);
	Condor condor = new Condor();

	JobDescription jd = new JobDescription("test.submit");
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
	//	c2.waitFor();
	System.out.println("done");
  }
}



