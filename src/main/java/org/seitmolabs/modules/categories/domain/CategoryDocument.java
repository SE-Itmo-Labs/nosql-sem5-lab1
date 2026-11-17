package org.seitmolabs.modules.categories.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "categories")
public class CategoryDocument {

    @MongoId(FieldType.STRING)
    private String id;

    private String name;

    private String description;

    private String systemCode;
}