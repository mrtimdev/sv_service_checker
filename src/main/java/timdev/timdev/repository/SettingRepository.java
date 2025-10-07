package timdev.timdev.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.Setting;
import timdev.timdev.enums.ApprovalLevel;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {
    // Optional<Setting> findById(1L);
    List<Setting> findByApprovedLevel(ApprovalLevel approvedLevel);

}