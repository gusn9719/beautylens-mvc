package kr.ac.kopo.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 상품명 화면 표시용 정제 유틸리티 (v1.1 safe 규칙만 적용)
 *
 * 적용 원칙:
 *   - Python propose_display_names_v1_1.py 의 riskLevel=safe 케이스만 정제
 *   - review/unsafe/NO_CHANGE → 원본 productName 그대로 반환
 *   - DB PRODUCT_NAME 불변, Mapper 수정 없음
 */
public final class DisplayNameCleaner {

    private DisplayNameCleaner() {}

    // ─── Risk 열거 ────────────────────────────────────────────────
    private enum Risk { SAFE, REVIEW, UNSAFE }

    private static Risk maxRisk(Risk a, Risk b) {
        return (a.ordinal() >= b.ordinal()) ? a : b;
    }

    // ─── Rule 1-A: 앞 대괄호 safe 키워드 ─────────────────────────
    private static final List<String> SAFE_LEADING_KW = Arrays.asList(
        "올영픽", "하루특가", "타임어택", "타임 어택", "무신사 단독", "무신사단독",
        "사은품", "포켓몬", "콜라보", "런칭", "에디션", "한정판", "시간한정",
        "올영", "올리브영픽", "위클리픽", "오늘의픽"
    );

    // ─── Rule 1-B: 앞 대괄호 review 키워드 ───────────────────────
    private static final List<String> REVIEW_LEADING_KW = Arrays.asList(
        "PICK", "NEW", "단독", "기획", "증정", "1+1", "세일", "특가",
        "한정", "할인", "이벤트", "프로모션", "쿠폰", "무료배송", "리뉴얼"
    );

    // ─── Rule 1-C: 마케팅 클레임 키워드 ──────────────────────────
    private static final List<String> MARKETING_CLAIM_KW = Arrays.asList(
        "1위", "연속", "수상", "판매량", "재구매율", "랭킹"
    );
    // "N년" + (위 or 연속) 패턴
    private static final Pattern YEAR_CLAIM_RE =
        Pattern.compile("\\d+\\s*년");

    // ─── [SET] 보존 ────────────────────────────────────────────────
    private static final Pattern SET_RE =
        Pattern.compile("^set$", Pattern.CASE_INSENSITIVE);

    // ─── 앞 대괄호에 수량/용량 있으면 보존 ────────────────────────
    private static final Pattern PRODUCT_IN_BRACKET_RE = Pattern.compile(
        "\\d+\\s*(ml|g|mg|L|oz|매|개|입|팩|pack|PACK|pcs)",
        Pattern.CASE_INSENSITIVE
    );

    // ─── 앞 대괄호 추출 패턴 ──────────────────────────────────────
    private static final Pattern LEADING_BRACKET_RE =
        Pattern.compile("^\\[([^\\]]+)\\]\\s*");

    // ─── 볼륨 패턴 (safe/review 판별용) ──────────────────────────
    private static final Pattern VOLUME_RE = Pattern.compile(
        "\\d+\\s*(ml|ML|g|mg|L|oz|매|개|입|팩)",
        Pattern.CASE_INSENSITIVE
    );

    // ─── 증정품 키워드 ────────────────────────────────────────────
    private static final List<String> GIFT_WORDS = Arrays.asList(
        "키링", "스티커", "파우치", "거울", "타올", "타월", "케이스", "아크릴",
        "포스트잇", "마켓백", "브러쉬", "굿즈", "미니어처", "액정클리너",
        "머리핀", "헤어핀", "클리너", "틴케이스", "이어폰", "볼펜", "부채",
        "에코백", "집게", "집게핀", "클립", "쿠션", "샘플"
    );

    // ─── Rule 2-A: +(증정) xxx at end ────────────────────────────
    private static final Pattern GIFT_A_RE = Pattern.compile(
        "\\s*\\+\\s*\\(?증정[품]?\\)?\\s+[\\uAC00-\\uD7A3A-Za-z0-9\\s()]+$"
    );

    // ─── Rule 2-B: (+xxx) at end ─────────────────────────────────
    private static final Pattern GIFT_B_RE = Pattern.compile(
        "\\s*\\(\\s*\\+([^)]+)\\)\\s*$"
    );

    // ─── Rule 2-C: 괄호 없는 끝 증정품 ──────────────────────────
    private static final Pattern BARE_GIFT_RE;
    static {
        StringBuilder sb = new StringBuilder(
            "\\s*\\+\\s*[\\uAC00-\\uD7A3A-Za-z0-9\\s]*?(");
        for (int i = 0; i < GIFT_WORDS.size(); i++) {
            if (i > 0) sb.append("|");
            sb.append(Pattern.quote(GIFT_WORDS.get(i)));
        }
        sb.append(")\\s*$");
        BARE_GIFT_RE = Pattern.compile(sb.toString());
    }

    // ─── Rule 2-D: (증정: xxx) at end ────────────────────────────
    private static final Pattern COLON_GIFT_RE = Pattern.compile(
        "\\s*\\(증정\\s*:\\s*[^)]+\\)\\s*$"
    );

    // ─── 공개 API ─────────────────────────────────────────────────

