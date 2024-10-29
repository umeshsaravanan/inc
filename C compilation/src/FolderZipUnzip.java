import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.nio.file.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.zip.*;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/uploadFoldersToCompare")
@MultipartConfig
public class FolderZipUnzip extends HttpServlet {
    private static int folderCount = 0;

    @Override
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Part filePart = request.getPart("zipFile");
        String zipFileName = filePart.getSubmittedFileName();

        Path tempDir = (Path) request.getSession(false).getAttribute("tempPath");
        if (!tempDir.toFile().exists()) {
            tempDir.toFile().mkdirs();
        }
        Path folderPath = tempDir.resolve("folder" + ++folderCount);
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }

        File file = new File(folderPath.toString(), zipFileName);
        try (InputStream fileContent = filePart.getInputStream();
            FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileContent.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        TreeNode root = unZip(folderPath.toString(), zipFileName);
        JSONObject json = treeToJson(root);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        LogoutServlet.deleteTempDir(folderPath.toString());
        response.getWriter().write(json.toString());
    }

    private TreeNode unZip(String dir, String zipFileName) {
        String zipPath = dir + "\\" + zipFileName;
        File zipFile = new File(zipPath);
        if (!zipFile.exists()) {
            System.out.println("ZIP file not found.");
            return null;
        }

        byte[] buffer = new byte[1024];
        TreeNode root = new TreeNode("Root of " + zipFileName, true);

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                File newFile = new File(zipFile.getParent(), entryName);
                TreeNode currentNode = root;

                String[] pathParts = entryName.split("/");
                for (String part : pathParts) {
                    if (!part.isEmpty()) {
                        TreeNode childNode = findOrCreateChild(currentNode, part, entry.isDirectory());
                        currentNode = childNode;
                    }
                }

                if (!entry.isDirectory()) {
                    new File(newFile.getParent()).mkdirs();
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile))) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            bos.write(buffer, 0, len);
                        }
                    }

                    long size = Files.size(newFile.toPath());
                    currentNode.size = size;
                }
                zis.closeEntry();
            }
            System.out.println("Unzipped successfully.");
        } catch (IOException e) {
            System.out.println("Error unzipping: " + e.getMessage());
        }
        zipFile.delete();
        return root;
    }

    private TreeNode findOrCreateChild(TreeNode parent, String name, boolean isDirectory) {
        for (TreeNode child : parent.children) {
            if (child.name.equals(name)) {
                return child;
            }
        }
        TreeNode newChild = new TreeNode(name, isDirectory);
        parent.addChild(newChild);
        return newChild;
    }

    private JSONObject treeToJson(TreeNode node) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", node.name);
        jsonObject.put("isDirectory", node.isDirectory);
        if (!node.isDirectory)
            jsonObject.put("size", node.size);

        JSONArray directoriesArray = new JSONArray();
        JSONArray filesArray = new JSONArray();

        for (TreeNode child : node.children) {
            if (child.isDirectory) {
                directoriesArray.put(treeToJson(child));
            } else {
                filesArray.put(treeToJson(child));
            }
        }

        if (node.isDirectory) {
            jsonObject.put("directories", directoriesArray);
            jsonObject.put("files", filesArray);
        }

        return jsonObject;
    }

}

class TreeNode {
    String name;
    boolean isDirectory;
    long size;
    List<TreeNode> children;

    public TreeNode(String name, boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.size = 0;
        this.children = new ArrayList<>();
    }

    public void addChild(TreeNode child) {
        children.add(child);
    }
}
