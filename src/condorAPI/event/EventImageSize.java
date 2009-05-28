package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventImageSize extends Event {
  public int size;
  public EventImageSize(RecordExpr expr){
	super(expr);
	type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
	size = ((Constant)expr.lookup("Size")).intValue();
  }
}

















