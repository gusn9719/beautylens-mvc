package kr.ac.kopo.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminViewController {

    @GetMapping({"/admin", "/admin/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "관리자 대시보드 | BeautyLens");
        model.addAttribute("activePage", "admin");
        return "admin/dashboard";
    }

    @GetMapping("/admin/comments")
    public String comments(Model model) {
        model.addAttribute("pageTitle", "댓글 관리 | BeautyLens");
        model.addAttribute("activePage", "admin");
        return "admin/comments";
    }

    @GetMapping("/comment-test")
    public String commentTest() {
        return "comment/test";
    }

    @GetMapping("/admin/products")
    public String products(Model model) {
        model.addAttribute("pageTitle", "상품 운영 관리 | BeautyLens");
        model.addAttribute("activePage", "admin");
        return "admin/products";
    }

    @GetMapping("/admin/comment-reports")
    public String commentReports(Model model) {
        model.addAttribute("pageTitle", "댓글 신고 관리 | BeautyLens");
        model.addAttribute("activePage", "admin");
        return "admin/comment_reports";
    }

    @GetMapping("/admin/logs")
    public String logs(Model model) {
        model.addAttribute("pageTitle", "관리자 활동 로그 | BeautyLens");
        model.addAttribute("activePage", "admin");
        return "admin/logs";
    }
}
