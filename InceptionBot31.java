import java.util.*;

/** Another smarter kind of bot, which implements a minimax algorithm with look-ahead of two turns.
 * It simulates the opponent using the BullyBot strategy and simulates the possible outcomes for any
 * choice of source and destination planets in the attack. The simulated outcome states are ranked by
 * the evaluation function, which returns the most promising one.
 * 
 * Try to improve this bot. For example, you can try to answer some of this questions. 
 * Can you come up with smarter heuristics/scores for the evaluation function? -- done ?
 * What happens if you run this bot against your bot from week1?  -- it wins
 * How can you change this bot to beat your week1 bot?  
 * Can you extend the bot to look ahead more than two turns? How many turns do you want to look ahead?
 * Is there a smart way to make this more efficient?
 */

public class InceptionBot31 {

	public static void DoTurn(PlanetWars pw) {
		
		int score = -1000;
		Planet source = null;
		Planet dest = null;
	
		for (Planet myPlanet: pw.MyPlanets()){
			if (myPlanet.NumShips() <= 1)
				continue;		
			for (Planet notMyPlanet: pw.NotMyPlanets()){
				SimulatedPlanetWars simpw = createSimulation(pw);
				simpw.simulateAttack(1, myPlanet, notMyPlanet);
				simpw.simulateGrowth();
				simpw.simulateEnemyBotAttack(simpw);
				simpw.simulateGrowth();
				SimulatedPlanetWars simpw2 = createSimulation(simpw);
				simpw2.simulateAttack(simpw);
				SimulatedPlanetWars simpw3 = createSimulation(simpw2);
				simpw3.simulateAttack(simpw2);
				SimulatedPlanetWars simpw4 = createSimulation(simpw3);
				simpw4.simulateAttack(simpw3);
				SimulatedPlanetWars simpw5 = createSimulation(simpw4);
				simpw5.simulateAttack(simpw4);

				int scoreMax = evaluateState(simpw5);
				
				if (scoreMax > score) {					
					score = scoreMax;
					source = myPlanet;
					dest = notMyPlanet;
				}
			}
		}
		
		if (source != null && dest != null) {
			pw.IssueOrder(source, dest);
		}
	}

	public static int evaluateState(SimulatedPlanetWars simpw){
		
		int enemyShips = 1;
		int myShips = 1;
		
		for (Planet planet: simpw.EnemyPlanets()){
			enemyShips += planet.NumShips() + planet.GrowthRate();
		}
		for (Planet planet: simpw.MyPlanets()){
			myShips += planet.NumShips() + planet.GrowthRate();
		}
		return myShips - enemyShips;	
	}
	
	public static void main(String[] args) {
		
		String line = "";
		String message = "";
		int c;
		try {
			while ((c = System.in.read()) >= 0) {
				switch (c) {
				case '\n':
					if (line.equals("go")) {
						PlanetWars pw = new PlanetWars(message);
						DoTurn(pw);
						pw.FinishTurn();
						message = "";
					} else {
						message += line + "\n";
					}
					line = "";
					break;
				default:
					line += (char) c;
					break;
				}
			}
		} catch (Exception e) {
			// Owned.
		}
	}
	
	/**
	 * Create the simulation environment. Returns a SimulatedPlanetWars instance.
	 * Call every time you want a new simulation environment.
	 * @param The original PlanetWars object
	 * @return SimulatedPlanetWars instance on which to simulate your attacks. Create a new one everytime you want to try alternative simulations.
	 */
	public static SimulatedPlanetWars createSimulation(PlanetWars pw){
		return dummyBot.new SimulatedPlanetWars(pw);
	}
	
	public static SimulatedPlanetWars createSimulation(SimulatedPlanetWars simpw){
		return dummyBot.new SimulatedPlanetWars(simpw);
	}
	

	static InceptionBot31 dummyBot = new InceptionBot31();
	
	public class SimulatedPlanetWars{

		List<Planet> planets = new ArrayList<Planet>();
		
		public SimulatedPlanetWars(PlanetWars pw) {

			for (Planet planet: pw.Planets()){
				planets.add(planet);
			}
		}
		public SimulatedPlanetWars(SimulatedPlanetWars simpw){
			for (Planet planet: simpw.Planets()){
				planets.add(planet);
			}
		}
		
		public void simulateGrowth() {
			for (Planet p: planets){	
				if(p.Owner() == 0)
					continue;
				Planet newp = new Planet(p.PlanetID(), p.Owner(), p.NumShips()+p.GrowthRate() , 
						p.GrowthRate(), p.X(), p.Y());
				planets.set(p.PlanetID(), newp);
			}
		}
		
