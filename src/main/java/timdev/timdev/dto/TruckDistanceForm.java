package timdev.timdev.dto;

import java.util.ArrayList;
import java.util.List;

public class TruckDistanceForm {
        private List<TruckDistanceDto> distances = new ArrayList<>();
        
        // public TruckDistanceForm() {
        //     // Initialize with one empty entry
        //     distances.add(new TruckDistanceDto());
        // }
        
        public List<TruckDistanceDto> getDistances() {
            return distances;
        }
        
        public void setDistances(List<TruckDistanceDto> distances) {
            this.distances = distances;
        }

        public void addEmptyEntry() {
            distances.add(new TruckDistanceDto());
        }
    }