package timdev.timdev.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import timdev.timdev.entity.DestinationSetting;
import timdev.timdev.repository.DestinationSettingRepository;

@AllArgsConstructor
@Service
public class DestinationSettingService {

    private DestinationSettingRepository repository;

    public DestinationSetting findByCode(String code) {
        return repository.findByCode(code);
    }
    public DestinationSetting findByName(String name) {
        return repository.findByName(name);
    }

    public Iterable<DestinationSetting> getAll() {
        return repository.findAll();
    }

    public List<DestinationSetting> getAllFiltered(String search, Sort sort) {
        return repository.searchAll(search, sort);
    }

    public Page<DestinationSetting> getAllWithPageable(String search, Pageable pageable, Sort sort) {
        return repository.searchWithPage(search, pageable);
    }

    public DestinationSetting save(DestinationSetting destinationSetting) {
        return repository.save(destinationSetting);
    }   

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    public DestinationSetting findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<DestinationSetting> findAllById(List<Long> ids) {
        return repository.findAllById(ids);
    }

    public List<DestinationSetting> searchSettingsByCodeAndName(String query) {
        return repository.searchSettingsByCodeAndName(query);
    }

    
}
