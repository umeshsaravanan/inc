import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/getHistory")
public class GetSingleHistoryServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Integer userId = (Integer) request.getSession().getAttribute("user_id");
        int historyId = Integer.parseInt(request.getParameter("hisId"));

        String getHistory = "select f2_data from historytable where h_id = ?";

        try (Connection con = DatabaseConnection.initializeDatabase();
                PreparedStatement pstmt = con.prepareStatement(getHistory)) {

            pstmt.setInt(1, historyId);

            ResultSet rs = pstmt.executeQuery();
            JSONArray jarray = new JSONArray();

            if (rs.next()) {
                String f2Data = rs.getString("f2_data");
                JSONObject f2Obj = new JSONObject(f2Data);
                jarray.put(f2Obj);
            }
            JSONObject folderObj = createObject(20, con);
            addSubFoldersAndFiles(folderObj, 20, con);
            response.setContentType("application/json");
            response.getWriter().write(jarray.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("An error occurred while retrieving history.");
        }
    }

    private JSONObject createObject(int parentFolderId, Connection con){

        String rootObj = "Select * from historyfolders where f_id = ?;";

        JSONObject rootJsonObj = new JSONObject();
        try (PreparedStatement psRoot = con.prepareStatement(rootObj)) {
            psRoot.setInt(1, parentFolderId);

            ResultSet rs = psRoot.executeQuery();
            
            if(rs.next()){
                rootJsonObj.put("name",rs.getString("f_name"));
                rootJsonObj.put("size",rs.getInt("size"));
                rootJsonObj.put("sizeDifference",rs.getInt("size_diff"));
                rootJsonObj.put("status",rs.getString("status"));
                rootJsonObj.put("directories", new JSONArray());
                rootJsonObj.put("files", new JSONArray());
                rootJsonObj.put("isDirectory", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exception in creating root obj for history id" + parentFolderId);
            return null;
        }

        return rootJsonObj;
    }

    public void addSubFoldersAndFiles(JSONObject rootObj, int parentId, Connection con){

        String subFoldersQuery = "select * from historyfolders where pf_id = ?;";

        try (PreparedStatement psSubFolder = con.prepareStatement(subFoldersQuery)) {
            psSubFolder.setInt(1, parentId);

            ResultSet rs = psSubFolder.executeQuery();

            while(rs.next()){
                JSONObject subFolObj = new JSONObject();
                subFolObj.put("name",rs.getString("f_name"));
                subFolObj.put("size",rs.getInt("size"));
                subFolObj.put("sizeDifference",rs.getInt("size_diff"));
                subFolObj.put("status",rs.getString("status"));
                subFolObj.put("directories", new JSONArray());
                subFolObj.put("files", new JSONArray());
                subFolObj.put("isDirectory", true);

                addSubFoldersAndFiles(subFolObj, rs.getInt("f_id"), con);
                rootObj.directories.put(subFolObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        return;
    }
}
