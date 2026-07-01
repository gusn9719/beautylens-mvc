package kr.ac.kopo.comment.service;

import kr.ac.kopo.comment.dao.CommentDAO;
import kr.ac.kopo.comment.vo.CommentVO;
import kr.ac.kopo.common.util.DisplayNameCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentDAO commentDAO;

    @Override
    public List<CommentVO> getComments(int productId) {
        return commentDAO.selectByProductId(productId);
    }

    @Override
    public List<CommentVO> getAllComments(String status, int size) {
        Map<String, Object> param = new HashMap<>();
        param.put("status", status);
        param.put("size", size);
        return commentDAO.selectAll(param);
    }

    @Override
    public List<CommentVO> getMyComments(int memberId) {
        List<CommentVO> list = commentDAO.selectByMemberId(memberId);
        for (CommentVO comment : list) {
            comment.setDisplayName(DisplayNameCleaner.clean(comment.getProductName()));
        }
        return list;
    }

    @Override
    public void postComment(int productId, int memberId, String content) {
        CommentVO c = new CommentVO();
        c.setProductId(productId);
        c.setMemberId(memberId);
        c.setContent(content);
        commentDAO.insert(c);
    }

    @Override
    public void updateComment(int commentId, int memberId, String content) {
        Map<String, Object> param = new HashMap<>();
        param.put("commentId", commentId);
        param.put("memberId", memberId);
        param.put("content", content);
        commentDAO.updateContent(param);
    }

    @Override
    public void deleteComment(int commentId, int deletedById, String reason) {
        Map<String, Object> param = new HashMap<>();
        param.put("commentId", commentId);
        param.put("adminId", deletedById);
        param.put("reason", reason);
        commentDAO.softDelete(param);
    }

    @Override
    public void restoreComment(int commentId, int restoredById) {
        Map<String, Object> param = new HashMap<>();
        param.put("commentId", commentId);
        param.put("adminId", restoredById);
        commentDAO.restore(param);
    }

    @Override
    public CommentVO getComment(int commentId) {
        return commentDAO.selectOne(commentId);
    }

    @Override
    public boolean productExists(int productId) {
        return commentDAO.existsProduct(productId) > 0;
    }
}
