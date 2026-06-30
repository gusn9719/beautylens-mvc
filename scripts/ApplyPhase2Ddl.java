import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ApplyPhase2Ddl {
    public static void main(String[] args) throws Exception {
        Class.forName("oracle.jdbc.OracleDriver");
        try (Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/xepdb1", "hr", "hr")) {
            conn.setAutoCommit(false);
            createTableIfMissing(conn, "BL_PRODUCT_ADMIN_FLAGS", """
                CREATE TABLE BL_PRODUCT_ADMIN_FLAGS (
                  PRODUCT_ID             NUMBER PRIMARY KEY,
                  IS_VISIBLE             CHAR(1) DEFAULT 'Y' NOT NULL,
                  EXCLUDE_RECOMMENDATION CHAR(1) DEFAULT 'N' NOT NULL,
                  IS_FEATURED            CHAR(1) DEFAULT 'N' NOT NULL,
                  QUALITY_STATUS         VARCHAR2(40) DEFAULT 'NORMAL' NOT NULL,
                  HIDE_REASON            VARCHAR2(300),
                  ADMIN_MEMO             VARCHAR2(1000),
                  UPDATED_BY             NUMBER,
                  UPDATED_AT             DATE DEFAULT SYSDATE,
                  CONSTRAINT FK_PRODUCT_FLAGS_PRODUCT FOREIGN KEY (PRODUCT_ID) REFERENCES BL_PRODUCTS(PRODUCT_ID),
                  CONSTRAINT FK_PRODUCT_FLAGS_ADMIN FOREIGN KEY (UPDATED_BY) REFERENCES BL_MEMBERS(MEMBER_ID),
                  CONSTRAINT CK_PRODUCT_FLAGS_VISIBLE CHECK (IS_VISIBLE IN ('Y', 'N')),
                  CONSTRAINT CK_PRODUCT_FLAGS_EXCLUDE CHECK (EXCLUDE_RECOMMENDATION IN ('Y', 'N')),
                  CONSTRAINT CK_PRODUCT_FLAGS_FEATURED CHECK (IS_FEATURED IN ('Y', 'N')),
                  CONSTRAINT CK_PRODUCT_FLAGS_QUALITY CHECK (QUALITY_STATUS IN (
                    'NORMAL', 'IMAGE_MISSING', 'LOW_REVIEW', 'HIGH_CAUTION',
                    'NAME_REVIEW_NEEDED', 'LINK_BROKEN'
                  ))
                )
                """);
            conn.commit();
            verify(conn, "BL_PRODUCT_ADMIN_FLAGS");
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
