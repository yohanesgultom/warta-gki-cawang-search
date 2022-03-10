# Warta GKI Cawang Search

Search for text in [Warta GKI Cawang PDF](http://gki-cawang.org/category/media/warta-jemaat/)

## Usage

Build Docker image:

```
docker build . -t warta-gki-cawang:latest
```

Search for text "Siregar":

```
docker run --rm warta-gki-cawang:latest java -jar app.jar "Siregar"
```
