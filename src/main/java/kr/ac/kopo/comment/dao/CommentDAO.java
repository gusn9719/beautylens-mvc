package kr.ac.kopo.comment.dao;

import kr.ac.kopo.comment.vo.CommentVO;
import java.util.List;
import java.util.Map;

public interface CommentDAO {
    List<CommentVO> selectByProductId(int productId);
    List<CommentVO> selectAll(Map<String, Object> param);
    List<CommentVO> selectByMemberId(int memberId);
    void insert(CommentVO comment);
    void updateContent(Map<String, Object> param);
    CommentVO selectOne(int commentId);
    void softDelete(Map<String, Object> param);
    void restore(Map<String, Object> param);
    int existsProduct(int productId);
}
