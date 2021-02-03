package gdmarketpremium;

public class Alerted extends AbstractEvent {

    private Long id;

    public Alerted(){
        super();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}