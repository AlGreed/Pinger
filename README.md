# Pinger 1.0

- pings IPBroker to get active players.

##Structure

- Sends a json object to IPBroker, that contains player nickname and uri to his server/client. The response is a map with all active players and theirs addresses.
- Communication is based on TLSv1.2 protocol.
  
##Usage

1) Ping IPBroker server acting as active player:
- Create an instance of HttpPingingService passing IPBroker address, client nickname, client ip address, keystore with certificates and password to keystore.
- Start the service.
- Create a thread, that calls getPlayers() with fixed rate.
```
final PingingService service = new HttpPingingService(this.brokerAddress, this.clientName, this.clientIp, this.keystore, this.password);
service.start();
...
Map<String,String> players = service.getPlayers();
...
```

2) Send a GET-Request to server getting once all active players without adding yourself to them:
- Create an instance of HttpPingingService. This time nickname and client ip are redundant.
- Do not start the service.
- Call getPlayersDirectlyOverHttpGetRequest().
```
final PingingService service = new HttpPingingService(this.brokerAddress, "", "", this.keystore, this.password);
Map<String,String> players = getPlayersDirectlyOverHttpGetRequest();
```

##Test

- tests and integration tests are executed during maven building.
