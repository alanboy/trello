import org.apache.logging.log4j.*;
import org.azd.exceptions.AzDException;
import org.azd.workitemtracking.WorkItemTrackingApi;
import org.azd.workitemtracking.types.WorkItemReference;
import org.azd.utils.AzDClientApi;
import org.azd.workitemtracking.types.WorkItem;
import javax.swing.SwingWorker;

import java.util.ArrayList;
import java.util.List;

public class AzureDevOps extends SwingWorker<Integer, Integer> {

    private Logger log;
    private String organisation = "DevMatch";
    private String personalAccessToken = "";
    private WorkItemTrackingApi wit;
    private static AzureDevOps azureDevOpsInstance;

    // *************************************************************************
    // *         Singleton Construction
    // *************************************************************************
    public static AzureDevOps GetInstance() {
        if (null == azureDevOpsInstance) {
            azureDevOpsInstance = new AzureDevOps();
        }
        return azureDevOpsInstance;
    }

    AzureDevOps() {
        this.log = LogManager.getLogger();
        log.debug("Building a new AzureDevOps client");
    }

    private void init() {
        AzDClientApi webApi = new AzDClientApi(organisation, "DevMatch", personalAccessToken);
        this.wit = webApi.getWorkItemTrackingApi();
    }

    // *************************************************************************
    // *         Worker thread (this is a new thread)
    // *************************************************************************
    protected Integer doInBackground() throws Exception {
        log.debug("Do in background");
        return null;
    }


    /**
     * Main entry point for retrieving tasks and bugs for the current sprint.
     * @return
     */
    public ArrayList<WorkItem> GetTasksAndBugs() {

        ArrayList<WorkItemReference> results = null;

        try {
            this.init();
        } catch (Exception exception) {
            this.log.error("Failed to initialize DevOps client");
            throw exception;
        }


        try {
            results = this.GetWorkItemReferenceFromQuery();
        } catch (AzDException exception) {
            this.log.error("Failed to run query on AzureDevOps");
            throw new RuntimeException("e");
        }

        try {
            return this.GetWorkItemDetails(results);

        } catch (AzDException exception) {
            this.log.error("Failed to run query on AzureDevOps");
            throw new RuntimeException("e");
        }
    }

    private ArrayList<WorkItem> GetWorkItemDetails(List<WorkItemReference> workItems) throws AzDException {
        List<Integer> ids  = new ArrayList<Integer>();
        for (WorkItemReference item : workItems) {
            ids.add(item.getId());
        }
        int[] arr = ids.stream().mapToInt(i -> i).toArray();
        return new ArrayList<WorkItem>(this.wit.getWorkItems(arr).getWorkItems());
    }

    private ArrayList<WorkItemReference> GetWorkItemReferenceFromQuery() throws AzDException {

        String query = " select                                              "
                + "     [System.Id],                                    "
                + "     [System.WorkItemType],                          "
                + "     [System.Title],                                 "
                + "     [System.State],                                 "
                + "     [Microsoft.VSTS.Common.Priority],               "
                + "     [Microsoft.VSTS.Scheduling.RemainingWork],      "
                + "     [Microsoft.VSTS.Scheduling.OriginalEstimate],   "
                + "     [System.Parent]                                 "
                + " from                                                "
                + "     WorkItems                                       "
                + " where                                               "
                + "     [System.TeamProject] = @project                 "
                + "     and ([System.WorkItemType] = 'Task' or [System.WorkItemType] = 'Bug') "
                + "     and [System.State] <> 'Removed'                 "
                + "     and [System.AssignedTo] = @me                   "
                + "     and [System.IterationPath] = @currentIteration('[DevMatch]\\DevMatch Team <id:9171bfe3-38a5-469d-bcac-80d43f331896>')              "
                + " order by [Microsoft.VSTS.Common.Priority]           ";

        return new ArrayList<WorkItemReference>(wit.queryByWiql("DevMatch Team", query).getWorkItems());

    }

}
