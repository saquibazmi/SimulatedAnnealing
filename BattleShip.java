// Saquib Azmi
// 8/06/2018

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

class BattleShip
{
	public static int TriesLeft = 0; // number of times we can try for a better solution
	public static int gridsize = 0;
	public static int[] ships;
	public static int[] xhits;
	public static int[] yhits;
	public static int[][] grid;
	public static int fails = 0;
	
	public static void main(String[] args)
	{
		if(args.length != 2)
		{
			System.err.println("Please make sure you enter a file name as well as the number of solutions allowed"); 
			System.exit(1);
		}
		
		
		BufferedReader in;
		String filename = args[0];
		
		try
		{
			
			TriesLeft = Integer.parseInt(args[1]);
			if(TriesLeft <1)
			{
				System.err.println("Please make sure number of solutions allowed is a positive integer"); 
				System.exit(1);
			}
			
			in = new BufferedReader(new FileReader(filename));
			initialSetUp(in);	
			
			// for simulated annealing, how many solutions to create at each stage
			// acts as a cooling rate with triesleft being the temperature
			int solsPerShip = TriesLeft / ships.length; 
			int extraTries = TriesLeft % ships.length;
			
			// number of attempts provided are too small to do simulated annealing so just generate and test
			// could technically still do simulated annealing but with this amount of attempts, generate and test is probably more efficient
			if(solsPerShip == 0) 
			{
				solution s = CreateRandomSolution();
				grid = s.sol;
				while(calcScore(grid) != 0 && TriesLeft != 0)
				{
					fails = 0;
					s = CreateRandomSolution();
					if(calcScore(s.sol) < calcScore(grid))
					{
						grid = s.sol;
					}
				}
			}
			else
			{
				solution s = simulatedAnnealing(solsPerShip);
				grid = s.sol;
				//System.err.println(extraTries);
				//System.err.println(TriesLeft);
			}
			
			DisplayGrid();
			System.out.println("Score : " + calcScore(grid));
			
		}
		catch(Exception Ex)
		{
			System.err.println(Ex);
		}
	}
	
	static solution simulatedAnnealing(int coolingrate)
	{
		int extraTries = TriesLeft % ships.length;
		solution currBest = CreateRandomSolution();
		solution currNew;
		int temperature = ships.length;
		
		
		// generate and tests for the extra tries it will have by end of loop.
		while(extraTries > 0)
		{
			currNew = CreateRandomSolution();
			if(calcScore(currNew.sol) < calcScore(currBest.sol))
			{
				currBest = currNew;
			}
			//System.err.println(extraTries);
			extraTries--;
		}
		
		// each number of ship combination (ships.length, ships.length -1, ships.length -2.... 1)
		for(int i = 0; i < temperature; i++)
		{
			// number of solutions to randomize ships
			for(int j = 0; j < coolingrate; j++)
			{
				//System.err.println(temperature-i + " " + j);
				if(TriesLeft > 0)
				{
					currNew = randomizeShips(temperature-i, currBest);
					
					//System.err.println(calcScore(currNew.sol) + " " + calcScore(currBest.sol));
					if(calcScore(currNew.sol) < calcScore(currBest.sol))
					{
						currBest = currNew;
						//System.err.println("new best case found" + calcScore(currNew.sol));
					}
				}
				else
				{
					//when matched number of tries allowed, return best case solution so far
					//shouldnt come here unless very special case
					return currBest;
				}
				
			}
			
		}
		//returns best solution found
		return currBest;
		
	}
	
	
	static solution randomizeShips(int numships, solution currSol)
	{
		
		Random rnd = new Random();
		
		//copying the current solution so we can modify and compare
		int[][] currshipsgrid = new int[gridsize][gridsize];
		for(int i =0; i<gridsize; i++)
		{
			for (int j =0; j < gridsize; j++)
			{
				currshipsgrid[i][j] = currSol.sol[i][j];
			}
		}
		
		List<ship> currships = new ArrayList<ship>(currSol.ships.size());
		for(ship c : currSol.ships)
		{
			currships.add(c);
		}
		
		ArrayList<ship> listShipstoRnd = new ArrayList<ship>();
		
		// remove random ships
		for(int i = 0; i < numships; i++) // loop through to get "numships" number of ships
		{
			// picks random ships to remove
			int indexshiptorandomize = rnd.nextInt(currships.size());
			ship shiptornd = currships.get(indexshiptorandomize);
			currships.remove(indexshiptorandomize);
			listShipstoRnd.add(shiptornd);
			//System.err.println("len : " + shiptornd.length + " row : " + shiptornd.posx + " col : " + shiptornd.posy);
			RemoveShip(currshipsgrid, shiptornd);
		}
		
		//int fail = 0;
		//add ships back randomly
		for(ship s : listShipstoRnd)
		{
			ship a = PlaceShipRandomly(s.length, currshipsgrid);
			// error case, PlaceShipRandomly returns a length -1 ship if it can't place anywhere.
			//while(a.length == -1)
			if(a.length == -1)
			{
				/*a = PlaceShipRandomly(s.length, currshipsgrid);
				fail ++;
				// after trying 100 times, conclude that there is no where to put the ship and thus this solution has failed
				if(fail > 100)
				{
					//grid = currshipsgrid;
					//DisplayGrid();
					//System.err.println("failed to place ship after moving previous ship");
					int[][] f = new int[gridsize][gridsize];
					solution failed = new solution(f, listShipstoRnd);
					TriesLeft-=1;
					return failed;
				}*/
				int[][] f = new int[gridsize][gridsize];
				solution failed = new solution(f, listShipstoRnd);
				TriesLeft-=1; // Technically hasn't presented a solution so shouldn't increment but not doing so exponentially increases runtime
				return failed;
			}
			currships.add(a);
			
		}
		
		//return solution
		solution returnsol = new solution(currshipsgrid, currships);
		TriesLeft -= 1;
		return returnsol;
	}
	
