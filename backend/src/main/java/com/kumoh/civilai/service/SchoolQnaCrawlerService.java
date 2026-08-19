package com.kumoh.civilai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kumoh.civilai.domain.document.DocumentSource;
import com.kumoh.civilai.domain.document.SourceDocument;
import com.kumoh.civilai.domain.document.SourceDocumentRepository;
import com.kumoh.civilai.dto.document.CrawlResult;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Set;
@Service
@RequiredArgsConstructor
public class SchoolQnaCrawlerService {

    private static final String QNA_LIST_URL =
            "https://www.kumoh.ac.kr/ko/sub06_03_02.do";

    private static final String COMMENT_LIST_URL =
            "https://www.kumoh.ac.kr/app/comment/ajax/list.do";

    /*
     * 기존 10개씩 가져오면 목록 요청 수가 많아진다.
     * QnA는 로그인 세션 시간이 짧아 보이므로 50개씩 가져와서 전체 시간을 줄인다.
     */
    private static final int PAGE_LIMIT = 10;

    /*
     * 세션 만료를 피하려고 전체 크롤링 속도를 올린다.
     * 필요하면 20~50 정도로 올려도 된다.
     */
    private static final long DETAIL_DELAY_MS = 0;
    private static final long PAGE_DELAY_MS = 0;

    private final SourceDocumentRepository sourceDocumentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${crawler.qna-cookie:}")
    private String qnaCookie;

    /**
     * QnA 전체 1회 크롤링
     */
    public CrawlResult crawlAllQna() {
        validateQnaCookie();

        int savedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        int offset = 0;

        /*
         * QnA 목록 안에서 같은 articleNo가 여러 번 잡힐 수 있으므로
         * 이번 크롤링 실행 중 이미 처리한 articleNo를 기억한다.
         */
        Set<String> visitedArticleNos = new HashSet<>();

        while (true) {
            try {
                String listUrl = buildListUrl(offset);

                Document listDocument = createHtmlConnection(listUrl)
                        .referrer(QNA_LIST_URL)
                        .get();

                Elements links = extractQnaLinks(listDocument);

                System.out.println(
                        "QnA 목록 확인 offset=" + offset
                                + ", rawLinkCount=" + links.size()
                                + ", title=" + listDocument.title()
                );

                if (links.isEmpty()) {
                    System.out.println("더 이상 QnA 게시글 링크가 없습니다. offset=" + offset);

                    if (isRealLoginPage(listDocument)) {
                        System.out.println("실제 로그인 페이지로 판단됩니다. qnaCookie를 확인하세요.");
                    }

                    break;
                }

                int pageSavedCount = 0;
                int pageSkippedCount = 0;
                int pageFailedCount = 0;

                for (Element link : links) {
                    String rawUrl = "";
                    String articleNo = "";
                    String detailUrl = "";

                    try {
                        rawUrl = link.absUrl("href");
                        articleNo = extractArticleNo(rawUrl);

                        if (articleNo.isBlank()) {
                            pageFailedCount++;
                            failedCount++;
                            continue;
                        }

                        /*
                         * 핵심 1:
                         * 같은 articleNo가 같은 페이지 또는 다른 페이지에서 반복되면 건너뛴다.
                         */
                        if (visitedArticleNos.contains(articleNo)) {
                            pageSkippedCount++;
                            skippedCount++;
                            continue;
                        }

                        visitedArticleNos.add(articleNo);

                        /*
                         * 핵심 2:
                         * rawUrl 그대로 저장하지 말고 canonical URL로 통일한다.
                         */
                        detailUrl = buildCanonicalQnaDetailUrl(articleNo);

                        if (sourceDocumentRepository.existsByUrl(detailUrl)) {
                            pageSkippedCount++;
                            skippedCount++;
                            continue;
                        }

                        SourceDocument qna = crawlQnaDetail(detailUrl);
                        sourceDocumentRepository.save(qna);

                        pageSavedCount++;
                        savedCount++;

                        sleep(DETAIL_DELAY_MS);

                    } catch (Exception e) {
                        pageFailedCount++;
                        failedCount++;
                        System.out.println(
                                "QnA 상세 크롤링 실패"
                                        + ", articleNo=" + articleNo
                                        + ", rawUrl=" + rawUrl
                                        + ", detailUrl=" + detailUrl
                                        + ", reason=" + e.getMessage()
                        );
                    }
                }

                System.out.println(
                        "QnA 목록 처리 완료 offset=" + offset
                                + ", pageSaved=" + pageSavedCount
                                + ", pageSkipped=" + pageSkippedCount
                                + ", pageFailed=" + pageFailedCount
                                + ", totalSaved=" + savedCount
                                + ", totalSkipped=" + skippedCount
                                + ", totalFailed=" + failedCount
                                + ", visitedArticleNos=" + visitedArticleNos.size()
                );

                offset += PAGE_LIMIT;

                sleep(PAGE_DELAY_MS);

            } catch (Exception e) {
                failedCount++;
                System.out.println("QnA 목록 크롤링 실패 offset=" + offset + ", reason=" + e.getMessage());
                break;
            }
        }

        return new CrawlResult(
                savedCount,
                skippedCount,
                failedCount,
                "전체 QnA 크롤링이 완료되었습니다."
        );
    }

