import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ApplyPhase1Ddl {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/xepdb1";
    private static final String USER = "hr";
    private static final String PASSWORD = "hr";

    public static void main(String[] args) throws Exception {
        Class.forName("oracle.jdbc.OracleDriver");
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);
            createSequenceIfMissing(conn, "SEQ_BL_PRODUCT_FAVORITES");
            createSequenceIfMissing(conn, "SEQ_BL_PRODUCT_RATINGS");
            createSequenceIfMissing(conn, "SEQ_BL_RECOMMENDATION_FEEDBACK");
            createSequenceIfMissing(conn, "SEQ_BL_USER_PRODUCT_EVENTS");

            createTableIfMissing(conn, "BL_PRODUCT_FAVORITES", """
                CREATE TABLE BL_PRODUCT_FAVORITES (
                  FAVORITE_ID NUMBER PRIMARY KEY,
                  MEMBER_ID   NUMBER NOT NULL,
                  PRODUCT_ID  NUMBER NOT NULL,
                  CREATED_AT  DATE DEFAULT SYSDATE,
                  CONSTRAINT FK_PRODUCT_FAV_MEMBER FOREIGN KEY (MEMBER_ID) REFERENCES BL_MEMBERS(MEMBER_ID),
                  CONSTRAINT FK_PRODUCT_FAV_PRODUCT FOREIGN KEY (PRODUCT_ID) REFERENCES BL_PRODUCTS(PRODUCT_ID),
                  CONSTRAINT UQ_PRODUCT_FAV UNIQUE (MEMBER_ID, PRODUCT_ID)
                )
                """);

            createTableIfMissing(conn, "BL_PRODUCT_RATINGS", """
                CREATE TABLE BL_PRODUCT_RATINGS (
                  RATING_ID         NUMBER PRIMARY KEY,
                  MEMBER_ID         NUMBER NOT NULL,
                  PRODUCT_ID        NUMBER NOT NULL,
                  RATING            NUMBER(2,1) NOT NULL,
                  SKIN_TYPE_AT_TIME VARCHAR2(20),
                  IRRITATION_YN     CHAR(1),
                  REPURCHASE_YN     CHAR(1),
                  REVIEW_TEXT       CLOB,
                  CREATED_AT        DATE DEFAULT SYSDATE,
                  UPDATED_AT        DATE,
                  CONSTRAINT FK_PRODUCT_RATING_MEMBER FOREIGN KEY (MEMBER_ID) REFERENCES BL_MEMBERS(MEMBER_ID),
                  CONSTRAINT FK_PRODUCT_RATING_PRODUCT FOREIGN KEY (PRODUCT_ID) REFERENCES BL_PRODUCTS(PRODUCT_ID),
                  CONSTRAINT UQ_PRODUCT_RATING UNIQUE (MEMBER_ID, PRODUCT_ID),
                  CONSTRAINT CK_PRODUCT_RATING_SCORE CHECK (RATING BETWEEN 1 AND 5),
                  CONSTRAINT CK_PRODUCT_RATING_IRRITATION CHECK (IRRITATION_YN IS NULL OR IRRITATION_YN IN ('Y', 'N')),
                  CONSTRAINT CK_PRODUCT_RATING_REPURCHASE CHECK (REPURCHASE_YN IS NULL OR REPURCHASE_YN IN ('Y', 'N'))
                )
                """);

            createTableIfMissing(conn, "BL_RECOMMENDATION_FEEDBACK", """
                CREATE TABLE BL_RECOMMENDATION_FEEDBACK (
                  FEEDBACK_ID       NUMBER PRIMARY KEY,
                  MEMBER_ID         NUMBER NOT NULL,
                  PRODUCT_ID        NUMBER NOT NULL,
                  FEEDBACK_TYPE     VARCHAR2(30) NOT NULL,
                  SKIN_TYPE_AT_TIME VARCHAR2(20),
                  CREATED_AT        DATE DEFAULT SYSDATE,
                  CONSTRAINT FK_REC_FB_MEMBER FOREIGN KEY (MEMBER_ID) REFERENCES BL_MEMBERS(MEMBER_ID),
                  CONSTRAINT FK_REC_FB_PRODUCT FOREIGN KEY (PRODUCT_ID) REFERENCES BL_PRODUCTS(PRODUCT_ID),
                  CONSTRAINT UQ_REC_FB_MEMBER_PRODUCT UNIQUE (MEMBER_ID, PRODUCT_ID),
                  CONSTRAINT CK_REC_FB_TYPE CHECK (FEEDBACK_TYPE IN ('LIKE', 'DISLIKE', 'NOT_INTERESTED'))
                )
                """);

            createTableIfMissing(conn, "BL_USER_PRODUCT_EVENTS", """
                CREATE TABLE BL_USER_PRODUCT_EVENTS (
                  EVENT_ID          NUMBER PRIMARY KEY,
                  MEMBER_ID         NUMBER NOT NULL,
                  PRODUCT_ID        NUMBER NOT NULL,
                  EVENT_TYPE        VARCHAR2(40) NOT NULL,
                  EVENT_VALUE       VARCHAR2(200),
                  SKIN_TYPE_AT_TIME VARCHAR2(20),
                  CREATED_AT        DATE DEFAULT SYSDATE,
                  CONSTRAINT FK_USER_EVENT_MEMBER FOREIGN KEY (MEMBER_ID) REFERENCES BL_MEMBERS(MEMBER_ID),
                  CONSTRAINT FK_USER_EVENT_PRODUCT FOREIGN KEY (PRODUCT_ID) REFERENCES BL_PRODUCTS(PRODUCT_ID),
                  CONSTRAINT CK_USER_EVENT_TYPE CHECK (EVENT_TYPE IN (
                    'VIEW', 'DETAIL_VIEW', 'FAVORITE', 'UNFAVORITE', 'RATE', 'COMMENT',
                    'RECOMMEND_LIKE', 'RECOMMEND_DISLIKE', 'NOT_INTERESTED', 'RECOMMEND_FEEDBACK'
                  ))
                )
                """);

            conn.commit();
            printTableCheck(conn);
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

    private static void printTableCheck(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("""
                 SELECT TABLE_NAME
                   FROM USER_TABLES
                  WHERE TABLE_NAME IN (
                    'BL_PRODUCT_FAVORITES',
                    'BL_PRODUCT_RATINGS',
                    'BL_RECOMMENDATION_FEEDBACK',
                    'BL_USER_PRODUCT_EVENTS'
                  )
                  ORDER BY TABLE_NAME
                 """)) {
            while (rs.next()) {
                System.out.println("verified " + rs.getString(1));
            }
        }
    }
}
