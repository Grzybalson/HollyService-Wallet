# HollyService-Wallet

Portfel WPLN dla serwerów Minecraft 1.20.1+. Kategorie i produkty w GUI, codzienna nagroda, historia operacji oraz obsługa SQLite, MySQL i MongoDB.

## Instalacja

1. Pobierz `HollyService-Wallet-1.0.jar` z [Releases](https://github.com/Grzybalson/HollyService-Wallet/releases).
2. Umieść plik w katalogu `plugins` i uruchom ponownie serwer.
3. Dostosuj `wallet.yml` i `database.yml` w katalogu `plugins/HollyService-Wallet`.

## Komendy

- `/portfel`
- `/aportfel add <nick> <ilosc>`
- `/aportfel set <nick> <ilosc>`
- `/aportfel remove <nick> <ilosc>`
- `/aportfel doladowanie <nick> <ilosc>`
- `/aportfel logs <nick>`
- `/aportfel reload`

## Uprawnienia

- `holly.cmd.wallet.use`
- `holly.cmd.wallet.add`
- `holly.cmd.wallet.set`
- `holly.cmd.wallet.remove`
- `holly.cmd.wallet.doladowanie`
- `holly.cmd.wallet.logs`
- `holly.cmd.wallet.reload`
- `holly.cmd.wallet.admin`
- `holly.cmd.wallet.*`

## PlaceholderAPI

`%hollywallet_wpln%` — saldo gracza z dwoma miejscami po przecinku, np. `12.50`.

Placeholder rejestruje się automatycznie po wykryciu PlaceholderAPI.

W konfiguracji menu dostępne są również znaczniki lokalne:

- `%holly_cost%` — cena wybranego produktu.
- `%reward_min%`, `%reward_max%` — zakres codziennej nagrody.
- `%player%` — nick gracza w obsługujących go komunikatach i komendach produktu.
- `%uuid%` — UUID gracza w komendach produktu.
- `%topup_amount%` — kwota w powiadomieniu o doładowaniu.

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
