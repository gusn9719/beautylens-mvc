package kr.ac.kopo.comment.dao;

import kr.ac.kopo.comment.vo.CommentVO;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class CommentDAOImpl implements CommentDAO {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public List<CommentVO> selectByProductId(int productId) {
        return sqlSessionTemplate.selectList("comment.dao.CommentDAO.selectByProductId", productId);
    }

    @Override
    public List<CommentVO> selectAll(Map<String, Object> param) {
        return sqlSessionTemplate.selectList("comment.dao.CommentDAO.selectAll", param);
    }

    @Override
    public List<CommentVO> selectByMemberId(int memberId) {
        return sqlSessionTemplate.selectList("comment.dao.CommentDAO.selectByMemberId", memberId);
    }

    @Override
    public void insert(CommentVO comment) {
        sqlSessionTemplate.insert("comment.dao.CommentDAO.insert", comment);
    }

    @Override
    public void updateContent(Map<String, Object> param) {
        sqlSessionTemplate.update("comment.dao.CommentDAO.updateContent", param);
    }

    @Override
    public CommentVO selectOne(int commentId) {
        return sqlSessionTemplate.selectOne("comment.dao.CommentDAO.selectOne", commentId);
    }

    @Override
    public void softDelete(Map<String, Object> param) {
        sqlSessionTemplate.update("comment.dao.CommentDAO.softDelete", param);
    }

    @Override
    public void restore(Map<String, Object> param) {
        sqlSessionTemplate.update("comment.dao.CommentDAO.restore", param);
    }

    @Override
    public int existsProduct(int productId) {
        return sqlSessionTemplate.selectOne("comment.dao.CommentDAO.existsProduct", productId);
    }
}
