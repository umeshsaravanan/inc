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
        
        JSONArray jsonArray = new JSONArray(files);
        int batchSize = 5;
        List<Thread> threads = new ArrayList<>();
        
        try (Connection con = DatabaseConnection.initializeDatabase()) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String fileName = jsonObject.getString("fileName");
                Double size = jsonObject.getDouble("size");
                String lastModified = jsonObject.getString("lastModified");

                Connection connection = con;
                Thread thread = new Thread(() -> {
                    processFile(fileName, size, lastModified, userId, response,connection);
                });
                threads.add(thread);
                thread.start();
    
                if ((i + 1) % batchSize == 0 || (i + 5) > jsonArray.length()) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        response.getWriter().println(1);
    }

    private void processFile(String fileName, Double size, String lastModified, int userId, HttpServletResponse response, Connection con) {
        int lastIndex = fileName.lastIndexOf("/");
        String filePath = fileName.substring(0,lastIndex == -1 ? fileName.length() : lastIndex + 1);

        String parentFolder = fileName.substring(0,fileName.indexOf("/"));
        if (fileName.startsWith(parentFolder + "/")) {
            fileName = fileName.replaceFirst("^" + parentFolder + "/", "");
        }

        if (fileName.indexOf("/") > 0) {
            String[] splitted = fileName.split("/");
            parentFolder = splitted[splitted.length - 2];
            fileName = splitted[splitted.length - 1];
        }

        if (fileName.trim().length() >= 1) {
            String findParentFolderId = "SELECT folder_id FROM foldertable WHERE user_id = ? AND folder_name = ? and path = ? order by folder_id DESC;";

            try (PreparedStatement pstmt = con.prepareStatement(findParentFolderId)) {

                pstmt.setInt(1, userId);
                pstmt.setString(2, parentFolder.trim());
                pstmt.setString(3, filePath);

                ResultSet rs = pstmt.executeQuery();
                int folderId = -1;
                if (rs.next()) {
                    folderId = rs.getInt("folder_id");
                    String insertFileQuery = "INSERT INTO filestable (file_name, folder_id, size, user_id, date) VALUES (?, ?, ?, ?, ?);";
                    try (PreparedStatement stmt = con.prepareStatement(insertFileQuery)) {
                        stmt.setString(1, fileName.trim());
                        stmt.setInt(2, folderId);
                        stmt.setDouble(3, size);
                        stmt.setInt(4, userId);
                        stmt.setString(5, lastModified);
                        stmt.executeUpdate();
                    }
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