	static void initialSetUp(BufferedReader in) throws Exception
	{
		//~~~~~~~~~ Reading from textfile provided in args to the arrays ~~~~~~~~
		
		String tempships = in.readLine();
		String tempxhits = in.readLine();
		String tempyhits = in.readLine();
		in.close();
		
		String[] splitships = tempships.split("\\s+");
		String[] splitxhits = tempxhits.split("\\s+");
		String[] splityhits = tempyhits.split("\\s+");
		
		
		// initialising arrays
		gridsize = splitxhits.length;
		grid = new int[gridsize][gridsize];
		
		ships = new int[splitships.length];
		// grid is always a square, could change this by adding a gridx/gridy with low effort
		xhits = new int[gridsize];
		yhits = new int[gridsize];
		
		// fill arrays with info from textfile
		for(int i = 0; i <splitships.length; i++)
		{
			ships[i] = Integer.parseInt(splitships[i]);
		}
		for(int i = 0; i <splitxhits.length; i++)
		{
			xhits[i] = Integer.parseInt(splitxhits[i]);
			yhits[i] = Integer.parseInt(splityhits[i]);
		}
	}
	
	static solution CreateRandomSolution()
	{
		int[][] solution = new int[gridsize][gridsize];
		ArrayList<ship> listofships = new ArrayList<ship>();
		solution solutionrand = new solution(solution, listofships);
		
		// fill the grid with 0s
		// not sure if still need, was giving errors for some cases without
		for(int i = 0; i < gridsize; i++) // rows
		{
			for(int j = 0; j < gridsize; j++) // columns
			{
				solution[i][j] = 0;
			}
		}
		
		// if fails 1000 times in a row, conclude that there is no solution. HARDCODED, change if better method found
		if(fails == 1000)
		{
			System.err.println("No solution found");
			System.exit(1);
		}
		
		// place all the ships randomly
		for(int i : ships)
		{
			ship a = PlaceShipRandomly(i, solution);
			if(a.length == -1)
			{	// fails to place all ships randomly
				fails++;
				return CreateRandomSolution();
			}
			else
			{	//success
				listofships.add(a);
			}
		}
		TriesLeft -=1;
		return solutionrand;
	}
	
