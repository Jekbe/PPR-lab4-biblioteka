import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final List<Ksiazka> ksiazki = new ArrayList<>();
    private static PrintWriter printWriter;
    private static ObjectOutputStream objectOutputStream;

    public static void main(String[] args) {
        ServerSocket pisarzSocket, czytelnikSocket;
        try{
            pisarzSocket = new ServerSocket(8001);
            czytelnikSocket = new ServerSocket(8002);

            Thread pisarze = getPisarze(pisarzSocket);
            pisarze.start();

            Thread czytelnicy = getCzytelnicy(czytelnikSocket);
            czytelnicy.start();
        } catch(IOException e){
            System.out.println("Błąd: " + e);
        }
    }

    private static Thread getPisarze(ServerSocket pisarzSocket) {
        return new Thread(() -> {
                while(true) try{
                    Socket socket = pisarzSocket.accept();

                    Thread pisarz = new Thread(() -> {
                        try {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            printWriter = new PrintWriter(socket.getOutputStream(), true);
                            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

                            System.out.println("Witamy nowego pisarza");
                            print("Witamy nowego pisarza");

                            boolean poloczony = true;
                            while (poloczony) {
                                print("Podaj operację: ");
                                try {
                                    switch (Integer.parseInt(bufferedReader.readLine())) {
                                        case 0 -> poloczony = false;
                                        case 1 -> {
                                            print("Przekaż książkę do dodania: ");
                                            ksiazki.add((Ksiazka) objectInputStream.readObject());
                                            print("Książka pomyślnie dodana");
                                        }
                                        case 2 -> {
                                            print("Podaj indeks książki do zmiany statusu: ");
                                            int index = bufferedReader.read();
                                            ksiazki.get(index).zmienStatus();
                                            print(ksiazki.get(index).getStatus() ? "Książka została zablokowana" : "Książka została odblokowna");
                                        }
                                        case 3 -> {
                                            StringBuilder response = new StringBuilder();
                                            ksiazki.forEach(ksiazka -> response.append(ksiazka.nieOddane()).append("\n"));
                                            print(response.toString());
                                        }
                                        case 4 -> {
                                            print("Podaj indeks książki do usuniecia: ");
                                            ksiazki.remove(bufferedReader.read());
                                            print("Książka została usunięta");
                                        }
                                        case 5 -> {
                                            print("Podaj indeks książki do zaktualizowania a następnie przekaż nową książkę");
                                            ksiazki.get(bufferedReader.read()).aktualizuj((Ksiazka) objectInputStream.readObject());
                                            print("Książka została zktualizowna");
                                        }
                                        case 6 -> {
                                            StringBuilder lista = new StringBuilder();
                                            ksiazki.forEach(ksiazka -> lista.append(ksiazki.indexOf(ksiazka)).append(", ").append(ksiazka.getAutor()).append(", ").append(ksiazka.getTytul()).append(", ").append(ksiazka.getData()).append("\n"));
                                            print(lista.isEmpty() ? "biblioteka jest pusta" : lista.toString());
                                            print("null");
                                        }
                                        default -> System.out.println("Nieznana opcja");
                                    }
                                } catch (IOException | ClassNotFoundException e) {
                                    System.out.println("Błąd: " + e);
                                }
                            }

                            print("Połączenie przerwane przez użytkownika. Do widzenia");
                        } catch (IOException e) {
                            System.out.println("Błąd: " + e);
                        }
                    });
                    pisarz.start();
                } catch (IOException e) {
                    System.out.println("Błąd pisarza: " + e);
                }
            });
    }

    private static Thread getCzytelnicy(ServerSocket czytelnikSocket){
        return new Thread(() -> {
            while(true) try{
                Socket socket = czytelnikSocket.accept();

                Thread czytelnik = new Thread(() -> {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        printWriter = new PrintWriter(socket.getOutputStream(), true);
                        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

                        System.out.println("Witamy nowego czytelnika");
                        print("Witamy nowego czytelnika");

                        boolean poloczony = true;
                        while (poloczony) {
                            print("Podaj operację: ");
                            try {
                                switch (Integer.parseInt(bufferedReader.readLine())) {
                                    case 0 -> poloczony = false;
                                    case 1 -> {
                                        StringBuilder lista = new StringBuilder();
                                        for (Ksiazka ksiazka : ksiazki)
                                            if (ksiazka.szukaj(bufferedReader.readLine()))
                                                lista.append(ksiazki.indexOf(ksiazka)).append(", ").append(ksiazka.getAutor()).append(", ").append(ksiazka.getTytul()).append(", ").append(ksiazka.getData()).append("\n");
                                        print(lista.toString());
                                    }
                                    case 2 -> {
                                        print("Podaj indeks, swoje dane oraz dzisiejszą datę");
                                        int index = bufferedReader.read();
                                        ksiazki.get(index).wyporzycz(bufferedReader.readLine(), bufferedReader.readLine());
                                        outObject(ksiazki.get(index));
                                        print("Index wyporzyczenia to: " + ksiazki.get(index).getIndexW());
                                    }
                                    case 3 -> {
                                        print("Podaj indeks książki, indeks wyporzyczenia oraz datę");
                                        ksiazki.get(bufferedReader.read()).oddaj(bufferedReader.read(), bufferedReader.readLine());
                                        print("Książka została zwrócona");
                                    }
                                    default -> System.out.println("Nieznana opcja");
                                }
                            } catch (IOException e) {
                                System.out.println("Błąd: " + e);
                            }
                        }

                        print("Połączenie przerwane przez użytkownika. Do widzenia");
                    } catch (IOException e) {
                        System.out.println("Błąd czytelnika: " + e);
                    }
                });
                czytelnik.start();
            } catch(IOException e) {
                System.out.println("Błąd czytelnika: " + e);
            }
        });
    }

    private static void print(String s){
        printWriter.println(s);
        printWriter.flush();
    }

    private static void outObject(Object object) throws IOException {
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
    }
}