package project;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Attendance extends HttpServlet {

    private Connection con = null;
    private PreparedStatement pst = null;
    private ResultSet rs = null;
    private   RequestDispatcher rd=null;

    private void connectToDB() throws SQLException, ClassNotFoundException {
        if (con == null || con.isClosed()) {
            Class.forName("oracle.jdbc.OracleDriver");
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "gopi", "7730");
            con.setAutoCommit(false);
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	doGet(req, res);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        PrintWriter pw = res.getWriter();
        String action = req.getParameter("submit");

        switch (action) {
            case "login":
                handleLogin(req, res, pw);
                break;
            case "register":
                handleRegistration(req, res, pw);
                break;
            case "update":
                handleUpdate(req, res, pw);
                break;
            case "delete":
                handleDelete(req, res, pw);
                break;
            case "setDetails":
            	
                handleLocation(req, res, pw);
                break;
            case "setLocation":
                handleLocation(req, res, pw);
                break;
            case "face":
                handleFace(req, res, pw);
                break;
            default:
                pw.println("<h3 style='color:red;'>Invalid action!</h3>");
        }

        pw.close();
    }

    private void handleFace(HttpServletRequest req, HttpServletResponse res, PrintWriter pw) {
		// TODO Auto-generated method stub
		
	}
	private void handleLocation(HttpServletRequest req, HttpServletResponse res, PrintWriter pw) {
		   
	        String action = req.getParameter("submit");
	        
	        if ("staffDetails".equalsIgnoreCase(action)) {
	        	 String course = req.getParameter("course");
	 	        String division = req.getParameter("division");
	 	        String teacherName = req.getParameter("teacherName");
	 	        String slotTime = req.getParameter("slotTime");
	 	        String date = req.getParameter("date");
	 	       pw.write(" gopi m");
	 	       rd = req.getRequestDispatcher("/mapurl") ;	        
	 	       return "/mapurl";
	        }
	        else  if ("stuDetails".equalsIgnoreCase(action)) {
	        	
	        	 String stuCourse = req.getParameter("stuCourse");
		 	        String stuDivision = req.getParameter("stuDivision");
		 	        String stuTeacherName = req.getParameter("StuTeacherName");
		 	        String stuSlotTime = req.getParameter("stuSlotTime");
		 	        String stuDate = req.getParameter("stuDate");
		 	        
		 	      
	        	
	        }
			
	}
	private void handleLogin(HttpServletRequest req, HttpServletResponse res, PrintWriter pw) throws ServletException, IOException {
        String option = req.getParameter("option");
        String enrollment = req.getParameter("senrollment");
        String password = req.getParameter("spassword");

        // Implement validation
        String validationErrors = validateLoginForm(option, enrollment, password);
        if (!validationErrors.isEmpty()) {
            pw.println("<ul style='color:red;'>" + validationErrors + "</ul>");
            return;
        }

        try {
            connectToDB();
            String select_query = "SELECT name FROM " + option + " WHERE enrollment = ? AND password = ?";
            pst = con.prepareStatement(select_query);
            pst.setString(1, enrollment);
            pst.setString(2, password);
            rs = pst.executeQuery();

            if (rs.next()) {
              String name = rs.getString("name");
              req.setAttribute("name", name);
                req.setAttribute("enrollment", enrollment);
               rd = req.getRequestDispatcher(getDashboardURL(option));
                rd.forward(req, res);
            } else {
                pw.println("<h3 style='color:red;'>Login Failed: Invalid credentials</h3>");
            }
        } catch (SQLException | ClassNotFoundException e) {
            handleError(pw, e);
        } finally {
            closeResources();
        }
    }

    private String getDashboardURL(String option) {
        switch (option) {
            case "admin": return "/admindashboardurl";
            case "student": return "/studentdashboardurl";
            case "staff": return "/staffdashboardurl";
            default: return "/errorPage";
        }
    }

    public String validateLoginForm(String option, String enrollment, String password) {
        // Implement validation logic, e.g., non-empty fields
        StringBuilder errors = new StringBuilder();
        if (enrollment == null || enrollment.trim().isEmpty()) {
            errors.append("<li>Enrollment must not be empty.</li>");
        }
        if (password == null || password.trim().isEmpty()) {
            errors.append("<li>Password must not be empty.</li>");
        }
        return errors.toString();
    }

    private void handleRegistration(HttpServletRequest req, HttpServletResponse res, PrintWriter pw) throws IOException, ServletException {
        String userType = req.getParameter("role");
        String enrollment = req.getParameter("enrollment");
        String password = req.getParameter("password");

        try {
            connectToDB();
            String insertQuery = createInsertQuery(userType);
            pst = con.prepareStatement(insertQuery);
            setInsertParameters(req, userType, enrollment, password);

            int rowCount = pst.executeUpdate();
            if (rowCount > 0) {
                con.commit();
                pw.println("<h3 style='color:green;'>Registration successful!</h3>");
            } else {
                pw.println("<h3 style='color:red;'>Registration failed!</h3>");
            }
        } catch (SQLException e) {
            handleSQLException(pw, e);
        } catch (ClassNotFoundException e) {
            pw.println("<h3>Database Driver not found: " + e.getMessage() + "</h3>");
        } finally {
            closeResources();
        }
    }
    private String createInsertQuery(String userType) {
        if ("student".equalsIgnoreCase(userType)) {
            return "INSERT INTO student (ENROLLMENT, PASSWORD, NAME, PH_NO, EMAIL, DOB, COURSE, SPECIALIZATION) VALUES (?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?)";
        } else if ("staff".equalsIgnoreCase(userType)) {
            return "INSERT INTO staff (ENROLLMENT, PASSWORD, NAME, FACULTY, DEPARTMENT) VALUES (?, ?, ?, ?, ?)";
        } else {
            return null;
        }
    }

    private void setInsertParameters(HttpServletRequest req, String userType, String enrollment, String password) throws SQLException {
        if ("student".equalsIgnoreCase(userType)) {
            pst.setString(1, enrollment);
            pst.setString(2, password); // Consider hashing this
            pst.setString(3, req.getParameter("fname"));
            pst.setString(4, req.getParameter("phno"));
            pst.setString(5, req.getParameter("email"));
            pst.setString(6, req.getParameter("dob"));
            pst.setString(7, req.getParameter("course"));
            pst.setString(8, req.getParameter("specialization"));
        } else if ("staff".equalsIgnoreCase(userType)) {
            pst.setString(1, enrollment);
            pst.setString(2, password); // Consider hashing this
            pst.setString(3, req.getParameter("staffName"));
            pst.setString(4, req.getParameter("faculty"));
            pst.setString(5, req.getParameter("department"));
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse res, PrintWriter pw) throws IOException, ServletException {
        String enrollment = req.getParameter("enrollment").trim();
        String password = req.getParameter("password");
        String userType = req.getParameter("role");

        try {
            connectToDB();
            String updateQuery = createUpdateQuery(userType);
            if (updateQuery == null) {
                pw.println("<h3 style='color:red;'>Error: Update query is null!</h3>");
                return;
            }

            pst = con.prepareStatement(updateQuery);
            setUpdateParameters(req, userType, enrollment, password);

            int rowCount = pst.executeUpdate();
            if (rowCount > 0) {
                con.commit();
                pw.println("<h3 style='color:green;'>Update successful!</h3>");
            } else {
                pw.println("<h3 style='color:red;'>Update failed: No record found for enrollment " + enrollment + "</h3>");
            }
        } catch (SQLException e) {
            handleSQLException(pw, e);
        } catch (ClassNotFoundException e) {
            pw.println("<h3>Database Driver not found: " + e.getMessage() + "</h3>");
        } finally {
            closeResources();
        }
    }

    private String createUpdateQuery(String userType) {
        if ("student".equalsIgnoreCase(userType)) {
            return "UPDATE student SET PASSWORD = ?, NAME = ?, PH_NO = ?, EMAIL = ?, DOB = TO_DATE(?, 'YYYY-MM-DD'), COURSE = ?, SPECIALIZATION = ? WHERE UPPER(TRIM(ENROLLMENT)) = UPPER(TRIM(?))";
        } else if ("staff".equalsIgnoreCase(userType)) {
        	
            return "UPDATE staff SET PASSWORD = ?, NAME = ?, FACULTY = ?, DEPARTMENT = ? WHERE UPPER(TRIM(ENROLLMENT)) = UPPER(TRIM(?))";
        } else {
            return null;
        }
    }

    private void setUpdateParameters(HttpServletRequest req, String userType, String enrollment, String password) throws SQLException {
        if ("student".equalsIgnoreCase(userType)) {
            pst.setString(1, password); // Consider hashing
            pst.setString(2, req.getParameter("fname"));  // Assuming 'fname' is the full name for students
            pst.setString(3, req.getParameter("phno"));
            pst.setString(4, req.getParameter("email"));
            pst.setString(5, req.getParameter("dob"));
            pst.setString(6, req.getParameter("course"));
            pst.setString(7, req.getParameter("specialization"));
            pst.setString(8, enrollment);
        } else if ("staff".equalsIgnoreCase(userType)) {
            pst.setString(1, password); // Consider hashing
            pst.setString(2, req.getParameter("staffName"));
            pst.setString(3, req.getParameter("faculty"));
            pst.setString(4, req.getParameter("department"));
            pst.setString(5, enrollment);
            
            System.out.println("Enrollment: " + enrollment);

        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse res, PrintWriter pw) throws IOException, ServletException {
        String enrollment = req.getParameter("enrollment");
        String userType = req.getParameter("role");

        try {
            connectToDB();
            String deleteQuery = createDeleteQuery(userType);
            if (deleteQuery == null) {
                pw.println("<h3 style='color:red;'>Error: Delete query is null!</h3>");
                return;
            }

            pst = con.prepareStatement(deleteQuery);
            pst.setString(1, enrollment);

            int rowCount = pst.executeUpdate();
            if (rowCount > 0) {
                con.commit();
                pw.println("<h3 style='color:green;'>Deletion successful!</h3>");
            } else {
                pw.println("<h3 style='color:red;'>Deletion failed: No record found for enrollment " + enrollment + "</h3>");
            }
        } catch (SQLException e) {
            handleSQLException(pw, e);
        } catch (ClassNotFoundException e) {
            pw.println("<h3>Database Driver not found: " + e.getMessage() + "</h3>");
        } finally {
            closeResources();
        }
    }

    private String createDeleteQuery(String userType) {
        if ("student".equalsIgnoreCase(userType)) {
            return "DELETE FROM student WHERE ENROLLMENT = ?";
        } else if ("staff".equalsIgnoreCase(userType)) {
            return "DELETE FROM staff WHERE ENROLLMENT = ?";
        } else {
            return null;
        }
    }

    private void handleSQLException(PrintWriter pw, SQLException e) {
        try {
            if (con != null) {
                con.rollback();
            }
            pw.println("<h3 style='color:red;'>SQL Error: " + e.getMessage() + "</h3>");
        } catch (SQLException se) {
            pw.println("<h3 style='color:red;'>Rollback failed: " + se.getMessage() + "</h3>");
        }
    }

    private void handleError(PrintWriter pw, Exception e) {
        pw.println("<h3 style='color:red;'>Error: " + e.getMessage() + "</h3>");
    }

    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (con != null && !con.isClosed()) con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        closeResources();
    }
}

            
