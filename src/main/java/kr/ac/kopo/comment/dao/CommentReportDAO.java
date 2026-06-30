package kr.ac.kopo.comment.dao;

import java.util.List;
import java.util.Map;

import kr.ac.kopo.comment.vo.CommentReportVO;

public interface CommentReportDAO {
    int existsReport(Map<String, Object> param);
    void insert(CommentReportVO report);
    List<CommentReportVO> selectAll(Map<String, Object> param);
    void resolve(Map<String, Object> param);
}
