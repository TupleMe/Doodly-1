package tuple.me.dtools.file.dup;

import android.util.Pair;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tuple.me.dtools.file.SystemFile;
import tuple.me.lily.Contexter;
import tuple.me.lily.util.FileUtils;
import tuple.me.lily.util.SafeClose;
import tuple.me.lily.model.Item;

public class DuplicateFinderUtil {

    public static Pair<ArrayList<Item>, Map<String, Set<File>>> findAllDuplicates() throws Exception {
        HashMap<String, Set<String>> hashVsDupMap = new HashMap<>();
        List<String> dirs = FileUtils.getStorageDirectories(Contexter.getAppContext());
        for (String dir : dirs) {
            calculateHash(dir, hashVsDupMap);
        }
        ArrayList<Item> result = new ArrayList<>();
        for (Map.Entry<String, Set<String>> dups : hashVsDupMap.entrySet()) {
            if (dups.getValue().size() > 1) {
                Iterator<String> dupFilePaths = dups.getValue().iterator();
                result.ensureCapacity(result.size() + dups.getValue().size() + 2);
                DuplicateFileHeader header = new DuplicateFileHeader("");
                result.add(header);
                long size = 0;
                while (dupFilePaths.hasNext()) {
                    String filePath = dupFilePaths.next();
                    File file = new File(filePath);
                    SystemFile systemFile = new SystemFile(file);
                    result.add(systemFile);
                    size += systemFile.size;
                }
                header.header = FileUtils.getReadableFileSize(size);
            }
        }
        return new Pair<>(result, null);
    }

    private static void calculateHash(String dir, HashMap<String, Set<String>> hashVsDupMap) throws Exception {
        File file = new File(dir);
        if (file.exists() && file.canRead()) {
            if (file.isFile()) {
                MessageDigest complete = MessageDigest.getInstance("SHA-1");
                byte[] buffer = new byte[16384];
                FileInputStream fis = new FileInputStream(file);
                int numRead = fis.read(buffer);
                if (numRead > 0 && file.length() > 1024 * 4) {
                    complete.update(buffer, 0, numRead);
                    SafeClose.safeClose(fis);
                    byte[] digest = complete.digest();
                    StringBuilder sb = new StringBuilder(digest.length);
                    for (byte aDigest : digest) {
                        sb.append(Integer.toString((aDigest & 0xff) + 0x100, 16).substring(1));
                    }
                    String hash = sb.toString();
                    if (hashVsDupMap.containsKey(hash)) {
                        hashVsDupMap.get(hash).add(file.getPath());
                    } else {
                        HashSet<String> set = new HashSet<>();
                        set.add(file.getPath());
                        hashVsDupMap.put(hash, set);
                    }
                }
            } else if (file.isDirectory()) {
                File[] files = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return !pathname.getPath().contains("Android/data/");
                    }
                });
                for (File file1 : files) {
                    calculateHash(file1.getPath(), hashVsDupMap);
                }
            }
        }
    }
}
