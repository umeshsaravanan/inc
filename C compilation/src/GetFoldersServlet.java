import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.SQLException;
import java.io.IOException;
import java.lang.Thread.State;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/getFolders")
public class GetFoldersServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String referer = request.getHeader("Referer");
        if (referer != null)
            request.getSession(false).setAttribute("leftAt",
                    "/mycompiler" + referer.split("mycompiler")[1]);
        int userId = Integer.parseInt(request.getParameter("userId"));
        int reqFolderId = Integer.parseInt(request.getParameter("folderId"));
        int id = (Integer) request.getSession().getAttribute("user_id");
        if (id == userId) {
            try (Connection con = DatabaseConnection.initializeDatabase();) {

                ArrayList<String> al = new ArrayList<>();
                String folderQuery = "select folder_name,folder_id,size,date::text AS date from foldertable where user_id = ? and parent_folder is null;";
                PreparedStatement pstmt = con.prepareStatement(folderQuery);
                pstmt.setInt(1, userId);
                ResultSet folderRs = pstmt.executeQuery();
                if (reqFolderId != -1) {
                    while (folderRs.next()) {
                        String dateString = folderRs.getString("date");
                        dateString = dateString.substring(0, dateString.indexOf(" ") + 6);
                        if (reqFolderId == folderRs.getInt("folder_id"))
                            al.add(folderRs.getString("folder_name") + ":folder:" + folderRs.getDouble("size") + ":"
                                    + folderRs.getInt("folder_id") + ":"
                                    + dateString);
                    }
                } else
                    while (folderRs.next()) {
                        String dateString = folderRs.getString("date");
                        dateString = dateString.substring(0, dateString.indexOf(" ") + 6);
                        al.add(folderRs.getString("folder_name") + ":folder:" + folderRs.getDouble("size") + ":"
                                + folderRs.getInt("folder_id") + ":"
                                + dateString);
                    }

                String fileQuery = "select file_name,size,date::text AS date from filestable where folder_id = 0 and user_id = ?;";
                pstmt = con.prepareStatement(fileQuery);
                pstmt.setInt(1, userId);
                ResultSet fileRs = pstmt.executeQuery();
                while (fileRs.next()) {
                    String dateString = fileRs.getString("date");
                    dateString = dateString.substring(0, dateString.indexOf(" ") + 6);
                    al.add(fileRs.getString("file_name") + ":file:" + fileRs.getDouble("size") + ":" + dateString);
                }
                StringBuilder jsonNames = new StringBuilder("[");
                for (int i = 0; i < al.size(); i++) {
                    jsonNames.append("\"").append(al.get(i)).append("\"");
                    if (i < al.size() - 1) {
                        jsonNames.append(", ");
                    }
                }
                jsonNames.append("]");
                response.getWriter().println("{\"status\" : \"success\", \"names\" : " + jsonNames.toString() + "}");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            response.getWriter().println("{\"access\" : \"UnAuthorized\"}");
        }
    }
}
