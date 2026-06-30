package kr.ac.kopo.admin.dao;

import java.util.List;
import java.util.Map;

import kr.ac.kopo.admin.vo.AdminAuditLogVO;

public interface AdminAuditLogDAO {
    void insert(AdminAuditLogVO log);
    List<AdminAuditLogVO> selectLogs(Map<String, Object> param);
}
