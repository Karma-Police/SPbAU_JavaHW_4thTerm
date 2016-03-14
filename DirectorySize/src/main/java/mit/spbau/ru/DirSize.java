package mit.spbau.ru;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Created by michael on 01.03.16.
 */

public class DirSize {
    public static long getDirectorySize(File root) {
        return new ForkJoinPool().invoke(new Node(root));
    }

    public static long getDirectorySizeSlow(File root) {
        if (!root.isDirectory()) {
            return root.length();
        }
        File[] files = root.listFiles();
        long result = 0;
        if (files != null) {
            for (File file : files) {
                result += getDirectorySizeSlow(file);
            }
        }
        return result;
    }

    private static class Node extends RecursiveTask<Long> {
        private final File root;
        public Node(File file) {
            root = file;
        }

        @Override
        protected Long compute() {
            if (!root.isDirectory()) {
                return root.length();
            }
            File[] nextFiles = root.listFiles();
            if (nextFiles == null) {
                return (long) 0;
            }
            long result = 0;
            ArrayList<Node> children = new ArrayList<>();
            for (File nextFile : nextFiles) {
                Node nextNode = new Node(nextFile);
                nextNode.fork();
                children.add(nextNode);
            }
            for (Node child : children) {
                result += child.join();
            }
            return result;
        }
    }
}
