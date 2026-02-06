package timdev.timdev.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import timdev.timdev.entity.Model;
import timdev.timdev.repository.ModelRepository;


@AllArgsConstructor
@Service
public class ModelService {
    
    @Autowired
    private ModelRepository modelRepo;

    public List<Model> getAll() {
        return modelRepo.findAll();
    }
    
    public Optional<Model> findById(Long id) {
        return modelRepo.findById(id);
    }
    
}
