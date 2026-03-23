// ScaleFeeService.java
package timdev.timdev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import timdev.timdev.entity.DestinationScaleStation;
import timdev.timdev.entity.Truck;
import timdev.timdev.repository.DestinationScaleStationRepository;
import timdev.timdev.repository.TruckRepository;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ScaleFeeService {

    @Autowired
    private DestinationScaleStationRepository scaleStationRepository;

    @Autowired
    private TruckRepository truckRepository;

    /**
     * Calculate scale fee based on Excel formula logic
     */
    public BigDecimal calculateScaleFee(String licensePlate, String scaleStationName, BigDecimal totalWeight) {
        try {
            // Check for highway station (free)
            if (isHighwayStation(scaleStationName)) {
                return BigDecimal.ZERO;
            }

            // Get truck to get route number (for special cases)
            Optional<Truck> truckOpt = truckRepository.findByLicensePlate(licensePlate);

            // Special case for Route 5 with specific scale stations and weight ranges
            if (truckOpt.isPresent() && truckOpt.get().getRouteNumber() != null) {
                String routeNumber = truckOpt.get().getRouteNumber();

                // Route 5 special conditions
                if ("5".equals(routeNumber)) {
                    // Condition for ជញ្ជីងថ្នល់កែង with weight between 40 and 42
                    if ("ជញ្ជីងថ្នល់កែង".equals(scaleStationName) &&
                            isBetween(totalWeight, 40, 42)) {
                        return new BigDecimal("10000");
                    }

                    // Condition for ជញ្ជីងអង្គស្នួល with weight between 40 and 41.99
                    if ("ជញ្ជីងអង្គស្នួល".equals(scaleStationName) &&
                            isBetween(totalWeight, 40, 41.99)) {
                        return new BigDecimal("10000");
                    }
                }
            }

            // For all other cases, look up in expense tab/database
            Optional<DestinationScaleStation> scaleFee = scaleStationRepository.findScaleFee(scaleStationName,
                    totalWeight);

            if (scaleFee.isPresent()) {
                return scaleFee.get().getAmount();
            }

            // Default fee
            return new BigDecimal("20000");

        } catch (Exception e) {
            e.printStackTrace();
            return new BigDecimal("20000");
        }
    }

    /**
     * Check if value is between min and max (exclusive of max, inclusive of min in
     * this case)
     */
    private boolean isBetween(BigDecimal value, double min, double max) {
        BigDecimal minBd = BigDecimal.valueOf(min);
        BigDecimal maxBd = BigDecimal.valueOf(max);

        // value > min AND value < max
        return value.compareTo(minBd) > 0 && value.compareTo(maxBd) < 0;
    }

    /**
     * Check if scale station is highway
     */
    private boolean isHighwayStation(String scaleStationName) {
        return "Hight Way".equals(scaleStationName) ||
                scaleStationRepository.isHighwayStation(scaleStationName);
    }
}