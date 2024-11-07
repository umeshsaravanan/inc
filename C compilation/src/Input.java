import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import java.io.*;

@WebServlet("/input")
public class Input extends HttpServlet {

    private static String str = null;

    @Override
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Path tempDir = (Path) request.getSession(false).getAttribute("tempPath");
        str = request.getParameter("line");
        File inputFile = new File(tempDir.toString(),"inputfile.txt");
        FileWriter writer = new FileWriter(inputFile, true);
        writer.write(str + "\n");
        writer.flush();
        writer.close();
    }
}
