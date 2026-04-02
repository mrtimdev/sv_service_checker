package timdev.timdev.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import timdev.timdev.dto.PortType;
import timdev.timdev.entity.Port;
import timdev.timdev.repository.PortRepository;

@Service
public class PortService {

    @Autowired
    private PortRepository portRepository;

    // Save one port
    public Port save(Port port) {
        return portRepository.save(port);
    }

    // Save multiple ports
    public List<Port> saveAll(List<Port> ports) {
        return portRepository.saveAll(ports);
    }

    // Get all ports
    public List<Port> findAll() {
        return portRepository.findAll();
    }

    // Find by ID
    public Optional<Port> findById(Long id) {
        return portRepository.findById(id);
    }

    // Delete by ID
    public void deleteById(Long id) {
        portRepository.deleteById(id);
    }

    // Delete all
    public void deleteAll() {
        portRepository.deleteAll();
    }

    // Find ports by type
    public List<Port> findByType(PortType type) {
        return portRepository.findByType(type);
    }

    // Check if a port exists by name
    public boolean existsByName(String name) {
        return portRepository.findAll()
                .stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name));
    }

    // Find a port by name
    public Port findByName(String name) {
        return portRepository.findAll()
                .stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}