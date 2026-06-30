package kr.ac.kopo.comment.service;

import java.util.List;

import kr.ac.kopo.comment.vo.CommentReportVO;
import kr.ac.kopo.member.vo.MemberVO;

public interface CommentReportService {
    void report(MemberVO reporter, int commentId, String reasonType, String reasonText);
    List<CommentReportVO> getReports(String status, int size);
    void resolve(MemberVO admin, int reportId, String status);
}
