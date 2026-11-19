package com.logan.formstrength;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

public final class BackupFilesTest {
    private static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
    interface Work {void run()throws Exception;}
    private static void rejects(Work w)throws Exception{try{w.run();throw new AssertionError("Expected bound/path failure");}catch(IllegalArgumentException expected){}}
    public static void main(String[] args)throws Exception {
        File directory=Files.createTempDirectory("form-backup-test").toFile();
        String name="export-01234567-89ab-cdef-0123-456789abcdef.json",raw="{\"name\":\"దాల్\",\"logs\":[]}";
        try {
            BackupFiles.create(directory,name,raw);
            // A recreated activity needs only directory+name, never the large Bundle payload.
            ByteArrayOutputStream output=new ByteArrayOutputStream();BackupFiles.copyTo(directory,name,output);
            check(raw.equals(output.toString("UTF-8")),"Snapshot survives loss of in-memory export string");
            check(raw.equals(BackupFiles.read(new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)))),"UTF-8 import roundtrip");
            rejects(()->BackupFiles.create(directory,"../outside.json",raw));
            rejects(()->BackupFiles.copyTo(directory,"../../outside",output));
            byte[] tooLarge=new byte[BackupFiles.MAX_BYTES+1];Arrays.fill(tooLarge,(byte)'a');
            rejects(()->BackupFiles.read(new ByteArrayInputStream(tooLarge)));
            rejects(()->BackupFiles.create(directory,name,new String(tooLarge,StandardCharsets.UTF_8)));
            ByteArrayOutputStream afterFailure=new ByteArrayOutputStream();BackupFiles.copyTo(directory,name,afterFailure);
            check(raw.equals(afterFailure.toString("UTF-8")),"Rejected replacement preserves previous valid export snapshot");
            byte[] exact=new byte[BackupFiles.MAX_BYTES];Arrays.fill(exact,(byte)'a');
            check(BackupFiles.read(new ByteArrayInputStream(exact)).length()==BackupFiles.MAX_BYTES,"Exact size cap accepted");
            File retained=new File(directory,name);retained.setLastModified(1);BackupFiles.prune(directory,name,System.currentTimeMillis());check(retained.exists(),"Pending picker snapshot retained");
            BackupFiles.prune(directory,null,System.currentTimeMillis());check(!retained.exists(),"Old unreferenced snapshots pruned");
            BackupFiles.create(directory,name,raw);BackupFiles.delete(directory,name);check(!retained.exists(),"Successful/cancelled snapshot cleaned");
            check(directory.list().length==0,"No temporary files left");
            System.out.println("Passed pending-backup snapshot recreation, UTF-8/5MB bounds, path guard and cleanup.");
        } finally {for(File f:directory.listFiles())f.delete();directory.delete();}
    }
}
