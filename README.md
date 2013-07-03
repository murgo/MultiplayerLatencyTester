Multiplayer Latency Tester
==========================

Multiplayer Latency Tester is a tool for testing latency between users via different technologies. Main goal is to see if [Google Play Game Services](http://developer.android.com/google/play-services/games.html) are fast enough when compared to pure TCP or UDP.

Project will use Google Play Game Services as the main channel, where anybody can join a public room to ping and be pinged.

Ping technology goals:

  - Google Play Game Services, unreliable
  - Google Play Game Services, reliable
  - TCP
  - UDP

Each technology will be tested with:

  - Small amounts
  - Burst amounts
  - Small packets
  - Large packets

Results should show up straight in the app as well as exportable `.csv`.
