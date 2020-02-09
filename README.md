# Warta GKI Cawang Search

Search for a name in the next week minister roster within the latest PDF of [Warta GKI Cawang](http://gki-cawang.org/category/media/warta-jemaat/)

Requirements:
* Python == 3.7.x (does not work yet with 3.8.x)
* Java (JRE) >= 1.8 (`JAVA_HOME` must be set. Required to run [Tabula](https://tabula.technology/)

Installation:
* Clone repository
* Project directory, install dependencies `pip install -r requirements.txt`

Usage:
1. As console script: run `python warta.py {name}` eg. `python warta.py Siregar` (result will be printed in the console)
2. As REST API: run [Bottle](https://bottlepy.org/docs/dev/) server `python server.py`
