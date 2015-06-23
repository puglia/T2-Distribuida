
public enum Operation {

    RESERVAR("RESERVAR",1),
    CONSULTAR("CONSULTAR",2);
    
    private String name;
    private int code;
    
    private Operation(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }
    
}
