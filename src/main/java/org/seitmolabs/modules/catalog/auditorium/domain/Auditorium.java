package org.seitmolabs.modules.catalog.auditorium.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "auditoriums")
public class Auditorium {
    @Id
    private Long id;

    private String name;
    @Builder.Default
    private boolean active = true;
}
