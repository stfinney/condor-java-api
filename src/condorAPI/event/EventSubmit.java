package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventSubmit extends Event {
  public EventSubmit(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















