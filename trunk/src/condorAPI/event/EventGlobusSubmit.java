package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventGlobusSubmit extends Event {
  public EventGlobusSubmit(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















