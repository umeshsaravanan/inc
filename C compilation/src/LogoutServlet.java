import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            new Thread(()->{
                deleteTempDir(((Path) session.getAttribute("tempPath")).toString());
                session.removeAttribute("tempPath");
            }).start();
            request.getSession(false).setAttribute("isLoggedIn", false);
            request.getSession(false).setAttribute("leftAt",
                    "/mycompiler" + request.getHeader("Referer").split("mycompiler")[1]);
        }
    }

    public static void deleteTempDir(String tempDirPath) {
        File tempDir = new File(tempDirPath);

        if (tempDir.exists() && tempDir.isDirectory()) {

            File[] files = tempDir.listFiles();
            if (files != null) { 
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteTempDir(file.getAbsolutePath());
                    }
                    file.delete();
                }
            }
            tempDir.delete();
        } else {
            System.out.println("Directory does not exist: " + tempDirPath);
        }
    }
}
