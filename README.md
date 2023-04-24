# flow-interpreter




Generate ANTLR visitor with:

    mvn clean antlr4:antlr4@generate-visitor

Build jar with:

    mvn clean install

Run jar with:

    java -jar target/flow-interpreter-1.0.jar -f samples/main.flow
    java -jar target/flow-interpreter-1.0.jar -i