    /**
     * productName → displayName
     * riskLevel=safe이면 정제명, 그 외이면 원본 반환
     */
    public static String clean(String productName) {
        if (productName == null) return null;
        String orig = productName.trim();
        if (orig.isEmpty()) return orig;

        CleanResult r = cleanInternal(orig);
        if (r.risk == Risk.SAFE && r.changed && r.name.length() >= 8) {
            return r.name;
        }
        return orig;
    }

    // ─── 내부 결과 ────────────────────────────────────────────────

    private static final class CleanResult {
        final String name;
        final Risk   risk;
        final boolean changed;
        CleanResult(String name, Risk risk, boolean changed) {
            this.name    = name;
            this.risk    = risk;
            this.changed = changed;
        }
    }

    // ─── 규칙 파이프라인 (Python v1.1 리스크 계산 동일 적용) ────────

    private static CleanResult cleanInternal(String orig) {
        String  name    = orig;
        Risk    risk    = Risk.SAFE;
        boolean changed = false;

        // Rule 1: 앞 대괄호 반복 처리
        while (true) {
            Matcher m = LEADING_BRACKET_RE.matcher(name);
            if (!m.find()) break;
            String content = m.group(1);

            // [SET] 항상 보존
            if (SET_RE.matcher(content).matches()) break;
            // 수량/용량 정보 대괄호 보존
            if (PRODUCT_IN_BRACKET_RE.matcher(content).find()) break;

            // Rule 1-A: safe 키워드 → safe 제거
            if (containsSafeKw(content)) {
                name = name.substring(m.end()).trim();
                changed = true;
                continue;
            }
            // Rule 1-B: review 키워드 → review 표시 후 제거
            if (containsReviewKw(content)) {
                name = name.substring(m.end()).trim();
                changed = true;
                risk = maxRisk(risk, Risk.REVIEW);
                continue;
            }
            // Rule 1-C: 마케팅 클레임 → review 표시 후 제거
            if (isMarketingClaim(content)) {
                name = name.substring(m.end()).trim();
                changed = true;
                risk = maxRisk(risk, Risk.REVIEW);
                continue;
            }
            break; // 알 수 없는 대괄호 → 중단
        }

        // Rule 2-A: +(증정) xxx at end
        Matcher mA = GIFT_A_RE.matcher(name);
        if (mA.find()) {
            String suffix = mA.group();
            boolean hasVol = VOLUME_RE.matcher(suffix).find();
            name = name.substring(0, mA.start()).trim();
            changed = true;
            risk = maxRisk(risk, hasVol ? Risk.REVIEW : Risk.SAFE);
        }

        // Rule 2-B: (+xxx) 반복 처리
        while (true) {
            Matcher mB = GIFT_B_RE.matcher(name);
            if (!mB.find()) break;
            String  inner   = mB.group(1).trim();
            boolean hasGift = containsGiftWord(inner);
            boolean has증정  = inner.contains("증정");
            boolean hasVol  = VOLUME_RE.matcher(inner).find();

            if (hasGift || has증정) {
                name = name.substring(0, mB.start()).trim();
                changed = true;
                // 볼륨 있고 gift 키워드 없으면 review
                Risk pr = (hasVol && !hasGift) ? Risk.REVIEW : Risk.SAFE;
                risk = maxRisk(risk, pr);
                continue;
            }
            break;
        }

        // Rule 2-C: 괄호 없는 끝 증정품 → 항상 review
        Matcher mC = BARE_GIFT_RE.matcher(name);
        if (mC.find()) {
            String remaining = name.substring(0, mC.start());
            if (VOLUME_RE.matcher(remaining).find()) {
                name = remaining.trim();
                changed = true;
                risk = maxRisk(risk, Risk.REVIEW);
            }
        }

        // Rule 2-D: (증정: xxx) → 항상 review
        Matcher mD = COLON_GIFT_RE.matcher(name);
        if (mD.find()) {
            name = name.substring(0, mD.start()).trim();
            changed = true;
            risk = maxRisk(risk, Risk.REVIEW);
        }

        // Rule 4: 연속 공백 정규화 (항상 safe)
        String normalized = name.replaceAll("  +", " ").trim();
        if (!normalized.equals(name)) {
            name = normalized;
            changed = true;
        }

        // Post-check: 8자 미만 → unsafe
        if (name.length() < 8) {
            return new CleanResult(orig, Risk.UNSAFE, false);
        }

        return new CleanResult(name, risk, changed);
    }

    // ─── 헬퍼 ────────────────────────────────────────────────────

    private static boolean containsSafeKw(String content) {
        String lower = content.toLowerCase();
        for (String kw : SAFE_LEADING_KW) {
            if (lower.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    private static boolean containsReviewKw(String content) {
        String upper = content.toUpperCase();
        for (String kw : REVIEW_LEADING_KW) {
            if (upper.contains(kw.toUpperCase())) return true;
        }
        return false;
    }

    private static boolean isMarketingClaim(String content) {
        for (String kw : MARKETING_CLAIM_KW) {
            if (content.contains(kw)) return true;
        }
        // "N년 + 위/연속" 복합 패턴
        if (YEAR_CLAIM_RE.matcher(content).find()
                && (content.contains("위") || content.contains("연속"))) {
            return true;
        }
        return false;
    }

    private static boolean containsGiftWord(String text) {
        for (String gw : GIFT_WORDS) {
            if (text.contains(gw)) return true;
        }
        return false;
    }
}
