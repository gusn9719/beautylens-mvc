import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ApplyPhase7Ddl {
    public static void main(String[] args) throws Exception {
        Class.forName("oracle.jdbc.OracleDriver");
        try (Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/xepdb1", "hr", "hr")) {
            conn.setAutoCommit(false);
            createSequenceIfMissing(conn, "SEQ_BL_ADMIN_AUDIT_LOGS");
            createTableIfMissing(conn, "BL_ADMIN_AUDIT_LOGS", """
                CREATE TABLE BL_ADMIN_AUDIT_LOGS (
                  LOG_ID       NUMBER PRIMARY KEY,
                  ADMIN_ID     NUMBER NOT NULL,
                  ACTION_TYPE  VARCHAR2(60) NOT NULL,
                  TARGET_TYPE  VARCHAR2(60) NOT NULL,
                  TARGET_ID    NUMBER NOT NULL,
                  BEFORE_VALUE CLOB,
                  AFTER_VALUE  CLOB,
                  CREATED_AT   DATE DEFAULT SYSDATE,
                  CONSTRAINT FK_ADMIN_LOG_ADMIN FOREIGN KEY (ADMIN_ID) REFERENCES BL_MEMBERS(MEMBER_ID)
                )
                """);
            conn.commit();
            verify(conn, "BL_ADMIN_AUDIT_LOGS");
        }
    }

    private static void createSequenceIfMissing(Connection conn, String name) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '" + name + "'")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                st.execute("CREATE SEQUENCE " + name + " START WITH 1 NOCACHE");
                System.out.println("created sequence " + name);
            } else {
                System.out.println("sequence exists " + name);
            }
        }
    }

    private static void createTableIfMissing(Connection conn, String name, String ddl) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = '" + name + "'")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                st.execute(ddl);
                System.out.println("created table " + name);
            } else {
                System.out.println("table exists " + name);
            }
        }
    }

    private static void verify(Connection conn, String name) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = '" + name + "'")) {
            rs.next();
            System.out.println("verified " + name + "=" + rs.getInt(1));
        }
    }
}
