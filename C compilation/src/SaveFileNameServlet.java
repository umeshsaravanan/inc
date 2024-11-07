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

@WebServlet("/saveFile")
public class SaveFileNameServlet extends HttpServlet {

    @Override
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        
        String fileName = request.getParameter("fileName");
        int access = Integer.parseInt(request.getParameter("access"));
        int id = (Integer)request.getSession().getAttribute("user_id");

        String query = "insert into filedetails (user_id,file_name,access) values (?,?,?);";
        int fileId = 0;
        try (Connection con = DatabaseConnection.initializeDatabase();){
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, id);
            stmt.setString(2, fileName);
            stmt.setInt(3, access);
            stmt.executeUpdate();
            query = "select file_id from filedetails where file_name = ? and user_id = ?;";
            stmt = con.prepareStatement(query);
            stmt.setString(1, fileName);
            stmt.setInt(2, id);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()){
                fileId = rs.getInt("file_id");
            }
            
            response.getWriter().println("fileid"+fileId);
        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
            System.out.println(e.getMessage());
        }
    }
}