    /**
     * 범위 지정 QnA 크롤링
     *
     * 예:
     * /crawl/qna?startOffset=270&endOffset=1000
     */
    public CrawlResult crawlQna(int startOffset, int endOffset) {
        validateQnaCookie();

        int savedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        Set<String> visitedArticleNos = new HashSet<>();

        for (int offset = startOffset; offset <= endOffset; offset += PAGE_LIMIT) {
            try {
                String listUrl = buildListUrl(offset);

                Document listDocument = createHtmlConnection(listUrl)
                        .referrer(QNA_LIST_URL)
                        .get();

                Elements links = extractQnaLinks(listDocument);

                if (links.isEmpty()) {
                    System.out.println("더 이상 QnA 게시글 링크가 없습니다. offset=" + offset);
                    break;
                }

                for (Element link : links) {
                    String rawUrl = "";
                    String articleNo = "";
                    String detailUrl = "";

                    try {
                        rawUrl = link.absUrl("href");
                        articleNo = extractArticleNo(rawUrl);

                        if (articleNo.isBlank()) {
                            failedCount++;
                            continue;
                        }

                        if (visitedArticleNos.contains(articleNo)) {
                            skippedCount++;
                            continue;
                        }

                        visitedArticleNos.add(articleNo);

                        detailUrl = buildCanonicalQnaDetailUrl(articleNo);

                        if (sourceDocumentRepository.existsByUrl(detailUrl)) {
                            skippedCount++;
                            continue;
                        }

                        SourceDocument qna = crawlQnaDetail(detailUrl);
                        sourceDocumentRepository.save(qna);

                        savedCount++;

                        sleep(DETAIL_DELAY_MS);

                    } catch (Exception e) {
                        failedCount++;
                        System.out.println(
                                "QnA 상세 크롤링 실패"
                                        + ", articleNo=" + articleNo
                                        + ", rawUrl=" + rawUrl
                                        + ", detailUrl=" + detailUrl
                                        + ", reason=" + e.getMessage()
                        );
                    }
                }

                System.out.println(
                        "QnA 목록 처리 완료 offset=" + offset
                                + ", saved=" + savedCount
                                + ", skipped=" + skippedCount
                                + ", failed=" + failedCount
                                + ", visitedArticleNos=" + visitedArticleNos.size()
                );

                sleep(PAGE_DELAY_MS);

            } catch (Exception e) {
                failedCount++;
                System.out.println("QnA 목록 크롤링 실패 offset=" + offset + ", reason=" + e.getMessage());
                break;
            }
        }

        return new CrawlResult(
                savedCount,
                skippedCount,
                failedCount,
                "QnA 크롤링이 완료되었습니다."
        );
    }
    private String buildCanonicalQnaDetailUrl(String articleNo) {
        return QNA_LIST_URL
                + "?mode=view"
                + "&articleNo=" + articleNo;
    }
    private SourceDocument crawlQnaDetail(String detailUrl) {
        try {
            Document detailDocument = createHtmlConnection(detailUrl)
                    .referrer(QNA_LIST_URL)
                    .get();

            if (isRealLoginPage(detailDocument)) {
                throw new IllegalStateException("상세 페이지 접근 중 실제 로그인 페이지로 이동되었습니다.");
            }

            String fullText = detailDocument.body().text();

            String title = extractTitle(detailDocument, fullText);
            String questionContent = extractQnaContent(fullText);

            String articleNo = extractArticleNo(detailUrl);
            String answerContent = "";

            if (!articleNo.isBlank()) {
                answerContent = crawlComments(articleNo, detailUrl);
            }

            String content = buildQnaContent(title, questionContent, answerContent);
            String category = classifyCategory(title, content);

            return new SourceDocument(
                    DocumentSource.QNA,
                    "금오공과대학교",
                    title,
                    content,
                    detailUrl,
                    category,
                    "미확인",
                    "미확인"
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "QnA 상세 페이지 처리 실패: " + detailUrl + ", reason=" + e.getMessage()
            );
        }
    }

