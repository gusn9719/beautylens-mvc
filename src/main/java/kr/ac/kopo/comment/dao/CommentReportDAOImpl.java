package kr.ac.kopo.comment.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import kr.ac.kopo.comment.vo.CommentReportVO;

@Repository
public class CommentReportDAOImpl implements CommentReportDAO {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public int existsReport(Map<String, Object> param) {
        return sqlSessionTemplate.selectOne("comment.report.dao.CommentReportDAO.existsReport", param);
    }

    @Override
    public void insert(CommentReportVO report) {
        sqlSessionTemplate.insert("comment.report.dao.CommentReportDAO.insert", report);
    }

    @Override
    public List<CommentReportVO> selectAll(Map<String, Object> param) {
        return sqlSessionTemplate.selectList("comment.report.dao.CommentReportDAO.selectAll", param);
    }

    @Override
    public void resolve(Map<String, Object> param) {
        sqlSessionTemplate.update("comment.report.dao.CommentReportDAO.resolve", param);
    }
}
