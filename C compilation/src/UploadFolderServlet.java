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

        Boolean isMain = request.getParameter("isMain").equals("true");
        int userId = (Integer) request.getSession().getAttribute("user_id");

        if (isMain) {
            String folderName = request.getParameter("folderName");
            String checkDuplicatesQuery = "SELECT folder_name FROM foldertable WHERE folder_id IN (SELECT folder_id FROM foldermapper WHERE user_id = ?)";
            String insertFolder = "insert into foldertable (folder_name, user_id, path) VALUES (?, ?, ?) RETURNING folder_id;";
            try (Connection con = DatabaseConnection.initializeDatabase()) {
                PreparedStatement pstmt = con.prepareStatement(checkDuplicatesQuery);
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                Boolean isDuplicate = false;
                while (rs.next()) {
                    String folderNames = rs.getString("folder_name");
                    if (folderNames.equals(folderName.trim())) {
                        isDuplicate = true;
                        response.getWriter().println("duplicate");
                        break;
                    }
                }

                if (!isDuplicate) {
                    pstmt = con.prepareStatement(insertFolder);
                    pstmt.setString(1, folderName.trim());
                    pstmt.setInt(2, userId);
                    pstmt.setString(3, folderName + "/");

                    rs = pstmt.executeQuery();
                    if (rs.next()) {
                        int folderId = rs.getInt("folder_id");
                        response.getWriter().println(1);
                        String updateToMapper = "insert into foldermapper (user_id, folder_id) VALUES (?, ?);";
                        try (PreparedStatement mapperPstmt = con.prepareStatement(updateToMapper)) {
                            mapperPstmt.setInt(1, userId);
                            mapperPstmt.setInt(2, folderId);
                            mapperPstmt.executeUpdate();
                        }
                    } else {
                        response.getWriter().println(0);
                    }
                }
            } catch (Exception e) {
                response.getWriter().println(0);
            }
        } else {
            String folders = request.getParameter("folders");
            JSONArray jsonArray = new JSONArray(folders);

            for (int j = 0; j < jsonArray.length(); j++) {
                String folderName = jsonArray.getString(j);
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

                    try (Connection con = DatabaseConnection.initializeDatabase();
                            PreparedStatement pstmt = con.prepareStatement(findParentFolderId)) {

                        pstmt.setInt(1, userId);
                        pstmt.setString(2, parentFolder.trim());
                        pstmt.setString(3, sbSearch.toString());
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            int folderId = rs.getInt("folder_id");
                            String insertFolderQuery = "insert into foldertable (user_id,folder_name,parent_folder,path) VALUES (?, ?, ?, ?);";
                            try (PreparedStatement stmt = con.prepareStatement(insertFolderQuery)) {
                                stmt.setInt(1, userId);
                                stmt.setString(2, folderName.trim());
                                stmt.setInt(3, folderId);
                                stmt.setString(4, sb.toString());
                                int modifiedCount = stmt.executeUpdate();
                                if (modifiedCount != 1) {
                                    response.getWriter().println(0);
                                }
                            }
                        } else {
                            response.getWriter().println(0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        response.getWriter().println(0);
                    }
                }
            }
            response.getWriter().println(1);
        }

    }
}