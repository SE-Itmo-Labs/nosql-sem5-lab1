package org.seitmolabs.modules.movie.repository;

import org.seitmolabs.modules.movie.repository.custom.TestRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

// Учебный пример: Integer не является JPA-сущностью.
@NoRepositoryBean
public interface TestRepository extends JpaRepository<Integer, Long>, TestRepositoryCustom {
    
    Integer someOtherMethod();

     // Метод someMethod() подтянется сюда автоматически из класса 
}
