import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/updateAccess")
public class UpdateAccessServlet extends HttpServlet {

    @Override
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        
        int fileId = Integer.parseInt(request.getParameter("fileId"));
        int access = Integer.parseInt(request.getParameter("access"));
        int id = (Integer)request.getSession().getAttribute("user_id");
        if(access == 0 || access == 1){
            String query = "update filedetails set access = ? where user_id = ? and file_id = ?";
    
            try (Connection con = DatabaseConnection.initializeDatabase();){
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setInt(1, access);
                stmt.setInt(2, id);
                stmt.setInt(3, fileId);
                int affectedRecords = stmt.executeUpdate();
                if(affectedRecords == 1)
                response.getWriter().println("updated access");
                else
                response.getWriter().println("UnAuthorized");
            } catch (SQLException e) {
                response.getWriter().println(e.getMessage());
                System.out.println(e.getMessage());
            }
        }else{
            response.getWriter().println("Invalid Access");
        }
    }
}