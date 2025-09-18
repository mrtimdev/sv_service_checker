package timdev.timdev.service;


import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import timdev.timdev.entity.FatsOilsSetting;
import timdev.timdev.repository.FatsOilsSettingRepository;

@Service
public class FatsOilsSettingService {

    private final FatsOilsSettingRepository repository;

    public FatsOilsSettingService(FatsOilsSettingRepository repository) {
        this.repository = repository;
    }

    public Optional<FatsOilsSetting> getById(Long id) {
        return repository.findById(id);
    }
    public Optional<FatsOilsSetting> findById(Long id) {
        return repository.findById(id);
    }
    public List<FatsOilsSetting> getAll() {
        return repository.findAll();
    }

    public List<FatsOilsSetting> getByType(String type) {
        return repository.findByType(type);
    }

    public FatsOilsSetting save(FatsOilsSetting setting) {
        return repository.save(setting);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public FatsOilsSetting findSettingForKm(String type, int km) {
        // between min and max
        FatsOilsSetting setting = repository
                .findFirstByTypeAndMinKmLessThanEqualAndMaxKmGreaterThanEqual(type, km, km);

        // If not found, maybe >max (maxKm null)
        if (setting == null) {
            List<FatsOilsSetting> above = repository.findByTypeAndMaxKmIsNull(type);
            if (!above.isEmpty()) {
                // optionally choose first one
                return above.get(0);
            }
        }
        return setting;
    }
}