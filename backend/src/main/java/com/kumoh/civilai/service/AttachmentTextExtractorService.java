package com.kumoh.civilai.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class AttachmentTextExtractorService {

    public String extractText(String fileName, byte[] fileBytes) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }

        if (fileBytes == null || fileBytes.length == 0) {
            return "";
        }

        String lowerName = fileName.toLowerCase();

        try {
            if (lowerName.endsWith(".pdf")) {
                return extractPdfText(fileBytes);
            }

            if (lowerName.endsWith(".docx")) {
                return extractDocxText(fileBytes);
            }

            if (lowerName.endsWith(".txt")) {
                return extractTxtText(fileBytes);
            }

            if (lowerName.endsWith(".hwpx")) {
                return extractHwpxText(fileBytes);
            }

            /*
             * HWP, PNG, JPG는 여기서 바로 처리하지 않는다.
             * HWP는 LibreOffice 변환 또는 별도 HWP 파서가 필요하고,
             * PNG/JPG는 OCR 엔진이 필요하다.
             */
            if (lowerName.endsWith(".hwp")) {
                return "[HWP 파일은 현재 텍스트 자동 추출을 지원하지 않습니다.]";
            }

            if (lowerName.endsWith(".png")
                    || lowerName.endsWith(".jpg")
                    || lowerName.endsWith(".jpeg")) {
                return "[이미지 파일은 현재 OCR 텍스트 추출을 지원하지 않습니다.]";
            }

            return "";

        } catch (Exception e) {
            System.out.println("첨부파일 텍스트 추출 실패 fileName=" + fileName + ", reason=" + e.getMessage());
            return "";
        }
    }

    private String extractPdfText(byte[] fileBytes) {
        try (PDDocument document = PDDocument.load(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return cleanExtractedText(stripper.getText(document));
        } catch (Exception e) {
            System.out.println("PDF 텍스트 추출 실패: " + e.getMessage());
            return "";
        }
    }

    private String extractDocxText(byte[] fileBytes) {
        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
                XWPFDocument document = new XWPFDocument(inputStream);
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)
        ) {
            return cleanExtractedText(extractor.getText());
        } catch (Exception e) {
            System.out.println("DOCX 텍스트 추출 실패: " + e.getMessage());
            return "";
        }
    }

    private String extractTxtText(byte[] fileBytes) {
        return cleanExtractedText(new String(fileBytes, StandardCharsets.UTF_8));
    }

    private String extractHwpxText(byte[] fileBytes) {
        StringBuilder sb = new StringBuilder();

        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
                ZipInputStream zipInputStream = new ZipInputStream(inputStream)
        ) {
            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();

                /*
                 * HWPX는 zip 안에 XML 문서들이 들어 있다.
                 * Contents/section*.xml 쪽에 본문이 들어가는 경우가 많다.
                 */
                if (!entryName.startsWith("Contents/")
                        || !entryName.endsWith(".xml")) {
                    continue;
                }

                String xml = new String(zipInputStream.readAllBytes(), StandardCharsets.UTF_8);
                String text = xmlToPlainText(xml);

                if (!text.isBlank()) {
                    sb.append(text).append("\n\n");
                }
            }

            return cleanExtractedText(sb.toString());

        } catch (Exception e) {
            System.out.println("HWPX 텍스트 추출 실패: " + e.getMessage());
            return "";
        }
    }

    private String xmlToPlainText(String xml) {
        if (xml == null || xml.isBlank()) {
            return "";
        }

        return xml
                .replaceAll("<[^>]+>", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String cleanExtractedText(String text) {
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
}