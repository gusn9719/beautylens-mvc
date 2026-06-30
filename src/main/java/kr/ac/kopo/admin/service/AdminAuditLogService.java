package kr.ac.kopo.admin.service;

import java.util.List;

import kr.ac.kopo.admin.vo.AdminAuditLogVO;
import kr.ac.kopo.member.vo.MemberVO;

public interface AdminAuditLogService {
    void log(MemberVO admin, String actionType, String targetType, int targetId, String beforeValue, String afterValue);
    List<AdminAuditLogVO> getLogs(String actionType, String targetType, Integer adminId, int size);
}
