package timdev.timdev.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import timdev.timdev.entity.InspectionItem;
import timdev.timdev.entity.ServiceCheckerItem;
import timdev.timdev.entity.ServiceCheckerItemNote;

public interface ServiceCheckerItemNoteRepository extends JpaRepository<ServiceCheckerItemNote, Long> {

    List<ServiceCheckerItemNote> findByInspectionItemId(Long inspectionItemId);

    void deleteByInspectionItemId(Long inspectionItemId);

    @Transactional
    @Modifying
    @Query("DELETE FROM ServiceCheckerItemNote n WHERE n.inspectionItem.id = :inspectionItemId")
    void deleteAllByInspectionItemId(@Param("inspectionItemId") Long inspectionItemId);

    Optional<ServiceCheckerItemNote> findByServiceCheckerItemAndInspectionItem(
            ServiceCheckerItem serviceCheckerItem,
            InspectionItem inspectionItem);

    Optional<ServiceCheckerItemNote> findByServiceCheckerItemIdAndInspectionItemId(
            Long serviceCheckerItemId,
            Long inspectionItemId);

    @Modifying
    @Query("DELETE FROM ServiceCheckerItemNote n WHERE n.serviceCheckerItem.id = :serviceItemId")
    void deleteByServiceCheckerItemId(@Param("serviceItemId") Long serviceItemId);

    @Modifying
    @Query("DELETE FROM ServiceCheckerItemNote n WHERE n.serviceCheckerItem.id = :serviceItemId")
    void deleteAllByServiceCheckerItemId(@Param("serviceItemId") Long serviceItemId);

}