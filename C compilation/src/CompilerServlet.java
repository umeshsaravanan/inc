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

@WebServlet("/compiler")
public class CompilerServlet extends HttpServlet {
    private Process currentProcess;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        request.getSession(false).setAttribute("leftAt",
                "/mycompiler" + request.getHeader("Referer").split("mycompiler")[1]);

        Path tempDir = (Path) request.getSession(false).getAttribute("tempPath");
        String cProgram = request.getParameter("cProgram");
        String fileName = request.getParameter("fileName");
        Boolean isPublic = request.getParameter("isPublic").equals("true");
        File file = null;
        if (fileName.indexOf("c++") >= 0 || fileName.indexOf("cpp") >= 0) {
            file = new File(tempDir.toString(), "Main.cpp");
            file.createNewFile();
        } else {
            file = new File(tempDir.toString(), "Main.c");
            file.createNewFile();
        }

        writeToFile(cProgram, file);
        int fileId = updateFileToDB(request, cProgram, fileName, isPublic);

        compileProgram(fileName, tempDir, session, response, false, new StringBuffer());
        ProcessBuilder run = new ProcessBuilder(tempDir.toString() + "\\" + fileName.split("\\.")[0] + ".exe");
        run.directory(tempDir.toFile());
        long compilationTime;
        File inputFile = new File(tempDir.toString(), "inputfile.txt");
        inputFile.createNewFile();
        long startTime = System.currentTimeMillis();
        Process p = run.start();
        long endTime = System.currentTimeMillis();
        currentProcess = p;
        int[] memoryUsed = { 0 };
        Thread memoryMonitor = new Thread(() -> {
            try {
                long pid = p.pid();
                while (p.isAlive()) {
                    memoryUsed[0] = checkMemoryUsage(session, pid);
                    Thread.sleep(1000);
                }
                memoryUsed[0] = checkMemoryUsage(session, pid);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        memoryMonitor.start();

        Thread inputMonitor = new Thread(() -> {
            try (BufferedReader readerr = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()))) {
                char[] buffer = new char[1024];
                int charRead = 0;
                while ((charRead = readerr.read(buffer)) != -1) {
                    OutputWebSocket.sendOutput(session.getId(), new String(buffer, 0, charRead));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        inputMonitor.start();
        int exitCode = 0;
        if (p.isAlive()) {
            try (OutputStream processInput = p.getOutputStream()) {
                inputFile = new File(tempDir.toString(), "inputfile.txt");
                outerLoop: while (p.isAlive()) {
                    while (inputFile.length() == 0 && p.isAlive()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        inputFile = new File(tempDir.toString(), "inputfile.txt");
                    }

                    try (BufferedReader fileReader = new BufferedReader(new FileReader(inputFile))) {
                        String Line;
                        while ((Line = fileReader.readLine()) != null && p.isAlive()) {
                            processInput.write((Line + "\n").getBytes());
                            processInput.flush();
                        }
                        try (PrintWriter writer = new PrintWriter(inputFile)) {
                            writer.print("");
                        }
                    }

                    if (inputFile.length() == 0 && p.isAlive()) {
                        continue outerLoop;
                    }
                }
                exitCode = p.waitFor();
                endTime = System.currentTimeMillis();
                memoryMonitor.interrupt();
                inputMonitor.interrupt();
                File exeFile = new File(tempDir.toString(), fileName.split("\\.")[0] + ".exe");
                exeFile.delete();
                file.delete();
                OutputWebSocket.sendOutput(session.getId(), "\nExited with : " + exitCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            File exeFile = new File(tempDir.toString(), fileName.split("\\.")[0] + ".exe");
            exeFile.delete();
            file.delete();
            OutputWebSocket.sendOutput(session.getId(), "\nExited with: " + exitCode);
        }
        compilationTime = endTime - startTime;
        OutputWebSocket.sendOutput(session.getId(), "Execution time : " + compilationTime / 1000f + "s\n");
        if (!isPublic)
            updateMemoryandTime(compilationTime, fileId, memoryUsed);
        inputFile.delete();
    }

    public static void compileProgram(String fileName, Path tempDir, HttpSession session, HttpServletResponse response,
            Boolean custom, StringBuffer output) {
        ProcessBuilder compilation = null;

        if (fileName.indexOf("c++") >= 0 || fileName.indexOf("cpp") >= 0)
            compilation = new ProcessBuilder("g++", "-o", fileName.split("\\.")[0] + ".exe", "Main.cpp");
        else {
            compilation = new ProcessBuilder("gcc", "-o", fileName.split("\\.")[0] + ".exe", "Main.c");
        }

        compilation.directory(tempDir.toFile());
        try {
            Process process = compilation.start();
            BufferedReader erroReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String error;

            while ((error = erroReader.readLine()) != null) {
                if (!custom) {
                    OutputWebSocket.sendOutput(session.getId(), error + "\n");
                } else {
                    output.append(error);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                if (!custom) {
                    response.setStatus(200);
                    OutputWebSocket.sendOutput(session.getId(), "running");
                }
            } else {
                if (custom) {
                    output.append("\nExited With  :" + exitCode);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    OutputWebSocket.sendOutput(session.getId(), "\nExited With  :" + exitCode);
                }
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
    }

    public static int checkMemoryUsage(HttpSession session, long pid) {
        int memoryUsed = 0;
        try {
            ProcessBuilder tasklistBuilder = new ProcessBuilder("cmd.exe", "/c", "tasklist /FI \"PID eq " + pid + "\"");
            Process tasklistProcess = tasklistBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(tasklistProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.contains(String.valueOf(pid))) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 5) {
                        String memUsage = parts[parts.length - 2];
                        memUsage = memUsage.replace(",", "");
                        memoryUsed = Integer.parseInt(memUsage);
                        OutputWebSocket.sendOutput(session.getId(), "Memory usage : " + memoryUsed + "K");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return memoryUsed;
    }

    private void updateMemoryandTime(long cp, int fileId, int[] memoryUsed) {
        try (Connection con = DatabaseConnection.initializeDatabase()) {
            String getCompileMemoryTimeQuery = "select compiletime,memory from filedetails where file_id = ?;";
            PreparedStatement ps = con.prepareStatement(getCompileMemoryTimeQuery);
            ps.setInt(1, fileId);
            double prevComp = 0.0;
            int prevMemory = 0;
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                prevComp = rs.getDouble("compiletime");
                prevMemory = rs.getInt("memory");
            }
            if (prevComp == 0.0 || (double) cp / 1000f < prevComp) {
                String compileTimeQuery = "update filedetails set compiletime = ? where file_id = ?;";
                ps = con.prepareStatement(compileTimeQuery);
                ps.setDouble(1, (double) cp / 1000f);
                ps.setInt(2, fileId);
                ps.executeUpdate();
            }
            if (prevMemory == 0 || memoryUsed[0] < prevMemory) {
                String compileTimeQuery = "update filedetails set memory = ? where file_id = ?;";
                ps = con.prepareStatement(compileTimeQuery);
                ps.setInt(1, memoryUsed[0]);
                ps.setInt(2, fileId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int updateFileToDB(HttpServletRequest request, String cProgram, String fileName, Boolean isPublic) {
        byte[] array = cProgram.getBytes();
        int id = (Integer) request.getSession(false).getAttribute("user_id");
        String query = "update filedetails set programfile = ? where file_name = ? and user_id = ?;";
        String fileIDQuery = "select file_id from filedetails where file_name = ? and user_id = ?;";

        try (Connection con = DatabaseConnection.initializeDatabase();) {
            PreparedStatement stmt = con.prepareStatement(query);
            if (!isPublic) {
                stmt.setBytes(1, array);
                stmt.setString(2, fileName);
                stmt.setInt(3, id);
                int affectedRecords = stmt.executeUpdate();
                if (affectedRecords == 1)
                    System.out.println("File uploaded successfully!");
                else
                    System.out.println("No File with specified Name :(");
            } else {
                System.out.println("public file");
            }

            stmt = con.prepareStatement(fileIDQuery);
            stmt.setString(1, fileName);
            stmt.setInt(2, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("file_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static void writeToFile(String cProgram, File file) {
        String regex = "^(?!\\s*//).*\\s*.*#include\\s*([\"<])stdio\\.h([\">]).*";

        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

        Matcher matcher = pattern.matcher(cProgram);
        if (matcher.find()) {
            try {
                String[] codeArray = cProgram.split("(?i)main\\(\\)\\s*\\{");
                codeArray[1] = "main(){\nsetvbuf(stdout,NULL,_IONBF,0);" + codeArray[1];
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write(String.join("", codeArray));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(cProgram);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
