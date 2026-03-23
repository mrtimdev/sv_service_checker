package timdev.timdev.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import timdev.timdev.dto.DestinationPortDTO;
import timdev.timdev.repository.DestinationPortRepository;

@Service
public class DestinationPortService {

    @Autowired
    private DestinationPortRepository repository;

    public List<DestinationPortDTO> getByDestinationId(Long destinationId) {
        return repository.findDTOByDestinationId(destinationId);
    }
}
