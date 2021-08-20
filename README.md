[https://dawidm.github.io/cryptonose2](https://dawidm.github.io/cryptonose2)

If you want to build Cryptonose you should install dependencies bellow in your local maven repository (use `mvn install`). Some of them depends on anothers, install them in the same order as listed to avoid problems.
https://github.com/dawidm/dmutils
https://github.com/dawidm/exchangeutils  
https://github.com/dawidm/cryptonoseengine
https://github.com/dawidm/minimal-fx-charts  

To run Cryptonose use `gradlew run`, to build packages/installer use `gradlew jpackage`. Use the code with the most recent git tag for the newest working version.
