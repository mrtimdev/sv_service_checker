package timdev.timdev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import timdev.timdev.entity.FatsOilsSetting;

@Repository
public interface FatsOilsSettingRepository extends JpaRepository<FatsOilsSetting, Long> {

    // find all settings by type
    List<FatsOilsSetting> findByType(String type);

    // find by type and km between min and max
    FatsOilsSetting findFirstByTypeAndMinKmLessThanEqualAndMaxKmGreaterThanEqual(
            String type, Integer km1, Integer km2);

    // find > max (where max is null)
    List<FatsOilsSetting> findByTypeAndMaxKmIsNull(String type);
}
