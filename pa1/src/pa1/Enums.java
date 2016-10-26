package pa1;

/**
 *
 * @author Joffrey Pannee
 */
public enum Enums {
    Error("error"), Confirmed("confirmed"), End("END");

    private final String msg;

    Enums(String msg) {
        this.msg = msg;
    }
    
    public String msg(){
        return msg;
    }
}
