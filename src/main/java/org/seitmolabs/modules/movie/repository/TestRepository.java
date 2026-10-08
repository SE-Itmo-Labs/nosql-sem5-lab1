package org.seitmolabs.modules.movie.repository;

import org.seitmolabs.modules.movie.repository.custom.TestRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface TestRepository extends JpaRepository<Integer, Long>, TestRepositoryCustom {
    
    Integer someOtherMethod();

     // Метод someMethod() подтянется сюда автоматически из класса 
}
