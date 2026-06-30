package kr.ac.kopo.admin.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.admin.vo.AdminAuditLogVO;

@Repository
public class AdminAuditLogDAOImpl implements AdminAuditLogDAO {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public void insert(AdminAuditLogVO log) {
        sqlSessionTemplate.insert("admin.audit.dao.AdminAuditLogDAO.insert", log);
    }

    @Override
    public List<AdminAuditLogVO> selectLogs(Map<String, Object> param) {
        return sqlSessionTemplate.selectList("admin.audit.dao.AdminAuditLogDAO.selectLogs", param);
    }
}
