# HollyService-Wallet

Portfel WPLN dla serwerów Minecraft 1.20.1+. Kategorie i produkty w GUI, codzienna nagroda, historia operacji oraz obsługa SQLite, MySQL i MongoDB.

## Instalacja

1. Pobierz `HollyService-Wallet-1.0.jar` z [Releases](https://github.com/Grzybalson/HollyService-Wallet/releases).
2. Umieść plik w katalogu `plugins` i uruchom ponownie serwer.
3. Dostosuj `wallet.yml` i `database.yml` w katalogu `plugins/HollyService-Wallet`.

PlaceholderAPI jest potrzebne do używania placeholdera w innych pluginach. Domyślny produkt VIP korzysta z LuckPerms; komendy nadawania produktów można zmienić.

## Komendy

- `/portfel` — otwiera portfel.
- `/aportfel add <nick> <ilosc>` — dodaje WPLN.
- `/aportfel set <nick> <ilosc>` — ustawia saldo.
- `/aportfel remove <nick> <ilosc>` — odejmuje WPLN.
- `/aportfel doladowanie <nick> <ilosc>` — dodaje WPLN, a przy dodatniej kwocie wyświetla wszystkim graczom title i odtwarza dźwięk.
- `/aportfel logs <nick>` — otwiera historię operacji gracza.
- `/aportfel reload` — przeładowuje `wallet.yml`.

Komendy obsługują znanych graczy offline oraz podpowiadanie nicków. Kwoty mogą mieć maksymalnie dwa miejsca po przecinku, np. `0`, `-1`, `0.01` lub `0,01`. Ujemna kwota odwraca działanie `add` i `remove`; `set` pozwala ustawić ujemne saldo.

## Uprawnienia

- `holly.cmd.wallet.use` — `/portfel`; domyślnie dostępne dla graczy.
- `holly.cmd.wallet.add` — `/aportfel add`.
- `holly.cmd.wallet.set` — `/aportfel set`.
- `holly.cmd.wallet.remove` — `/aportfel remove`.
- `holly.cmd.wallet.doladowanie` — `/aportfel doladowanie`.
- `holly.cmd.wallet.logs` — `/aportfel logs`.
- `holly.cmd.wallet.reload` — `/aportfel reload`.
- `holly.cmd.wallet.admin` — wszystkie komendy administratora.
- `holly.cmd.wallet.*` — wszystkie uprawnienia portfela.

Uprawnienia administratora są domyślnie dostępne dla operatorów. Historia operacji i portfel otwierają GUI, więc wymagają wykonania komendy przez gracza.

## PlaceholderAPI

`%hollywallet_wpln%` — saldo gracza z dwoma miejscami po przecinku, np. `12.50`.

Placeholder rejestruje się automatycznie po wykryciu PlaceholderAPI.

W konfiguracji menu dostępne są również znaczniki lokalne:

- `%holly_cost%` — cena wybranego produktu.
- `%reward_min%`, `%reward_max%` — zakres codziennej nagrody.
- `%player%` — nick gracza w obsługujących go komunikatach i komendach produktu.
- `%uuid%` — UUID gracza w komendach produktu.
- `%iloscwpln%` — kwota w powiadomieniu o doładowaniu.

## Konfiguracja

Pliki generuje Okaeri Configs na podstawie klas Java przy pierwszym uruchomieniu.

- `wallet.yml` — wygląd GUI, kategorie, produkty, ceny, listy komend, CustomModelData, komunikaty, dźwięki i codzienna nagroda.
- `database.yml` — wybór `SQLITE`, `MYSQL` lub `MONGODB` i ustawienia połączenia.

Domyślnie używane jest SQLite z plikiem `database/wallet.db` w katalogu pluginu. Codzienna nagroda wynosi od `0.10` do `0.50` WPLN i jest dostępna co 24 godziny. Sloty GUI są liczone od zera.

## Budowanie

Projekt wymaga Maven oraz JDK 17 lub nowszego.

```sh
mvn package
```

Gotowy plik: `target/HollyService-Wallet-1.0.jar`.