	static ship PlaceShipRandomly(int length, int[][] shipgrid)
	{
		List<ship> spots = returnAvailableSpots(shipgrid);
		ArrayList<ship> shortlistspots = new ArrayList<ship>();
		// remove unneccesary spots that have a length smaller than required
		for(ship s : spots)
		{
			if(s.length >= length)
			{
				shortlistspots.add(s);
			}
		}
		
		Random rnd = new Random();
		if(shortlistspots.size() == 0)
		{
			// if there aren't any available spots for the ship
			// return that placement has failed by indicating length of ship placed as -1
			ship fail = new ship(-1, -1, -1, false);
			return fail;
		}
		int r = rnd.nextInt(shortlistspots.size());
		ship openplace = shortlistspots.get(r);
		
		//System.err.println("chosen ship spot " + openplace.length + " " + openplace.posx + " " + openplace.posy);
		
		// randomises again if the open spot it find is bigger than the ship you want to place, 
		// e.g. a length 3 ship can fit 3 ways in a length 5 spot : XXX00 0XXX0 00XXX
		int offset = 0;
		if(openplace.length > length)
		{
			offset = rnd.nextInt(openplace.length - length);
		}
		if(openplace.horizontal)
		{
			openplace.posy += offset;
		}
		else
		{
			openplace.posx += offset;
		}
		
		//System.err.println("final ship spot " + length + " " + openplace.posx + " " + openplace.posy);
		
		openplace.length = length;
		PlaceShip(shipgrid, openplace);
		return openplace;
	}
	
	static void RemoveShip(int[][]shipgrid, ship s)
	{
		if(s.horizontal)
		{
			//System.err.println("horizontal len:" + s.length + " row:" + s.posx + " col:" + s.posy);
			//make 3 spots left of ship available
			if(s.posy > 0) // ship isnt touching left wall
			{
				if(s.posx > 0) //ship isnt touching top wall
				{
					shipgrid[s.posx - 1][s.posy-1] += 1;
				}
				shipgrid[s.posx][s.posy-1] += 1;
				if(s.posx < (gridsize-1)) // ship isnt touching bottom wall
				{
					shipgrid[s.posx + 1][s.posy-1] += 1;
				}
			}
			//make spots along length of ship unavailable
			for(int i = 0; i < s.length; i++)
			{
				shipgrid[s.posx][s.posy + i] =0; // delete ship
				//System.err.println(i);
				if(s.posx != 0) //if not top row set spot above ship unavailable
				{
					shipgrid[s.posx - 1][s.posy + i] += 1;
				}
				if(s.posx != (gridsize-1)) //if not bottom row set spot below ship unavailable
				{
					shipgrid[s.posx + 1][s.posy + i] += 1;
				}
			}
			//make 3 spots right of ship unavailable
			if(s.posy+s.length-1 < (gridsize-1)) //ship isnt touching right wall
			{
				if(s.posx > 0) //ship isnt touching top wall
				{
					shipgrid[s.posx - 1][s.posy+s.length] += 1;
				}
				
				shipgrid[s.posx][s.posy+s.length] += 1;
				
				if(s.posx < (gridsize-1)) // ship isnt touching bottom wall
				{
					shipgrid[s.posx + 1][s.posy+s.length] += 1;
				}
			}
		}
		else
		{
			//System.err.println("vertical len:" + s.length + " row:" + s.posx + " col:" + s.posy);
			
			//make 3 spots above ship unavailable
			if(s.posx > 0) // ship isnt touching top wall
			{
				if(s.posy > 0) //ship isnt touching left wall
				{
					shipgrid[s.posx - 1][s.posy - 1] += 1;
				}
				shipgrid[s.posx-1][s.posy] += 1;
				if(s.posy < (gridsize-1)) // ship isnt touching right wall
				{
					shipgrid[s.posx - 1][s.posy + 1] += 1;
				}
			}
			// make spots along length of ship unavailable
			for(int i = 0; i < s.length; i++)
			{
				shipgrid[s.posx + i][s.posy] = 0; // delete ship
				if(s.posy != 0) //set spots around ship as unavailable
				{
					shipgrid[s.posx + i][s.posy - 1] += 1;
				}
				if(s.posy != (gridsize-1))
				{
					shipgrid[s.posx + i][s.posy + 1] += 1;
				}
			}
			// make 3 spots below ship unavailable
			if(s.posx+s.length < gridsize) //ship isnt touching bottom wall
			{
				if(s.posy > 0) //ship isnt touching left wall
				{
					shipgrid[s.posx +s.length][s.posy - 1] += 1;
				}
				shipgrid[s.posx + s.length][s.posy] += 1;
				if(s.posy < (gridsize-1)) // ship isnt touching right wall
				{
					shipgrid[s.posx + s.length][s.posy + 1] += 1;
				}
			}
		}
	}
	
