FROM python:3.7.12-slim-buster
RUN apt-get update && apt-get install -y openjdk-11-jre-headless
RUN export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 && export PATH=$JAVA_HOME/bin:$PATH
WORKDIR /app
COPY . .
RUN pip install -r requirements.txt
EXPOSE 5000
CMD python server.py