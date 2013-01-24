package IntelligentSystems;
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

public class JorroZorRoBot {

	public static void DoTurn(PlanetWars pw) {
		
		List<Heuristic> gameState = new ArrayList<Heuristic>();
	
		for(Planet myPlanet : pw.MyPlanets()){
			if (myPlanet.NumShips() <= 1)
				continue;		
			for(Planet notMyPlanet : pw.EnemyPlanets()){
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

				gameState.add(new Heuristic(simpw4, myPlanet, notMyPlanet));
			}
		}
		
		Heuristic bestHeuristic = getHeuristic(gameState);
		
		if (bestHeuristic.source != null && bestHeuristic.destination != null) {
			pw.IssueOrder(bestHeuristic.source, bestHeuristic.destination);
		}
	}

	private static Heuristic getHeuristic(List<Heuristic> gameState) {
		Heuristic result = null;
		double value = -1000.;
		for(Heuristic h : gameState){
			if(h.value > value)
				result = h;
				value = h.value;
		}
		return result;
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
	
	public static class Heuristic{
		/**
		 * This class is meant to analyze and calculate a heuristic value,
		 * given a source and a destination planet and the original pw object.
		 * @param The original PlanetWars object
		 * @param The source planet
		 * @param The destination planet
		 */
		
		double value;
		Planet source, destination;
		
		public Heuristic(PlanetWars pw, Planet source, Planet destination){
			this.value = calcHeuristic(pw, source, destination);
			this.source = source;
			this.destination = destination;
		}
		
		public Heuristic(SimulatedPlanetWars simpw, Planet source, Planet destination){
			this.value = calcHeuristic(simpw, source, destination);
			this.source = source;
			this.destination = destination;
		}

		private double calcHeuristic(PlanetWars pw, Planet source, Planet destination) {
			double enemyShips = 1.;
			double myShips = 1.;
			
			for (Planet planet: pw.EnemyPlanets()){
				enemyShips += planet.NumShips() + planet.GrowthRate();
			}
			
			for (Planet planet: pw.MyPlanets()){
				myShips += planet.NumShips() + planet.GrowthRate();
			}
			
			return myShips - enemyShips;
		}
		
		private double calcHeuristic(SimulatedPlanetWars pw, Planet source, Planet destination) {
			double enemyShips = 1.;
			double myShips = 1.;
			
			for (Planet planet: pw.EnemyPlanets()){
				enemyShips += planet.NumShips() + planet.GrowthRate();
			}
			
			for (Planet planet: pw.MyPlanets()){
				myShips += planet.NumShips() + planet.GrowthRate();
			}
			
			return myShips - enemyShips;
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
	

	static JorroZorRoBot dummyBot = new JorroZorRoBot();
	
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
			
			List<Heuristic> gameState = new ArrayList<Heuristic>();

//			double score = 1000.;
//			Planet source = null;
//			Planet dest = null;
			
			for(Planet enemyPlanet : simpw.EnemyPlanets()){				
				if (enemyPlanet.NumShips() <= 1)
					continue;
				
				for(Planet notEnemyPlanet : simpw.NotEnemyPlanets()){
					SimulatedPlanetWars simpw2 = createSimulation(simpw);
					simpw2.simulateAttack(2, enemyPlanet, notEnemyPlanet);
					simpw2.simulateGrowth();
					
					gameState.add(new Heuristic(simpw2, notEnemyPlanet, enemyPlanet));
					
//					double scoreMin = evaluateState(simpw2, gameState);
//					
//					if (scoreMin < score) {					
//						score = scoreMin;
//						source = enemyPlanset;
//						dest = notEnemyPlanet;
//					}
				}
			}
			
			Heuristic bestHeuristic = getHeuristic(gameState);
			
			if (bestHeuristic.source != null && bestHeuristic.destination != null) {
				simulateAttack(2, bestHeuristic.source, bestHeuristic.destination);
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