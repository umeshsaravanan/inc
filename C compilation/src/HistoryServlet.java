import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

@WebServlet("/history")
public class HistoryServlet extends HttpServlet {

    @Override
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String f2ObjString = request.getParameter("data2");
        String fName = request.getParameter("fName");

        int userId = (Integer) request.getSession().getAttribute("user_id");

        String historyInsert = "insert into historytable (user_id, f2_data, f_name) VALUES (?, ?, ?)";

        try (Connection con = DatabaseConnection.initializeDatabase();
                PreparedStatement pstmt = con.prepareStatement(historyInsert)) {

            con.setAutoCommit(false);

            pstmt.setInt(1, userId);
            pstmt.setObject(2, f2ObjString.toString(), java.sql.Types.OTHER);
            pstmt.setString(3, fName.replace(".zip", "").replace("Root of ", ""));

            pstmt.executeUpdate();

            JSONObject folderStructure = new JSONObject(f2ObjString);
            insertFolderStructure(folderStructure, "", userId, con);

            con.commit();
            response.getWriter().write("saved");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("Error saving data.");
        }
    }

    @Override
    protected synchronized void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        int userId = (Integer) request.getSession().getAttribute("user_id");
        ArrayList<String> al = new ArrayList<>();
        String getHistories = "select h_id,f_name,compared_time::text AS date from historytable where user_id = ?";

        try (Connection con = DatabaseConnection.initializeDatabase();
                PreparedStatement pstmt = con.prepareStatement(getHistories);) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            JSONArray jsonArray = new JSONArray();

            while (rs.next()) {
                JSONObject historyObject = new JSONObject();

                String dateString = rs.getString("date");
                dateString = dateString.substring(0, dateString.indexOf(" ") + 6);

                historyObject.put("h_id", rs.getInt("h_id"));
                historyObject.put("f_name", rs.getString("f_name"));
                historyObject.put("ct", dateString);
                jsonArray.put(historyObject);
            }

            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("status", "success");
            jsonResponse.put("history", jsonArray);

            response.setContentType("application/json");
            response.getWriter().write(jsonResponse.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("{\"status\" : \"failed\"}");
            return;
        }
    }

    private void insertFolderStructure(JSONObject folderObj, String parentPath, int userId, Connection con)
            throws SQLException {

        String folderName = folderObj.optString("name", "Unnamed Folder");
        // System.out.println("Folder: " + parentPath + "/" + folderName);
        uploadFolder(folderObj, userId, parentPath, con);
        if (folderObj.has("files")) {
            JSONArray files = folderObj.getJSONArray("files");
            for (int i = 0; i < files.length(); i++) {
                JSONObject file = files.getJSONObject(i);
                // System.out.println(" File: " + parentPath + "/" + folderName + "/" +
                // file.getString("name"));
                String findParentFolderId = "select f_id from historyfolders where user_id = ? and f_name = ? and path = ? order by f_id DESC;";
                String fileInsert = "insert into historyfiles (file_name, parent_id, size, size_diff, status, user_id) values (?, ?, ?, ?,?, ?);";

                try (PreparedStatement pstmt = con.prepareStatement(findParentFolderId)) {
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, folderName);
                    pstmt.setString(3, parentPath + "/" + folderName);

                    ResultSet rs = pstmt.executeQuery();
                    int pf_id = -1;

                    if (rs.next()) {
                        pf_id = rs.getInt("f_id");

                        try (PreparedStatement psInsert = con.prepareStatement(fileInsert)) {
                            psInsert.setString(1, file.getString("name"));
                            psInsert.setInt(2, pf_id);
                            psInsert.setInt(3, file.getInt("size"));
                            psInsert.setInt(4, file.has("sizeDifference") ? file.getInt("sizeDifference") : 0);
                            psInsert.setString(5, file.has("status") ? file.getString("status") : "deleted/new");
                            psInsert.setInt(6, userId);

                            int modifiedCount = psInsert.executeUpdate();

                            if (modifiedCount != 1)
                                System.err.println("file insertion failed count");
                                
                        } catch (Exception e) {
                            con.rollback();
                            e.printStackTrace();
                            System.err.println("file insertion failed excep");
                            return;
                        }
                    } else {
                        con.rollback();
                        System.err.println("file insertion failed next");
                    }
                } catch (Exception e) {
                    con.rollback();
                    e.printStackTrace();
                    System.err.println("file insertion failed");
                    return;
                }
            }
        }

        if (folderObj.has("directories")) {
            JSONArray directories = folderObj.getJSONArray("directories");
            for (int i = 0; i < directories.length(); i++) {
                JSONObject subFolder = directories.getJSONObject(i);
                insertFolderStructure(subFolder, parentPath + "/" + folderName, userId, con);
            }
        }
    }

    private void uploadFolder(JSONObject folderObj, int userId, String path, Connection con) throws SQLException {

        if (path == "") {
            uploadParentFolder(folderObj, userId, con);
            return;
        }
        String findParentFolderId = "select f_id from historyfolders where user_id = ? and path = ? order by f_id DESC;";

        try (PreparedStatement pstmt = con.prepareStatement(findParentFolderId)) {

            pstmt.setInt(1, userId);
            // pstmt.setString(2, parentFolder.trim());
            pstmt.setString(2, path);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int folderId = rs.getInt("f_id");
                String insertFolderQuery = "insert into historyfolders (user_id, f_name, pf_id, path, size, size_diff, status) VALUES (?, ?, ?, ?, ?, ?, ?);";
                try (PreparedStatement stmt = con.prepareStatement(insertFolderQuery)) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, folderObj.getString("name"));
                    stmt.setInt(3, folderId);
                    stmt.setString(4, path + "/" + folderObj.getString("name"));
                    stmt.setInt(5, folderObj.getInt("size"));
                    stmt.setInt(6, folderObj.has("sizeDifference") ? folderObj.getInt("sizeDifference") : 0);
                    stmt.setString(7, folderObj.has("status") ? folderObj.getString("status") : "deleted/new");
                    int modifiedCount = stmt.executeUpdate();
                    if (modifiedCount != 1) {
                        System.out.println("folder insertion failed modified count");
                        // response.getWriter().println(0);
                        return;
                    }
                }
            } else {
                con.rollback();
                System.out.println("folder insertion failed next");
                // response.getWriter().println(0);
                return;
            }
        } catch (Exception e) {
            con.rollback();
            e.printStackTrace();
            // response.getWriter().println(0);
            return;
        }
    }

    private void uploadParentFolder(JSONObject folderObj, int userId, Connection con) throws SQLException {
        String insertFolderQuery = "insert into historyfolders (user_id, f_name, path, size, size_diff, status) VALUES (?, ?, ?, ?, ?, ?) returning f_id;";
        try (PreparedStatement stmt = con.prepareStatement(insertFolderQuery)) {
            stmt.setInt(1, userId);
            stmt.setString(2, folderObj.getString("name"));
            stmt.setString(3, "/" + folderObj.getString("name"));
            stmt.setInt(4, folderObj.getInt("size"));
            stmt.setInt(5, folderObj.has("sizeDifference") ? folderObj.getInt("sizeDifference") : 0);
            stmt.setString(6, folderObj.has("status") ? folderObj.getString("status") : "deleted/new");
            ResultSet rs = stmt.executeQuery();
            int folderId = -1;

            if (rs.next()) {
                folderId = rs.getInt("f_id");
                String insertIntoMapper = "insert into historymapper (user_id, f_id) values (?, ?);";
                try (PreparedStatement psMapper = con.prepareStatement(insertIntoMapper)) {
                    psMapper.setInt(1, userId);
                    psMapper.setInt(2, folderId);

                    int modifiedCount = psMapper.executeUpdate();

                    if (modifiedCount != 1)
                        System.out.println("parent folder insertion failed");
                } catch (Exception e) {
                    con.rollback();
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}
