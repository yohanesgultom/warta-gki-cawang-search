# Warta GKI Cawang Search

Search for text in [Warta GKI Cawang PDF](http://gki-cawang.org/category/media/warta-jemaat/)

## Buid and Run

Build

```
docker build . -t warta-gki-cawang-api:latest
```

Run simple search

```
docker run --rm warta-gki-cawang-api:latest python warta.py Siregar
```

Run API server

```
docker run --rm -d -p 5000:5000 warta-gki-cawang-api:latest
curl http://localhost:5000?query=Siregar
```

