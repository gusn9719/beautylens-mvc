package kr.ac.kopo.comment.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.ac.kopo.comment.dao.CommentReportDAO;
import kr.ac.kopo.comment.vo.CommentReportVO;
import kr.ac.kopo.common.util.DisplayNameCleaner;
import kr.ac.kopo.member.vo.MemberVO;

@Service
public class CommentReportServiceImpl implements CommentReportService {

    @Autowired
    private CommentReportDAO commentReportDAO;

    @Override
    public void report(MemberVO reporter, int commentId, String reasonType, String reasonText) {
        Map<String, Object> existsParam = new HashMap<>();
        existsParam.put("commentId", commentId);
        existsParam.put("reporterId", reporter.getMemberId());
        if (commentReportDAO.existsReport(existsParam) > 0) {
            return;
        }
        CommentReportVO report = new CommentReportVO();
        report.setCommentId(commentId);
        report.setReporterId(reporter.getMemberId());
        report.setReasonType(normalizeReason(reasonType));
        report.setReasonText(reasonText);
        commentReportDAO.insert(report);
    }

    @Override
    public List<CommentReportVO> getReports(String status, int size) {
        Map<String, Object> param = new HashMap<>();
        param.put("status", status);
        param.put("size", size);
        List<CommentReportVO> list = commentReportDAO.selectAll(param);
        for (CommentReportVO report : list) {
            report.setDisplayName(DisplayNameCleaner.clean(report.getProductName()));
        }
        return list;
    }

    @Override
    public void resolve(MemberVO admin, int reportId, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("reportId", reportId);
        param.put("handledBy", admin.getMemberId());
        param.put("status", status == null || status.isBlank() ? "RESOLVED" : status.toUpperCase(Locale.ROOT));
        commentReportDAO.resolve(param);
    }

    private String normalizeReason(String reasonType) {
        if (reasonType == null || reasonType.isBlank()) return "ETC";
        String value = reasonType.toUpperCase(Locale.ROOT);
        return switch (value) {
            case "SPAM", "ABUSE", "AD", "FALSE_INFO", "ETC" -> value;
            default -> "ETC";
        };
    }
}
