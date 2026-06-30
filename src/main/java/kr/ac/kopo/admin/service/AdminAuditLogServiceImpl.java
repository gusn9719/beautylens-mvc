package kr.ac.kopo.admin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.admin.dao.AdminAuditLogDAO;
import kr.ac.kopo.admin.vo.AdminAuditLogVO;
import kr.ac.kopo.member.vo.MemberVO;

@Service
public class AdminAuditLogServiceImpl implements AdminAuditLogService {

    @Autowired
    private AdminAuditLogDAO adminAuditLogDAO;

    @Override
    public void log(MemberVO admin, String actionType, String targetType, int targetId, String beforeValue, String afterValue) {
        if (admin == null) return;
        AdminAuditLogVO log = new AdminAuditLogVO();
        log.setAdminId(admin.getMemberId());
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setBeforeValue(beforeValue);
        log.setAfterValue(afterValue);
        adminAuditLogDAO.insert(log);
    }

    @Override
    public List<AdminAuditLogVO> getLogs(String actionType, String targetType, Integer adminId, int size) {
        Map<String, Object> param = new HashMap<>();
        param.put("actionType", actionType);
        param.put("targetType", targetType);
        param.put("adminId", adminId);
        param.put("size", size);
        return adminAuditLogDAO.selectLogs(param);
    }
}
