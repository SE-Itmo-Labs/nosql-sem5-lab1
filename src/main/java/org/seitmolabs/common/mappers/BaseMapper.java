package org.seitmolabs.common.mappers;

import java.util.List;

// это базовый интерфейс маппера (преобразователя объектов). Его задача не дублировать одни и те же методы маппинга во всех мапперах сущностей.
public interface BaseMapper<D, E> {
    D toDto(E entity);
    E toEntity(D dto);
    List<D> toDtoList(List<E> entities);
    List<E> toEntityList(List<D> dtos);
}

// использование

// @Mapper(componentModel = "spring")
// public interface MovieMapper extends BaseMapper<MovieResponse, Movie> {
//     // Можно добавить специфичные методы, если нужны
// }