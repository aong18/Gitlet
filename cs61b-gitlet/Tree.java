package gitlet;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.util.Set;

public class Tree implements Serializable {
    private Branch currentbranch;
    private Map<String, Commit> shatocommit;
    private HashMap<String, String> sha;
    private Map<String, Branch> nametobranch;
    private Map<String, ArrayList<String>> messagetosha;

    public Tree() {
        sha = new HashMap<>();
        nametobranch = new HashMap<>();
        shatocommit = new HashMap<>();
        messagetosha = new HashMap<>();
    }
    public static Tree init() throws ParseException {
        Tree temp = new Tree();
        ArrayList<String> ids = new ArrayList<>();
        Commit init = new Commit(null, "initial commit");
        Branch b = new Branch("master", init);
        if(init.getId() != null){
            ids.add(init.getId());
        }
        setup(temp, "initial commit", ids, b, "master", init);
        return temp;
    }
    public static void setup(Tree t, String m, ArrayList<String> i, Branch b, String branchname, Commit init){
        t.messagetosha.put(m, i);
        t.currentbranch = b;
        t.nametobranch.put(branchname, b);
        t.shatocommit.put(init.getId(), init);
    }

    public void CheckoutBranch(String nameofbranch) {
        for (String file : currentbranch.getLatestCommit().getAddedFiles()) {
            if(!shacompare(file, currentbranch)){
                System.out.println("Untracked file found.");
            }
            Utils.restrictedDelete(file);
        }
        currentbranch = nametobranch.get(nameofbranch);
        currentbranch.getLatestCommit().checkout();
    }
    public static boolean shacompare(String file, Branch currentbranch){
        String saved = currentbranch.getLatestCommit().getFilePointers().get(file);
        String curr = Utils.sha1(Utils.readContentsAsString(new File(file)));
        return saved.equals(curr);
    }

    public void CheckoutCommit(String commitID, String fileName) {
        Commit c = shatocommit.get(commitID);
        if(c.contains(fileName)){
            c.checkoutwithfile(fileName);
        }
        if (!c.contains(fileName)) {
            System.out.println("File does not exist in that commit.");
        }
    }

    public void CheckoutFile(String fileName) {
        if (!currentbranch.getLatestCommit().contains(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        currentbranch.getLatestCommit().checkoutwithfile(fileName);
    }

    public void Log() {
        Commit curr = currentbranch.getLatestCommit();
        if(curr != null){
            while (curr != null) {
                System.out.println(curr.toString());
                System.out.println();
                curr = curr.getParent();
            }
        }
    }

    public void add(String name) {
        if (name != null) {
            currentbranch.stageFile(name);
        }
    }

    public void commit(String message) throws ParseException {
        currentbranch.commit(message);
        Commit current = currentbranch.getLatestCommit();
        shatocommit.put(current.getId(), currentbranch.getLatestCommit());
        ArrayList<String> temp = messagetosha.get(message);
        if(messagetosha.get(message) != null){
            temp = messagetosha.get(message);
        } else{
            temp = new ArrayList<>();
        }
        temp.add(current.getId());
        messagetosha.put(message, temp);
        sha.put(current.getId(), current.getId());
    }
}
