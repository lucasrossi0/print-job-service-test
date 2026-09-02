package com.adobe.printservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Pre-existing reference data: the set of templates jobs can be rendered against.
 * Read-only for this exercise - you do not need to add template management.
 */
@Getter
@Setter
@Entity
@Table(name = "render_template")
public class RenderTemplate {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;
}
