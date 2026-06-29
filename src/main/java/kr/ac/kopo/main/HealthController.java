package kr.ac.kopo.main;

import java.sql.Connection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.kopo.common.vo.ApiResponse;

@RestController
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/api/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("app", "beautylens-mvc");
        info.put("version", "1.0.0");

        String dbStatus;
        try (Connection conn = dataSource.getConnection()) {
            dbStatus = conn.isValid(2) ? "ok" : "error";
        } catch (Exception e) {
            dbStatus = "error: " + e.getMessage();
        }

        info.put("db", dbStatus);
        info.put("timestamp", new Date().toString());

        boolean ok = "ok".equals(dbStatus);
        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
            ok,
            ok ? "healthy" : "db connection failed",
            info
        );

        return ResponseEntity
            .status(ok ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
            .body(response);
    }
}
