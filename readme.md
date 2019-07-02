# BfInterpret
Documentation is written in JavaDoc format.
The interpreter can either load & execute the '.bf' script passed to it as the first commandline argument or be used in REPL mode when no arguments are given.
The default fallback from any errors is to clear program memory and start REPL mode, meaning one can do real-time debugging if their program crashes.
The BfInterpret class can be used in compositions as well, just read the API.
The REPL mode supports a few non-standard Brainf*ck tokens:
- '#' Exits the loop
- '*' Clears data memory
- '!' Repeats last input

By default, the data memory is a 30,000 cell byte array (initialized to 0) defined by the 'DATA_ARR_SIZE' constant; The source must be modified and the program recomplied to change this.

Example usage:

`java BfInterpret myscript.bf` to load & execute a pre-written script

or

`java BfInterpret` to enter REPL
