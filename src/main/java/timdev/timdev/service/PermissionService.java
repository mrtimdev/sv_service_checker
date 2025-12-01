package timdev.timdev.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import timdev.timdev.entity.Permission;
import timdev.timdev.repository.PermissionRepository;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

  
    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public Set<Permission> findByIds(List<Long> ids) {
        return new HashSet<>(permissionRepository.findAllById(ids));
    }
}
