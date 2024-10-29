import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/getFiles")
public class GetFileServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession(false).setAttribute("leftAt","/mycompiler"+request.getHeader("Referer").split("mycompiler")[1]);
        String username = request.getParameter("username");
        Boolean publicAccess = request.getParameter("public").equals("true");
        try(Connection con = DatabaseConnection.initializeDatabase();) {
            // String userIdQuery = "select user_id from userdetails where user_name = ?;";
            // PreparedStatement pstmt = con.prepareStatement(userIdQuery);
            // pstmt.setString(1, username);
            // ResultSet rs = pstmt.executeQuery();
            // int id = -1;
            // while (rs.next())
            //     id = rs.getInt("user_id");
            int id = (Integer)request.getSession().getAttribute("user_id");
            if(publicAccess){
                String query = "select file_name, file_id, access, user_name from fileDetails f inner join userDetails u on u.user_id = f.user_id where f.user_id <> ? and f.access = 0 order by f.file_id desc;";
                PreparedStatement pstmt = con.prepareStatement(query);
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                StringBuffer sb = new StringBuffer();
                while (rs.next()) {
                    String s = rs.getString("file_name");
                    int fileId = rs.getInt("file_id");
                    int access = rs.getInt("access");
                    String userName = rs.getString("user_name");
                    // OutputWebSocket.sendOutput(request.getSession().getId(),"File" + s + ":" + fileId);
                    sb.append(s + ":" + access + ":" + fileId + ":" + userName + "\n");
                }
                response.setContentType("text/plain");
                response.getWriter().print(sb.toString());
            }else{
                String query = "select file_name,file_id,access from fileDetails where user_id = ? order by file_id DESC;";
                PreparedStatement pstmt = con.prepareStatement(query);
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                StringBuffer sb = new StringBuffer();
                while (rs.next()) {
                    String s = rs.getString("file_name");
                    int fileId = rs.getInt("file_id");
                    int access = rs.getInt("access");
                    // OutputWebSocket.sendOutput(request.getSession().getId(),"File" + s + ":" + fileId);
                    sb.append(s + ":" + access + ":" + fileId + "\n");
                }
                response.setContentType("text/plain");
                response.getWriter().print(sb.toString());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