		public void simulateAttack( int player, Planet source, Planet dest){
			
			if (source.Owner() != player){
				return;
			}
			if (source != null && dest != null) {
						
				Planet newSource = new Planet(source.PlanetID(), source.Owner(), source.NumShips()/2 , 
						source.GrowthRate(), source.X(), source.Y());
				Planet newDest = new Planet(dest.PlanetID(), dest.Owner(), Math.abs(dest.NumShips()-source.NumShips()/2 ), 
						dest.GrowthRate(), dest.X(), dest.Y());
				
				if(dest.NumShips() < source.NumShips() / 2){
					//change owner
					newDest.Owner(player);
				}	
				planets.set(source.PlanetID(), newSource);
				planets.set(dest.PlanetID(), newDest);
			}
		}

		public SimulatedPlanetWars simulateAttack(SimulatedPlanetWars simpw){
			
			SimulatedPlanetWars simpw2 = createSimulation(simpw);
			for (Planet myPlanet: simpw.MyPlanets()){
				if (myPlanet.NumShips() <= 1)
					continue;		
				for (Planet notMyPlanet: simpw.NotMyPlanets()){
					
					simpw2.simulateAttack(1, myPlanet, notMyPlanet);
					simpw2.simulateGrowth();
					simpw2.simulateEnemyBotAttack(simpw2);
					simpw2.simulateGrowth();
				}
			}
			return simpw2;
		}
		
		
		public void simulateEnemyBotAttack(SimulatedPlanetWars simpw){
			
			Planet source = null;
			Planet dest = null;
			int score = 1000;
			
			for (Planet enemyPlanet : simpw.EnemyPlanets()) {
				if (enemyPlanet.NumShips() <= 1)
					continue;
				for (Planet notEnemyPlanet : simpw.NotEnemyPlanets()) {
					SimulatedPlanetWars simpw2 = createSimulation(simpw);
					simpw2.simulateAttack(2, enemyPlanet, notEnemyPlanet);
					simpw2.simulateGrowth();
					int scoreMin = evaluateState(simpw2);
					
					if (scoreMin < score) {					
						score = scoreMin;
						source = enemyPlanet;
						dest = notEnemyPlanet;
					}
				}
			}
			
			if (source != null && dest != null) {
				simulateAttack(2, source, dest);
			}

		}
		
		public List<Planet> Planets(){
			return planets;
		}
		
	    public int NumPlanets() {
	    	return planets.size();
	    }
		
	    public Planet GetPlanet(int planetID) {
	    	return planets.get(planetID);
	    }
	    
	    public List<Planet> MyPlanets() {
			List<Planet> r = new ArrayList<Planet>();
			for (Planet p : planets) {
			    if (p.Owner() == 1) {
				r.add(p);
			    }
			}
			return r;
	    }
	    
	    public List<Planet> NeutralPlanets() {
		
		    List<Planet> r = new ArrayList<Planet>();
			for (Planet p : planets) {
			    if (p.Owner() == 0) {
				r.add(p);
			    }
			}
			return r;
		}

	    public List<Planet> EnemyPlanets() {
		
	    	List<Planet> r = new ArrayList<Planet>();
			for (Planet p : planets) {
			    if (p.Owner() >= 2) {
				r.add(p);
			    }
			}
			return r;
	    }

	    public List<Planet> NotMyPlanets() {
	    	
			List<Planet> r = new ArrayList<Planet>();
			for (Planet p : planets) {
			    if (p.Owner() != 1) {
				r.add(p);
			    }
			}
			return r;
	    }
	    
	    public List<Planet> NotEnemyPlanets() {
			List<Planet> r = new ArrayList<Planet>();
			for (Planet p : planets) {
			    if (p.Owner() < 2) {
				r.add(p);
			    }
			}
			return r;
		    }
		
	    public int Distance(int sourcePlanet, int destinationPlanet) {
			
			Planet source = planets.get(sourcePlanet);
			Planet destination = planets.get(destinationPlanet);
			double dx = source.X() - destination.X();
			double dy = source.Y() - destination.Y();
			return (int) Math.ceil(Math.sqrt(dx * dx + dy * dy));
		}
	    
		public int Winner() {
			
			Set<Integer> remainingPlayers = new TreeSet<Integer>();
			for (Planet p : planets) {
				remainingPlayers.add(p.Owner());
			}
			switch (remainingPlayers.size()) {
			case 0:
				return 0;
			case 1:
				return ((Integer) remainingPlayers.toArray()[0]).intValue();
			default:
				return -1;
			}
		}

	    public int NumShips(int playerID) {
	    	
			int numShips = 0;
			for (Planet p : planets) {
			    if (p.Owner() == playerID) {
				numShips += p.NumShips();
			    }
			}
			return numShips;
	    }

	    public void IssueOrder(Planet source, Planet dest) {
	    	simulateAttack(1, source,dest);
	    }
	    
	}
}