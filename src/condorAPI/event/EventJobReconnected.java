package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventJobReconnected extends Event {
  public EventJobReconnected(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















