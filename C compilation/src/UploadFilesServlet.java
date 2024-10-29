import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/uploadFiles")
public class UploadFilesServlet extends HttpServlet {

    @Override
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession();
        int userId = (Integer) session.getAttribute("user_id");
        String files = request.getParameter("files");
        String folders = request.getParameter("folders");
        
        JSONArray jsonArray = new JSONArray(files);
        int batchSize = 5;
        List<Thread> threads = new ArrayList<>();
        Connection con = null;
        try{
            con = DatabaseConnection.initializeDatabase();
        }catch(Exception e){
            e.printStackTrace();
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String fileName = jsonObject.getString("fileName");
            Double size = jsonObject.getDouble("size");
            Connection connection = con;
            Thread thread = new Thread(() -> {
                processFile(fileName, size, userId, response,connection);
            });
            threads.add(thread);
            thread.start();

            if ((i + 1) % batchSize == 0) {
                for (Thread t : threads) {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                threads.clear();
            }
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Connection conn = con;
        new Thread(()->{
            JSONArray jsonArr = new JSONArray(folders);
        for (int j = jsonArr.length() - 1; j >= 0; j--) {

            String folderName = jsonArr.getString(j);
            String[] array = folderName.split("/");
            StringBuilder sb = new StringBuilder();
            StringBuilder sbSearch = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                sb.append(array[i] + "/");
                if (i < array.length - 1)
                    sbSearch.append(array[i] + "/");
            }

            String parentFolder = array[0];
            if (folderName.equals(parentFolder) || folderName.startsWith(parentFolder + "/")) {
                folderName = folderName.replaceFirst("^" + parentFolder, "");
            }
            if (folderName.startsWith("/")) {
                folderName = folderName.substring(1);
            }
            if (folderName.endsWith("/")) {
                folderName = folderName.substring(0, folderName.length() - 1);
            }

            if (folderName.indexOf("/") > 0) {
                String[] splitted = folderName.split("/");
                parentFolder = splitted[splitted.length - 2];
                folderName = splitted[splitted.length - 1];
            }

            if (folderName.trim().length() >= 1) {
                String findParentFolderId = "select folder_id from foldertable where user_id = ? and folder_name = ? and path = ?;";

                try (PreparedStatement pstmt = conn.prepareStatement(findParentFolderId)) {

                    pstmt.setInt(1, userId);
                    pstmt.setString(2, parentFolder.trim());
                    pstmt.setString(3, sbSearch.toString());
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        int folderId = rs.getInt("folder_id");
                        Double currentFolderSize = 0.0;
                        String getCurrentFolderSizeQuery = "select size from foldertable where user_id = ? and path = ?;";
                        try (PreparedStatement sizeStmt = conn.prepareStatement(getCurrentFolderSizeQuery)) {
                            sizeStmt.setInt(1, userId);
                            sizeStmt.setString(2, sb.toString());
                            ResultSet sizeSet = sizeStmt.executeQuery();
                            if (sizeSet.next()) {
                                currentFolderSize = sizeSet.getDouble("size");
                            }
                        }
                        String updateFolderSizeQuery = "update foldertable set size = size + ? where folder_id = ?;";
                        try (PreparedStatement stmt = conn.prepareStatement(updateFolderSizeQuery)) {
                            // query to update size of parent folder
                            stmt.setDouble(1, currentFolderSize);
                            stmt.setInt(2, folderId);
                            int modifiedCount = stmt.executeUpdate();
                            if (modifiedCount != 1) {
                                // response.getWriter().println(0);
                            }
                        }

                    } else {
                        // response.getWriter().println(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // response.getWriter().println(0);
                }
            }
        }
        }).start();

        response.getWriter().println(1);
    }

    private void processFile(String fileName, Double size, int userId, HttpServletResponse response, Connection con) {
        String[] array = fileName.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length - 1; i++)
            sb.append(array[i] + "/");

        String parentFolder = array[0];
        if (fileName.equals(parentFolder) || fileName.startsWith(parentFolder + "/")) {
            fileName = fileName.replaceFirst("^" + parentFolder, "");
        }
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        if (fileName.endsWith("/")) {
            fileName = fileName.substring(0, fileName.length() - 1);
        }

        if (fileName.indexOf("/") > 0) {
            String[] splitted = fileName.split("/");
            parentFolder = splitted[splitted.length - 2];
            fileName = splitted[splitted.length - 1];
        }

        if (fileName.trim().length() >= 1) {
            String findParentFolderId = "SELECT folder_id FROM foldertable WHERE user_id = ? AND folder_name = ? and path = ?;";

            try (PreparedStatement pstmt = con.prepareStatement(findParentFolderId)) {

                pstmt.setInt(1, userId);
                pstmt.setString(2, parentFolder.trim());
                pstmt.setString(3, sb.toString());

                ResultSet rs = pstmt.executeQuery();
                int folderId = -1;
                if (rs.next()) {
                    folderId = rs.getInt("folder_id");
                    String insertFileQuery = "INSERT INTO filestable (file_name, folder_id, size, user_id) VALUES (?, ?, ?, ?);";
                    try (PreparedStatement stmt = con.prepareStatement(insertFileQuery)) {
                        stmt.setString(1, fileName.trim());
                        stmt.setInt(2, folderId);
                        stmt.setDouble(3, size);
                        stmt.setInt(4, userId);
                        stmt.executeUpdate();
                    }
                }

                String updateFolderSizeQuery = "update foldertable set size = size + ? where folder_id = ?;";

                try (PreparedStatement updateFolderStmt = con.prepareStatement(updateFolderSizeQuery);) {
                    updateFolderStmt.setDouble(1, size);
                    updateFolderStmt.setInt(2, folderId);
                    updateFolderStmt.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    response.getWriter().println(0);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
