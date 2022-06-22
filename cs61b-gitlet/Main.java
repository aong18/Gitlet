package gitlet;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;

public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */

    /** Returns an initial Gitlet system. Only run if there's no
     * system in the current directory.
     */
    private static Tree init() throws ParseException {
        File directory = new File(".gitlet" + File.separator);
        if(!directory.exists()){
            directory.mkdirs();
            return Tree.init();
        }
        else {
            System.out.println("Gitlet already exists.");
        }
        return null;
    }


    private static Tree loadtree() {
        Tree test = null;
        String pathname = ".gitlet";
        pathname += File.separator;
        pathname += "path";
        File f = new File(pathname);
        if(!f.exists()){
            return null;
        }
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(f));
            test = (Tree) inp.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return test;
    }

    /** Saves a snapshot of our REPO so we can use it again.*/
    private static void savetree(Tree repo) {
        if(repo != null){
            try {
                String pathname = ".gitlet";
                pathname += File.separator;
                pathname += "path";
                File f = new File(pathname);
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
                out.writeObject(repo);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /** Performs checkout on ARGS in REPO. */
    private static void checkout(Tree repo, String...args) {
        String mid = "0";
        String pat = "--";
        if(args.length == 3){
            mid = args[1];
        }
        if(args.length == 4){
            mid = args[2];
        }
        if (args.length == 2) {
            repo.CheckoutBranch(args[1]);
        }
        if (args.length == 3 && mid.equals(pat)) {
            repo.CheckoutFile(args[2]);
        }
        if (args.length == 4 && mid.equals(pat)) {
            repo.CheckoutCommit(args[1], args[3]);
        }
    }

    /** Main, get input ARGS and does appropriate action.*/
    public static void main(String... args) throws ParseException {
        Tree repository = loadtree();
        if(args[0].equals("init")){
            repository = init();
        }
        if(args[0].equals("add")){
            assert repository != null;
            repository.add(args[1]);
        }
        if(args[0].equals("commit")){
            assert repository != null;
            repository.commit(args[1]);
        }
        if(args[0].equals("log")){
            assert repository != null;
            repository.Log();
        }
        if(args[0].equals("checkout")){
            assert repository != null;
            checkout(repository, args);
        }
        savetree(repository);
    }
}
