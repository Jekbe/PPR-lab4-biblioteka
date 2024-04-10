import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ksiazka implements Serializable {
    private String autor, tytul, tresc;
    private final String data;
    private boolean zablokowana = false;
    private final List<Wyporzyczenie> rejestr = new ArrayList<>();

    public Ksiazka(String autor, String tytul, String data, String tresc) {
        this.autor = autor;
        this.tytul = tytul;
        this.data = data;
        this.tresc = tresc;
    }

    public String getAutor() {
        return autor;
    }

    public String getTytul() {
        return tytul;
    }

    public String getData() {
        return data;
    }

    public boolean getStatus(){
        return zablokowana;
    }

    public void zmienStatus(){
        zablokowana = !zablokowana;
    }

    public int wyporzycz(String kto, String data){
        rejestr.add(new Wyporzyczenie(kto, data));
        return rejestr.size() - 1;
    }

    public void oddaj(int index, String data){
        rejestr.get(index).oddaj(data);
    }

    public String nieOddane(){
        StringBuilder response = new StringBuilder(tytul + " od " + autor);
        int count = 0;
        for (Wyporzyczenie wyporzyczenie : rejestr) if (!wyporzyczenie.czyOddana()) {
                response.append("nie został zwrócony przez ").append(wyporzyczenie.getKto()).append(" z dnia ").append(wyporzyczenie.getDataW());
                count++;
            }
        return count == 0 ? response.append("został w całości zwrócony").toString() : response.toString();
    }

    public void aktualizuj(Ksiazka nowa){
        autor = nowa.autor;
        tytul = nowa.tytul;
        tresc = nowa.tresc;
    }

    public boolean szukaj(String wzor){
        return autor.contains(wzor) | tytul.contains(wzor) | data.contains(wzor) | tresc.contains(wzor);
    }
}

class Wyporzyczenie{
    private final String kto, dataW;
    private String dataO;

    public Wyporzyczenie(String kto, String dataW) {
        this.kto = kto;
        this.dataW = dataW;
        this.dataO = null;
    }

    public String getKto() {
        return kto;
    }

    public String getDataW() {
        return dataW;
    }

    public void oddaj(String data){
        dataO = data;
    }

    public boolean czyOddana(){
        return dataO != null;
    }
}
