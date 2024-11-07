import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@WebServlet("/customCompiler")
public class CustomCompiler extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        request.getSession(false).setAttribute("leftAt",
                "/mycompiler" + request.getHeader("Referer").split("mycompiler")[1]);

        Path tempDir = (Path) request.getSession(false).getAttribute("tempPath");

        String fileName = request.getParameter("fileName");
        String customProgram = request.getParameter("customCprogram");
        String input = request.getParameter("input");
        String program = request.getParameter("cProg");
        StringBuffer output = new StringBuffer();
        File file = null;
        if (fileName.indexOf("c++") >= 0 || fileName.indexOf("cpp") >= 0) {
            file = new File(tempDir.toString(), "Main.cpp");
            file.createNewFile();
        } else {
            file = new File(tempDir.toString(), "Main.c");
            file.createNewFile();
        }

        CompilerServlet.writeToFile(customProgram, file);
        CompilerServlet.compileProgram(fileName, tempDir, session, response, true, output);

        ProcessBuilder run = new ProcessBuilder(tempDir.toString() + "\\" + fileName.split("\\.")[0] + ".exe");
        run.directory(tempDir.toFile());
        Process p = run.start();

        Thread inputMonitor = new Thread(() -> {
            try (BufferedReader readerr = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                char[] buffer = new char[1024];
                int charRead = 0;
                while ((charRead = readerr.read(buffer)) != -1) {
                    output.append(new String(buffer, 0, charRead));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        inputMonitor.start();

        try (OutputStream processInput = p.getOutputStream()) {
            processInput.write(input.getBytes());
            processInput.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int exitCode = 0;
        try {
            exitCode = p.waitFor();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        output.append("\n Exited With : " + exitCode);
        File exeFile = new File(tempDir.toString(), fileName.split("\\.")[0] + ".exe");
        exeFile.delete();
        response.getWriter().println(output.toString());
    }
}