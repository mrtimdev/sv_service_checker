package timdev.timdev.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import timdev.timdev.dto.DestinationScaleStationDTO;
import timdev.timdev.entity.Destination;
import timdev.timdev.entity.DestinationPort;
import timdev.timdev.entity.ScaleStation;
import timdev.timdev.entity.DestinationScaleStation;
import timdev.timdev.entity.DestinationSetting;
import timdev.timdev.entity.Port;
import timdev.timdev.repository.ScaleStationRepository;
import timdev.timdev.repository.DestinationPortRepository;
import timdev.timdev.repository.DestinationScaleStationRepository;
import timdev.timdev.repository.PortRepository;

@Service
public class DestinationScaleStationService {

    @Autowired
    private DestinationScaleStationRepository repository;

    @Autowired
    private DestinationPortRepository destinationPortRepository;
    @Autowired
    private PortRepository portRepository;

    @Autowired
    private ScaleStationRepository scaleStationRepository;

    @Autowired
    private DestinationSettingService destinationSettingService;

    @PersistenceContext
    private EntityManager em;

    // List all
    public List<DestinationScaleStation> getAll() {
        return repository.findAll();
    }

    // Get one by id
    public DestinationScaleStation getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ScaleStation not found"));
    }

    public List<DestinationScaleStation> findByDestination(Long destinationId) {
        return repository.findByDestinationId(destinationId);
    }

    public Page<DestinationScaleStation> findByTruckWithFilter(Long destinationId, Pageable pageable) {
        if (destinationId != null) {
            return repository.findByDestinationId(destinationId, pageable);
        } else {
            return repository.findAll(pageable);
        }
    }

    @Transactional
    public void deleteDestinationScaleStationByDestination(Long destinationId) {
        em.createQuery("DELETE FROM DestinationScaleStation d WHERE d.destination.id = :destinationId")
                .setParameter("destinationId", destinationId)
                .executeUpdate();
    }

    @Transactional
    public void deleteDestinationPortsByDestination(Long destinationId) {
        em.createQuery("DELETE FROM DestinationPort d WHERE d.destination.id = :destinationId")
                .setParameter("destinationId", destinationId)
                .executeUpdate();
    }

    @Transactional
    public void saveMultiple(Long destinationId, Map<String, String> params) {
        DestinationSetting destination = destinationSettingService.findById(destinationId);
        deleteDestinationScaleStationByDestination(destinationId);
        for (String key : params.keySet()) {
            if (!key.startsWith("amount_"))
                continue;

            Long scaleStationId = Long.valueOf(key.replace("amount_", ""));
            String rawAmountValue = params.get(key);

            if (rawAmountValue == null || rawAmountValue.isBlank())
                continue;

            BigDecimal amount = new BigDecimal(rawAmountValue);

            Optional<DestinationScaleStation> existingOpt = repository.findByDestinationIdAndScaleStationId(
                    destinationId,
                    scaleStationId);

            if (existingOpt.isPresent()) {
                DestinationScaleStation existing = existingOpt.get();
                existing.setAmount(amount);
                repository.save(existing);
            } else {
                boolean duplicate = repository.existsByDestinationIdAndScaleStationId(destinationId, scaleStationId);

                if (duplicate) {
                    throw new RuntimeException("This scale already exists for this truck");
                }
                ScaleStation scaleStation = scaleStationRepository.findById(scaleStationId).orElse(null);
                DestinationScaleStation truckScaleStation = new DestinationScaleStation();
                truckScaleStation.setDestination(destination);
                truckScaleStation.setAmount(amount);
                truckScaleStation.setScaleStation(scaleStation);

                repository.save(truckScaleStation);
            }
        }
    }

    public Map<Long, BigDecimal> getAmountsByDestinationId(Long destinationId) {

        List<DestinationScaleStation> list = repository.findByDestinationId(destinationId);

        Map<Long, BigDecimal> map = new HashMap<>();

        for (DestinationScaleStation d : list) {
            map.put(d.getScaleStation().getId(), d.getAmount());
        }

        return map;
    }

    // pors
    public Map<Long, BigDecimal> getPortAmountsByDestinationId(Long destinationId) {

        List<DestinationPort> list = destinationPortRepository.findByDestinationId(destinationId);

        Map<Long, BigDecimal> map = new HashMap<>();

        for (DestinationPort d : list) {
            map.put(d.getPort().getId(), d.getAmount());
        }

        return map;
    }

    @Transactional
    public void saveMultiplePorts(Long destinationId, Map<String, String> params) {
        DestinationSetting destination = destinationSettingService.findById(destinationId);
        deleteDestinationPortsByDestination(destinationId);
        for (String key : params.keySet()) {
            if (!key.startsWith("amount_"))
                continue;

            Long scaleStationId = Long.valueOf(key.replace("amount_", ""));
            String rawAmountValue = params.get(key);

            if (rawAmountValue == null || rawAmountValue.isBlank())
                continue;

            BigDecimal amount = new BigDecimal(rawAmountValue);

            Optional<DestinationPort> existingOpt = destinationPortRepository.findByDestinationIdAndPortId(
                    destinationId,
                    scaleStationId);

            if (existingOpt.isPresent()) {
                DestinationPort existing = existingOpt.get();
                existing.setAmount(amount);
                destinationPortRepository.save(existing);
            } else {
                boolean duplicate = destinationPortRepository.existsByDestinationIdAndPortId(destinationId,
                        scaleStationId);

                if (duplicate) {
                    throw new RuntimeException("This scale already exists for this truck");
                }
                Port port = portRepository.findById(scaleStationId).orElse(null);
                DestinationPort destinationPort = new DestinationPort();
                destinationPort.setDestination(destination);
                destinationPort.setAmount(amount);
                destinationPort.setPort(port);

                destinationPortRepository.save(destinationPort);
            }
        }
    }

    public List<DestinationScaleStationDTO> getByDestinationId(Long destinationId) {
        List<DestinationScaleStation> entities = repository.findByDestinationId(destinationId);

        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private DestinationScaleStationDTO toDTO(DestinationScaleStation entity) {
        return new DestinationScaleStationDTO(
                entity.getId(),
                entity.getDestination() != null ? entity.getDestination().getId() : null,
                entity.getScaleStation() != null ? entity.getScaleStation().getId() : null,
                entity.getScaleStation() != null ? entity.getScaleStation().getName() : null,
                entity.getAmount());
    }

    public List<DestinationScaleStationDTO> getByDestinationIdWithDTO(Long destinationId) {
        return repository.findDTOByDestinationId(destinationId);
    }

}
