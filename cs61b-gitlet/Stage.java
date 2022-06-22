package gitlet;

import java.io.Serializable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

class Stage implements Serializable {
    private Commit currentcommit;

    private ArrayList<String> addedtostage;

    private ArrayList<String> filestoremove;

    private ArrayList<String> filesstaged;

    public Commit getCurrentcommit() {
        return currentcommit;
    }

    public ArrayList<String> getStagedFiles() {
        return filesstaged;
    }
    public ArrayList<String> getAddedtostage() {
        return addedtostage;
    }
    public ArrayList<String> getFilestoremove() {
        return filestoremove;
    }

    public Stage(Commit latest) {
        currentcommit = latest;
        addedtostage = new ArrayList<String>();
        filesstaged = new ArrayList<String>();
        filestoremove = new ArrayList<String>();
        if (latest.getAddedFiles() != null) {
            filesstaged.addAll(latest.getAddedFiles());
        }
    }

    public void add(String fileName) {
        File f = new File(fileName);
        if(f.exists()){
            if(thesame(fileName)){
                filesstaged.remove(fileName);
            }
            if (!thesame(fileName)) {
                addedtostage.add(fileName);
                filesstaged.add(fileName);
            }
            filestoremove.remove(fileName);
        } else{
            System.out.println("File doesn't exist");
        }
    }

    public void remove(String fileName) {
        boolean ans = false;
        if(filesstaged.contains(fileName)){
            ans = true;
        }
        if(addedtostage.contains(fileName)){
            ans = true;
        }
        if (ans) {
            if (currentcommit.contains(fileName)) {
                Utils.restrictedDelete(fileName);
                filestoremove.add(fileName);
            }
            filesstaged.remove(fileName);
            addedtostage.remove(fileName);
        }
    }

    private boolean thesame(String fileName) {
        File f = new File(fileName);
        String SHAID = Utils.sha1((Utils.readContentsAsString(f)));
        String saved;
        if(!currentcommit.contains(fileName)){
            saved = null;
        } else{
            saved = currentcommit.getFilePointers().get(fileName);
        }
        return SHAID.equals(saved);
    }
}
