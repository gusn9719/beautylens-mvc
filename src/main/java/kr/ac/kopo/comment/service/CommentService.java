package kr.ac.kopo.comment.service;

import kr.ac.kopo.comment.vo.CommentVO;
import java.util.List;
import java.util.Map;

public interface CommentService {
    List<CommentVO> getComments(int productId);
    List<CommentVO> getAllComments(String status, int size);
    List<CommentVO> getMyComments(int memberId);
    void postComment(int productId, int memberId, String content);
    void updateComment(int commentId, int memberId, String content);
    void deleteComment(int commentId, int deletedById, String reason);
    void restoreComment(int commentId, int restoredById);
    CommentVO getComment(int commentId);
    boolean productExists(int productId);
}
