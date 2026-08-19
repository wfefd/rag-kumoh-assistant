package com.kumoh.civilai.domain.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SourceDocumentRepository extends JpaRepository<SourceDocument, Long> {

    boolean existsByUrl(String url);

    Optional<SourceDocument> findByUrl(String url);

    List<SourceDocument> findBySource(DocumentSource source);

    List<SourceDocument> findByCategory(String category);
}