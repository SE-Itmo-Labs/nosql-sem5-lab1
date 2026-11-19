package org.seitmolabs.modules.categories.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    public static final String BOOKING_CODE = "BOOKING";

    private Long id;

    private String name;

    private String description;

    private String systemCode;

    public Category(Long id, String name, String description) {
        this(id, name, description, null);
    }
}
