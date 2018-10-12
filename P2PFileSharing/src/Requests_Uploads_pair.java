public class Requests_Uploads_pair {

    private int NumOfRequests;
    private int NumOfUploads;

    Requests_Uploads_pair() {
        NumOfRequests = 0;
        NumOfUploads = 0;
    }

    public void incrementRequests() {
        NumOfRequests++;
    }

    public void incrementUploads() {
        NumOfUploads++;
    }

    public int getScore() {
        return (int)((double)NumOfUploads/NumOfRequests*100);
    }
}
