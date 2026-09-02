package com.adobe.printservice.web.api;

import com.adobe.printservice.model.RenderTemplate;

public record RenderTemplateApiOutput(String id, String name) {
    public static RenderTemplateApiOutput fromRenderTemplate(RenderTemplate renderTemplate) {
        return new RenderTemplateApiOutput(renderTemplate.getId(), renderTemplate.getName());
    }
}
