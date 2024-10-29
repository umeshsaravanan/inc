import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String hashedPassword = hashString(password);
        HttpSession prevSec = request.getSession(false);
        HttpSession curSec = null;
        if(prevSec != null){
            if(prevSec.getAttribute("username") != null && prevSec.getAttribute("username").equals(name)){
                curSec = prevSec;
            }else{
                prevSec.invalidate();
                curSec = request.getSession(true);
            }
        }
        else{
            curSec = request.getSession(true);
        }
        
        String passwordQuery = "SELECT password, user_id FROM userdetails WHERE user_name = ?;";

        try (Connection con = DatabaseConnection.initializeDatabase();
                PreparedStatement stmt = con.prepareStatement(passwordQuery)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hp = rs.getString("password");
                int userId = rs.getInt("user_id");
                if (hp.equals(hashedPassword)) {
                    curSec.setAttribute("username", name);
                    curSec.setAttribute("isLoggedIn", true);
                    curSec.setAttribute("user_id", rs.getInt("user_id"));
                    Path tempDir = Files.createTempDirectory(curSec.getId());
                    curSec.setAttribute("tempPath",tempDir);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"status\":\"success\", \"leftAt\":\""
                            + curSec.getAttribute("leftAt") + "\" , \"userId\" :\"" + userId + "\"}");
                } else {
                    response.getWriter().write("{\"status\":\"error\", \"message\":\"Invalid Credentials\"}");
                }
            } else {
                response.getWriter().write("{\"status\":\"error\", \"message\":\"Invalid Credentials\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Database error\"}");
        }
    }

    protected synchronized void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        Boolean user = session != null ? (Boolean) session.getAttribute("isLoggedIn") : null;
        if (user != null && user) {
            String username = (String) session.getAttribute("username");
            String leftAt = (String) session.getAttribute("leftAt");
            Integer userId = (Integer) session.getAttribute("user_id");
            response.getWriter().write("{\"status\":\"success\", \"user\":{\"username\":\""
                    + username + "\", \"leftAt\":\"" + leftAt + "\" , \"userId\" : \"" + userId + "\"}}");
        } else {
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Login error\"}");
        }
    }

    public String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }
}