    private String crawlComments(String articleNo, String detailUrl) {
        try {
            String commentUrl = COMMENT_LIST_URL
                    + "?cmtKey=" + articleNo
                    + "&cmtType=B";

            String response = createJsonConnection(commentUrl)
                    .referrer(detailUrl)
                    .execute()
                    .body();

            if (response == null || response.isBlank()) {
                return "";
            }

            return extractCommentTextFromJson(response);

        } catch (Exception e) {
            System.out.println(
                    "QnA 댓글 크롤링 실패 articleNo=" + articleNo + ", reason=" + e.getMessage()
            );
            return "";
        }
    }

    private String extractCommentTextFromJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.path("items");

            if (!items.isArray() || items.isEmpty()) {
                return "";
            }

            StringBuilder sb = new StringBuilder();

            for (JsonNode item : items) {
                String deleteYn = item.path("cmtDeleteYn").asText("");

                if ("Y".equalsIgnoreCase(deleteYn)) {
                    continue;
                }

                String writer = item.path("cmtWriterNm").asText("");
                String text = item.path("cmtText").asText("");

                writer = cleanText(writer);
                text = cleanCommentText(text);

                if (text.isBlank()) {
                    continue;
                }

                if (!writer.isBlank()) {
                    sb.append(writer).append(": ");
                }

                sb.append(text).append("\n\n");
            }

