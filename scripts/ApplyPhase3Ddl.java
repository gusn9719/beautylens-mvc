import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ApplyPhase3Ddl {
    public static void main(String[] args) throws Exception {
        Class.forName("oracle.jdbc.OracleDriver");
        try (Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/xepdb1", "hr", "hr")) {
            conn.setAutoCommit(false);
            createSequenceIfMissing(conn, "SEQ_BL_COMMENT_REPORTS");
            createTableIfMissing(conn, "BL_COMMENT_REPORTS", """
                CREATE TABLE BL_COMMENT_REPORTS (
                  REPORT_ID   NUMBER PRIMARY KEY,
                  COMMENT_ID  NUMBER NOT NULL,
                  REPORTER_ID NUMBER NOT NULL,
                  REASON_TYPE VARCHAR2(30) NOT NULL,
                  REASON_TEXT VARCHAR2(1000),
                  STATUS      VARCHAR2(30) DEFAULT 'PENDING' NOT NULL,
                  HANDLED_BY  NUMBER,
                  HANDLED_AT  DATE,
                  CREATED_AT  DATE DEFAULT SYSDATE,
                  CONSTRAINT FK_COMMENT_REPORT_COMMENT FOREIGN KEY (COMMENT_ID) REFERENCES BL_PRODUCT_COMMENTS(COMMENT_ID),
                  CONSTRAINT FK_COMMENT_REPORT_REPORTER FOREIGN KEY (REPORTER_ID) REFERENCES BL_MEMBERS(MEMBER_ID),
                  CONSTRAINT FK_COMMENT_REPORT_HANDLER FOREIGN KEY (HANDLED_BY) REFERENCES BL_MEMBERS(MEMBER_ID),
                  CONSTRAINT UQ_COMMENT_REPORT UNIQUE (COMMENT_ID, REPORTER_ID),
                  CONSTRAINT CK_COMMENT_REPORT_REASON CHECK (REASON_TYPE IN ('SPAM', 'ABUSE', 'AD', 'FALSE_INFO', 'ETC')),
                  CONSTRAINT CK_COMMENT_REPORT_STATUS CHECK (STATUS IN ('PENDING', 'RESOLVED', 'REJECTED'))
                )
                """);
            conn.commit();
            verify(conn, "BL_COMMENT_REPORTS");
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
