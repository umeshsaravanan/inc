import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/uploadFolder")
public class UploadFolderServlet extends HttpServlet {

    @Override
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        int userId = (Integer) request.getSession().getAttribute("user_id");

        String folders = request.getParameter("folders");
        JSONArray jsonArray = new JSONArray(folders);
        int insertedFolderId = -1;

        try (Connection con = DatabaseConnection.initializeDatabase()) {
            con.setAutoCommit(false);
            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject folderObject = jsonArray.getJSONObject(j);
                String folderName = folderObject.getString("folder");
                Double size = folderObject.getDouble("size");
                String lastModified = folderObject.getString("lastModified");
                if (j == 0 && ((insertedFolderId = insertIntoMapper(userId, folderName, size, lastModified, con)) == -1)) {
                    response.getWriter().println(0);
                    return;
                }

                if (folderName.indexOf("/") < 0)
                    continue;

                String folderPath = folderName + "/";
                int lastIndex = folderName.lastIndexOf("/");
                String sbSearch = folderName.substring(0, lastIndex == -1 ? folderName.length() : lastIndex + 1);

                String parentFolder = folderName.substring(0, folderName.indexOf("/"));
                if (folderName.startsWith(parentFolder + "/")) {
                    folderName = folderName.replaceFirst("^" + parentFolder + "/", "");
                }

                if (folderName.indexOf("/") > 0) {
                    String[] splitted = folderName.split("/");
                    parentFolder = splitted[splitted.length - 2];
                    folderName = splitted[splitted.length - 1];
                }

                if (folderName.trim().length() >= 1) {
                    String findParentFolderId = "select folder_id from foldertable where user_id = ? and folder_name = ? and path = ? order by folder_id DESC;";

                    try (PreparedStatement pstmt = con.prepareStatement(findParentFolderId)) {

                        pstmt.setInt(1, userId);
                        pstmt.setString(2, parentFolder.trim());
                        pstmt.setString(3, sbSearch);
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            int folderId = rs.getInt("folder_id");
                            String insertFolderQuery = "insert into foldertable (user_id,folder_name,parent_folder,path,size,date) VALUES (?, ?, ?, ?, ?, ?);";
                            try (PreparedStatement stmt = con.prepareStatement(insertFolderQuery)) {
                                stmt.setInt(1, userId);
                                stmt.setString(2, folderName.trim());
                                stmt.setInt(3, folderId);
                                stmt.setString(4, folderPath);
                                stmt.setDouble(5, size);
                                stmt.setString(6, lastModified);
                                int modifiedCount = stmt.executeUpdate();
                                if (modifiedCount != 1) {
                                    response.getWriter().println(0);
                                    return;
                                }
                                // if(folderName.equals("classes"))
                                // throw new Exception("dummy exception");
                            }
                        } else {
                            response.getWriter().println(0);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        response.getWriter().println(0);
                        return;
                    }
                }
            }
            con.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.getWriter().println(insertedFolderId);

    }

    public int insertIntoMapper(int userId, String folderName, Double size, String lastModified, Connection con) {
        String insertFolder = "insert into foldertable (folder_name, user_id, path, size,date) VALUES (?, ?, ?, ?, ?) RETURNING folder_id;";
        int folderId = -1;
        try {
            PreparedStatement pstmt = con.prepareStatement(insertFolder);
            pstmt.setString(1, folderName.trim());
            pstmt.setInt(2, userId);
            pstmt.setString(3, folderName + "/");
            pstmt.setDouble(4, size);
            pstmt.setString(5, lastModified);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                folderId = rs.getInt("folder_id");
                String updateToMapper = "insert into foldermapper (user_id, folder_id) VALUES (?, ?);";
                try (PreparedStatement mapperPstmt = con.prepareStatement(updateToMapper)) {
                    mapperPstmt.setInt(1, userId);
                    mapperPstmt.setInt(2, folderId);
                    mapperPstmt.executeUpdate();
                }catch(Exception e){
                    e.printStackTrace();
                    return folderId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return folderId;
        }

        return folderId;
    }
}