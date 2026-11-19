package com.logan.formstrength;

import java.io.*;
import java.nio.charset.StandardCharsets;

/** Bounded app-private export snapshots. Saved instance state carries only the filename. */
final class BackupFiles {
    static final int MAX_BYTES=5*1024*1024;
    private static File file(File directory,String name) {
        if(name==null||!name.matches("export-[a-f0-9-]{36}\\.json"))throw new IllegalArgumentException("Invalid pending backup.");
        return new File(directory,name);
    }
    static boolean validName(String name){return name!=null&&name.matches("export-[a-f0-9-]{36}\\.json");}
    static void create(File directory,String name,String json)throws IOException {
        byte[] bytes=json.getBytes(StandardCharsets.UTF_8);
        if(bytes.length>MAX_BYTES)throw new IllegalArgumentException("Maximum backup size is 5 MB.");
        if(!directory.isDirectory()&&!directory.mkdirs())throw new IOException("Cannot prepare backup.");
        File target=file(directory,name),temporary=new File(target.getPath()+".tmp");
        try{try(FileOutputStream out=new FileOutputStream(temporary)){out.write(bytes);out.getFD().sync();}
            if(!temporary.renameTo(target))throw new IOException("Cannot prepare backup.");
        }finally{temporary.delete();}
    }
    static void copyTo(File directory,String name,OutputStream out)throws IOException {
        try(InputStream in=new FileInputStream(file(directory,name))){copyBounded(in,out);}
    }
    static String read(InputStream in)throws IOException {
        try(ByteArrayOutputStream out=new ByteArrayOutputStream()){copyBounded(in,out);return out.toString("UTF-8");}
    }
    private static void copyBounded(InputStream in,OutputStream out)throws IOException {
        if(in==null||out==null)throw new IOException("Cannot open backup file.");
        byte[] bytes=new byte[8192];int total=0,n;
        while((n=in.read(bytes))!=-1){if(total>MAX_BYTES-n)throw new IllegalArgumentException("Maximum backup size is 5 MB.");out.write(bytes,0,n);total+=n;}
    }
    static void delete(File directory,String name){if(validName(name)){File f=file(directory,name);f.delete();new File(f.getPath()+".tmp").delete();}}
    static void prune(File directory,String keep,long now){File[] files=directory.listFiles();if(files==null)return;for(File f:files){String n=f.getName();String base=n.endsWith(".tmp")?n.substring(0,n.length()-4):n;if(validName(base)&&!base.equals(keep)&&now-f.lastModified()>24L*60*60*1000)f.delete();}}
}
