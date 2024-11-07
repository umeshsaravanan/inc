import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/getSession")
public class GetServletSession extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        
        response.setContentType("text/plain");

        if (session != null) {
            String sessionId = session.getId();
            response.getWriter().write(sessionId);
        } else {
            response.getWriter().write("No session found.");
        }
    }
}
