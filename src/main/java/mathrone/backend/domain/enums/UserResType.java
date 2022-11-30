package mathrone.backend.domain.enums;


public enum UserResType {

    MATHRONE("MATHRONE"),
    GOOGLE("GOOGLE");
    
    private final String typeName;

    UserResType(String name) {
        this.typeName = name;
    }

    public String getTypeName() {
        return typeName;
    }
}
