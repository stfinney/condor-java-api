package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventShadowException extends Event {
  public EventShadowException(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
  }
}

















