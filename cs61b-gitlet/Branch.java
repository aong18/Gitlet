package gitlet;
import java.io.Serializable;
import java.io.File;
import java.text.ParseException;

class Branch implements Serializable {
    private String branchname;
    private Commit latestCommit;
    private Stage currentstage;

    public Branch(String b, Commit head) {
        branchname = b;
        latestCommit = head;
        currentstage = new Stage(head);
    }
    public void commit(String message) throws ParseException {
        latestCommit = new Commit(currentstage, message);
        currentstage = new Stage(latestCommit);
    }

    public void stageFile(String n) {
        currentstage.add(n);
    }
    public void remove(String n) {
        currentstage.remove(n);
    }
    public Commit getLatestCommit() {
        return latestCommit;
    }

}
