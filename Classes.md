List of classes.

| Condor |  This class represents the Condor system itself. |
|:-------|:-------------------------------------------------|
| JobDescription | This class represents job submission description. Users can create instances of this class from Condor submit files, or compose them inside the program. |
| Cluster | This class stands for a job cluster that will be created by submitting a job description to the condor system. |
| Job    | This class stands for each job in a job cluster. |
| Event  | This class stands for 'events' jobs will get. When a job get submitted, evicted, termintated, the job will get events. |
| Handler | This interface is a template for handlers for events. Users can implements handlers to handle the events above. |