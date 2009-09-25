package condorAPI.event;
import condorAPI.*;

import condor.classad.*;

public class EventJobAborted extends Event {
	public String reason = "";
	
	public EventJobAborted(RecordExpr expr){
		super(expr);
		type = ((Constant)expr.lookup("EventTypeNumber")).intValue();
		Constant rc = (Constant)expr.lookup("Reason");
		if (rc != null) {
			reason = rc.stringValue();
		}
	}
}

















