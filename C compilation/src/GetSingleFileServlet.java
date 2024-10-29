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

@WebServlet("/getSingleFile")
public class GetSingleFileServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int fileId = Integer.parseInt(request.getParameter("fileId").trim());
        Boolean isPublic = request.getParameter("public").equals("true");
        int reqUserId = Integer.parseInt(request.getParameter("userId").trim());
        int id = (Integer) request.getSession().getAttribute("user_id");
        Boolean isLoggedIn = (Boolean) request.getSession().getAttribute("isLoggedIn");
        String referer = request.getHeader("Referer");
        if (id == reqUserId && isLoggedIn) {
            if (referer != null)
                request.getSession(false).setAttribute("leftAt",
                        "/mycompiler" + referer.split("mycompiler")[1]);

            try (Connection con = DatabaseConnection.initializeDatabase();) {
                if(isPublic){
                    String query = "select programfile,compiletime,memory,user_id from fileDetails where file_id = ? and access = 0;";
                    PreparedStatement ps = con.prepareStatement(query);
                    ps.setInt(1, fileId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        byte[] array = rs.getBytes("programfile");
                        int createdUserId = rs.getInt("user_id");
                        double compilationTime = rs.getDouble("compiletime");
                        double memory = rs.getDouble("memory");
                        response.getWriter().print(((array != null) ? new String(array) : "") + "$%^" + compilationTime
                                + "$%^" + memory + "K" + "$%^" + createdUserId);
                    }else {
                        response.getWriter().println("{\"access\" : \"UnAuthorized\"}");
                    }
                }else{
                    String query = "select programfile,compiletime,memory from fileDetails where user_id = ? and file_id = ?;";
                    PreparedStatement ps = con.prepareStatement(query);
                    ps.setInt(1, id);
                    ps.setInt(2, fileId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        byte[] array = rs.getBytes("programfile");
                        double compilationTime = rs.getDouble("compiletime");
                        double memory = rs.getDouble("memory");
                        response.getWriter().print(((array != null) ? new String(array) : "") + "$%^" + compilationTime
                                + "$%^" + memory + "K" + "$%^" + id);
                    }else {
                        response.getWriter().println("{\"access\" : \"UnAuthorized\"}");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            response.getWriter().println("{\"access\" : \"UnAuthorized\"}");
        }
    }
}
