package timdev.timdev.dto;

public class ModelDTO {
    private Long id;
    private String code;
    private String name;
    
    public ModelDTO() {}
        
    public ModelDTO(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }
}
