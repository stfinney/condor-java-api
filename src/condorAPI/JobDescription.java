package condorAPI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

interface Attribute {
}

class Queue implements Attribute{
  int times;
  Queue(int times){
	this.times = times;
  }
  public String toString(){
	return "queue " + times;
  }
}


class Pair implements Attribute{
  String key;
  String val;
  Pair(String key, String val){
	this.key = key;
	this.val = val;
  }
  public String toString(){
	return key + " = " + val;
  }
}

/**
 * This class stands for a submit file for condor.
 * Users can create this object ether from an existing file
 * or from scratch within his/her programs.
 */

public class JobDescription {
  List<Attribute> attributes = new ArrayList<Attribute>();
  HandlerSet handlers = new HandlerSet();

  String filename; 
  boolean fileGiven = false;

  /**
   * Creates a blank job description to be filled by addXXX.
   */
  public JobDescription(){
  }

  /**
   * Creates a description file from an existing submit file.
   * @param filename filename for the submit file
   */
  public JobDescription(String filename) throws CondorException {
	if (!(new File(filename)).exists())
	  throw new CondorException("File " + filename + " does not exist");
	this.filename = filename;
	fileGiven = true;
  }

  /**
   *
   */
  public void addAttribute(String name, String val) throws CondorException {
	if (fileGiven) throw new CondorException("The submit file is given");
	attributes.add(new Pair(name, val));
  }

  /** 
   * Adds "queue TIMES" line to the submit file.
   * @param val - times to be queued
   */
  public void addQueue(int val) throws CondorException {
	if (fileGiven) throw new CondorException("The submit file is given");
	attributes.add(new Queue(val));
  }

  /** 
   * Adds "queue" line to the submit file.
   */
  public void addQueue() throws CondorException {
	if (fileGiven) throw new CondorException("The submit file is given");
	attributes.add(new Queue(1));
  }


  /**
   * Sets a handler that will be called on failure.
   * @param handler - Handler to be invoked
   */ 
  public void setHandlerOnFailure(Handler handler){
	handlers.setHandlerOnFailure(handler);
  }

  /**
   * Sets a handler that will be called on success.
   * @param handler - Handler to be invoked
   */ 
  public void setHandlerOnSuccess(Handler handler){
	handlers.setHandlerOnSuccess(handler);
  }


  /** 
   * Sets a handler for a specific kind of events
   * @param eventType - the Event Type
   * @param handler   - Handler for the event
   */
  public void setHandler(int eventType, Handler handler){
	handlers.setHandler(eventType, handler);
  }
  


  /*****************************************************************
	  private or package access functions
  *****************************************************************/

  byte [] getByteArray(){
	ByteArrayOutputStream bs = new ByteArrayOutputStream();
	PrintWriter pw = new PrintWriter(new OutputStreamWriter(bs));
	for (Attribute attr: attributes) 
	  pw.println(attr);
	pw.close();
	return bs.toByteArray();
  }

  Cluster submit(Condor condor) throws CondorException{
	String tmpFilename = null;
	InputStream is;
	try {
	  if (!fileGiven) 
		is = new ByteArrayInputStream(this.getByteArray());		
	  else 
		is = new FileInputStream(filename);
	  tmpFilename = createTmpSubmit(condor.logfile, is, condor.tmpDir);
	}
	catch (IOException e){
	  e.printStackTrace();
	  throw new CondorException(e.toString());
	}	
	Cluster cluster = condor.submitFile(tmpFilename);
	(new File(tmpFilename)).delete();
	cluster.setHandlerSet(handlers);
	return cluster;
  }

  private String createTmpSubmit(String logfile, InputStream is, File tmpDir) throws IOException {
	LineNumberReader lnr = new LineNumberReader(new InputStreamReader(is));

	File tmpFile = File.createTempFile("/tmp", ".submit", tmpDir);
	PrintWriter pw = new PrintWriter(new FileWriter(tmpFile));

	// override written things 
	pw.println("log = " + logfile);
	pw.println("log_xml = True");

	String patternString = "^(\\S+)\\s*=\\s*(\\S+)$";
	Pattern pattern = Pattern.compile(patternString);
	
	String tmp;
	while ((tmp = lnr.readLine()) != null){
	  Matcher matcher = pattern.matcher(tmp);
	  if (!matcher.matches()){
		// not match XXX = YYY form; might be comment ignore
		pw.println(tmp);
		continue;
	  }
	  String key = matcher.group(1);
	  if (key.equalsIgnoreCase("log") || key.equalsIgnoreCase("log_xml"))
		continue;
	  pw.println(tmp);
	}
	pw.close();
	return tmpFile.getAbsolutePath();
  }

  public String toString() {
	  StringBuffer sb = new StringBuffer();
	  for (Attribute attr: attributes){
		  sb.append(attr + "\n");
	  }
	  return sb.toString();
  }
  
}
