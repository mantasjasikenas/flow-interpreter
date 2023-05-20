# flow-interpreter

## Build

Generate ANTLR visitor with:

    mvn clean antlr4:antlr4@generate-visitor

Build jar with:

    mvn clean install

## Run

Run jar with:

    java -jar target/flow-interpreter-1.0.jar -f samples/main.flow
    java -jar target/flow-interpreter-1.0.jar -i

## Features


## Requirements

- Struktūra: sąlyga, ciklas, kintamieji, funkcijos
- Galimybė kviesti sistemines funkcijas (privalomi skaitymas/rašymas į konsolę/failą)
- Bent du tipai: skaičius, eilutė (string)
- Bent viena savybė (unikali)
