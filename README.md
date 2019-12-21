# Simulated Annealing Battleship Solitaire

This is a java implementation of a simulated annealing method of discovering the solution for battleship solitaire.

https://en.wikipedia.org/wiki/Simulated_annealing
https://en.wikipedia.org/wiki/Battleship_(puzzle)

### How to run

Compile just as any other java program.

```javac BattleShip.java```

Then run the program by specifying the arguments
```java Battleship <input file> <number of iterations>```

Note that large inputs with large amounts of iterations will cause extended time taken for the score to be calcuated
The number of iterations and the size of the grid are taken into consideration when finding a solution (e.g. factored in when considering how many times to move a ship around before trying a different ship instead).

### Supported input file format

The input file has to follow a certain standard.

- 3 lines with endline characters at the end of line 1 and 2.
- The first line is the ships
- The second line represents the x axis
- The third line represents the y axis
- The numbers in each row need to add up to the same number. (number and length of ships need to add up properly)
- The numbers need to be integers with a single space between them.

There are a few examples are provided in the repository.
- 6x6x6.txt
- 7x6x6.txt
- 10x10x10.txt
- 15x15x15.txt
- my6x6x6.txt

### Score

The lower the score the better (With 0 being perfect solution found).


### Future improvements

  - Currently hardcoded so that if it cannot find a solution in which all the ships are placed in 1000 attempts it deems the solution not possible.






