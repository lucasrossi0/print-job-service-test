package com.adobe.printservice.web;

import com.adobe.printservice.repository.RenderTemplateRepository;
import com.adobe.printservice.web.api.RenderTemplateApiOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class RenderTemplateResource {

    private final RenderTemplateRepository renderTemplateRepository;

    @GetMapping
    public ResponseEntity<List<RenderTemplateApiOutput>> getTemplates() {
        return ResponseEntity.ok(renderTemplateRepository.findAll().stream()
                .map(RenderTemplateApiOutput::fromRenderTemplate)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RenderTemplateApiOutput> getTemplate(@PathVariable String id) {
        return renderTemplateRepository.findById(id)
                .map(RenderTemplateApiOutput::fromRenderTemplate)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
