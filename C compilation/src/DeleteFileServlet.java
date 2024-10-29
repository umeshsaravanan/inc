import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/delete")
public class DeleteFileServlet extends HttpServlet{
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException{
		int currId = Integer.parseInt(request.getParameter("id"));
		int fileId = Integer.parseInt(request.getParameter("fileId"));
        int loggedUserId = (Integer)request.getSession().getAttribute("user_id");
		String query = "delete from filedetails where file_id = ? and user_id = ?";
		PrintWriter output = response.getWriter();
		if(loggedUserId == currId){
            try(Connection con = DatabaseConnection.initializeDatabase();
                PreparedStatement ps = con.prepareStatement(query)){
                    ps.setInt(1,fileId);
                    ps.setInt(2,loggedUserId);
                    int modifiedCount = ps.executeUpdate();
                    
                    if(modifiedCount == 1)
                        output.write("{\"result\" :\"Deleted\"}");
                    else
                        output.write("{\"result\" :\"No such file Exists\"}");
            }
            catch(Exception e){
                e.printStackTrace();
                output.write("An error occured " + e.getMessage());
            }
        }else{
            output.write("{\"access\" :\"unAuthorized\"}");
        }
	}
	
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}