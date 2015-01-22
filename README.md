Desktop Trello Bugger
==================

How to use
==================

Go to:
`https://trello.com/1/authorize?key=c67c1cdec3b70b84a052b4d085c15eb1&expiration=30days&name=trelloc&response_type=token`
click `Allow` to get a token. Use that token in config.json next to this app.



How to build
==================
```
  $git clone https://github.com/alanboy/trello

  $cd trello
  $gradle build
  $cd build/libs/
  $java -jar trello-0.0.1-SNAPSHOT.jar

```

