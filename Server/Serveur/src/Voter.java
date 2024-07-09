import java.io.Serializable;


public class Voter {
    private static final long serialVersionUID = 1L;
    private int voterId;
    private String firstName;
    private String lastName;
    private int nomineeId;
    private String publicKey;
    private String sessionKey;

    public Voter(int voterId, String firstName, String lastName, int nomineeId, String publicKey, String sessionKey) {
        this.voterId = voterId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nomineeId = nomineeId;
        this.publicKey = publicKey;
        this.sessionKey = sessionKey;
    }

    public int getVoterId() {
        return voterId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getNomineeId() {
        return nomineeId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getSessionKey() {
        return sessionKey;
    }
}