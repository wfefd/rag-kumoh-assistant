package com.kumoh.civilai.service;

import com.kumoh.civilai.domain.document.DocumentSource;
import com.kumoh.civilai.domain.document.SourceDocument;
import com.kumoh.civilai.domain.document.SourceDocumentRepository;
import com.kumoh.civilai.dto.document.CrawlResult;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.Connection;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SchoolNoticeCrawlerService {

    private static final String NOTICE_LIST_URL =
            "https://www.kumoh.ac.kr/ko/sub06_01_01_01.do";

    private static final int PAGE_LIMIT = 10;

    private final SourceDocumentRepository sourceDocumentRepository;
    private final AttachmentTextExtractorService attachmentTextExtractorService;
    private static final int MAX_ATTACHMENT_SIZE_BYTES = 20 * 1024 * 1024;
    /**
     * 범위 지정 공지사항 크롤링
     *
     * 예:
     * crawlNotices(0, 100)
     */
    public CrawlResult crawlNotices(int startOffset, int endOffset) {
        int savedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        Set<String> crawledArticleNos = new HashSet<>();

        for (int offset = startOffset; offset <= endOffset; offset += PAGE_LIMIT) {
            CrawlPageResult pageResult = crawlNoticeListPage(offset, crawledArticleNos);

            savedCount += pageResult.savedCount();
            skippedCount += pageResult.skippedCount();
            failedCount += pageResult.failedCount();

            sleep(300);
        }

        return new CrawlResult(
                savedCount,
                skippedCount,
                failedCount,
                "공지사항 크롤링이 완료되었습니다."
        );
    }

    /**
     * 공지사항 전체 1회 크롤링
     *
     * offset=0부터 시작해서 더 이상 상세 링크가 없을 때까지 반복한다.
     */
    public CrawlResult crawlAllNotices() {
        int savedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        int offset = 0;

        Set<String> crawledArticleNos = new HashSet<>();

        while (true) {
            CrawlPageResult pageResult = crawlNoticeListPage(offset, crawledArticleNos);

            if (pageResult.linkCount() == 0) {
                System.out.println("더 이상 공지사항 게시글이 없습니다. offset=" + offset);
                break;
            }

            savedCount += pageResult.savedCount();
            skippedCount += pageResult.skippedCount();
            failedCount += pageResult.failedCount();

            System.out.println(
                    "공지사항 목록 처리 완료 offset=" + offset
                            + ", saved=" + savedCount
                            + ", skipped=" + skippedCount
                            + ", failed=" + failedCount
            );

            offset += PAGE_LIMIT;

            sleep(300);
        }

        return new CrawlResult(
                savedCount,
                skippedCount,
                failedCount,
                "전체 공지사항 크롤링이 완료되었습니다."
        );
    }

    private CrawlPageResult crawlNoticeListPage(int offset, Set<String> crawledArticleNos) {
        int savedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        try {
            String listUrl = buildListUrl(offset);

            Document listDocument = Jsoup.connect(listUrl)
                    .userAgent("Mozilla/5.0")
                    .referrer(NOTICE_LIST_URL)
                    .timeout(10000)
                    .get();

            Elements links = listDocument.select("a[href*='mode=view'][href*='articleNo=']");

            if (links.isEmpty()) {
                return new CrawlPageResult(0, 0, 0, 0);
            }

            int validLinkCount = 0;

            for (Element link : links) {
                try {
                    String rawDetailUrl = link.absUrl("href");

                    if (rawDetailUrl == null || rawDetailUrl.isBlank()) {
                        failedCount++;
                        continue;
                    }

                    String articleNo = extractArticleNo(rawDetailUrl);

                    if (articleNo.isBlank()) {
                        failedCount++;
                        continue;
                    }

                    validLinkCount++;

                    /*
                     * 핵심:
                     * 고정 공지는 모든 페이지에 반복되므로
                     * 현재 실행 중 이미 처리한 articleNo면 건너뛴다.
                     */
                    if (crawledArticleNos.contains(articleNo)) {
                        skippedCount++;
                        continue;
                    }

                    crawledArticleNos.add(articleNo);

                    /*
                     * URL 정규화:
                     * 같은 글인데 offset 등의 파라미터가 달라져도
                     * DB에는 같은 URL 형태로 저장되게 만든다.
                     */
                    String detailUrl = buildCanonicalNoticeDetailUrl(articleNo);

                    if (sourceDocumentRepository.existsByUrl(detailUrl)) {
                        skippedCount++;
                        continue;
                    }

                    SourceDocument notice = crawlNoticeDetail(detailUrl);
                    sourceDocumentRepository.save(notice);

                    savedCount++;

                    sleep(200);

                } catch (Exception e) {
                    failedCount++;
                    System.out.println("공지 상세 크롤링 실패: " + e.getMessage());
                }
            }

            return new CrawlPageResult(
                    validLinkCount,
                    savedCount,
                    skippedCount,
                    failedCount
            );

        } catch (Exception e) {
            failedCount++;
            System.out.println("공지 목록 크롤링 실패 offset=" + offset + ", reason=" + e.getMessage());

            return new CrawlPageResult(
                    0,
                    savedCount,
                    skippedCount,
                    failedCount
            );
        }
    }
    private String extractArticleNo(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }

        Pattern pattern = Pattern.compile("articleNo=(\\d+)");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    private String buildCanonicalNoticeDetailUrl(String articleNo) {
        return NOTICE_LIST_URL
                + "?mode=view"
                + "&articleNo=" + articleNo;
    }
    private String buildListUrl(int offset) {
        return NOTICE_LIST_URL
                + "?mode=list"
                + "&articleLimit=" + PAGE_LIMIT
                + "&article.offset=" + offset;
    }

    private SourceDocument crawlNoticeDetail(String detailUrl) {
        try {
            Document detailDocument = Jsoup.connect(detailUrl)
                    .userAgent("Mozilla/5.0")
                    .referrer(NOTICE_LIST_URL)
                    .timeout(10000)
                    .get();

            String fullText = detailDocument.body().text();

            String title = extractTitle(detailDocument, fullText);
            String category = extractCategory(title, fullText);
            String author = extractAuthor(fullText);
            String postedDate = extractPostedDate(fullText);
            String noticeBody = extractNoticeBody(fullText);
            String attachmentText = crawlAttachmentTexts(detailDocument, detailUrl);

            String content = buildNoticeContent(
                    title,
                    noticeBody,
                    attachmentText,
                    category,
                    author,
                    postedDate
            );
            return new SourceDocument(
                    DocumentSource.NOTICE,
                    "금오공과대학교",
                    title,
                    content,
                    detailUrl,
                    category,
                    author,
                    postedDate
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "공지 상세 페이지 처리 실패: " + detailUrl + ", reason=" + e.getMessage()
            );
        }
    }

    /**
     * RAG 검색용 공지사항 content 생성
     */
    private String buildNoticeContent(
            String title,
            String body,
            String attachmentText,
            String category,
            String author,
            String postedDate
    ) {
        String cleanTitle = cleanText(title);
        String cleanBody = cleanText(body);
        String cleanAttachmentText = cleanAttachmentText(attachmentText);
        String cleanCategory = normalizeUnknown(category);
        String cleanAuthor = normalizeUnknown(author);
        String cleanPostedDate = normalizeUnknown(postedDate);

        StringBuilder sb = new StringBuilder();

        sb.append("[문서유형]\n")
                .append("공지사항")
                .append("\n\n");

        if (!cleanTitle.isBlank()) {
            sb.append("[제목]\n")
                    .append(cleanTitle)
                    .append("\n\n");
        }

        sb.append("[분류]\n")
                .append(cleanCategory)
                .append("\n\n");

        sb.append("[작성자]\n")
                .append(cleanAuthor)
                .append("\n\n");

        sb.append("[작성일]\n")
                .append(cleanPostedDate)
                .append("\n\n");

        if (!cleanBody.isBlank()) {
            sb.append("[본문]\n")
                    .append(cleanBody)
                    .append("\n\n");
        }

        if (!cleanAttachmentText.isBlank()) {
            sb.append("[첨부파일 내용]\n")
                    .append(cleanAttachmentText)
                    .append("\n");
        }

        return sb.toString().trim();
    }
    private String extractTitle(Document document, String fullText) {
        String title = document.select(
                "h4, h3, .view-title, .board-view-title, .bbs-title"
        ).text();

        if (title == null || title.isBlank()) {
            title = extractTitleFromFullText(fullText);
        }

        if (title == null || title.isBlank()) {
            title = "공지사항 제목 미확인";
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

        String[] parts = beforeWriter.split("공지사항|학사안내|공지");

        if (parts.length == 0) {
            return cleanText(beforeWriter);
        }

        return cleanText(parts[parts.length - 1]);
    }

    private String extractCategory(String title, String fullText) {
        String categoryFromTitle = extractCategoryFromTitle(title);

        if (!categoryFromTitle.isBlank() && !"기타".equals(categoryFromTitle)) {
            return categoryFromTitle;
        }

        return classifyCategory(title + " " + fullText);
    }

    private String extractCategoryFromTitle(String title) {
        if (title == null || title.isBlank()) {
            return "기타";
        }

        int start = title.indexOf("[");
        int end = title.indexOf("]");

        if (start >= 0 && start < end) {
            return cleanText(title.substring(start + 1, end));
        }

        return "기타";
    }

    private String classifyCategory(String text) {
        if (text == null || text.isBlank()) {
            return "기타";
        }

        if (text.contains("등록금") || text.contains("등록")) {
            return "등록";
        }

        if (text.contains("장학") || text.contains("장학금")) {
            return "장학";
        }

        if (text.contains("휴학") || text.contains("복학") || text.contains("전과")) {
            return "학적";
        }

        if (text.contains("수강") || text.contains("수업") || text.contains("계절수업")) {
            return "수업";
        }

        if (text.contains("졸업")) {
            return "졸업";
        }

        if (text.contains("채용") || text.contains("취업") || text.contains("현장실습")) {
            return "취업";
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

    private String extractAuthor(String fullText) {
        String author = extractValueBetweenLabels(
                fullText,
                "작성자",
                "조회",
                "작성일",
                "첨부"
        );

        return normalizeUnknown(author);
    }

    private String extractPostedDate(String fullText) {
        if (fullText == null || fullText.isBlank()) {
            return "미확인";
        }

        Pattern pattern = Pattern.compile("\\d{4}[.-]\\d{2}[.-]\\d{2}");
        Matcher matcher = pattern.matcher(fullText);

        if (matcher.find()) {
            return matcher.group().replace(".", "-");
        }

        return "미확인";
    }

    private String extractNoticeBody(String fullText) {
        if (fullText == null || fullText.isBlank()) {
            return "";
        }

        String content = fullText;

        /*
         * 보통 상세 페이지는:
         * 제목 작성자 ... 조회 ... 작성일 ... 첨부 ... 본문 ... 이전글 ...
         *
         * 작성일 날짜 이후부터 본문 후보로 본다.
         */
        int afterDateIndex = findAfterPostedDateIndex(content);

        if (afterDateIndex >= 0 && afterDateIndex < content.length()) {
            content = content.substring(afterDateIndex);
        }

        /*
         * 첨부 라벨이 있으면, 첨부 라벨 뒤부터 본문 후보로 본다.
         * 단, 첨부파일명만 앞에 붙을 수 있으므로 완벽한 정제는 아님.
         */
        int attachmentIndex = content.indexOf("첨부");
        if (attachmentIndex >= 0) {
            content = content.substring(attachmentIndex + "첨부".length());
        }

        int end = findFirstExistingIndex(
                content,
                "이전글",
                "다음글",
                "목록"
        );

        if (end > 0) {
            content = content.substring(0, end);
        }

        content = removeCommonNoise(content);

        return cleanText(content);
    }

    private int findAfterPostedDateIndex(String text) {
        if (text == null || text.isBlank()) {
            return -1;
        }

        Pattern pattern = Pattern.compile("작성일\\s+\\d{4}[.-]\\d{2}[.-]\\d{2}");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.end();
        }

        return -1;
    }

    private String extractValueBetweenLabels(String text, String label, String... stopLabels) {
        if (text == null || text.isBlank() || label == null || label.isBlank()) {
            return "";
        }

        int labelIndex = text.indexOf(label);

        if (labelIndex < 0) {
            return "";
        }

        String sliced = text.substring(labelIndex + label.length()).trim();

        int end = sliced.length();

        for (String stopLabel : stopLabels) {
            int stopIndex = sliced.indexOf(stopLabel);

            if (stopIndex >= 0) {
                end = Math.min(end, stopIndex);
            }
        }

        return cleanText(sliced.substring(0, end));
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

    private String removeCommonNoise(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("첨부파일", " ")
                .replace("첨부", " ")
                .replace("등록", " ")
                .replace("목록", " ");
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

    private String normalizeUnknown(String text) {
        String cleaned = cleanText(text);

        if (cleaned.isBlank()) {
            return "미확인";
        }

        return cleaned;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("크롤링 대기 중 인터럽트 발생", e);
        }
    }

    private record CrawlPageResult(
            int linkCount,
            int savedCount,
            int skippedCount,
            int failedCount
    ) {
    }
    private String crawlAttachmentTexts(Document document, String detailUrl) {
        Elements links = extractAttachmentLinks(document);

        if (links.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Set<String> visitedFileUrls = new HashSet<>();

        for (Element link : links) {
            try {
                String fileUrl = link.absUrl("href");

                if (fileUrl == null || fileUrl.isBlank()) {
                    continue;
                }

                if (visitedFileUrls.contains(fileUrl)) {
                    continue;
                }

                visitedFileUrls.add(fileUrl);

                String fileName = extractFileName(link, fileUrl);

                if (!isSupportedAttachmentCandidate(fileName, fileUrl)) {
                    continue;
                }

                DownloadedAttachment downloadedAttachment = downloadAttachment(fileUrl, detailUrl, fileName);

                if (downloadedAttachment.fileBytes() == null
                        || downloadedAttachment.fileBytes().length == 0) {
                    continue;
                }

                if (downloadedAttachment.fileBytes().length > MAX_ATTACHMENT_SIZE_BYTES) {
                    System.out.println("첨부파일 크기 초과로 스킵 fileName="
                            + downloadedAttachment.fileName()
                            + ", size="
                            + downloadedAttachment.fileBytes().length);
                    continue;
                }

                String extractedText = attachmentTextExtractorService.extractText(
                        downloadedAttachment.fileName(),
                        downloadedAttachment.fileBytes()
                );

                if (extractedText == null || extractedText.isBlank()) {
                    continue;
                }

                sb.append("[첨부파일: ")
                        .append(downloadedAttachment.fileName())
                        .append("]\n")
                        .append(extractedText)
                        .append("\n\n");

            } catch (Exception e) {
                System.out.println("첨부파일 처리 실패 detailUrl=" + detailUrl + ", reason=" + e.getMessage());
            }
        }

        return cleanAttachmentText(sb.toString());
    }

    private Elements extractAttachmentLinks(Document document) {
        /*
         * 학교 사이트 첨부파일 링크는 꼭 .pdf로 끝나지 않을 수 있다.
         * download, attach, file 키워드와 파일 확장자를 함께 본다.
         */
        return document.select(
                "a[href*='download'], " +
                        "a[href*='attach'], " +
                        "a[href*='file'], " +
                        "a[href$='.pdf'], " +
                        "a[href$='.hwp'], " +
                        "a[href$='.hwpx'], " +
                        "a[href$='.docx'], " +
                        "a[href$='.txt'], " +
                        "a[href$='.png'], " +
                        "a[href$='.jpg'], " +
                        "a[href$='.jpeg']"
        );
    }

    private DownloadedAttachment downloadAttachment(String fileUrl, String referrerUrl, String fallbackFileName) {
        try {
            Connection.Response response = Jsoup.connect(fileUrl)
                    .userAgent("Mozilla/5.0")
                    .referrer(referrerUrl)
                    .ignoreContentType(true)
                    .timeout(20000)
                    .execute();

            byte[] fileBytes = response.bodyAsBytes();

            String fileName = extractFileNameFromContentDisposition(
                    response.header("Content-Disposition")
            );

            if (fileName.isBlank()) {
                fileName = fallbackFileName;
            }

            fileName = cleanFileName(fileName);

            return new DownloadedAttachment(fileName, fileBytes);

        } catch (Exception e) {
            System.out.println("첨부파일 다운로드 실패 fileUrl=" + fileUrl + ", reason=" + e.getMessage());
            return new DownloadedAttachment(fallbackFileName, new byte[0]);
        }
    }

    private String extractFileName(Element link, String fileUrl) {
        String text = cleanText(link.text());

        if (looksLikeFileName(text)) {
            return cleanFileName(text);
        }

        String title = cleanText(link.attr("title"));

        if (looksLikeFileName(title)) {
            return cleanFileName(title);
        }

        return extractFileNameFromUrl(fileUrl);
    }

    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return "attachment";
        }

        try {
            String decodedUrl = URLDecoder.decode(fileUrl, StandardCharsets.UTF_8);
            int slashIndex = decodedUrl.lastIndexOf("/");

            if (slashIndex >= 0 && slashIndex < decodedUrl.length() - 1) {
                String lastPart = decodedUrl.substring(slashIndex + 1);

                int queryIndex = lastPart.indexOf("?");

                if (queryIndex >= 0) {
                    lastPart = lastPart.substring(0, queryIndex);
                }

                if (!lastPart.isBlank()) {
                    return cleanFileName(lastPart);
                }
            }

        } catch (Exception ignored) {
        }

        return "attachment";
    }

    private String extractFileNameFromContentDisposition(String contentDisposition) {
        if (contentDisposition == null || contentDisposition.isBlank()) {
            return "";
        }

        /*
         * 예:
         * Content-Disposition: attachment; filename="abc.pdf"
         * Content-Disposition: attachment; filename*=UTF-8''%EC%9E%90%EB%A3%8C.pdf
         */
        String lower = contentDisposition.toLowerCase();

        try {
            if (lower.contains("filename*=")) {
                String value = contentDisposition.substring(lower.indexOf("filename*=") + "filename*=".length());
                value = value.replace("UTF-8''", "")
                        .replace("utf-8''", "")
                        .replace("\"", "")
                        .trim();

                int semicolonIndex = value.indexOf(";");

                if (semicolonIndex >= 0) {
                    value = value.substring(0, semicolonIndex);
                }

                return URLDecoder.decode(value, StandardCharsets.UTF_8);
            }

            if (lower.contains("filename=")) {
                String value = contentDisposition.substring(lower.indexOf("filename=") + "filename=".length());
                value = value.replace("\"", "").trim();

                int semicolonIndex = value.indexOf(";");

                if (semicolonIndex >= 0) {
                    value = value.substring(0, semicolonIndex);
                }

                return URLDecoder.decode(value, StandardCharsets.UTF_8);
            }

        } catch (Exception e) {
            System.out.println("첨부파일명 파싱 실패 Content-Disposition=" + contentDisposition);
        }

        return "";
    }

    private boolean isSupportedAttachmentCandidate(String fileName, String fileUrl) {
        String target = (fileName + " " + fileUrl).toLowerCase();

        return target.contains(".pdf")
                || target.contains(".docx")
                || target.contains(".txt")
                || target.contains(".hwpx")
                || target.contains(".hwp")
                || target.contains(".png")
                || target.contains(".jpg")
                || target.contains(".jpeg")
                || target.contains("download")
                || target.contains("attach");
    }

    private boolean looksLikeFileName(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String lower = text.toLowerCase();

        return lower.endsWith(".pdf")
                || lower.endsWith(".docx")
                || lower.endsWith(".txt")
                || lower.endsWith(".hwpx")
                || lower.endsWith(".hwp")
                || lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg");
    }

    private String cleanFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "attachment";
        }

        return fileName
                .replace("\\", "_")
                .replace("/", "_")
                .replace(":", "_")
                .replace("*", "_")
                .replace("?", "_")
                .replace("\"", "_")
                .replace("<", "_")
                .replace(">", "_")
                .replace("|", "_")
                .trim();
    }

    private String cleanAttachmentText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\u00A0", " ")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private record DownloadedAttachment(
            String fileName,
            byte[] fileBytes
    ) {
    }
}