            return sb.toString().trim();

        } catch (Exception e) {
            System.out.println("댓글 JSON 파싱 실패: " + e.getMessage());
            return "";
        }
    }

    private String buildQnaContent(String title, String questionContent, String answerContent) {
        String cleanTitle = cleanText(title);
        String question = cleanText(questionContent);
        String answer = cleanCommentText(answerContent);

        StringBuilder sb = new StringBuilder();

        sb.append("[문서유형]\n")
                .append("QnA")
                .append("\n\n");

        if (!cleanTitle.isBlank()) {
            sb.append("[질문 제목]\n")
                    .append(cleanTitle)
                    .append("\n\n");
        }

        if (!question.isBlank()) {
            sb.append("[질문 내용]\n")
                    .append(question)
                    .append("\n\n");
        }

        if (!answer.isBlank()) {
            sb.append("[공식 답변]\n")
                    .append(answer)
                    .append("\n");
        } else {
            sb.append("[답변 상태]\n")
                    .append("아직 공식 답변이 등록되지 않았습니다.")
                    .append("\n");
        }

        return sb.toString().trim();
    }

    private Elements extractQnaLinks(Document listDocument) {
        return listDocument.select(
                "a[href*='mode=view'][href*='articleNo='], " +
                        "a[href*='articleNo='][href*='sub06_03_02.do']"
        );
    }

    private boolean isValidQnaDetailUrl(String detailUrl) {
        return detailUrl != null
                && !detailUrl.isBlank()
                && detailUrl.contains("sub06_03_02.do")
                && detailUrl.contains("mode=view")
                && detailUrl.contains("articleNo=");
    }

    private String buildListUrl(int offset) {
        return QNA_LIST_URL
                + "?mode=list"
                + "&articleLimit=" + PAGE_LIMIT
                + "&article.offset=" + offset
                + "&srSearchKey="
                + "&srSearchVal=";
    }

    private Connection createHtmlConnection(String url) {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                .header("Cookie", qnaCookie)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Connection", "keep-alive")
                .timeout(10000);
    }

    private Connection createJsonConnection(String url) {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                .header("Cookie", qnaCookie)
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("X-Requested-With", "XMLHttpRequest")
                .ignoreContentType(true)
                .timeout(10000);
    }

    private void validateQnaCookie() {
        if (qnaCookie == null || qnaCookie.isBlank()) {
            throw new IllegalStateException("QnA 크롤링용 로그인 쿠키가 설정되지 않았습니다.");
        }
    }

    private String extractArticleNo(String detailUrl) {
        if (detailUrl == null || detailUrl.isBlank()) {
            return "";
        }

        Pattern pattern = Pattern.compile("articleNo=(\\d+)");
        Matcher matcher = pattern.matcher(detailUrl);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    private boolean isRealLoginPage(Document document) {
        if (document == null || document.body() == null) {
            return true;
        }

        String text = document.body().text();

        /*
         * 단순히 '로그인', '아이디', '비밀번호' 단어가 있다고 로그인 페이지로 보면 안 된다.
         * 정상 게시판 페이지에도 상단 로그인 영역 때문에 들어갈 수 있다.
         */
        boolean hasPasswordInput = !document.select("input[type=password]").isEmpty();
        boolean hasLoginForm = !document.select("form[action*=login], form[action*=Login]").isEmpty();

        boolean hasLoginText =
                text.contains("통합로그인")
                        || text.contains("아이디")
                        || text.contains("비밀번호");

        return hasPasswordInput && hasLoginText || hasLoginForm;
    }
    private String extractTitle(Document document, String fullText) {
        String title = document.select(
                "h4, h3, .view-title, .board-view-title, .bbs-title"
        ).text();

        if (title == null || title.isBlank()) {
            title = extractTitleFromFullText(fullText);
        }

        if (title == null || title.isBlank()) {
            title = "QnA 제목 미확인";
        }

        return cleanText(title);
    }

    private String extractTitleFromFullText(String fullText) {
        if (fullText == null || fullText.isBlank()) {
            return "";
        }

        int writerIndex = fullText.indexOf("작성자");

        if (writerIndex <= 0) {
            return "";
        }

        String beforeWriter = fullText.substring(0, writerIndex).trim();

        String[] parts = beforeWriter.split("Q&A|QnA|묻고답하기|학사상담");

        if (parts.length == 0) {
            return cleanText(beforeWriter);
        }

        return cleanText(parts[parts.length - 1]);
    }

    private String extractQnaContent(String fullText) {
        if (fullText == null || fullText.isBlank()) {
            return "";
        }

        String content = fullText;

        int afterDateIndex = findAfterWrittenDateIndex(content);

        if (afterDateIndex >= 0 && afterDateIndex < content.length()) {
            content = content.substring(afterDateIndex);
        } else {
            int start = findFirstExistingIndex(content, "내용", "질문", "본문");

            if (start >= 0) {
                content = content.substring(start);
                content = removeLeadingLabel(content);
            }
        }

        int end = findFirstExistingIndex(
                content,
                "전체댓글",
                "댓글을 입력해주세요",
                "답변달기",
                "이전글",
                "다음글",
                "등록 목록",
                "등록",
                "목록"
        );

        if (end > 0) {
            content = content.substring(0, end);
        }

        return cleanText(content);
    }

    private int findAfterWrittenDateIndex(String text) {
        if (text == null || text.isBlank()) {
            return -1;
        }

        Pattern pattern = Pattern.compile("작성일\\s+\\d{4}\\.\\d{2}\\.\\d{2}");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.end();
        }

        return -1;
    }

    private String removeLeadingLabel(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text
                .replaceFirst("^내용\\s*", "")
                .replaceFirst("^질문\\s*", "")
                .replaceFirst("^본문\\s*", "");
    }

    private int findFirstExistingIndex(String text, String... keywords) {
        int result = -1;

        if (text == null || text.isBlank()) {
            return result;
        }

        for (String keyword : keywords) {
            int index = text.indexOf(keyword);

            if (index >= 0) {
                if (result == -1 || index < result) {
                    result = index;
                }
            }
        }

        return result;
    }

    private String classifyCategory(String title, String content) {
        String text = title + " " + content;

        if (text.contains("등록금") || text.contains("등록")) {
            return "등록금";
        }

        if (text.contains("장학") || text.contains("장학금")) {
            return "장학";
        }

        if (text.contains("휴학") || text.contains("복학")) {
            return "휴복학";
        }

        if (text.contains("수강") || text.contains("수강신청")) {
            return "수강신청";
        }

        if (text.contains("졸업")) {
            return "졸업";
        }

        if (text.contains("식권")
                || text.contains("식당")
                || text.contains("학생식당")
                || text.contains("정찬식당")
                || text.contains("환불")) {
            return "식당";
        }

        return "기타";
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String cleanCommentText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\u00A0", " ")
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private void sleep(long millis) {
        if (millis <= 0) {
            return;
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("크롤링 대기 중 인터럽트 발생", e);
        }
    }
}