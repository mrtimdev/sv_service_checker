package timdev.timdev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import timdev.timdev.entity.ServiceCheckerItemNote;

public interface ServiceCheckerItemNoteRepository extends JpaRepository<ServiceCheckerItemNote, Long> {

    List<ServiceCheckerItemNote> findByInspectionItemId(Long inspectionItemId);
    
    void deleteByInspectionItemId(Long inspectionItemId);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM ServiceCheckerItemNote n WHERE n.inspectionItem.id = :inspectionItemId")
    void deleteAllByInspectionItemId(@Param("inspectionItemId") Long inspectionItemId);
}