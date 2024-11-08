import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.SQLException;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/getSubFolders")
public class GetSubFoldersServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String referer = request.getHeader("Referer");
        if (referer != null)
            request.getSession(false).setAttribute("leftAt",
                    "/mycompiler" + referer.split("mycompiler")[1]);

        int parentFolderId = Integer.parseInt(request.getParameter("folderId"));
        int userId = Integer.parseInt(request.getParameter("userId"));
        int id = (Integer) request.getSession().getAttribute("user_id");
        if(id == userId){
            try (Connection con = DatabaseConnection.initializeDatabase()) {
                ArrayList<String> al = new ArrayList<>();
                String folderQuery = "select folder_name,folder_id,size,date::text AS date from foldertable where parent_folder = ?;";
    
                PreparedStatement pstmt = con.prepareStatement(folderQuery);
                pstmt = con.prepareStatement(folderQuery);
                pstmt.setInt(1, parentFolderId);
    
                ResultSet folderRs = pstmt.executeQuery();
    
                while (folderRs.next()) {
                    String dateString = folderRs.getString("date");
                    dateString = dateString.substring(0,dateString.indexOf(" ") + 6);
                    al.add(folderRs.getString("folder_name") + ":folder:" + folderRs.getDouble("size") + ":" + folderRs.getInt("folder_id") + ":" + dateString);
                }
    
                String fileQuery = "select file_name,size,date::text AS date from filestable where folder_id = ?;";
                pstmt = con.prepareStatement(fileQuery);
                pstmt.setInt(1, parentFolderId);
                
                ResultSet fileRs = pstmt.executeQuery();
    
                while (fileRs.next()) {
                    String dateString = fileRs.getString("date");
                    dateString = dateString.substring(0,dateString.indexOf(" ") + 6);
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
        }else {
            response.getWriter().println("{\"access\" : \"UnAuthorized\"}");
        }
        
    }
}
