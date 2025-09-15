package timdev.timdev.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import timdev.timdev.entity.Model;
import timdev.timdev.repository.ModelRepository;


@RequiredArgsConstructor
@Service
public class ModelService {
    
    private final ModelRepository modelRepo;

    public List<Model> getAll() {
        return modelRepo.findAll();
    }
    
    public Optional<Model> findById(Long id) {
        return modelRepo.findById(id);
    }
    
}
