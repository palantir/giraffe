package com.palantir.giraffe.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A {@link PrintWriter} that defines additional methods for creating shell
 * scripts.
 *
 * @author bkeyes
 */
public class ScriptWriter extends PrintWriter {

    public ScriptWriter(File file, Charset cs) throws FileNotFoundException {
        super(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), cs)));
    }

    public void createFile(String path) {
        printf("touch %s%n", path);
    }

    public void createFile(String path, int octalPerms) {
        createFile(path);
        setPermissions(path, octalPerms);
    }

    public void createFile(String path, String contents) {
        printf("printf %%s '%s' > %s%n", contents, path);
    }

    public void createFile(String path, String contents, int octalPerms) {
        createFile(path, contents);
        setPermissions(path, octalPerms);
    }

    public void createDir(String path) {
        printf("mkdir %s%n", path);
    }

    public void setModifiedTime(String path, Date time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm.ss");
        printf("touch -m -t %s %s%n", dateFormat.format(time), path);
    }

    public void setPermissions(String path, int octalPerms) {
        printf("chmod %o %s%n", octalPerms, path);
    }

    public void createSymlink(String link, String target) {
        printf("ln -s %s %s%n", target, link);
    }

}