	static void PlaceShip(int[][] shipgrid, ship s)
	{
		if(s.horizontal)
		{
			//System.err.println("horizontal len:" + s.length + " row:" + s.posx + " col:" + s.posy);
			//make 3 spots left of ship unavailable
			if(s.posy > 0) // ship isnt touching left wall
			{
				if(s.posx > 0) //ship isnt touching top wall
				{
					shipgrid[s.posx - 1][s.posy-1] -= 1;
				}
				shipgrid[s.posx][s.posy-1] -= 1;
				if(s.posx < (gridsize-1)) // ship isnt touching bottom wall
				{
					shipgrid[s.posx + 1][s.posy-1] -= 1;
				}
			}
			//make spots along length of ship unavailable
			for(int i = 0; i < s.length; i++)
			{
				shipgrid[s.posx][s.posy + i] = 1; // place ship
				//System.err.println(i);
				if(s.posx != 0) //if not top row set spot above ship unavailable
				{
					shipgrid[s.posx - 1][s.posy + i] -= 1;
				}
				if(s.posx != (gridsize-1)) //if not bottom row set spot below ship unavailable
				{
					shipgrid[s.posx + 1][s.posy + i] -= 1;
				}
			}
			//make 3 spots right of ship unavailable
			if(s.posy+s.length-1 < (gridsize-1)) //ship isnt touching right wall
			{
				if(s.posx > 0) //ship isnt touching top wall
				{
					shipgrid[s.posx - 1][s.posy+s.length] -= 1;
				}
				
				shipgrid[s.posx][s.posy+s.length] -= 1;
				
				if(s.posx < (gridsize-1)) // ship isnt touching bottom wall
				{
					shipgrid[s.posx + 1][s.posy+s.length] -= 1;
				}
			}
		}
		else
		{
			//System.err.println("vertical len:" + s.length + " row:" + s.posx + " col:" + s.posy);
			
			//make 3 spots above ship unavailable
			if(s.posx > 0) // ship isnt touching top wall
			{
				if(s.posy > 0) //ship isnt touching left wall
				{
					shipgrid[s.posx - 1][s.posy - 1] -= 1;
				}
				shipgrid[s.posx-1][s.posy] -= 1;
				if(s.posy < (gridsize-1)) // ship isnt touching right wall
				{
					shipgrid[s.posx - 1][s.posy + 1] -= 1;
				}
			}
			// make spots along length of ship unavailable
			for(int i = 0; i < s.length; i++)
			{
				shipgrid[s.posx + i][s.posy] = 1;
				if(s.posy != 0) //set spots around ship as unavailable
				{
					shipgrid[s.posx + i][s.posy - 1] -= 1;
				}
				if(s.posy != (gridsize-1))
				{
					shipgrid[s.posx + i][s.posy + 1] -= 1;
				}
			}
			// make 3 spots below ship unavailable
			if(s.posx+s.length < gridsize) //ship isnt touching bottom wall
			{
				if(s.posy > 0) //ship isnt touching left wall
				{
					shipgrid[s.posx +s.length][s.posy - 1] -= 1;
				}
				shipgrid[s.posx + s.length][s.posy] -= 1;
				if(s.posy < (gridsize-1)) // ship isnt touching right wall
				{
					shipgrid[s.posx + s.length][s.posy + 1] -= 1;
				}
			}
		}
	}
	
