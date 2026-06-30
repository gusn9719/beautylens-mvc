package kr.ac.kopo.main;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DemoViewController {

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("pageTitle", "로그인 | BeautyLens");
        model.addAttribute("activePage", "login");
        return "demo/login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("pageTitle", "회원가입 | BeautyLens");
        model.addAttribute("activePage", "signup");
        return "demo/signup";
    }

    @GetMapping("/mypage")
    public String mypage(Model model) {
        model.addAttribute("pageTitle", "마이페이지 | BeautyLens");
        model.addAttribute("activePage", "mypage");
        return "demo/mypage";
    }

    @GetMapping("/recommend")
    public String recommend(Model model) {
        model.addAttribute("pageTitle", "추천 상품 | BeautyLens");
        model.addAttribute("activePage", "recommend");
        return "demo/recommend";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("pageTitle", "상품 탐색 | BeautyLens");
        model.addAttribute("activePage", "products");
        return "demo/products";
    }

    @GetMapping("/products/{productId}")
    public String productDetail(@PathVariable int productId, Model model) {
        model.addAttribute("pageTitle", "상품 상세 | BeautyLens");
        model.addAttribute("activePage", "products");
        model.addAttribute("productId", productId);
        return "demo/product_detail";
    }
}
