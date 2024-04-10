import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final List<Ksiazka> ksiazki = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket pisarzSocket = new ServerSocket(8001);
             ServerSocket czytelnikSocket = new ServerSocket(8002)){
            Thread pisarze = new Thread(() -> {
                while (true) try (Socket socket = pisarzSocket.accept();
                                  BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                  PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                                  ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
                    Thread pisarz = new Thread(() -> {
                        System.out.println("Witamy nowego pisarza");
                        printWriter.println("Witamy nowego pisarza");

                        boolean poloczony = true;
                        while (poloczony) {
                            printWriter.println("Podaj operację: ");
                            try {
                                switch (bufferedReader.read()) {
                                    case 0 -> poloczony = false;
                                    case 1 -> {
                                        printWriter.println("Przekaż książkę do dodania: ");
                                        ksiazki.add((Ksiazka) objectInputStream.readObject());
                                        printWriter.println("Książka pomyślnie dodana");
                                    }
                                    case 2 -> {
                                        printWriter.println("Podaj indeks książki do zmiany statusu: ");
                                        int index = bufferedReader.read();
                                        ksiazki.get(index).zmienStatus();
                                        printWriter.println(ksiazki.get(index).getStatus() ? "Książka została zablokowana" : "Książka została odblokowna");
                                    }
                                    case 3 -> {
                                        StringBuilder response = new StringBuilder();
                                        ksiazki.stream().map(ksiazka -> STR."\{ksiazka.nieOddane()}\n").forEach(response::append);
                                        printWriter.println(response);
                                    }
                                    case 4 -> {
                                        printWriter.println("Podaj indeks książki do usuniecia: ");
                                        ksiazki.remove(bufferedReader.read());
                                        printWriter.println("Książka została usunięta");
                                    }
                                    case 5 -> {
                                        printWriter.println("Podaj indeks, książki do zaktualizowania a następnie przekaż nową książkę");
                                        ksiazki.get(bufferedReader.read()).aktualizuj((Ksiazka) objectInputStream.readObject());
                                        printWriter.println("Książka została zktualizowna");
                                    }
                                    case 6 -> {
                                        StringBuilder lista = new StringBuilder();
                                        ksiazki.stream().map(ksiazka -> STR."\{ksiazki.indexOf(ksiazka)}, \{ksiazka.getAutor()}, \{ksiazka.getTytul()}, \{ksiazka.getData()}\n").forEach(lista::append);
                                        printWriter.println(lista);
                                    }
                                    default -> System.out.println("Nieznana opcja");
                                }
                            } catch (IOException | ClassNotFoundException e) {
                                System.out.println(STR."Błąd: \{e}");
                            }
                        }

                        printWriter.println("Połączenie przerwane przez użytkownika. Do widzenia");
                    });
                    pisarz.start();
                } catch (IOException e) {
                    System.out.println(STR."Błąd: \{e}");
                }
            });
            pisarze.start();

            Thread czytelnicy = new Thread(() -> {
                while (true) try (Socket socket = czytelnikSocket.accept();
                                  BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                  PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                                  ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
                    Thread czytelnik = new Thread(() -> {
                        System.out.println("Witamy nowego czytelnika");
                        printWriter.println("Witamy nowego czytelnika");

                        boolean poloczony = true;
                        while (poloczony) {
                            printWriter.println("Podaj operację: ");
                            try {
                                switch (Integer.parseInt(bufferedReader.readLine())){
                                    case 0 -> poloczony = false;
                                    case 1 -> {
                                        StringBuilder lista = new StringBuilder();
                                        for (Ksiazka ksiazka : ksiazki) if (ksiazka.szukaj(bufferedReader.readLine())) lista.append(STR."\{ksiazki.indexOf(ksiazka)}, \{ksiazka.getAutor()}, \{ksiazka.getTytul()}, \{ksiazka.getData()}\n");
                                        printWriter.println(lista);
                                    }
                                    case 2 -> {
                                        printWriter.println("Podaj indeks, swoje dane oraz dzisiejszą datę");
                                        int index = bufferedReader.read();
                                        int indexW = ksiazki.get(index).wyporzycz(bufferedReader.readLine(), bufferedReader.readLine());
                                        objectOutputStream.writeObject(ksiazki.get(index));
                                        printWriter.println(STR."Index wyporzyczenia to: \{indexW}");
                                    }
                                    case 3 -> {
                                        printWriter.println("Podaj indeks książki, indeks wyporzyczenia oraz datę");
                                        ksiazki.get(bufferedReader.read()).oddaj(bufferedReader.read(), bufferedReader.readLine());
                                        printWriter.println("Książka została zwrócona");
                                    }
                                    default -> System.out.println("Nieznana opcja");
                                }
                            } catch (IOException e){
                                System.out.println(STR."Błąd: \{e}");
                            }
                        }

                        printWriter.println("Połączenie przerwane przez użytkownika. Do widzenia");
                    });
                    czytelnik.start();
                } catch (IOException e) {
                    System.out.println(STR."Błąd: \{e}");
                }
            });
            czytelnicy.start();
        } catch (IOException e){
            System.out.println(STR."Błąd: \{e}");
        }
    }
}