	static void DisplayGrid()
	{
		for(int i = 0; i < gridsize; i++) // rows
		{
			for(int j = 0; j < gridsize; j++) // columns
			{
				if(grid[i][j] <= 0)
				{
					System.out.print("."); // Can change this to "O" but a lot easier to see ships when using a period
				}
				//else if(grid[i][j] < 0) //temporary, used to show unavailable spots (cant have 2 ships next to each other)
				//{
				//	System.out.print("I");
				//}
				else
				{
					System.out.print("X");
				}
			}
			System.out.println("");
		}
	}
	
	static int calcScore(int[][] solution)
	{
		int score = 0;
		
		for(int i = 0; i < gridsize; i++) // calculates row score
		{
			int rowscore = 0;
			for(int j = 0; j < gridsize; j++)
			{
				if(solution[i][j] == 1)
				{
					rowscore++;
				}
			}
			rowscore -= xhits[i];
			// can be a negative so need absolute value
			if (rowscore > 0)
			{
				score += rowscore;
			}
			else
			{
				score -= rowscore;
			}
		}
		
		for(int i = 0; i < gridsize; i++) // calculates column score
		{
			int columnscore = 0;
			for(int j = 0; j < gridsize; j++)
			{
				if(solution[j][i] == 1)
				{
					columnscore++;
				}
			}
			columnscore -= yhits[i];
			// can be a negative so need absolute value
			if (columnscore > 0)
			{
				score += columnscore;
			}
			else
			{
				score -= columnscore;
			}
		}
		
		return score;
		
	}
	
	static List<ship> returnAvailableSpots(int[][] gridships)
	{
		ArrayList<ship> availspots = new ArrayList<ship>();
		
		
		// adds to the list all available horizontal spots for new ships
		for(int i = 0; i < gridsize; i++) //rows
		{
			int templength = -1; // -1 means start of new ship spot, else just length of current ship spot
			for(int j = 0; j < gridsize; j++) //columns
			{
				if(gridships[i][j] == 0) // empty spot
				{
					if(templength == -1) // start of a new ship spot
					{
						// create a new ship object to act as "open ship spot"
						ship tempShip = new ship(1, i, j, true);
						availspots.add(tempShip);
						templength = 1;
					}
					else	// part of previous ship spot
					{
						// adding to the length of ship spot
						ship tempShip = availspots.get(availspots.size()-1);
						tempShip.length += 1;		
						templength += 1;
					}
				}
				else // not available ship spot
				{
					templength = -1;
				}
			}
		}
		
		
		// adds to the list all available vertical spots for new ships
		for(int i = 0; i < gridsize; i++) //columns
		{
			int templength = -1; // -1 means start of new ship spot, else just length of current ship spot
			for(int j = 0; j < gridsize; j++) //rows
			{
				if(gridships[j][i] == 0) // empty spot
				{
					if(templength == -1) // start of a new ship spot
					{
						// create a new ship object to act as "open ship spot"
						ship tempShip = new ship(1, j, i, false);
						availspots.add(tempShip);
						templength = 1;
					}
					else	// part of previous ship spot
					{
						// adding to the length of ship spot
						ship tempShip = availspots.get(availspots.size()-1);
						tempShip.length += 1;						
						templength += 1;
					}
				}
				else // not available ship spot
				{
					templength = -1;
				}
			}
		}
		
		return availspots;
	}
}

class ship
{	// properties the ship requires
	int length;
	int posx;
	int posy;
	boolean horizontal; //if true then ship goes across from posx else goes down from posy
	
	public ship(int l, int positionx, int positiony, boolean horizpos)
	{
		length = l;
		posx = positionx;
		posy = positiony;
		horizontal = horizpos;
	}
	
}

class solution
{	// contains grid and a list of ships. Useful for removing/adding ships
	int[][] sol;
	List<ship> ships;
	
	public solution(int[][] solut, List<ship> shiplist)
	{
		sol = solut;
		ships = shiplist;
	}
	
	
}