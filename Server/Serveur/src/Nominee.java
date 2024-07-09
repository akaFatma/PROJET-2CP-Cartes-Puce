
public class Nominee {

    private int nominee_id;
    private String nominee_name;

    public Nominee(int nominee_id,String name){
        this.nominee_id=nominee_id;
        this.nominee_name=name;
    }
    public int getNomineeId(){
        return nominee_id;
    }
    public String getName(){
        return nominee_name;
    }

}