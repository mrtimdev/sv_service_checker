package timdev.timdev.dto;

import java.util.List;

import timdev.timdev.entity.FatsOilsSetting;

public class FatsOilsSettingsForm {
    
    private List<FatsOilsSetting> settings;

    public List<FatsOilsSetting> getSettings() {
        return settings;
    }

    public void setSettings(List<FatsOilsSetting> settings) {
        this.settings = settings;
    }
}
