package gitlet;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.*;
import java.text.SimpleDateFormat;
import java.nio.file.Paths;
import java.nio.file.Path;

public class Commit implements Serializable {
    private Map<String, String> filePointers;
    private Map<String, String> addedFiles;
    private String msg;
    private Date time;
    private String commitdir;
    private Commit parent;
    private String id;
    private boolean merged = false;
    private String parents;
    public String getId() {
        return id;
    }
    public Commit(Stage snapshot) throws ParseException {
        addedFiles = new HashMap<>();
        filePointers = new HashMap<>();
        if (snapshot != null) {
            parent = snapshot.getCurrentcommit();
            time = new Date();
        } else {
            String date = "Wed Dec 31 16:00:00 1969 -0800";
            SimpleDateFormat dt = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
            time = dt.parse(date);
        }
    }


    public Commit(Stage snap,  String message) throws ParseException {
        this(snap);
        msg = message;
        if (snap == null) {
            String time = getTime().toString();
            id = Utils.sha1(time, getMsg());
        } else {
            String parentid = getParent().getId();
            String time = getTime().toString();
            String addedfile = snap.getAddedtostage().toString();
            id = Utils.sha1(addedfile, parentid, getMsg(), time);
        }
        committodir(snap);
        if (!samecommit()) {
            commitdir = ".gitlet";
            commitdir += File.separator;
            commitdir += "objects";
            commitdir += File.separator;
            commitdir += getId();
            commitdir += File.separator;
            save();
        } else {
            System.out.println("No changes added to the commit.");
        }
    }

    public boolean samecommit() {
        if (parent == null || (parent.getAddedFiles().size() != getAddedFiles().size())) {
            return false;
        }
        for (String name : addedFiles.keySet()) {
            if(!parent.getAddedFiles().contains(name)){
                return false;
            }
            if (parent.getAddedFiles().contains(name)) {
                return filePointers.get(name).equals(parent.getFilePointers().get(name));
            }
        }
        return true;
    }

    public void committodir(Stage snapshot) {
        if (snapshot != null) {
            for (String fileName : snapshot.getStagedFiles()) {
                File f = new File(fileName);
                String id2 = Utils.sha1(Utils.readContentsAsString(f));
                if (filePointers.containsKey(fileName)) {
                    String id1 = filePointers.get(fileName);
                    shacompare(id1, id2, fileName);
                } else {
                    addedFiles.put(fileName, id);
                    filePointers.put(fileName, id2);
                }
            }
        }
    }
    public void shacompare(String id1, String id2, String fileName){
        boolean ans;
        if(id.equals(id2)){
            ans = true;
        } else{
            ans = false;
        }
        if (!ans) {
            addedFiles.put(fileName, id);
            filePointers.replace(fileName, id2);
        }
    }

    public void checkoutwithfile(String fileName) {
        File f = new File(fileName);
        String pathname = commitdir;
        pathname += filePointers.get(fileName);
        pathname += File.separator;
        pathname += f;
        File dir = new File(pathname);
        try {
            Utils.writeContents(f, Utils.readContentsAsString(dir));
        } catch (GitletException e) {
            System.out.println(e.getMessage());
        }
    }


    public void checkout() {
        for (String item : addedFiles.keySet()) {
            checkoutwithfile(item);
        }
        File gitlet = new File(commitdir);
        File x = new File(gitlet.getAbsolutePath());
        File[] files = x.listFiles();
        assert files != null;
        for (File f: files) {
            String filename = f.getName();
            if (!addedFiles.containsKey(filename)) {
                Utils.restrictedDelete(f);
            }
        }
    }

    public void save() {
        File dir = new File(commitdir);
        dir.mkdirs();
        if(!addedFiles.isEmpty()){
            for (String name : addedFiles.keySet()) {
                String path = commitdir;
                path += filePointers.get(name);
                path += File.separator;
                path += name;
                Path pa = Paths.get(path);
                try {
                    Files.createDirectories(pa.getParent());
                    Files.createFile(pa);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                String contents = Utils.readContentsAsString(new File(name));
                Utils.writeContents(pa.toFile(), contents);
                addedFiles.put(name, commitdir);
            }
        }
    }



    public boolean contains(String fileName) {
        if(addedFiles != null){
            if(addedFiles.containsKey(fileName)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder test = new StringBuilder();
        buildstring(test);
        SimpleDateFormat dt = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss yyyy Z");
        String timeStamp = dt.format(time);
        String newline = "\n";
        test.append("Date: ").append(timeStamp).append(newline);
        test.append(msg);
        return test.toString();
    }
    public void buildstring(StringBuilder str){
        String newline = "\n";
        str.append("===");
        str.append(newline);
        str.append("commit ");
        str.append(id);
        str.append(newline);
        while(merged){
            str.append(parents);
            str.append(newline);
        }
    }

    public Set<String> getAddedFiles() {
        return addedFiles.keySet();
    }
    public String getMsg() {
        return msg;
    }
    public Date getTime() {
        return time;
    }
    public Map<String, String> getFilePointers() {
        return filePointers;
    }
    public Commit getParent() {
        return parent;
    }

}
