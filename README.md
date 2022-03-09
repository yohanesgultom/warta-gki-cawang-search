# Warta GKI Cawang Search

Search for text in [Warta GKI Cawang PDF](http://gki-cawang.org/category/media/warta-jemaat/)

## Usage

Build Docker image:

```
docker build . -t warta-gki-cawang-api:latest
```

Search for text "Siregar":

```
docker run --rm warta-gki-cawang-api:latest python warta.py Siregar
```

Run API server and search for text "Siregar":

```
docker run --rm -d -p 5000:5000 warta-gki-cawang-api:latest
curl http://localhost:5000?query=Siregar
```

